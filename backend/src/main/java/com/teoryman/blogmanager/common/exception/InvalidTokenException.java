package com.teoryman.blogmanager.common.exception;

public class InvalidTokenException extends ApiException {
  public InvalidTokenException(String message) {
    super(message);
  }
}
