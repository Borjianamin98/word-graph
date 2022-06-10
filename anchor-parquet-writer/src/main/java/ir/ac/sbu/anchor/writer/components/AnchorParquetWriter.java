package ir.ac.sbu.anchor.writer.components;

import com.alibaba.dcm.DnsCacheManipulator;
import ir.ac.sbu.anchor.writer.config.ApplicationConfigs;
import ir.ac.sbu.anchor.writer.config.ApplicationConfigs.AnchorParquetWriterConfigs;
import ir.ac.sbu.anchor.writer.config.ApplicationConfigs.HadoopConfigs;
import ir.ac.sbu.anchor.writer.config.ApplicationConfigs.KafkaConfigs;
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
public class AnchorParquetWriter {

    private static final Logger logger = LoggerFactory.getLogger(AnchorParquetWriter.class);
    public static final String LOCALHOST_IP = "127.0.0.1";

    private final KafkaProtoParquetWriter<Anchor> parquetWriter;

    public AnchorParquetWriter(ApplicationConfigs applicationConfigs) {
        KafkaConfigs kafkaConfigs = applicationConfigs.getKafkaConfigs();
        AnchorParquetWriterConfigs anchorParquetWriterConfigs = applicationConfigs.getAnchorParquetWriterConfigs();
        HadoopConfigs hadoopConfigs = applicationConfigs.getHadoopConfigs();

        HdfsConfiguration hdfsConfiguration = new HdfsConfiguration();
        hdfsConfiguration.set(DFSConfigKeys.FS_DEFAULT_NAME_KEY,
                String.format("hdfs://%s:%d",
                        hadoopConfigs.getHadoopNameNodeHostname(), hadoopConfigs.getHadoopNameNodePort()));
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
                .topicName(kafkaConfigs.getAnchorsTopicName())
                .hadoopConf(hdfsConfiguration)
                .targetDir(anchorParquetWriterConfigs.getTargetParquetDirectory())
                .maxFileOpenDurationSeconds(anchorParquetWriterConfigs.getMaxFileOpenDurationSeconds())
                .maxFileSize(anchorParquetWriterConfigs.getMaxFileSizeBytes())
                .compressionCodecName(CompressionCodecName.SNAPPY)
                .threadCount(anchorParquetWriterConfigs.getThreadCount())
                .protoClass(Anchor.class)
                .parser(Anchor.parser())
                .build();
        parquetWriter.start();
    }

    @PreDestroy
    public void destroy() {
        logger.info("Stopping anchor parquet writer ...");
        try {
            parquetWriter.close();
            logger.info("Anchor parquet writer stopped successfully");
        } catch (IOException e) {
            Thread.currentThread().interrupt();
            logger.error("Unexpected interrupt", e);
        }
    }
}
