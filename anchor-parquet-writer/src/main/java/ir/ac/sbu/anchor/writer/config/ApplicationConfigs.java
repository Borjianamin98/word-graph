package ir.ac.sbu.anchor.writer.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@EnableConfigurationProperties
public class ApplicationConfigs {

    private final KafkaConfigs kafkaConfigs = new KafkaConfigs();
    private final AnchorParquetWriterConfigs anchorParquetWriterConfigs = new AnchorParquetWriterConfigs();
    private final HadoopConfigs hadoopConfigs = new HadoopConfigs();

    public KafkaConfigs getKafkaConfigs() {
        return kafkaConfigs;
    }

    public AnchorParquetWriterConfigs getAnchorParquetWriterConfigs() {
        return anchorParquetWriterConfigs;
    }

    public HadoopConfigs getHadoopConfigs() {
        return hadoopConfigs;
    }

    public static class HadoopConfigs {

        private String hadoopNameNodeHostname;
        private String hadoopDataNodeHostname;
        private String hadoopNameNodePort;

        public String getHadoopNameNodeHostname() {
            return hadoopNameNodeHostname;
        }

        public void setHadoopNameNodeHostname(String hadoopNameNodeHostname) {
            this.hadoopNameNodeHostname = hadoopNameNodeHostname;
        }

        public String getHadoopDataNodeHostname() {
            return hadoopDataNodeHostname;
        }

        public void setHadoopDataNodeHostname(String hadoopDataNodeHostname) {
            this.hadoopDataNodeHostname = hadoopDataNodeHostname;
        }

        public String getHadoopNameNodePort() {
            return hadoopNameNodePort;
        }

        public void setHadoopNameNodePort(String hadoopNameNodePort) {
            this.hadoopNameNodePort = hadoopNameNodePort;
        }
    }

    public static class KafkaConfigs {

        private String bootstrapServers;
        private String kafkaConsumerGroup;
        private String anchorsTopicName;

        public String getKafkaConsumerGroup() {
            return kafkaConsumerGroup;
        }

        public void setKafkaConsumerGroup(String kafkaConsumerGroup) {
            this.kafkaConsumerGroup = kafkaConsumerGroup;
        }

        public String getBootstrapServers() {
            return bootstrapServers;
        }

        public void setBootstrapServers(String bootstrapServers) {
            this.bootstrapServers = bootstrapServers;
        }

        public String getAnchorsTopicName() {
            return anchorsTopicName;
        }

        public void setAnchorsTopicName(String anchorsTopicName) {
            this.anchorsTopicName = anchorsTopicName;
        }

        public Map<String, Object> getConsumerProperties(boolean fromBeginning) {
            Map<String, Object> kafkaConsumerConfigs = new HashMap<>();
            kafkaConsumerConfigs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
            kafkaConsumerConfigs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, fromBeginning ? "earliest" : "latest");
            kafkaConsumerConfigs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
            kafkaConsumerConfigs
                    .put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
            kafkaConsumerConfigs
                    .put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
            kafkaConsumerConfigs.put(ConsumerConfig.GROUP_ID_CONFIG, getKafkaConsumerGroup());
            return kafkaConsumerConfigs;
        }
    }

    public static class AnchorParquetWriterConfigs {

        private String targetParquetDirectory;
        private int maxFileOpenDurationSeconds;
        private int maxFileSizeBytes;
        private int threadCount;

        public String getTargetParquetDirectory() {
            return targetParquetDirectory;
        }

        public void setTargetParquetDirectory(String targetParquetDirectory) {
            this.targetParquetDirectory = targetParquetDirectory;
        }

        public int getMaxFileOpenDurationSeconds() {
            return maxFileOpenDurationSeconds;
        }

        public void setMaxFileOpenDurationSeconds(int maxFileOpenDurationSeconds) {
            this.maxFileOpenDurationSeconds = maxFileOpenDurationSeconds;
        }

        public int getMaxFileSizeBytes() {
            return maxFileSizeBytes;
        }

        public void setMaxFileSizeBytes(int maxFileSizeBytes) {
            this.maxFileSizeBytes = maxFileSizeBytes;
        }

        public int getThreadCount() {
            return threadCount;
        }

        public void setThreadCount(int threadCount) {
            this.threadCount = threadCount;
        }
    }
}
