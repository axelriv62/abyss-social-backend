package fr.univartois.butinfo.sae.abyss.social.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "Groups")
public class Group {

    @Id
    private String id;

    @DBRef
    private User user;

    private String name;

    private String[] tags;

    private String[] posts;

    @Field("created_at")
    private LocalDateTime createdAt;

    public Group() {}

    public Group(String name) {
        this.name = name;
        this.createdAt = LocalDateTime.now();
    }

    public Group(String name, String[] tags) {
        this.name = name;
        this.tags = tags;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String[] getTags() { return tags; }
    public void setTags(String[] tags) { this.tags = tags; }

    public String[] getPosts() { return posts; }
    public void setPosts(String[] posts) { this.posts = posts; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}