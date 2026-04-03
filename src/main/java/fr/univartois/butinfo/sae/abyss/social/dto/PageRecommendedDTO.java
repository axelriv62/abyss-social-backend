package fr.univartois.butinfo.sae.abyss.social.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.PositiveOrZero;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.List;

public record PageRecommendedDTO(
        String id,

        @PositiveOrZero(message = "Score must be zero or positive")
        Double score,

        @JsonProperty("friends_count")
        @PositiveOrZero(message = "Friends count must be zero or positive")
        Integer friendsCount,

        String name,

        List<String> tags,

        @JsonProperty("post_count")
        @PositiveOrZero(message = "Post count must be zero or positive")
        Integer postCount,

        @JsonProperty("created_at")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
        LocalDateTime createdAt
) { }