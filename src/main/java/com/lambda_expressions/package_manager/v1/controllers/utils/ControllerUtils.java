package com.lambda_expressions.package_manager.v1.controllers.utils;

import com.lambda_expressions.package_manager.exceptions.MalformedURLException;

/**
 * Created by steccothal
 * on Tuesday 19 January 2021
 * at 9:46 AM
 */
public class ControllerUtils {
  public static int checkVersionParameter(String appName, String version) throws MalformedURLException {
    int intVersion;

    try {
      intVersion = Integer.parseInt(version);
    } catch (NumberFormatException formatException) {
      throw new MalformedURLException("Version is not a number", appName, version);
    }

    return intVersion;
  }
}
