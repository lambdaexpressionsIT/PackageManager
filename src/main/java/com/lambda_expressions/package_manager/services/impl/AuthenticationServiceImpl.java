package com.lambda_expressions.package_manager.services.impl;

import com.lambda_expressions.package_manager.exceptions.UnauthenticatedRequestException;
import com.lambda_expressions.package_manager.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by steccothal
 * on Monday 18 January 2021
 * at 9:35 AM
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

  @Value("${authentication.token.http.header}")
  String AUTH_TOKEN_HTTP_HEADER;

  @Value("${authentication.server.url}")
  String AUTH_SERVER_URL;

  @Override
  public void authenticateRequest(HttpServletRequest httpReq) throws UnauthenticatedRequestException {
    String token = this.getUserTokenFromRequest(httpReq);

    if(!this.authenticateUser(token)){
      throw new UnauthenticatedRequestException(httpReq, token, "User cannot be authenticated");
    }
  }

  private String getUserTokenFromRequest(HttpServletRequest httpReq){
    return httpReq.getHeader(AUTH_TOKEN_HTTP_HEADER);
  }

  private boolean authenticateUser(String token){
    return true;
  }
}
