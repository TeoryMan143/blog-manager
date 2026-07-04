package com.teoryman.blogmanager.comment;

import com.teoryman.blogmanager.comment.dto.CommentRequest;
import com.teoryman.blogmanager.comment.dto.CommentResponse;
import com.teoryman.blogmanager.common.response.ApiResponse;
import com.teoryman.blogmanager.user.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {
  @Autowired
  private CommentService commentService;

  @PostMapping
  public ResponseEntity<ApiResponse<CommentResponse>> createComment(
          @PathVariable String postId,
          @AuthenticationPrincipal User currentUser,
          @Valid @RequestBody CommentRequest request
  ) {
    CommentResponse created = commentService.createComment(postId, request, currentUser.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(created, null));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<CommentResponse>>> getPostComments(
          @PathVariable String postId
  ) {
    List<CommentResponse> comments = commentService.getCommentsByPost(postId);
    return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(comments, null));
  }

  @GetMapping("/{commentId}")
  public ResponseEntity<ApiResponse<CommentResponse>> getCommentById(
          @PathVariable String postId,
          @PathVariable String commentId
  ) {
    CommentResponse comment = commentService.getCommentById(commentId);
    return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(comment, null));
  }

  @PutMapping("/{commentId}")
  public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
          @PathVariable String postId,
          @PathVariable String commentId,
          @AuthenticationPrincipal User currentUser,
          @Valid @RequestBody CommentRequest request
  ) {
    CommentResponse updated = commentService.updateComment(commentId, request, currentUser.getId());
    return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(updated, null));
  }

  @DeleteMapping("/{commentId}")
  public ResponseEntity<ApiResponse<CommentResponse>> deleteComment(
          @PathVariable String postId,
          @PathVariable String commentId,
          @AuthenticationPrincipal User currentUser
  ) {
    CommentResponse deleted = commentService.deleteComment(commentId, currentUser.getId());
    return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(deleted, null));
  }
}

