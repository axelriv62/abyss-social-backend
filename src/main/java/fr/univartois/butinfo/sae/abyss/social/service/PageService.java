package fr.univartois.butinfo.sae.abyss.social.service;

import fr.univartois.butinfo.sae.abyss.social.dto.PostDTO;
import fr.univartois.butinfo.sae.abyss.social.mapper.PageMapper;
import fr.univartois.butinfo.sae.abyss.social.mapper.PostMapper;
import fr.univartois.butinfo.sae.abyss.social.model.Page;
import fr.univartois.butinfo.sae.abyss.social.repository.PageRepository;
import fr.univartois.butinfo.sae.abyss.social.repository.PostRepository;
import fr.univartois.butinfo.sae.abyss.social.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The PageService class provides the business logic for managing Page entities.
 * It acts as an intermediary between the controller and the repository layers.
 */
@Service
public class PageService {
    private final PageRepository pageRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostMapper postMapper;

    /**
     * Constructs a PageService with the specified PageRepository.
     *
     * @param pageRepository The repository for performing CRUD operations on Page entities.
     */
    public PageService(PageRepository pageRepository, UserRepository userRepository, PageMapper pageMapper, PostRepository postRepository, PostMapper postMapper) {
        this.pageRepository = pageRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.postMapper = postMapper;
    }

    public Optional<Page> findById(ObjectId id) {
        return pageRepository.findById(id);
    }


    /**
     * Saves a Page entity to the database.
     *
     * @param page The Page entity to save.
     * @return The saved Page entity.
     */
    public Page save(Page page) {
        ObjectId userId = page.getUser() != null ? page.getUser().getId() : null;
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for userId=" + userId.toHexString());
        }
        return pageRepository.save(page);
    }

    /**
     * Updates an existing Page entity with the specified ID using the provided Page data.
     * @param id The ObjectId of the Page to be updated.
     * @param body The Page object containing the updated data for the Page entity.
     * @return The updated Page entity after saving it to the database.
     */
    public Page updatePage(ObjectId id, Page body) {
        return pageRepository.findById(id).map(page -> {
            if (body.getName() != null) {
                page.setName(body.getName());
            }
            if (body.getTags() != null) {
                page.setTags(body.getTags());
            }
            return pageRepository.save(page);
        }).orElseThrow(() -> new IllegalArgumentException("Page not found: " + id));
    }

    /**
     * Deletes a Page entity by its ID.
     * @param id The ObjectId of the Page to be deleted.
     * @return true if the Page was successfully deleted, false if the Page does not exist.
     */
    public boolean deleteById(ObjectId id) {
        if (pageRepository.existsById(id)) {
            pageRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Searches for Page entities whose names contain the specified fragment, ignoring case.
     * @param nameFragment The fragment of the name to search for in Page entities.
     * @return A list of Page entities whose names contain the specified fragment, ignoring case.
     */
    public List<Page> searchByName(String nameFragment) {
        String trimmed = nameFragment.trim();
        if (trimmed.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Query cannot be blank");
        }
        return pageRepository.findByNameContainingIgnoreCase(trimmed);
    }

    /**
     * Retrieves Posts associated with a page.
     * @param pageId The ID of the page whose posts are to be retrieved.
     *
     * @return A list of Post objects associated with the page.
     */
    public List<PostDTO> getPagesPosts(ObjectId pageId) {
        Page page = pageRepository.findById(pageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found"));

        if (page.getPosts() == null) {
            return List.of();
        }

        return Arrays.stream(page.getPosts())
                .map(postId -> postRepository.findById(postId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found for id=" + postId)))
                .map(postMapper::toDTO)
                .toList();
    }
}