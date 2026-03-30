package fr.univartois.butinfo.sae.abyss.social.service;

import fr.univartois.butinfo.sae.abyss.social.model.Post;
import fr.univartois.butinfo.sae.abyss.social.repository.PostRepository;
import org.springframework.stereotype.Service;

@Service
public class PostService {
    
    private PostRepository postRepository;
    
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }
    
    public Post create(Post post) {return postRepository.save(post);}
}
