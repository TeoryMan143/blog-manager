package com.teoryman.blogmanager.user;

import com.teoryman.blogmanager.user.dto.UserRequest;
import com.teoryman.blogmanager.user.dto.UserResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {
  public UserResponse toResponse(User user) {
    return new UserResponse(user);
  }

  public User toEntity(UserRequest userRequest) {
    User user = new User();
    user.setUsername(userRequest.getUsername());
    user.setEmail(userRequest.getEmail());
    user.setPassword(userRequest.getPassword());
    return user;
  }

  public List<UserResponse> toListResponse(List<User> users) {
    return users.stream().map(this::toResponse).toList();
  }
}
