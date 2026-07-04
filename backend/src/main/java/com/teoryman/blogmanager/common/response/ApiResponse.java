package com.teoryman.blogmanager.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
  private T data;
  private String error;
  private LocalDateTime timestamp = LocalDateTime.now();

  public ApiResponse(T data, String error) {
    this.data = data;
    this.error = error;
  }
}
