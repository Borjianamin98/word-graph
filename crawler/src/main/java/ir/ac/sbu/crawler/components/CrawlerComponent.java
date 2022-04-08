package ir.ac.sbu.crawler.components;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class CrawlerComponent {

    @Async
    public void someAsyncMethod() throws InterruptedException {
        for (; ; ) {
            Thread.sleep(1_000);
            System.out.println(1);
        }
    }
}
