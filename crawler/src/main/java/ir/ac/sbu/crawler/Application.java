package ir.ac.sbu.crawler;

import ir.ac.sbu.crawler.components.CrawlerComponent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Application {

    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        context.getBean(CrawlerComponent.class).someAsyncMethod();
    }

}
