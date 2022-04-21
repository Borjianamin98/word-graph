package ir.ac.sbu.crawler.components;

import ir.ac.sbu.crawler.config.ApplicationConfigs;
import ir.ac.sbu.crawler.config.ApplicationConfigs.KafkaConfigs;
import ir.ac.sbu.model.Models.Page;
import java.util.Properties;
import javax.annotation.PreDestroy;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PageSender {

    private static final Logger logger = LoggerFactory.getLogger(PageSender.class);

    private final KafkaConfigs kafkaConfigs;
    private final KafkaProducer<byte[], byte[]> kafkaProducer;
    private final Thread senderThread;

    private volatile boolean running = false;

    public PageSender(ApplicationConfigs applicationConfigs, LinkCrawler linkCrawler) {
        this.kafkaConfigs = applicationConfigs.getKafkaConfigs();

        Properties kafkaProducerConfigs = kafkaConfigs.getBaseProducerProperties();
        kafkaProducerConfigs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        kafkaProducerConfigs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        kafkaProducer = new KafkaProducer<>(kafkaProducerConfigs);

        running = true;
        this.senderThread = new Thread(() -> {
            while (running) {
                Page page;
                try {
                    page = linkCrawler.getNextPage();
                } catch (InterruptedException e) {
                    if (running) {
                        throw new AssertionError("Unexpected interrupt while polling links", e);
                    }
                    Thread.currentThread().interrupt();
                    break;
                }
                processPage(page);
            }
        }, "Page Sender");
        this.senderThread.start();
    }

    @PreDestroy
    public void destroy() {
        logger.info("Stopping Page sender ...");
        running = false;
        senderThread.interrupt();
        try {
            senderThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Unexpected interrupt while waiting for link sender closing");
        }
        kafkaProducer.close();
        logger.info("Page sender stopped successfully");
    }

    private void processPage(Page page) {
        kafkaProducer.send(new ProducerRecord<>(kafkaConfigs.getPagesTopicName(), page.toByteArray()));
    }

}
