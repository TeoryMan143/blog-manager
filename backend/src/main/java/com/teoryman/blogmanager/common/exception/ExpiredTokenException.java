package com.teoryman.blogmanager.common.exception;

public class ExpiredTokenException extends ApiException {
  public ExpiredTokenException(String message) {
    super(message);
  }
}
