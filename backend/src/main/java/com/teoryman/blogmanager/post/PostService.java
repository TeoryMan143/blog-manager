package com.teoryman.blogmanager.post;

import com.teoryman.blogmanager.common.exception.ForbiddenException;
import com.teoryman.blogmanager.common.exception.ResourceNotFoundException;
import com.teoryman.blogmanager.post.dto.PostRequest;
import com.teoryman.blogmanager.post.dto.PostResponse;
import com.teoryman.blogmanager.user.User;
import com.teoryman.blogmanager.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostService {
  @Autowired private PostRepository postRepository;
  @Autowired private PostMapper postMapper;
  @Autowired private UserRepository userRepository;

  public PostResponse addPost(PostRequest post, String userId) {
    User author = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

    Post created = postMapper.toEntity(post);
    created.setAuthor(author);
    postRepository.save(created);
    return new PostResponse(created);
  }

  private Post findPostById(String id) throws ResourceNotFoundException {
    return postRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
  }

  private void validatePostOwnership(Post post, String userId) {
    if (!post.getAuthor().getId().equals(userId)) {
      throw new ForbiddenException("You are not authorized to perform this action on this post");
    }
  }

  public PostResponse updatePost(String id, PostRequest request, String userId) {
    Post existing = findPostById(id);
    validatePostOwnership(existing, userId);

    existing.setTitle(request.getTitle());
    existing.setContent(request.getContent());

    Post saved = postRepository.save(existing);
    return postMapper.toResponse(saved);
  }

  public PostResponse deletePost(String id, String userId) {
    Post existing = findPostById(id);
    validatePostOwnership(existing, userId);
    postRepository.delete(existing);
    return postMapper.toResponse(existing);
  }

  public List<PostResponse> getAllPosts() {
    List<Post> allPosts = postRepository.findAll();
    return postMapper.toListResponse(allPosts);
  }

  public PostResponse getPostById(String id) {
    Post post = findPostById(id);
    return postMapper.toResponse(post);
  }
}
