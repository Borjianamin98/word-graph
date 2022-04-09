package ir.ac.sbu.crawler.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("links")
public class Link {

    @Id
    public String id;

    public Link() {
    }

    public Link(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Link{" +
                "id='" + id + '\'' +
                '}';
    }
}
