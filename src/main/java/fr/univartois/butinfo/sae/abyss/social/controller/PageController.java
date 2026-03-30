package fr.univartois.butinfo.sae.abyss.social.controller;

import fr.univartois.butinfo.sae.abyss.social.dto.PageDTO;
import fr.univartois.butinfo.sae.abyss.social.mapper.PageMapper;
import fr.univartois.butinfo.sae.abyss.social.model.Page;
import fr.univartois.butinfo.sae.abyss.social.service.PageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The PageController class handles HTTP requests related to the Page entity.
 * It provides endpoints for creating and managing pages.
 */
@RestController
@RequestMapping("/pages")
public class PageController {
    private final PageService pageService;
    private final PageMapper pageMapper;

    /**
     * Constructs a PageController with the specified PageService and PageMapper.
     *
     * @param pageService The service layer for managing Page entities.
     * @param pageMapper  The mapper for converting between Page and PageDTO.
     */
    public PageController(PageService pageService, PageMapper pageMapper) {
        this.pageService = pageService;
        this.pageMapper = pageMapper;
    }

    /**
     * Handles the creation of a new Page.
     * Accepts a PageDTO as input, converts it to a Page entity, saves it, and returns the saved PageDTO.
     *
     * @param pageDTO The PageDTO containing the details of the page to be created.
     * @return A ResponseEntity containing the created PageDTO and HTTP status 201 (Created).
     */
    @PostMapping
    public ResponseEntity<PageDTO> createPage(@Valid @RequestBody PageDTO pageDTO) {
        Page page = pageMapper.toEntity(pageDTO);
        Page savedPage = pageService.save(page);
        return ResponseEntity.status(HttpStatus.CREATED).body(pageMapper.toDTO(savedPage));
    }
}