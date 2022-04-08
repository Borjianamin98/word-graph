package ir.ac.sbu.crawler.components;

import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LinkCrawler {

    private static final Logger logger = LoggerFactory.getLogger(LinkCrawler.class);

    private final Thread crawlerThread;

    private volatile boolean running = false;

    public LinkCrawler(LinkReader linkReader) {
        running = true;
        this.crawlerThread = new Thread(() -> {
            while (running) {
                String link;
                try {
                    link = linkReader.getNextLink();
                } catch (InterruptedException e) {
                    if (running) {
                        throw new AssertionError("Unexpected interrupt while polling links", e);
                    }
                    Thread.currentThread().interrupt();
                    break;
                }
                // process link
            }
        }, "Link Crawler");
        this.crawlerThread.start();
    }

    @PreDestroy
    public void destroy() {
        logger.info("Stopping link crawler ...");
        running = false;
        crawlerThread.interrupt();
        try {
            crawlerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Unexpected interrupt while waiting for link reader closing");
        }
        logger.info("Link crawler stopped successfully");
    }

}
