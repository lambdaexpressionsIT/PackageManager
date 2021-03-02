package com.lambda_expressions.package_manager.exceptions;

/**
 * Created by steccothal
 * on Monday 18 January 2021
 * at 11:21 AM
 */
public class PackageNotFoundException extends PackageException {
  public PackageNotFoundException(String message, String appName, String version) {
    super(message, appName, version);
  }
}
