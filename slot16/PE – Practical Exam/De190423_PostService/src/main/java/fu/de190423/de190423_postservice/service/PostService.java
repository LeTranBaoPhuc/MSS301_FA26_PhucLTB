package fu.de190423.de190423_postservice.service;

import fu.de190423.de190423_postservice.dto.ApiResponse;
import fu.de190423.de190423_postservice.dto.PostDTO;
import org.springframework.data.domain.Pageable;

public interface PostService {
    ApiResponse createPost(PostDTO dto);
    ApiResponse updatePost(Long postId, PostDTO dto);
    ApiResponse getPostById(Long postId);
    ApiResponse cancelPost(Long postId);
    ApiResponse getPostList(String advertiserName, String status, Pageable pageable);
}
