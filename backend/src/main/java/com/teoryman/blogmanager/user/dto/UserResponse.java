package com.teoryman.blogmanager.user.dto;

import com.teoryman.blogmanager.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
  private String id;
  private String username;
  private String email;

  public UserResponse(User user) {
    this.id = user.getId();
    this.username = user.getUsername();
    this.email = user.getEmail();
  }
}
