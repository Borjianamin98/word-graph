package ir.ac.sbu.crawler.components;

import ir.ac.sbu.crawler.config.ApplicationConfigs;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.springframework.stereotype.Component;

@Component
public class InMemoryLinks {

    private final BlockingQueue<String> linksQueue;

    public InMemoryLinks(ApplicationConfigs applicationConfigs) {
        this.linksQueue = new ArrayBlockingQueue<>(applicationConfigs.getInMemoryLinkQueueSize());
    }

    public BlockingQueue<String> getLinksQueue() {
        return linksQueue;
    }
}
