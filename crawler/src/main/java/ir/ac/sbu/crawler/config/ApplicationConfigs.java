package ir.ac.sbu.crawler.config;

import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@EnableConfigurationProperties
public class ApplicationConfigs {

    private int inMemoryLinkQueueSize;
    private final KafkaConfigs kafkaConfigs = new KafkaConfigs();
    private final CrawlerConfigs crawlerConfigs = new CrawlerConfigs();

    public int getInMemoryLinkQueueSize() {
        return inMemoryLinkQueueSize;
    }

    public void setInMemoryLinkQueueSize(int inMemoryLinkQueueSize) {
        this.inMemoryLinkQueueSize = inMemoryLinkQueueSize;
    }

    public KafkaConfigs getKafkaConfigs() {
        return kafkaConfigs;
    }

    public CrawlerConfigs getCrawlerConfigs() {
        return crawlerConfigs;
    }

    public static class KafkaConfigs {

        private String bootstrapServers;
        private String kafkaConsumerGroup;
        private String linksTopicName;

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

        public String getLinksTopicName() {
            return linksTopicName;
        }

        public void setLinksTopicName(String linksTopicName) {
            this.linksTopicName = linksTopicName;
        }

        public Properties getConsumerProperties() {
            Properties kafkaConsumerConfigs = new Properties();
            kafkaConsumerConfigs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
            kafkaConsumerConfigs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
            kafkaConsumerConfigs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            kafkaConsumerConfigs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            kafkaConsumerConfigs.put(ConsumerConfig.GROUP_ID_CONFIG, getKafkaConsumerGroup());
            return kafkaConsumerConfigs;
        }

        public Properties getProducerProperties() {
            Properties kafkaProducerConfigs = new Properties();
            kafkaProducerConfigs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());
            kafkaProducerConfigs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            kafkaProducerConfigs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            return kafkaProducerConfigs;
        }
    }

    public static class CrawlerConfigs {

        private int politenessDurationInSeconds;
        private int maxInMemoryPolitenessRecords;

        public int getPolitenessDurationInSeconds() {
            return politenessDurationInSeconds;
        }

        public void setPolitenessDurationInSeconds(int politenessDurationInSeconds) {
            this.politenessDurationInSeconds = politenessDurationInSeconds;
        }

        public int getMaxInMemoryPolitenessRecords() {
            return maxInMemoryPolitenessRecords;
        }

        public void setMaxInMemoryPolitenessRecords(int maxInMemoryPolitenessRecords) {
            this.maxInMemoryPolitenessRecords = maxInMemoryPolitenessRecords;
        }
    }
}
