package com.lambda_expressions.package_manager.v1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by steccothal
 * on Friday 22 January 2021
 * at 12:22 PM
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VersionDTO {
  private long id;
  private String appVersion;
  private Long appVersionNumber;
  private String fileName;
  private String url;
  private boolean valid;
}
