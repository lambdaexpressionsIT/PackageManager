package com.lambda_expressions.package_manager.exceptions;

/**
 * Created by steccothal
 * on Monday 18 January 2021
 * at 12:01 PM
 */
public class MalformedURLException extends PackageException {
  public MalformedURLException(String message, String appName, String version) {
    super(message, appName, version);
  }
}
