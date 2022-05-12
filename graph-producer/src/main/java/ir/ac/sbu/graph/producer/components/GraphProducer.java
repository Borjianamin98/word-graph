package ir.ac.sbu.graph.producer.components;

import ir.ac.sbu.graph.producer.config.ApplicationConfigs;
import ir.ac.sbu.graph.producer.config.ApplicationConfigs.HadoopConfigs;
import ir.ac.sbu.graph.producer.config.ApplicationConfigs.SparkConfigs;
import java.nio.charset.StandardCharsets;
import javax.annotation.PreDestroy;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GraphProducer {

    private static final Logger logger = LoggerFactory.getLogger(GraphProducer.class);

    private final SparkSession sparkSession;

    public GraphProducer(ApplicationConfigs applicationConfigs) {
        HadoopConfigs hadoopConfigs = applicationConfigs.getHadoopConfigs();
        SparkConfigs sparkConfigs = applicationConfigs.getSparkConfigs();

        String hdfsDefaultFs = String.format("hdfs://%s:%d",
                hadoopConfigs.getHadoopNameNodeHostname(), hadoopConfigs.getHadoopNameNodePort());
        SparkConf sparkConf = new SparkConf();
        sparkConfigs.getSparkConfigs().forEach(sparkConf::set);
        sparkSession = SparkSession.builder()
                .appName("graph-producer")
                .master("spark://" + sparkConfigs.getSparkMasterAddress())
                .config(sparkConf)
                .getOrCreate();

        Dataset<Row> anchorsDataset = sparkSession.read().parquet(
                hdfsDefaultFs + applicationConfigs.getAnchorsParquetDirectory());
        Dataset<Row> keywordsDataset = sparkSession.read().parquet(
                hdfsDefaultFs + applicationConfigs.getKeywordsParquetDirectory());

        createGraph(sparkSession, anchorsDataset, keywordsDataset)
                .limit(applicationConfigs.getMaximumGraphEdges())
                // Use 'coalesce' to avoid creation of multiple result files
                .coalesce(1)
                .write()
                .option("encoding", StandardCharsets.UTF_8.name())
                .csv(applicationConfigs.getResultDirectoryPath());
    }

    public static Dataset<Row> createGraph(SparkSession sparkSession,
            Dataset<Row> anchorsDataset, Dataset<Row> keywordsDataset) {
        // Because each page keywords have relation too, we create a logical link between a page and its own
        // link. It will added to the list of real crawled links.
        Dataset<Row> pageOwnLink = anchorsDataset
                .select(functions.col("source")).distinct()
                .union(anchorsDataset.select(functions.col("destination").as("source")).distinct())
                .withColumn("destination", functions.col("source"));

        anchorsDataset.union(pageOwnLink).createOrReplaceTempView("anchors");
        keywordsDataset.createOrReplaceTempView("keywords");

        Dataset<Row> result = sparkSession.sql("SELECT "
                + "src.keywords as src_keywords, "
                + "dest.keywords as dest_keywords " +
                "FROM anchors AS e " +
                "JOIN keywords AS src ON e.source = src.link " +
                "JOIN keywords AS dest ON e.destination = dest.link");

        Dataset<Row> keywordsMatching = result.withColumn("keyword_1", functions.explode(functions.col("src_keywords")))
                .withColumn("keyword_2", functions.explode(functions.col("dest_keywords")))
                .select("keyword_1", "keyword_2")
                .filter(functions.col("keyword_1").notEqual(functions.col("keyword_2")));

        Dataset<Row> keywordsMatchingAggregated = keywordsMatching
                .select(
                        functions.least("keyword_1", "keyword_2").alias("from"),
                        functions.greatest("keyword_1", "keyword_2").alias("to"),
                        functions.lit(1).as("count")
                )
                .groupBy("from", "to")
                .agg(functions.sum("count").alias("total_count"))
                .sort(functions.desc("total_count"));

        return keywordsMatchingAggregated;
    }

    @PreDestroy
    public void destroy() {
        logger.info("Stopping graph producer ...");
        sparkSession.close();
        logger.info("Graph producer stopped successfully");
    }
}
