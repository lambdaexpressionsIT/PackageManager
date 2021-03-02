package com.lambda_expressions.package_manager.exceptions;

/**
 * Created by steccothal
 * on Monday 18 January 2021
 * at 8:35 PM
 */
public class InvalidPackageException extends PackageException {
  public InvalidPackageException(String message, String appName, String version) {
    super(message, appName, version);
  }
}
