package com.teoryman.blogmanager.comment;

import com.teoryman.blogmanager.comment.dto.CommentResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommentMapper {
  public CommentResponse toResponse(Comment comment) {
    return new CommentResponse(comment);
  }

  public List<CommentResponse> toListResponse(List<Comment> comments) {
    return comments.stream()
            .map(this::toResponse)
            .toList();
  }
}
