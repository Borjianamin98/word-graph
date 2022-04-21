package ir.ac.sbu.crawler.components;

import ir.ac.sbu.crawler.config.ApplicationConfigs;
import ir.ac.sbu.crawler.config.ApplicationConfigs.KafkaConfigs;
import ir.ac.sbu.model.Models.Page;
import java.util.Collections;
import javax.annotation.PreDestroy;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LinkSender {

    private static final Logger logger = LoggerFactory.getLogger(LinkSender.class);

    private final KafkaConfigs kafkaConfigs;
    private final KafkaProducer<byte[], byte[]> kafkaProducer;
    private final Thread senderThread;

    private volatile boolean running = false;

    public LinkSender(ApplicationConfigs applicationConfigs, LinkCrawler linkCrawler) {
        this.kafkaConfigs = applicationConfigs.getKafkaConfigs();
        kafkaProducer = new KafkaProducer<>(kafkaConfigs.getProducerProperties());

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
        }, "Link Sender");
        this.senderThread.start();
    }

    @PreDestroy
    public void destroy() {
        logger.info("Stopping link sender ...");
        running = false;
        senderThread.interrupt();
        try {
            senderThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Unexpected interrupt while waiting for link sender closing");
        }
        kafkaProducer.close();
        logger.info("Link sender stopped successfully");
    }

    private void processPage(Page page) {
        kafkaProducer.send(new ProducerRecord<>(kafkaConfigs.getPagesTopicName(), page.toByteArray()));
    }

}
