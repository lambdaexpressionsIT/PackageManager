package com.lambda_expressions.package_manager.v1.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Created by steccothal
 * on Friday 22 January 2021
 * at 12:22 PM
 */
@Getter
@SuperBuilder
public class VersionDTO {
  private final long id;
  private final int appVersion;
  private final String fileName;
  private final String url;
  private final boolean valid;
}
