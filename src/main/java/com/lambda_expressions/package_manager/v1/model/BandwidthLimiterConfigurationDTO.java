package com.lambda_expressions.package_manager.v1.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by steccothal
 * on Monday 22 March 2021
 * at 5:51 PM
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BandwidthLimiterConfigurationDTO {
  private long maxThresholdKbps;
  private long downstreamKbps;
  private long upstreamKbps;
  private boolean isActive;
}
