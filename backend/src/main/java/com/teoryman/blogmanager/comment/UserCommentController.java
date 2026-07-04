package com.teoryman.blogmanager.comment;

import com.teoryman.blogmanager.comment.dto.CommentResponse;
import com.teoryman.blogmanager.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/comments")
public class UserCommentController {
  @Autowired
  private CommentService commentService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<CommentResponse>>> getUserComments(
          @PathVariable String userId
  ) {
    List<CommentResponse> comments = commentService.getCommentsByAuthor(userId);
    return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(comments, null));
  }
}
