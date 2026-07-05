package com.teoryman.blogmanager.user.dto;

import com.teoryman.blogmanager.auth.roleaccess.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.teoryman.blogmanager.user.User;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
  private String id;
  private String username;
  private String email;
  private Role role;

  public UserResponse(User user) {
    this.id = user.getId();
    this.username = user.getUsername();
    this.email = user.getEmail();
    this.role = user.getRole();
  }
}