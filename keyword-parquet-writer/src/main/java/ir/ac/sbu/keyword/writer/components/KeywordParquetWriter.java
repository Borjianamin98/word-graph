package ir.ac.sbu.keyword.writer.components;

import com.alibaba.dcm.DnsCacheManipulator;
import ir.ac.sbu.exception.InitializerFailureException;
import ir.ac.sbu.keyword.writer.config.ApplicationConfigs;
import ir.ac.sbu.keyword.writer.config.ApplicationConfigs.HadoopConfigs;
import ir.ac.sbu.keyword.writer.config.ApplicationConfigs.KafkaConfigs;
import ir.ac.sbu.keyword.writer.config.ApplicationConfigs.KeywordParquetWriterConfigs;
import ir.ac.sbu.model.Models.Anchor;
import ir.sahab.kafka.reader.KafkaProtoParquetWriter;
import java.io.IOException;
import javax.annotation.PreDestroy;
import org.apache.hadoop.hdfs.DFSConfigKeys;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class KeywordParquetWriter {

    private static final Logger logger = LoggerFactory.getLogger(KeywordParquetWriter.class);
    public static final String LOCALHOST_IP = "127.0.0.1";

    private final KafkaProtoParquetWriter<Anchor> parquetWriter;

    public KeywordParquetWriter(ApplicationConfigs applicationConfigs) {
        KafkaConfigs kafkaConfigs = applicationConfigs.getKafkaConfigs();
        KeywordParquetWriterConfigs keywordParquetWriterConfigs = applicationConfigs.getKeywordParquetWriterConfigs();
        HadoopConfigs hadoopConfigs = applicationConfigs.getHadoopConfigs();

        HdfsConfiguration hdfsConfiguration = new HdfsConfiguration();
        hdfsConfiguration.set(DFSConfigKeys.FS_DEFAULT_NAME_KEY,
                String.format("hdfs://%s:%d",
                        hadoopConfigs.getHadoopDataNodeHostname(), hadoopConfigs.getHadoopNameNodePort()));
        if (!hadoopConfigs.isInHadoopNetwork()) {
            // Fix issue of connecting to hadoop infrastructure from outside of docker network
            // Related link: https://github.com/big-data-europe/docker-hadoop/issues/98#issuecomment-919815981
            hdfsConfiguration.set(DFSConfigKeys.DFS_CLIENT_USE_DN_HOSTNAME, "true");
            hdfsConfiguration.set(DFSConfigKeys.DFS_DATANODE_USE_DN_HOSTNAME, "true");
            DnsCacheManipulator.setDnsCache(hadoopConfigs.getHadoopNameNodeHostname(), LOCALHOST_IP);
            DnsCacheManipulator.setDnsCache(hadoopConfigs.getHadoopDataNodeHostname(), LOCALHOST_IP);
        }

        parquetWriter = new KafkaProtoParquetWriter.Builder<Anchor>()
                .consumerConfig(kafkaConfigs.getConsumerProperties(true))
                .topicName(kafkaConfigs.getKeywordsTopicName())
                .hadoopConf(hdfsConfiguration)
                .targetDir(keywordParquetWriterConfigs.getTargetParquetDirectory())
                .maxFileOpenDurationSeconds(keywordParquetWriterConfigs.getMaxFileOpenDurationSeconds())
                .maxFileSize(keywordParquetWriterConfigs.getMaxFileSizeBytes())
                .compressionCodecName(CompressionCodecName.SNAPPY)
                .threadCount(keywordParquetWriterConfigs.getThreadCount())
                .protoClass(Anchor.class)
                .parser(Anchor.parser())
                .build();
        try {
            parquetWriter.start();
        } catch (IOException | InterruptedException e) {
            throw new InitializerFailureException(e);
        }
    }

    @PreDestroy
    public void destroy() {
        logger.info("Stopping keyword parquet writer ...");
        try {
            parquetWriter.close();
            logger.info("Keyword parquet writer stopped successfully");
        } catch (IOException e) {
            Thread.currentThread().interrupt();
            logger.error("Unexpected interrupt", e);
        }
    }
}
