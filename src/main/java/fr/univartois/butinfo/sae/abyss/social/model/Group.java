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

    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String[] getTags() { return tags; }
    public void setTags(String[] tags) { this.tags = tags; }

    public String[] getPosts() { return posts; }
    public void setPosts(String[] posts) { this.posts = posts; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public ObjectId getUserId() { return user.getId(); }
    public void setUserId(ObjectId userId) {
        if (userId == null) {
            this.user = null;
            return;
        }
        User u = new User();
        u.setId(userId);
        this.user = u;
    }
    /**
     * Gets the user who created the Page.
     *
     * @return The User who created the Page.
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user who created the Page.
     *
     * @param user The User to set as the creator of the Page.
     */
    public void setUser(User user) {
        this.user = user;
    }
}