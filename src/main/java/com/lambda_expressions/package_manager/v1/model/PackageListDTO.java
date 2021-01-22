package com.lambda_expressions.package_manager.v1.model;

import lombok.Data;
import lombok.Builder;

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
  private List<VersionDTO> versions;
}
