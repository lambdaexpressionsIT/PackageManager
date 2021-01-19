package com.lambda_expressions.package_manager.v1.model;

import lombok.Builder;
import lombok.Data;

/**
 * Created by steccothal
 * on Monday 18 January 2021
 * at 10:35 AM
 */
@Data
@Builder
public class PackageDTO {
  private String appName;
  private int appVersion;
  private String url;
  private boolean valid;
}
