package com.lambda_expressions.package_manager.exceptions;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by steccothal
 * on Monday 18 January 2021
 * at 11:47 AM
 */
public class UnauthenticatedRequestException extends Exception{

  private HttpServletRequest httpRequest;
  private String userToken;

  public UnauthenticatedRequestException(HttpServletRequest httpRequest, String userToken, String message) {
    super(message);
    this.httpRequest = httpRequest;
    this.userToken = userToken;
  }
}
