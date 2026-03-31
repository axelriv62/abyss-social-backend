package fr.univartois.butinfo.sae.abyss.social.service;

import fr.univartois.butinfo.sae.abyss.social.model.Page;
import fr.univartois.butinfo.sae.abyss.social.repository.PageRepository;
import fr.univartois.butinfo.sae.abyss.social.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * The PageService class provides the business logic for managing Page entities.
 * It acts as an intermediary between the controller and the repository layers.
 */
@Service
public class PageService {
    private final PageRepository pageRepository;
    private final UserRepository userRepository;

    /**
     * Constructs a PageService with the specified PageRepository.
     *
     * @param pageRepository The repository for performing CRUD operations on Page entities.
     */
    public PageService(PageRepository pageRepository, UserRepository userRepository) {
        this.pageRepository = pageRepository;
        this.userRepository = userRepository;
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
}