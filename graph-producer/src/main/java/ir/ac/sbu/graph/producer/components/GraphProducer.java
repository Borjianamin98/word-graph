package ir.ac.sbu.graph.producer.components;

import static org.apache.spark.sql.types.DataTypes.createArrayType;
import static org.apache.spark.sql.types.DataTypes.createStructField;
import static org.apache.spark.sql.types.DataTypes.createStructType;

import ir.ac.sbu.graph.producer.config.ApplicationConfigs;
import ir.ac.sbu.graph.producer.config.ApplicationConfigs.HadoopConfigs;
import ir.ac.sbu.graph.producer.config.ApplicationConfigs.SparkConfigs;
import ir.ac.sbu.model.Models.Anchor;
import ir.ac.sbu.model.Models.PageKeywords;
import java.util.Arrays;
import java.util.List;
import javax.annotation.PreDestroy;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
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

//        Dataset<Row> anchorsDataset = sparkSession.read().parquet(
//                hdfsDefaultFs + applicationConfigs.getAnchorsParquetDirectory());
//        Dataset<Row> keywordsDataset = sparkSession.read().parquet(
//                hdfsDefaultFs + applicationConfigs.getKeywordsParquetDirectory());

        List<Row> anchorsData = Arrays.asList(
                RowFactory.create("src1", "dest1"),
                RowFactory.create("src2", "dest2")
        );
        StructType anchorsSchema = createStructType(Arrays.asList(
                createStructField("source", DataTypes.StringType, false),
                createStructField("destination", DataTypes.StringType, false)));

        List<Row> keywordsData = Arrays.asList(
                RowFactory.create("src1", Arrays.asList("key1")),
                RowFactory.create("src2", Arrays.asList("key2")),
                RowFactory.create("dest1", Arrays.asList("key3")),
                RowFactory.create("dest2", Arrays.asList("key4"))
        );
        StructType keywordsSchema = createStructType(Arrays.asList(
                createStructField("link", DataTypes.StringType, false),
                createStructField("keywords", createArrayType(DataTypes.StringType, false), false)));

        Dataset<Row> anchorsDataset = sparkSession.createDataFrame(anchorsData, anchorsSchema);
        Dataset<Row> keywordsDataset = sparkSession.createDataFrame(keywordsData, keywordsSchema);

        anchorsDataset.show();
        keywordsDataset.show();

        anchorsDataset.createOrReplaceTempView("anchors");
        keywordsDataset.createOrReplaceTempView("keywords");

        SparkContext sparkContext = sparkSession.sparkContext();
        Dataset<Row> result = sparkSession.sql(
                "SELECT src.link, dest.link, src.keywords, dest.keywords " +
                        "FROM anchors AS e " +
                        "JOIN keywords AS src ON e.source = src.link " +
                        "JOIN keywords AS dest ON e.destination = dest.link");
//        val temp = anchorsDataset
//                .join(keywordsDataset, anchorsDataset.col("source").equalTo(keywordsDataset.col("link")))
//                .join(keywordsDataset, anchorsDataset.col("destination").equalTo(keywordsDataset.col("link")))
//                .select(keywordsDataset.col("link"))

        result.show(1000, false);
    }

    @PreDestroy
    public void destroy() {
        logger.info("Stopping graph producer ...");
        sparkSession.close();
        logger.info("Graph producer stopped successfully");
    }
}
