package com.lambda_expressions.package_manager.exceptions;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by steccothal
 * on Monday 18 January 2021
 * at 12:33 PM
 */
@Slf4j
public class PackageException extends Exception {

  public PackageException(String message, String appName, String version) {
    super(message);
    log.error(String.format("%s: %s %s", message, appName, version));
  }
}
