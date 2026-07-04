package com.teoryman.blogmanager.auth.dto;

import com.teoryman.blogmanager.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterResponse {
  private UserResponse user;
  private String accessToken;
  private String refreshToken;
}
