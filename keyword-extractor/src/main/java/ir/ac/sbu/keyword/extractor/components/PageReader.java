package ir.ac.sbu.keyword.extractor.components;

import com.google.protobuf.InvalidProtocolBufferException;
import ir.ac.sbu.keyword.extractor.config.ApplicationConfigs;
import ir.ac.sbu.keyword.extractor.config.ApplicationConfigs.KafkaConfigs;
import ir.ac.sbu.model.Models.Page;
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
import org.apache.kafka.common.errors.InterruptException;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PageReader {

    private static final Logger logger = LoggerFactory.getLogger(PageReader.class);

    private final KafkaConfigs kafkaConfigs;
    private final KafkaConsumer<byte[], byte[]> kafkaConsumer;
    private final BlockingQueue<Page> pagesQueue;
    private final Thread pageReaderThread;

    private volatile boolean running = false;

    public PageReader(ApplicationConfigs applicationConfigs) {
        this.pagesQueue = new ArrayBlockingQueue<>(
                applicationConfigs.getKeywordExtractorConfigs().getInMemoryPageQueueSize());
        this.kafkaConfigs = applicationConfigs.getKafkaConfigs();

        kafkaConsumer = new KafkaConsumer<>(kafkaConfigs.getConsumerProperties(true));
        kafkaConsumer.subscribe(Collections.singletonList(kafkaConfigs.getPagesTopicName()));

        running = true;
        this.pageReaderThread = new Thread(() -> {
            while (running) {
                try {
                    // Polls at most 500 pages from Kafka topic
                    ConsumerRecords<byte[], byte[]> pageRecords = kafkaConsumer.poll(Duration.ofMinutes(5));
                    for (ConsumerRecord<byte[], byte[]> pageRecord : pageRecords) {
                        byte[] value = pageRecord.value();
                        Page page = Page.parseFrom(value);
                        logger.info("Putting '{}' page in in-memory queue ...", page.getLink());
                        pagesQueue.put(page);
                    }
                    kafkaConsumer.commitSync();
                } catch (InvalidProtocolBufferException e) {
                    logger.warn("Invalid protobuf record skipped.", e);
                } catch (InterruptedException | InterruptException e) {
                    if (running) {
                        throw new AssertionError("Unexpected interrupt while polling pages", e);
                    }
                    Thread.currentThread().interrupt();
                }
            }
        }, "Page Reader");
        this.pageReaderThread.start();
    }

    public Page getNextPage() throws InterruptedException {
        return pagesQueue.take();
    }

    @PreDestroy
    public void destroy() {
        logger.info("Stopping page reader ...");
        running = false;
        pageReaderThread.interrupt();
        try {
            pageReaderThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Unexpected interrupt", e);
        }
        kafkaConsumer.close();
        restoreInMemoryPages();
        logger.info("Page reader stopped successfully");
    }

    private void restoreInMemoryPages() {
        logger.info("Restore in-memory pages to Kafka topic: count = {}", pagesQueue.size());
        Properties kafkaProducerConfigs = kafkaConfigs.getBaseProducerProperties();
        kafkaProducerConfigs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        kafkaProducerConfigs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        try (KafkaProducer<byte[], byte[]> kafkaProducer = new KafkaProducer<>(kafkaProducerConfigs)) {
            for (Page page : pagesQueue) {
                kafkaProducer.send(new ProducerRecord<>(kafkaConfigs.getPagesTopicName(), page.toByteArray()));
            }
        }
        logger.info("In-memory pages restored successfully");
    }

}
