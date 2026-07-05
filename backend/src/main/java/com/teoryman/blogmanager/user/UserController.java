package com.teoryman.blogmanager.user;

import com.teoryman.blogmanager.common.exception.ResourceNotFoundException;
import com.teoryman.blogmanager.common.response.ApiResponse;
import com.teoryman.blogmanager.user.dto.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

  @Autowired
  private UserRepository userRepository;

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
          @AuthenticationPrincipal User currentUser
  ) {
    return ResponseEntity.status(HttpStatus.OK)
            .body(new ApiResponse<>(new UserResponse(currentUser), null));
  }

  @GetMapping("/by-username/{username}")
  public ResponseEntity<ApiResponse<UserResponse>> getByUsername(@PathVariable String username) {
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return ResponseEntity.status(HttpStatus.OK)
            .body(new ApiResponse<>(new UserResponse(user), null));
  }
}