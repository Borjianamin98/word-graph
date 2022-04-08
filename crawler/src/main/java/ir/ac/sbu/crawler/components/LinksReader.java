package ir.ac.sbu.crawler.components;

import ir.ac.sbu.crawler.config.ApplicationConfigs;
import ir.ac.sbu.crawler.config.ApplicationConfigs.KafkaConfigs;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import javax.annotation.PreDestroy;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LinksReader {

    private static final Logger logger = LoggerFactory.getLogger(LinksReader.class);

    private final KafkaConsumer<String, String> kafkaConsumer;
    private final Thread linkReader;

    private volatile boolean running = false;

    public LinksReader(ApplicationConfigs applicationConfigs, InMemoryLinks inMemoryLinks) {
        // initializing Kafka consumer
        KafkaConfigs applicationKafkaConfigs = applicationConfigs.getKafkaConfigs();
        Properties kafkaConsumerConfig = new Properties();
        kafkaConsumerConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        kafkaConsumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, applicationKafkaConfigs.getBootstrapServers());
        kafkaConsumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaConsumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaConsumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, applicationKafkaConfigs.getKafkaConsumerGroup());
        kafkaConsumer = new KafkaConsumer<>(kafkaConsumerConfig);

        BlockingQueue<String> linksQueue = inMemoryLinks.getLinksQueue();

        this.linkReader = new Thread(() -> {
            running = true;
            while (running) {
                try {
                    linksQueue.put("1");
                    logger.info("Put another record ...");
                    Thread.sleep(1_000);
                } catch (InterruptedException e) {
                    if (running) {
                        throw new AssertionError("Unexpected interrupt", e);
                    }
                    Thread.currentThread().interrupt();
                }
            }
        }, "Link Reader");
        this.linkReader.start();
    }

    @PreDestroy
    public void destroy() {
        logger.info("Stopping links reader ...");
        running = false;
        linkReader.interrupt();
        try {
            linkReader.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Unexpected interrupt while waiting for link reader closing");
        }
        kafkaConsumer.close();
        logger.info("Links reader stopped successfully");
    }

}
