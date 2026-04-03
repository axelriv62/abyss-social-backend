package fr.univartois.butinfo.sae.abyss.social.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Tokens")
public class Token {

    @Id
    private ObjectId id;

    @Indexed(unique = true)
    private String userToken;

    private ObjectId userId;

    private boolean revoked;

    private boolean expired;

    public Token() {}

    public Token(String token, ObjectId userId) {
        this.userToken = token;
        this.userId = userId;
        this.revoked = false;
        this.expired = false;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public ObjectId getUserId() {
        return userId;
    }

    public void setUserId(ObjectId userId) {
        this.userId = userId;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

}
