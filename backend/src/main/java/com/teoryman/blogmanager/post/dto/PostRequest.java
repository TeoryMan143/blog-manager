package com.teoryman.blogmanager.post.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostRequest {
  @NotBlank(message = "Title is required")
  private String title;

  @NotBlank(message = "Content is required")
  private String content;
}
