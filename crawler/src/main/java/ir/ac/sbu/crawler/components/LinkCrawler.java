package ir.ac.sbu.crawler.components;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import ir.ac.sbu.crawler.config.ApplicationConfigs;
import ir.ac.sbu.crawler.config.ApplicationConfigs.CrawlerConfigs;
import ir.ac.sbu.crawler.service.LinkService;
import ir.ac.sbu.link.LinkUtility;
import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LinkCrawler {

    private static final Logger logger = LoggerFactory.getLogger(LinkCrawler.class);

    // In-memory cache used to check politeness before sending request to URL
    private final Cache<String, Boolean> politenessCache;
    private final LinkService linkService;
    private final Thread crawlerThread;

    private volatile boolean running = false;

    public LinkCrawler(ApplicationConfigs applicationConfigs, LinkReader linkReader, LinkService linkService) {
        CrawlerConfigs crawlerConfigs = applicationConfigs.getCrawlerConfigs();
        this.politenessCache = Caffeine.newBuilder()
                .maximumSize(crawlerConfigs.getMaxInMemoryPolitenessRecords())
                .expireAfterWrite(crawlerConfigs.getPolitenessDurationInSeconds(), TimeUnit.SECONDS)
                .build();
        this.linkService = linkService;

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
                processLink(link);
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

    private void processLink(String link) {
        String linkMainDomain;
        try {
            linkMainDomain = LinkUtility.getMainDomain(link);
        } catch (MalformedURLException e) {
            logger.warn("Illegal URL for crawling: {}", link, e);
            return;
        }

        if (isPoliteToCrawl(linkMainDomain)) {
            if (linkService.isCrawled(link)) {
                logger.info("Skip link because link crawled before: {}", link);
                return;
            }
            linkService.addLink(link);
            politenessCache.put(linkMainDomain, true);
            crawlLink(link);
            // TODO: handle result of crawl
        } else {
            logger.info("Skip link because of politeness duration: {}", link);
        }
    }

    private void crawlLink(String link) {
        // TODO: Implement it
    }

    private boolean isPoliteToCrawl(String linkMainDomain) {
        return politenessCache.getIfPresent(linkMainDomain) == null;
    }

}
