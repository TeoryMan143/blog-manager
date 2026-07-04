package com.teoryman.blogmanager.comment.dto;

import com.teoryman.blogmanager.comment.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {
  private String id;
  private String content;
  private String postId;
  private String authorId;
  private String authorUsername;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public CommentResponse(Comment comment) {
    this.id = comment.getId();
    this.content = comment.getContent();
    this.postId = comment.getPost().getId();
    this.authorId = comment.getAuthor().getId();
    this.authorUsername = comment.getAuthor().getUsername();
    this.createdAt = comment.getCreatedAt();
    this.updatedAt = comment.getUpdatedAt();
  }
}
