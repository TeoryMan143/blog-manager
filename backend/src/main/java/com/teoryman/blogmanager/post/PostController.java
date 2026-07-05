package com.teoryman.blogmanager.post;

import com.teoryman.blogmanager.auth.roleaccess.PostAuthorizationService;
import com.teoryman.blogmanager.auth.roleaccess.dto.GrantAccessRequest;
import com.teoryman.blogmanager.common.response.ApiResponse;
import com.teoryman.blogmanager.post.dto.PostRequest;
import com.teoryman.blogmanager.post.dto.PostResponse;
import com.teoryman.blogmanager.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {
  @Autowired private PostService postService;
  @Autowired private PostAuthorizationService postAuthorizationService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<PostResponse>>> getAll() {
    List<PostResponse> posts = postService.getAllPosts();
    return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(posts, null));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<PostResponse>> upload(
          @AuthenticationPrincipal User currentUser,
          @Valid @RequestBody PostRequest postRequest
  ) {
    PostResponse created = postService.addPost(postRequest, currentUser.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(created, null));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<PostResponse>> getById(@PathVariable String id) {
    PostResponse found = postService.getPostById(id);
    return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(found, null));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<PostResponse>> deleteById(
          @PathVariable String id,
          @AuthenticationPrincipal User currentUser
  ) {
    PostResponse removed = postService.deletePost(id, currentUser.getId());
    return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(removed, null));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<PostResponse>> editPost(
          @PathVariable String id,
          @AuthenticationPrincipal User currentUser,
          @Valid @RequestBody PostRequest postRequest
  ) {
    PostResponse updated = postService.updatePost(id, postRequest, currentUser.getId());
    return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(updated, null));
  }

  @PostMapping("/{postId}/access")
  public ResponseEntity<ApiResponse<String>> grantAccess(
          @PathVariable String postId,
          @AuthenticationPrincipal User currentUser,
          @Valid @RequestBody GrantAccessRequest grantAccessRequest
  ) {
    postService.grantAccess(postId, currentUser.getId(), grantAccessRequest);
    return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>("Access granted", null));
  }

  @DeleteMapping("/{postId}/access")
  public ResponseEntity<ApiResponse<String>> revokeAccess(
          @PathVariable String postId,
          @AuthenticationPrincipal User currentUser,
          @Valid @RequestBody GrantAccessRequest grantAccessRequest
  ) {
    postService.revokeAccess(postId, currentUser.getId(), grantAccessRequest);
    return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>("Access revoked", null));
  }
}
