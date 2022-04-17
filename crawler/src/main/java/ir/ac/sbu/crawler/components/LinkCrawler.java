package ir.ac.sbu.crawler.components;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import ir.ac.sbu.crawler.config.ApplicationConfigs;
import ir.ac.sbu.crawler.config.ApplicationConfigs.CrawlerConfigs;
import ir.ac.sbu.crawler.exception.LinkRequestException;
import ir.ac.sbu.crawler.service.LinkService;
import ir.ac.sbu.link.LinkUtility;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLHandshakeException;
import org.apache.tika.langdetect.optimaize.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LinkCrawler {

    private static final Logger logger = LoggerFactory.getLogger(LinkCrawler.class);

    private final CrawlerConfigs crawlerConfigs;
    // In-memory cache used to check politeness before sending request to URL
    private final Cache<String, Boolean> politenessCache;
    private final LinkService linkService;
    private final Thread crawlerThread;
    // Language detector to detect language of crawled pages
    private LanguageDetector languageDetector;

    private volatile boolean running = false;

    public LinkCrawler(ApplicationConfigs applicationConfigs, LinkReader linkReader, LinkService linkService) {
        this.crawlerConfigs = applicationConfigs.getCrawlerConfigs();
        this.politenessCache = Caffeine.newBuilder()
                .maximumSize(crawlerConfigs.getMaxInMemoryPolitenessRecords())
                .expireAfterWrite(crawlerConfigs.getPolitenessDurationInSeconds(), TimeUnit.SECONDS)
                .build();
        this.linkService = linkService;
        this.languageDetector = new OptimaizeLangDetector().loadModels();

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

    private Connection.Response requestLink(String link) throws LinkRequestException {
        try {
            return Jsoup.connect(link)
                    .userAgent(this.crawlerConfigs.getRequestUserAgent())
                    .timeout(this.crawlerConfigs.getRequestTimeoutMilliseconds())
                    .followRedirects(true)
                    .ignoreContentType(true)
                    .execute();
        } catch (SSLHandshakeException e) {
            logger.warn("Server certificate verification failed: {}", link);
        } catch (UnknownHostException e) {
            logger.warn("Could not resolve host: {}", link);
        } catch (MalformedURLException | IllegalArgumentException e) {
            logger.warn("Illegal link format: {}", link);
        } catch (HttpStatusException e) {
            logger.warn("Response is not OK: link={} status-code={}", e.getUrl(), e.getStatusCode());
        } catch (SocketTimeoutException e) {
            logger.warn("Link connection timeout: {}", link);
        } catch (StringIndexOutOfBoundsException | IOException e) {
            logger.warn("Unable to parse page with jsoup: {}", link);
        }
        throw new LinkRequestException("Unable to get response from link: " + link);
    }

    public boolean isEnglishLanguage(String text) {
        LanguageResult detectionResult = languageDetector.detect(text);
        return detectionResult.isLanguage("en") &&
                detectionResult.getRawScore() > this.crawlerConfigs.getEnglishLanguageDetectorMinimumScore();
    }

    private boolean isPoliteToCrawl(String linkMainDomain) {
        return politenessCache.getIfPresent(linkMainDomain) == null;
    }

}
