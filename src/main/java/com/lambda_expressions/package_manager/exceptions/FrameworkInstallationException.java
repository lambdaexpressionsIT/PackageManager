package com.lambda_expressions.package_manager.exceptions;

/**
 * Created by steccothal
 * on Monday 22 March 2021
 * at 1:04 PM
 */
public class FrameworkInstallationException extends PackageException{
  public FrameworkInstallationException(String message, String tag) {
    super(message, tag, "");
  }
}
