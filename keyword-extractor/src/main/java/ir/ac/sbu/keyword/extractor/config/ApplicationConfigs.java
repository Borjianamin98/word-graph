package ir.ac.sbu.keyword.extractor.config;

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
    private final KeywordExtractorConfigs keywordExtractorConfigs = new KeywordExtractorConfigs();

    public KafkaConfigs getKafkaConfigs() {
        return kafkaConfigs;
    }

    public KeywordExtractorConfigs getKeywordExtractorConfigs() {
        return keywordExtractorConfigs;
    }

    public static class KafkaConfigs {

        private String bootstrapServers;
        private String kafkaConsumerGroup;
        private String pagesTopicName;
        private String keywordsTopicName;

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

        public String getKeywordsTopicName() {
            return keywordsTopicName;
        }

        public void setKeywordsTopicName(String keywordsTopicName) {
            this.keywordsTopicName = keywordsTopicName;
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

    public static class KeywordExtractorConfigs {

        private int inMemoryPageQueueSize;
        private int inMemoryPageKeywordsQueueSize;

        public int getInMemoryPageQueueSize() {
            return inMemoryPageQueueSize;
        }

        public void setInMemoryPageQueueSize(int inMemoryPageQueueSize) {
            this.inMemoryPageQueueSize = inMemoryPageQueueSize;
        }

        public int getInMemoryPageKeywordsQueueSize() {
            return inMemoryPageKeywordsQueueSize;
        }

        public void setInMemoryPageKeywordsQueueSize(int inMemoryPageKeywordsQueueSize) {
            this.inMemoryPageKeywordsQueueSize = inMemoryPageKeywordsQueueSize;
        }
    }
}
