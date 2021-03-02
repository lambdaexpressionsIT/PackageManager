package com.lambda_expressions.package_manager.exceptions;

/**
 * Created by steccothal
 * on Monday 18 January 2021
 * at 12:32 PM
 */
public class IOFileException extends PackageException {
  public IOFileException(String message, String appName, String version) {
    super(message, appName, version);
  }
}
