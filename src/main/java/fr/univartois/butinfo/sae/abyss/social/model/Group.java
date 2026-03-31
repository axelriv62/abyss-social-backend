package fr.univartois.butinfo.sae.abyss.social.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "Groups")
public class Group {

    @Id
    private ObjectId id;

    private String name;

    private String[] tags;

    private ObjectId[] posts;

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

    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String[] getTags() { return tags; }
    public void setTags(String[] tags) { this.tags = tags; }

    public ObjectId[] getPosts() { return posts; }
    public void setPosts(ObjectId[] posts) { this.posts = posts; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}