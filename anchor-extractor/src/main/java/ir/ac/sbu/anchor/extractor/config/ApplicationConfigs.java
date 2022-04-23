package ir.ac.sbu.anchor.extractor.config;

import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@EnableConfigurationProperties
public class ApplicationConfigs {

    private final KafkaConfigs kafkaConfigs = new KafkaConfigs();
    private final AnchorExtractorConfigs anchorExtractorConfigs = new AnchorExtractorConfigs();

    public KafkaConfigs getKafkaConfigs() {
        return kafkaConfigs;
    }

    public AnchorExtractorConfigs getAnchorExtractorConfigs() {
        return anchorExtractorConfigs;
    }

    public static class KafkaConfigs {

        private String bootstrapServers;
        private String kafkaConsumerGroup;
        private String pagesTopicName;
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

        public String getPagesTopicName() {
            return pagesTopicName;
        }

        public void setPagesTopicName(String pagesTopicName) {
            this.pagesTopicName = pagesTopicName;
        }

        public String getAnchorsTopicName() {
            return anchorsTopicName;
        }

        public void setAnchorsTopicName(String anchorsTopicName) {
            this.anchorsTopicName = anchorsTopicName;
        }

        public Properties getConsumerProperties(boolean fromBeginning) {
            Properties kafkaConsumerConfigs = new Properties();
            kafkaConsumerConfigs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
            kafkaConsumerConfigs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, fromBeginning ? "earliest" : "latest");
            kafkaConsumerConfigs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
            kafkaConsumerConfigs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
            kafkaConsumerConfigs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
            kafkaConsumerConfigs.put(ConsumerConfig.GROUP_ID_CONFIG, getKafkaConsumerGroup());
            return kafkaConsumerConfigs;
        }

        public Properties getBaseProducerProperties() {
            Properties kafkaProducerConfigs = new Properties();
            kafkaProducerConfigs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
            kafkaProducerConfigs.put(ProducerConfig.LINGER_MS_CONFIG, "1000");
            return kafkaProducerConfigs;
        }
    }

    public static class AnchorExtractorConfigs {

        private int inMemoryPageQueueSize;
        private int inMemoryAnchorsQueueSize;

        public int getInMemoryPageQueueSize() {
            return inMemoryPageQueueSize;
        }

        public void setInMemoryPageQueueSize(int inMemoryPageQueueSize) {
            this.inMemoryPageQueueSize = inMemoryPageQueueSize;
        }

        public int getInMemoryAnchorsQueueSize() {
            return inMemoryAnchorsQueueSize;
        }

        public void setInMemoryAnchorsQueueSize(int inMemoryAnchorsQueueSize) {
            this.inMemoryAnchorsQueueSize = inMemoryAnchorsQueueSize;
        }
    }
}
