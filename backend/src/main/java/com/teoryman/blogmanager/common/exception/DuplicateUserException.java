package com.teoryman.blogmanager.common.exception;

public class DuplicateUserException extends ApiException {
  public DuplicateUserException(String message) {
    super(message);
  }
}
