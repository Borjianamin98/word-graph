package ir.ac.sbu.crawler.components;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import ir.ac.sbu.crawler.config.ApplicationConfigs;
import ir.ac.sbu.crawler.config.ApplicationConfigs.CrawlerConfigs;
import ir.ac.sbu.crawler.exception.LinkDocumentException;
import ir.ac.sbu.crawler.exception.LinkRequestException;
import ir.ac.sbu.crawler.service.LinkService;
import ir.ac.sbu.link.LinkUtility;
import ir.ac.sbu.model.Models.Page;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLHandshakeException;
import org.apache.tika.langdetect.optimaize.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
    private final LanguageDetector languageDetector;

    private final BlockingQueue<Page> crawledPagesQueue;
    private final BlockingQueue<String> newLinksQueue;

    private volatile boolean running = false;

    public LinkCrawler(ApplicationConfigs applicationConfigs, LinkReader linkReader, LinkService linkService) {
        this.crawlerConfigs = applicationConfigs.getCrawlerConfigs();
        this.politenessCache = Caffeine.newBuilder()
                .maximumSize(crawlerConfigs.getMaxInMemoryPolitenessRecords())
                .expireAfterWrite(crawlerConfigs.getPolitenessDurationInSeconds(), TimeUnit.SECONDS)
                .build();
        this.linkService = linkService;
        this.languageDetector = new OptimaizeLangDetector().loadModels();
        this.crawledPagesQueue = new ArrayBlockingQueue<>(crawlerConfigs.getInMemoryPageQueueSize());
        this.newLinksQueue = new ArrayBlockingQueue<>(crawlerConfigs.getInMemoryNewLinkQueueSize());

        running = true;
        this.crawlerThread = new Thread(() -> {
            while (running) {
                try {
                    String link = linkReader.getNextLink();
                    processLink(link);
                } catch (InterruptedException e) {
                    if (running) {
                        throw new AssertionError("Unexpected interrupt while processing links", e);
                    }
                    Thread.currentThread().interrupt();
                    break;
                }
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
            logger.info("Link crawler stopped successfully");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Unexpected interrupt", e);
        }
    }

    public Page getNextPage() throws InterruptedException {
        return crawledPagesQueue.take();
    }

    public String getNextNewLink() throws InterruptedException {
        return newLinksQueue.take();
    }

    public Queue<String> getAllNewLinks() {
        return newLinksQueue;
    }

    private void processLink(String link) throws InterruptedException {
        String linkMainDomain;
        try {
            linkMainDomain = LinkUtility.getMainDomain(link);
        } catch (MalformedURLException e) {
            logger.warn("Illegal URL for crawling (Ignored for crawling): {}", link, e);
            return;
        }

        if (!isPoliteToCrawl(linkMainDomain)) {
            // We will crawl it again later.
            logger.info("Skip link because of politeness duration: link = {}, domain = {}", link, linkMainDomain);
            newLinksQueue.put(link);
            return;
        }
        if (isCrawledBefore(link)) {
            logger.info("Skip link because crawled before: {}", link);
            return;
        }

        linkService.addLink(link);
        politenessCache.put(linkMainDomain, true);

        Optional<Page> crawledPage;
        try {
            crawledPage = crawlLink(link);
        } catch (LinkRequestException | LinkDocumentException e) {
            logger.warn("Unable to crawl link (ignored): {}", link, e);
            return;
        }
        if (crawledPage.isPresent()) {
            Page page = crawledPage.get();
            for (String anchorLink : page.getAnchorsList()) {
                newLinksQueue.put(anchorLink);
            }
            crawledPagesQueue.put(page);
        }
    }

    private Optional<Page> crawlLink(String link)
            throws LinkRequestException, LinkDocumentException, InterruptedException {
        Connection.Response linkResponse = requestLink(link);

        String redirectedLink = linkResponse.url().toExternalForm();
        if (isCrawledBefore(redirectedLink)) {
            return Optional.empty();
        }

        linkService.addLink(link);
        Document linkDocument = extractDocument(redirectedLink, linkResponse);

        String linkContent = linkDocument.text().replace("\n", " ");
        if (linkContent.isEmpty()) {
            logger.info("There is no content for link (ignored): {}", redirectedLink);
            return Optional.empty();
        } else if (!isEnglishLanguage(linkContent)) {
            logger.info("Content of link is not in english language (ignored): {}", redirectedLink);
            return Optional.empty();
        }

        Set<String> anchors = getAnchors(redirectedLink, linkDocument);
        return Optional.of(Page.newBuilder()
                .setLink(redirectedLink)
                .setContent(linkContent)
                .addAllAnchors(anchors)
                .build());
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

    private Document extractDocument(String link, Response response) throws LinkDocumentException {
        try {
            String contentType = response.contentType();
            if (contentType != null && !contentType.contains("text/html")) {
                logger.warn("Skip link with unknown content type: {}", link);
            }
            return response.parse();
        } catch (StringIndexOutOfBoundsException | IOException e) {
            logger.warn("Unable to parse link content with jsoup: {}", link);
            throw new LinkDocumentException("Unable to extract content from link: " + link, e);
        }
    }

    private boolean isEnglishLanguage(String text) {
        LanguageResult detectionResult = languageDetector.detect(text);
        return detectionResult.isLanguage("en") &&
                detectionResult.getRawScore() > this.crawlerConfigs.getEnglishLanguageDetectorMinimumScore();
    }

    private Set<String> getAnchors(String link, Document document) throws LinkDocumentException {
        Set<String> anchors = new HashSet<>();
        for (Element linkElement : document.getElementsByTag("a")) {
            String absUrl = linkElement.absUrl("href");
            if (!absUrl.isEmpty() && !absUrl.matches("mailto:.*") && LinkUtility.isValidUrl(absUrl)) {
                try {
                    String normalizedUrl = LinkUtility.normalize(absUrl);
                    anchors.add(normalizedUrl);
                } catch (MalformedURLException e) {
                    throw new LinkDocumentException("Unable to normalize anchor href: link = "
                            + link + " href link = " + absUrl, e);
                }
            }
        }
        return anchors;
    }

    private boolean isPoliteToCrawl(String linkMainDomain) {
        return politenessCache.getIfPresent(linkMainDomain) == null;
    }

    private boolean isCrawledBefore(String link) throws InterruptedException {
        return linkService.isCrawled(link);
    }
}
