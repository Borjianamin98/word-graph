package ir.ac.sbu.graph.producer.components;

import ir.ac.sbu.graph.producer.config.ApplicationConfigs;
import ir.ac.sbu.graph.producer.config.ApplicationConfigs.HadoopConfigs;
import ir.ac.sbu.graph.producer.config.ApplicationConfigs.SparkConfigs;
import javax.annotation.PreDestroy;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
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

        anchorsDataset.show(1000, false);
        keywordsDataset.show(1000, false);
    }

    @PreDestroy
    public void destroy() {
        logger.info("Stopping graph producer ...");
        sparkSession.close();
        logger.info("Graph producer stopped successfully");
    }
}
