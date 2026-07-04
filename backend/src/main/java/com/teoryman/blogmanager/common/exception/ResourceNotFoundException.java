package com.teoryman.blogmanager.common.exception;

public class ResourceNotFoundException extends ApiException {
  public ResourceNotFoundException(String message) {
    super(message);
  }
}
