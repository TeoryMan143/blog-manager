package com.teoryman.blogmanager.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshRequest {
  @NotBlank(message = "refreshToken is required")
  private String refreshToken;
}
