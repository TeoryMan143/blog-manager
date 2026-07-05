package com.teoryman.blogmanager.post.dto;

import com.teoryman.blogmanager.post.Post;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {
  private String id;
  private String title;
  private String content;
  private String userId;
  private String authorUsername;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public PostResponse(Post post) {
    this.id = post.getId();
    this.title = post.getTitle();
    this.content = post.getContent();
    this.userId = post.getAuthor().getId();
    this.authorUsername = post.getAuthor().getUsername();
    this.createdAt = post.getCreatedAt();
    this.updatedAt = post.getUpdatedAt();
  }
}
