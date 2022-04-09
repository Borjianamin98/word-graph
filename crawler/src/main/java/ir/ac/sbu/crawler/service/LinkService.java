package ir.ac.sbu.crawler.service;

import ir.ac.sbu.crawler.model.Link;
import ir.ac.sbu.crawler.repository.LinkRepository;
import ir.ac.sbu.link.LinkUtility;

public class LinkService {

    private final LinkRepository linkRepository;

    public LinkService(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    public void addLink(String link) {
        linkRepository.save(new Link(LinkUtility.hashLinkCompressed(link)));
    }

    public boolean isCrawled(String link) {
        return linkRepository.findById(LinkUtility.hashLinkCompressed(link)).isPresent();
    }
}
