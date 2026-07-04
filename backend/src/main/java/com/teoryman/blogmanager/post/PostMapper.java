package com.teoryman.blogmanager.post;

import com.teoryman.blogmanager.post.dto.PostRequest;
import com.teoryman.blogmanager.post.dto.PostResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PostMapper {
  public PostResponse toResponse(Post post) {
    return new PostResponse(post);
  }

  public Post toEntity(PostRequest post) {
    Post postEntity = new Post();
    postEntity.setTitle(post.getTitle());
    postEntity.setContent(post.getContent());
    return postEntity;
  }

  public List<PostResponse> toListResponse(List<Post> posts) {
    return posts.stream().map(this::toResponse).toList();
  }
}
