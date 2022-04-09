package ir.ac.sbu.crawler.repository;

import ir.ac.sbu.crawler.model.Link;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LinkRepository extends MongoRepository<Link, String> {

}