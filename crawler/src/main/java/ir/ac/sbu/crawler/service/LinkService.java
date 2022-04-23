package ir.ac.sbu.crawler.service;

import com.mongodb.MongoInterruptedException;
import ir.ac.sbu.crawler.model.Link;
import ir.ac.sbu.crawler.repository.LinkRepository;
import ir.ac.sbu.link.LinkUtility;
import org.springframework.data.mongodb.UncategorizedMongoDbException;
import org.springframework.stereotype.Component;

@Component
public class LinkService {

    private final LinkRepository linkRepository;

    public LinkService(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    public void addLink(String link) throws InterruptedException {
        try {
            linkRepository.save(new Link(LinkUtility.hashLinkCompressed(link)));
        } catch (UncategorizedMongoDbException e) {
            throw unwrapMongoInterruptException(e);
        }
    }

    public boolean isCrawled(String link) throws InterruptedException {
        try {
            return linkRepository.findById(LinkUtility.hashLinkCompressed(link)).isPresent();
        } catch (UncategorizedMongoDbException e) {
            throw unwrapMongoInterruptException(e);
        }
    }

    private RuntimeException unwrapMongoInterruptException(UncategorizedMongoDbException exception)
            throws InterruptedException {
        if (exception.getCause() instanceof MongoInterruptedException) {
            InterruptedException e = new InterruptedException("Interruption during database operation!");
            e.initCause(exception);
            throw e;
        }
        return exception;
    }
}
