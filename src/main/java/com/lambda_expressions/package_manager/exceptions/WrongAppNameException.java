package com.lambda_expressions.package_manager.exceptions;

/**
 * Created by steccothal
 * on Friday 09 April 2021
 * at 1:53 PM
 */
public class WrongAppNameException extends PackageException{
  public WrongAppNameException(String message, String appName, String version) {
    super(message, appName, version);
  }
}
