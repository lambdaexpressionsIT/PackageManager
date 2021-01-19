package com.lambda_expressions.package_manager.services;

import com.lambda_expressions.package_manager.exceptions.UnauthenticatedRequestException;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by steccothal
 * on Monday 18 January 2021
 * at 9:33 AM
 */
public interface AuthenticationService {

  void authenticateRequest(HttpServletRequest httpReq) throws UnauthenticatedRequestException;

}
