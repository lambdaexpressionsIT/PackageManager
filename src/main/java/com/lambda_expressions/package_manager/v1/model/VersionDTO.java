package com.lambda_expressions.package_manager.v1.model;

import lombok.Builder;
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
  private int appVersion;
  private String fileName;
  private String url;
  private boolean valid;
}
