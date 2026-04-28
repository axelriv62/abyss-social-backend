package fr.univartois.butinfo.sae.abyss.social.mapper;

import fr.univartois.butinfo.sae.abyss.social.dto.CommentDTO;
import fr.univartois.butinfo.sae.abyss.social.dto.PostDTO;
import fr.univartois.butinfo.sae.abyss.social.dto.UserResponseDTO;
import fr.univartois.butinfo.sae.abyss.social.model.Post;
import fr.univartois.butinfo.sae.abyss.social.model.User;
import fr.univartois.butinfo.sae.abyss.social.repository.CommentRepository;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring", uses = {CommentMapper.class, UserMapper.class})
public abstract class PostMapper {

    protected CommentRepository commentRepository;

    /**
     * Constructor to inject CommentRepository
     */
    public PostMapper() {
    }

    /**
     * Setter for CommentRepository - will be injected by Spring
     */
    public void setCommentRepository(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    /**
     * Converts a Post entity to a PostDTO.
     * Handles complex mappings for likes/dislikes/comments and user reference.
     *
     * @param post the Post entity to convert
     * @return the corresponding PostDTO
     */
    @Mapping(target = "id", expression = "java(ObjectIdConverter.objectIdToString(post.getId()))")
    @Mapping(target = "userId", expression = "java(ObjectIdConverter.objectIdToString(post.getUser().getId()))")
    @Mapping(target = "image", expression = "java(toPostImageDataUrl(post))")
    @Mapping(target = "likes", ignore = true)
    @Mapping(target = "dislikes", ignore = true)
    @Mapping(target = "comments", ignore = true)
    public abstract PostDTO toDTOPartial(Post post);

    /**
     * Maps a list of Post entities to PostDTOs.
     */
    public List<PostDTO> toDTOs(List<Post> posts) {
        if (posts == null) {
            return new ArrayList<>();
        }
        return posts.stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Converts a Post entity to a PostDTO with full mapping of likes, dislikes and comments as complete objects.
     */
    public PostDTO toDTO(Post post) {
        if (post == null) {
            return null;
        }

        PostDTO partial = toDTOPartial(post);

        return new PostDTO(
                partial.id(),
                partial.userId(),
                partial.content(),
                partial.image(),
                mapCommentsToDTO(post.getComments()),
                mapUsersToResponseDTO(post.getLikes()),
                mapUsersToResponseDTO(post.getDislikes()),
                partial.createdAt()
        );
    }

    /**
     * Converts a PostDTO to a Post entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "image", ignore = true)
    @Mapping(target = "likes", ignore = true)
    @Mapping(target = "dislikes", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "user", ignore = true)
    public abstract Post toEntityPartial(@Valid PostDTO postDTO);

    /**
     * Maps a PostDTO to a Post entity with full mapping of likes and dislikes.
     */
    public Post toEntity(@Valid PostDTO postDTO) {
        if (postDTO == null) {
            return null;
        }

        Post partial = toEntityPartial(postDTO);
        partial.setLikes(mapResponseDTOToUsers(postDTO.likes()));
        partial.setDislikes(mapResponseDTOToUsers(postDTO.dislikes()));
        partial.setComments(mapCommentDTOToObjectIds(postDTO.comments()).toArray(new ObjectId[0]));

        return partial;
    }

    /**
     * Converts a Post entity's image to a data URL.
     * Returns null if no image is present.
     *
     * @param post the Post entity
     * @return the image as a data URL (e.g., "data:image/png;base64,...") or null
     */
    public String toPostImageDataUrl(Post post) {
        if (post == null || post.getImage() == null || post.getImage().getData() == null) {
            return null;
        }

        String mimeType = post.getImageContentType();
        if (mimeType == null || mimeType.isBlank()) {
            mimeType = inferMimeType(post.getImage().getData());
        }

        String base64 = Base64.getEncoder().encodeToString(post.getImage().getData());
        return "data:" + mimeType + ";base64," + base64;
    }

    /**
     * Infers the MIME type from binary data.
     *
     * @param bytes the binary data
     * @return the inferred MIME type
     */
    public String inferMimeType(byte[] bytes) {
        if (bytes.length >= 8
                && (bytes[0] & 0xFF) == 0x89
                && bytes[1] == 0x50
                && bytes[2] == 0x4E
                && bytes[3] == 0x47) {
            return "image/png";
        }
        if (bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (bytes.length >= 6
                && bytes[0] == 'G'
                && bytes[1] == 'I'
                && bytes[2] == 'F') {
            return "image/gif";
        }
        if (bytes.length >= 12
                && bytes[0] == 'R'
                && bytes[1] == 'I'
                && bytes[2] == 'F'
                && bytes[3] == 'F'
                && bytes[8] == 'W'
                && bytes[9] == 'E'
                && bytes[10] == 'B'
                && bytes[11] == 'P') {
            return "image/webp";
        }
        return "application/octet-stream";
    }

    /**
     * Converts Comment ObjectIds to full CommentDTO objects by fetching from database.
     * This method loads the actual Comment entities from the repository.
     *
     * @param commentIds the comment ObjectIds from Post
     * @return list of CommentDTO objects (empty list if null or repository unavailable)
     */
    public List<CommentDTO> mapCommentsToDTO(ObjectId[] commentIds) {
        if (commentIds == null || commentIds.length == 0 || commentRepository == null) {
            return new ArrayList<>();
        }

        List<CommentDTO> commentDTOs = new ArrayList<>();
        CommentMapper commentMapper = new CommentMapperImpl();

        for (ObjectId commentId : commentIds) {
            commentRepository.findById(commentId).ifPresent(comment -> {
                CommentDTO dto = commentMapper.toDTO(comment);
                commentDTOs.add(dto);
            });
        }

        return commentDTOs;
    }

    /**
     * Converts a list of User entities to UserResponseDTOs.
     *
     * @param users the users to convert
     * @return list of UserResponseDTO objects
     */
    public List<UserResponseDTO> mapUsersToResponseDTO(List<User> users) {
        if (users == null || users.isEmpty()) {
            return new ArrayList<>();
        }
        return users.stream()
                .map(this::userToResponseDTO)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Converts a UserResponseDTO to User entity.
     *
     * @param userDTO the DTO to convert
     * @return User entity with only ID set
     */
    public User userResponseDTOToUser(UserResponseDTO userDTO) {
        if (userDTO == null) {
            return null;
        }
        User user = new User();
        user.setId(ObjectIdConverter.stringToObjectId(userDTO.id()));
        return user;
    }

    /**
     * Converts a User entity to UserResponseDTO.
     *
     * @param user the user to convert
     * @return UserResponseDTO
     */
    public UserResponseDTO userToResponseDTO(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponseDTO(
                ObjectIdConverter.objectIdToString(user.getId()),
                user.getUsernameField(),
                user.getEmail(),
                null, // Profile picture - set to null to avoid large payloads
                user.getRole()
        );
    }

    /**
     * Converts a list of UserResponseDTOs back to User entities.
     *
     * @param dtos the DTOs to convert
     * @return list of User entities
     */
    public List<User> mapResponseDTOToUsers(List<UserResponseDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return new ArrayList<>();
        }
        return dtos.stream()
                .map(this::userResponseDTOToUser)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Converts a list of CommentDTOs to ObjectIds list.
     *
     * @param comments the comment DTOs to convert
     * @return list of ObjectIds
     */
    public List<ObjectId> mapCommentDTOToObjectIds(List<CommentDTO> comments) {
        if (comments == null || comments.isEmpty()) {
            return new ArrayList<>();
        }
        return comments.stream()
                .map(dto -> ObjectIdConverter.stringToObjectId(dto.id()))
                .filter(Objects::nonNull)
                .toList();
    }
}