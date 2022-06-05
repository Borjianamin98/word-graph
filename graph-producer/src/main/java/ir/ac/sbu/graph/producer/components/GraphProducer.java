package ir.ac.sbu.graph.producer.components;

import ir.ac.sbu.graph.producer.config.ApplicationConfigs;
import ir.ac.sbu.graph.producer.config.ApplicationConfigs.HadoopConfigs;
import ir.ac.sbu.graph.producer.config.ApplicationConfigs.SparkConfigs;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.annotation.PreDestroy;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DFSConfigKeys;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
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

        HdfsConfiguration hdfsConfiguration = new HdfsConfiguration();
        hdfsConfiguration.set(DFSConfigKeys.FS_DEFAULT_NAME_KEY,
                String.format("hdfs://%s:%d",
                        hadoopConfigs.getHadoopNameNodeHostname(), hadoopConfigs.getHadoopNameNodePort()));

        SparkConf sparkConf = new SparkConf();
        sparkConfigs.getSparkConfigs().forEach(sparkConf::set);
        sparkSession = SparkSession.builder()
                .appName("graph-producer")
                .master("spark://" + sparkConfigs.getSparkMasterAddress())
                .config(sparkConf)
                .getOrCreate();
        sparkSession.sparkContext().hadoopConfiguration().addResource(hdfsConfiguration);

        Dataset<Row> anchorsDataset = sparkSession.read().parquet(applicationConfigs.getAnchorsParquetDirectory());
        Dataset<Row> keywordsDataset = sparkSession.read().parquet(applicationConfigs.getKeywordsParquetDirectory());

        Dataset<Row> graph = createGraph(anchorsDataset, keywordsDataset);

        String hdfsResultDirectory = applicationConfigs.getResultDirectoryPath();
        graph.limit(applicationConfigs.getMaximumGraphEdges())
                // Use 'coalesce' to avoid creation of multiple result files
                .coalesce(1)
                .write()
                .mode(SaveMode.Overwrite)
                .option("encoding", StandardCharsets.UTF_8.name())
                .option("header", "true")
                .csv(hdfsResultDirectory);

        try (FileSystem fileSystem = FileSystem.get(hdfsConfiguration)) {
            Path resultFilePath = fileSystem.globStatus(new Path(hdfsResultDirectory + "/part*"))[0]
                    .getPath();
            fileSystem.rename(resultFilePath, new Path(hdfsResultDirectory + "/result.csv"));
        } catch (IOException e) {
            throw new AssertionError("Unexpected IO exception", e);
        }
    }

    public static Dataset<Row> createGraph(Dataset<Row> anchorsDataset, Dataset<Row> keywordsDataset) {
        // Because each page keywords have relation too, we create a logical link between a page and its own
        // link. It will added to the list of real crawled links.
        Dataset<Row> srcLinks = anchorsDataset.select(functions.col("source").as("source")).distinct();
        Dataset<Row> destLinks = anchorsDataset.select(functions.col("destination").as("source")).distinct();
        Dataset<Row> pageOwnLink = srcLinks.union(destLinks).withColumn("destination", functions.col("source"));

        // Because of Kafka config (at least delivery), we may have duplicate links.
        // We remove them and keep just one of them
        Dataset<Row> allAnchorsDataset = anchorsDataset.union(pageOwnLink).dropDuplicates("source", "destination");
        Dataset<Row> uniqueKeywordsDataset = keywordsDataset.dropDuplicates("link");

        Dataset<Row> pageKeywordsRelation = allAnchorsDataset
                .join(uniqueKeywordsDataset.as("join_1"),
                        functions.col("join_1.link").equalTo(functions.col("destination")))
                .join(uniqueKeywordsDataset.as("join_2"),
                        functions.col("join_2.link").equalTo(functions.col("source")))
                .select(
                        functions.col("join_1.keywords").as("src_keywords"),
                        functions.col("join_2.keywords").as("dest_keywords")
                );

//        pageKeywordsRelation.show(30, false);

        Dataset<Row> keywordsMatching = pageKeywordsRelation
                .withColumn("keyword_1", functions.explode(functions.col("src_keywords")))
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

//        keywordsMatchingAggregated.show(30, false);

        return keywordsMatchingAggregated;
    }

    @PreDestroy
    public void destroy() {
        logger.info("Stopping graph producer ...");
        sparkSession.close();
        logger.info("Graph producer stopped successfully");
    }
}
