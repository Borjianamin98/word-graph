package ir.ac.sbu.keyword.extractor.components;

import ir.ac.sbu.keyword.extractor.config.ApplicationConfigs;
import ir.ac.sbu.keyword.extractor.config.ApplicationConfigs.KeywordExtractorConfigs;
import ir.ac.sbu.model.Models.Page;
import ir.ac.sbu.model.Models.PageKeywords;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class KeywordExtractor {

    private static final Logger logger = LoggerFactory.getLogger(KeywordExtractor.class);

    private final Thread keywordExtractorThread;
    private final BlockingQueue<PageKeywords> extractedPageKeywordsQueue;

    private volatile boolean running = false;

    public KeywordExtractor(ApplicationConfigs applicationConfigs, PageReader pageReader) {
        KeywordExtractorConfigs keywordExtractorConfigs = applicationConfigs.getKeywordExtractorConfigs();
        this.extractedPageKeywordsQueue = new ArrayBlockingQueue<>(
                keywordExtractorConfigs.getInMemoryPageKeywordsQueueSize());

        running = true;
        this.keywordExtractorThread = new Thread(() -> {
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
        }, "Keyword Extractor");
        this.keywordExtractorThread.start();
    }

    @PreDestroy
    public void destroy() {
        logger.info("Stopping keyword extractor ...");
        running = false;
        keywordExtractorThread.interrupt();
        try {
            keywordExtractorThread.join();
            logger.info("Keyword extractor stopped successfully");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Unexpected interrupt", e);
        }
    }

    public PageKeywords getNextPageKeywords() throws InterruptedException {
        return extractedPageKeywordsQueue.take();
    }

    private void processPage(Page page) throws InterruptedException {
        logger.info("Extract anchors of page: link = {}", page.getLink());

        // TODO: Implement this method
        List<String> extractedKeywords = Collections.emptyList();

        extractedPageKeywordsQueue.put(PageKeywords.newBuilder()
                .setLink(page.getLink())
                .addAllKeywords(extractedKeywords)
                .build());
    }
}
