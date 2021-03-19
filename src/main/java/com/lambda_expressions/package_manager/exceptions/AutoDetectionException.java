package com.lambda_expressions.package_manager.exceptions;

/**
 * Created by steccothal
 * on Wednesday 17 March 2021
 * at 12:08 PM
 */
public class AutoDetectionException extends PackageException{
  public AutoDetectionException(String message, String appName, String version) {
    super(message, appName, version);
  }
}
