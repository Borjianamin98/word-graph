package ir.ac.sbu.crawler.components;

import ir.ac.sbu.crawler.config.ApplicationConfigs;
import ir.ac.sbu.crawler.config.ApplicationConfigs.KafkaConfigs;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import javax.annotation.PreDestroy;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LinkReader {

    private static final Logger logger = LoggerFactory.getLogger(LinkReader.class);

    private final KafkaConfigs kafkaConfigs;
    private final KafkaConsumer<String, String> kafkaConsumer;
    private final BlockingQueue<String> linksQueue;
    private final Thread linkReaderThread;

    private volatile boolean running = false;

    public LinkReader(ApplicationConfigs applicationConfigs) {
        this.linksQueue = new ArrayBlockingQueue<>(applicationConfigs.getCrawlerConfigs().getInMemoryLinkQueueSize());
        this.kafkaConfigs = applicationConfigs.getKafkaConfigs();

        kafkaConsumer = new KafkaConsumer<>(kafkaConfigs.getConsumerProperties());
        kafkaConsumer.subscribe(Collections.singletonList(kafkaConfigs.getLinksTopicName()));

        running = true;
        this.linkReaderThread = new Thread(() -> {
            while (running) {
                try {
                    // Polls at most 500 links from Kafka topic
                    ConsumerRecords<String, String> linkRecords = kafkaConsumer.poll(Duration.ofMinutes(5));
                    for (ConsumerRecord<String, String> linkRecord : linkRecords) {
                        String link = linkRecord.value();
                        logger.info("Putting '{}' link in in-memory queue ...", link);
                        linksQueue.put(link);
                    }
                    kafkaConsumer.commitSync();
                } catch (InterruptedException | org.apache.kafka.common.errors.InterruptException e) {
                    if (running) {
                        throw new AssertionError("Unexpected interrupt while polling links", e);
                    }
                    Thread.currentThread().interrupt();
                }
            }
        }, "Link Reader");
        this.linkReaderThread.start();
    }

    public String getNextLink() throws InterruptedException {
        return linksQueue.take();
    }

    @PreDestroy
    public void destroy() {
        logger.info("Stopping link reader ...");
        running = false;
        linkReaderThread.interrupt();
        try {
            linkReaderThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Unexpected interrupt while waiting for link reader closing");
        }
        kafkaConsumer.close();
        restoreInMemoryLinks();
        logger.info("Link reader stopped successfully");
    }

    private void restoreInMemoryLinks() {
        logger.info("Restore in-memory links to Kafka topic: count = {}", linksQueue.size());
        Properties kafkaProducerConfigs = kafkaConfigs.getBaseProducerProperties();
        kafkaProducerConfigs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        kafkaProducerConfigs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        try (KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(kafkaProducerConfigs)) {
            for (String link : linksQueue) {
                kafkaProducer.send(new ProducerRecord<>(kafkaConfigs.getLinksTopicName(), link));
            }
        }
        logger.info("In-memory links restored successfully");
    }

}
