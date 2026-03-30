package fr.univartois.butinfo.sae.abyss.social.service;

import fr.univartois.butinfo.sae.abyss.social.model.Page;
import fr.univartois.butinfo.sae.abyss.social.repository.PageRepository;
import org.springframework.stereotype.Service;

/**
 * The PageService class provides the business logic for managing Page entities.
 * It acts as an intermediary between the controller and the repository layers.
 */
@Service
public class PageService {
    private final PageRepository pageRepository;

    /**
     * Constructs a PageService with the specified PageRepository.
     *
     * @param pageRepository The repository for performing CRUD operations on Page entities.
     */
    public PageService(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    /**
     * Saves a Page entity to the database.
     *
     * @param page The Page entity to save.
     * @return The saved Page entity.
     */
    public Page save(Page page) {
        return pageRepository.save(page);
    }
}