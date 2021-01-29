package com.lambda_expressions.package_manager.v1.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Created by steccothal
 * on Friday 22 January 2021
 * at 12:21 PM
 */
@Data
@Builder
public class PackageListDTO {
  private String appName;
  private String packageName;
  private List<VersionDTO> versions;
}
