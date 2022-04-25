package ir.ac.sbu.anchor.extractor.components;

import ir.ac.sbu.anchor.extractor.config.ApplicationConfigs;
import ir.ac.sbu.anchor.extractor.config.ApplicationConfigs.AnchorExtractorConfigs;
import ir.ac.sbu.model.Models.Anchor;
import ir.ac.sbu.model.Models.Page;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AnchorExtractor {

    private static final Logger logger = LoggerFactory.getLogger(AnchorExtractor.class);

    private final Thread anchorExtractorThread;
    private final BlockingQueue<Anchor> extractedAnchorsQueue;

    private volatile boolean running = false;

    public AnchorExtractor(ApplicationConfigs applicationConfigs, PageReader pageReader) {
        AnchorExtractorConfigs anchorExtractorConfigs = applicationConfigs.getAnchorExtractorConfigs();
        this.extractedAnchorsQueue = new ArrayBlockingQueue<>(anchorExtractorConfigs.getInMemoryAnchorsQueueSize());

        running = true;
        this.anchorExtractorThread = new Thread(() -> {
            while (running) {
                try {
                    Page page = pageReader.getNextPage();
                    processPage(page);
                } catch (InterruptedException e) {
                    if (running) {
                        throw new AssertionError("Unexpected interrupt while processing pages", e);
                    }
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "Anchor Extractor");
        this.anchorExtractorThread.start();
    }

    @PreDestroy
    public void destroy() {
        logger.info("Stopping anchor extractor ...");
        running = false;
        anchorExtractorThread.interrupt();
        try {
            anchorExtractorThread.join();
            logger.info("Anchor extractor stopped successfully");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Unexpected interrupt", e);
        }
    }

    public Anchor getNextAnchor() throws InterruptedException {
        return extractedAnchorsQueue.take();
    }

    private void processPage(Page page) throws InterruptedException {
        logger.info("Extract anchors of page: link = {}", page.getLink());
        String pageLink = page.getLink();
        for (String anchorLink : page.getAnchorsList()) {
            extractedAnchorsQueue.put(Anchor.newBuilder()
                    .setSource(pageLink)
                    .setDestination(anchorLink)
                    .build());
        }
    }
}
