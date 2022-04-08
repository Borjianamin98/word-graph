package ir.ac.sbu.crawler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@EnableConfigurationProperties
public class ApplicationConfigs {

    private int inMemoryLinkQueueSize;
    private final KafkaConfigs kafkaConfigs = new KafkaConfigs();

    public int getInMemoryLinkQueueSize() {
        return inMemoryLinkQueueSize;
    }

    public void setInMemoryLinkQueueSize(int inMemoryLinkQueueSize) {
        this.inMemoryLinkQueueSize = inMemoryLinkQueueSize;
    }

    public KafkaConfigs getKafkaConfigs() {
        return kafkaConfigs;
    }

    public static class KafkaConfigs {

        private String bootstrapServers;
        private String kafkaConsumerGroup;

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
    }
}
