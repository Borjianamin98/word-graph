package ir.ac.sbu.keyword.extractor.components;

import ir.ac.sbu.keyword.extractor.config.ApplicationConfigs;
import ir.ac.sbu.keyword.extractor.config.ApplicationConfigs.KafkaConfigs;
import ir.ac.sbu.model.Models.PageKeywords;
import java.util.Properties;
import javax.annotation.PreDestroy;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class KeywordSender {

    private static final Logger logger = LoggerFactory.getLogger(KeywordSender.class);

    private final KafkaConfigs kafkaConfigs;
    private final KafkaProducer<byte[], byte[]> kafkaProducer;
    private final Thread keywordSenderThread;

    private volatile boolean running = false;

    public KeywordSender(ApplicationConfigs applicationConfigs, KeywordExtractor keywordExtractor) {
        this.kafkaConfigs = applicationConfigs.getKafkaConfigs();

        Properties kafkaProducerConfigs = kafkaConfigs.getBaseProducerProperties();
        kafkaProducerConfigs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        kafkaProducerConfigs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        kafkaProducer = new KafkaProducer<>(kafkaProducerConfigs);

        running = true;
        this.keywordSenderThread = new Thread(() -> {
            while (running) {
                try {
                    PageKeywords anchor = keywordExtractor.getNextPageKeywords();
                    processPageKeywords(anchor);
                } catch (InterruptedException e) {
                    if (running) {
                        throw new AssertionError("Unexpected interrupt while processing page keywords", e);
                    }
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "Keyword Sender");
        this.keywordSenderThread.start();
    }

    @PreDestroy
    public void destroy() {
        logger.info("Stopping keyword sender ...");
        running = false;
        keywordSenderThread.interrupt();
        try {
            keywordSenderThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Unexpected interrupt", e);
        }
        kafkaProducer.close();
        logger.info("Keyword sender stopped successfully");
    }

    private void processPageKeywords(PageKeywords anchor) {
        kafkaProducer.send(new ProducerRecord<>(kafkaConfigs.getKeywordsTopicName(), anchor.toByteArray()));
    }

}
