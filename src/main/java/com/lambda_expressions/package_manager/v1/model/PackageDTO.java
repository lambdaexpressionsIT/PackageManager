package com.lambda_expressions.package_manager.v1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by steccothal
 * on Monday 18 January 2021
 * at 10:35 AM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageDTO extends VersionDTO {
  private String appName;
  private String packageName;

  public PackageDTO(long id, String appVersion, Long appVersionNumber, String fileName, String url, boolean valid, String appName, String packageName) {
    super(id, appVersion, appVersionNumber, fileName, url, valid);
    this.appName = appName;
    this.packageName = packageName;
  }
}
