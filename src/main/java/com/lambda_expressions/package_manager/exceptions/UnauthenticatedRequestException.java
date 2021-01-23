package com.lambda_expressions.package_manager.exceptions;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by steccothal
 * on Monday 18 January 2021
 * at 11:47 AM
 */
@Slf4j
public class UnauthenticatedRequestException extends Exception {
  public UnauthenticatedRequestException(String userToken, String message) {
    super(message);
    log.error(String.format("%s. UserToken: %s", message, userToken));
  }
}
