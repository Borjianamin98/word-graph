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
public class LinkSender {

    private static final Logger logger = LoggerFactory.getLogger(LinkSender.class);

    private final KafkaConfigs kafkaConfigs;
    private final KafkaProducer<String, String> kafkaProducer;
    private final Thread senderThread;

    private volatile boolean running = false;

    public LinkSender(ApplicationConfigs applicationConfigs, LinkCrawler linkCrawler) {
        this.kafkaConfigs = applicationConfigs.getKafkaConfigs();

        Properties kafkaProducerConfigs = kafkaConfigs.getBaseProducerProperties();
        kafkaProducerConfigs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        kafkaProducerConfigs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        kafkaProducer = new KafkaProducer<>(kafkaProducerConfigs);

        running = true;
        this.senderThread = new Thread(() -> {
            while (running) {
                String newLink;
                try {
                    newLink = linkCrawler.getNextNewLink();
                } catch (InterruptedException e) {
                    if (running) {
                        throw new AssertionError("Unexpected interrupt while polling links", e);
                    }
                    Thread.currentThread().interrupt();
                    break;
                }
                processLink(newLink);
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
            logger.error("Unexpected interrupt", e);
        }
        kafkaProducer.close();
        logger.info("Link sender stopped successfully");
    }

    private void processLink(String link) {
        logger.info("Putting '{}' link in kafka queue ...", link);
        kafkaProducer.send(new ProducerRecord<>(kafkaConfigs.getLinksTopicName(), link));
    }

}
