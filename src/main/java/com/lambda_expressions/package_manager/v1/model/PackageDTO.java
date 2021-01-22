package com.lambda_expressions.package_manager.v1.model;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Created by steccothal
 * on Monday 18 January 2021
 * at 10:35 AM
 */
@Getter
@SuperBuilder
public class PackageDTO extends VersionDTO{
  private String appName;
}
