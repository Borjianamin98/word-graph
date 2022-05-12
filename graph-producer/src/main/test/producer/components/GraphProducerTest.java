package producer.components;

import static org.apache.spark.sql.types.DataTypes.createArrayType;
import static org.apache.spark.sql.types.DataTypes.createStructField;
import static org.apache.spark.sql.types.DataTypes.createStructType;

import ir.ac.sbu.graph.producer.components.GraphProducer;
import java.util.Arrays;
import java.util.List;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GraphProducerTest {

    private static final Logger logger = LoggerFactory.getLogger(GraphProducerTest.class);

    private static SparkSession sparkSession;

    @BeforeAll
    public static void before() {
        SparkConf sparkConf = new SparkConf();
        sparkSession = SparkSession.builder()
                .appName("graph-producer")
                .master("local")
                .config(sparkConf)
                .getOrCreate();
    }

    @AfterAll
    public static void tearDown() {
        sparkSession.close();
    }

    @Test
    public void testGraphProducer() {
        List<Row> anchorsData = Arrays.asList(
                RowFactory.create("src1", "dest1"),
                RowFactory.create("src2", "dest2"),
                RowFactory.create("src2", "dest3"),
                RowFactory.create("src3", "dest1")
        );
        StructType anchorsSchema = createStructType(Arrays.asList(
                createStructField("source", DataTypes.StringType, false),
                createStructField("destination", DataTypes.StringType, false)));

        List<Row> keywordsData = Arrays.asList(
                RowFactory.create("src1", Arrays.asList("key1_1", "key1_2", "shared_key_1")),
                RowFactory.create("src2", Arrays.asList("key2_1", "key2_2", "shared_key_2")),
                RowFactory.create("dest1", Arrays.asList("key3_1", "key3_2", "shared_key_2")),
                RowFactory.create("dest2", Arrays.asList("key4_1", "key4_2", "shared_key_1"))
        );
        StructType keywordsSchema = createStructType(Arrays.asList(
                createStructField("link", DataTypes.StringType, false),
                createStructField("keywords", createArrayType(DataTypes.StringType, false), false)));

        Dataset<Row> anchorsDataset = sparkSession.createDataFrame(anchorsData, anchorsSchema);
        Dataset<Row> keywordsDataset = sparkSession.createDataFrame(keywordsData, keywordsSchema);

        anchorsDataset.show(false);
        keywordsDataset.show(false);

        Dataset<Row> result = GraphProducer.createGraph(sparkSession, anchorsDataset, keywordsDataset);

        result.show(1000, false);
        // Expected final output
        // +------------+------------+-----------+
        // |from        |to          |total_count|
        // +------------+------------+-----------+
        // |key1_1      |shared_key_1|2          |
        // |key1_2      |shared_key_1|2          |
        // |key4_1      |shared_key_1|2          |
        // |key1_1      |key1_2      |2          |
        // |key4_2      |shared_key_1|2          |
        // |key4_1      |key4_2      |2          |
        // |key2_1      |shared_key_2|2          |
        // |key3_2      |shared_key_2|2          |
        // |key3_1      |shared_key_2|2          |
        // |key2_2      |shared_key_2|2          |
        // |key3_1      |key3_2      |2          |
        // |key2_1      |key2_2      |2          |
        // |shared_key_1|shared_key_2|2          |
        // |key1_2      |key3_1      |1          |
        // |key2_2      |key4_2      |1          |
        // |key2_1      |shared_key_1|1          |
        // |key3_2      |shared_key_1|1          |
        // |key4_1      |shared_key_2|1          |
        // |key2_2      |shared_key_1|1          |
        // |key3_1      |shared_key_1|1          |
        // |key4_2      |shared_key_2|1          |
        // |key1_2      |shared_key_2|1          |
        // |key2_1      |key4_1      |1          |
        // |key1_1      |key3_2      |1          |
        // |key2_1      |key4_2      |1          |
        // |key1_1      |shared_key_2|1          |
        // |key1_2      |key3_2      |1          |
        // |key2_2      |key4_1      |1          |
        // |key1_1      |key3_1      |1          |
        // +------------+------------+-----------+
    }

}
