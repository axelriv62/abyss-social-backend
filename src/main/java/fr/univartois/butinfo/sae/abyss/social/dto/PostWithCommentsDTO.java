package fr.univartois.butinfo.sae.abyss.social.dto;

import java.util.List;

/**
 * Aggregated payload containing a post and its comments.
 *
 * @param post post details
 * @param comments comments attached to the post
 */
public record PostWithCommentsDTO(
        PostDTO post,
        List<CommentDTO> comments
) {
}

