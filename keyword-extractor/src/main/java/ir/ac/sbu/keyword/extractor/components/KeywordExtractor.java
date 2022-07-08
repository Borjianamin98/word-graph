package ir.ac.sbu.keyword.extractor.components;

import ir.ac.sbu.keyword.extractor.config.ApplicationConfigs;
import ir.ac.sbu.keyword.extractor.config.ApplicationConfigs.KeywordExtractorConfigs;
import ir.ac.sbu.keyword.extractor.model.YakeSingleResponseDto;
import ir.ac.sbu.keyword.extractor.service.YakeService;
import ir.ac.sbu.model.Models.Page;
import ir.ac.sbu.model.Models.PageKeywords;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class KeywordExtractor {

    private static final Logger logger = LoggerFactory.getLogger(KeywordExtractor.class);

    private final Thread keywordExtractorThread;
    private final KeywordExtractorConfigs keywordExtractorConfigs;
    private final BlockingQueue<PageKeywords> extractedPageKeywordsQueue;
    private final YakeService yakeService;

    private volatile boolean running = false;

    public KeywordExtractor(ApplicationConfigs applicationConfigs, PageReader pageReader, YakeService yakeService) {
        this.keywordExtractorConfigs = applicationConfigs.getKeywordExtractorConfigs();
        this.extractedPageKeywordsQueue = new ArrayBlockingQueue<>(
                keywordExtractorConfigs.getInMemoryPageKeywordsQueueSize());
        this.yakeService = yakeService;

        running = true;
        this.keywordExtractorThread = new Thread(() -> {
            while (running) {
                try {
                    Page page = pageReader.getNextPage();
                    processPage(page);
                } catch (InterruptedException e) {
                    if (running) {
                        throw new AssertionError("Unexpected exception while processing pages", e);
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
        if (page.getContent().length() < keywordExtractorConfigs.getMinimumPageContentSize()) {
            logger.info("Content of page is less than configured config (ignored): {}", page.getLink());
        }

        logger.info("Extract keywords of page: link = {}", page.getLink());
        List<String> topKeywords;
        try {
            List<YakeSingleResponseDto> extractedKeywords = yakeService.getKeywords(page.getContent());
            topKeywords = extractedKeywords.stream()
                    .filter(entry ->
                            containsNoneOf(entry.getKeyword(),
                                    keywordExtractorConfigs.getDiscardedCharacterSequences()))
                    .sorted((o1, o2) -> Double.compare(o2.getScore(), o1.getScore())) // Reverse sort
                    .map(YakeSingleResponseDto::getKeyword)
                    .limit(keywordExtractorConfigs.getMaxKeywordsPerPage())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("Unexpected exception during extracting keywords", e);
            throw new AssertionError("Unexpected exception during extracting keywords", e);
        }

        extractedPageKeywordsQueue.put(PageKeywords.newBuilder()
                .setLink(page.getLink())
                .addAllKeywords(topKeywords)
                .build());
        logger.info("Keywords of page extracted: link = {}", page.getLink());
    }

    private static boolean containsNoneOf(String content, List<String> strings) {
        for (String string : strings) {
            if (content.contains(string)) {
                return false;
            }
            // Skip keywords if contains numbers
            if (content.matches(".*\\d+.*")) {
                return false;
            }
        }
        return true;
    }
}
