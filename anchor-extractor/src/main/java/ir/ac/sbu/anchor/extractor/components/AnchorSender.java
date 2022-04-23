package ir.ac.sbu.anchor.extractor.components;

import ir.ac.sbu.anchor.extractor.config.ApplicationConfigs;
import ir.ac.sbu.anchor.extractor.config.ApplicationConfigs.KafkaConfigs;
import ir.ac.sbu.model.Models.Anchor;
import ir.ac.sbu.model.Models.Page;
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
public class AnchorSender {

    private static final Logger logger = LoggerFactory.getLogger(AnchorSender.class);

    private final KafkaConfigs kafkaConfigs;
    private final KafkaProducer<byte[], byte[]> kafkaProducer;
    private final Thread anchorSenderThread;

    private volatile boolean running = false;

    public AnchorSender(ApplicationConfigs applicationConfigs, AnchorExtractor anchorExtractor) {
        this.kafkaConfigs = applicationConfigs.getKafkaConfigs();

        Properties kafkaProducerConfigs = kafkaConfigs.getBaseProducerProperties();
        kafkaProducerConfigs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        kafkaProducerConfigs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        kafkaProducer = new KafkaProducer<>(kafkaProducerConfigs);

        running = true;
        this.anchorSenderThread = new Thread(() -> {
            while (running) {
                try {
                    Anchor anchor = anchorExtractor.getNextAnchor();
                    processAnchor(anchor);
                } catch (InterruptedException e) {
                    if (running) {
                        throw new AssertionError("Unexpected interrupt while processing anchors", e);
                    }
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "Anchor Sender");
        this.anchorSenderThread.start();
    }

    @PreDestroy
    public void destroy() {
        logger.info("Stopping anchor sender ...");
        running = false;
        anchorSenderThread.interrupt();
        try {
            anchorSenderThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Unexpected interrupt while waiting for anchor sender closing");
        }
        kafkaProducer.close();
        logger.info("Anchor sender stopped successfully");
    }

    private void processAnchor(Anchor anchor) {
        kafkaProducer.send(new ProducerRecord<>(kafkaConfigs.getAnchorsTopicName(), anchor.toByteArray()));
    }

}
