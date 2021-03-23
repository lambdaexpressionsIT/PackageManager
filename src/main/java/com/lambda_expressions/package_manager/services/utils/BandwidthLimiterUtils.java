package com.lambda_expressions.package_manager.services.utils;

import com.lambda_expressions.package_manager.bandwidth_limiter.utils.StreamManager;
import com.lambda_expressions.package_manager.v1.model.BandwidthLimiterConfigurationDTO;
import org.springframework.beans.factory.annotation.Value;

/**
 * Created by steccothal
 * on Monday 22 March 2021
 * at 6:23 PM
 */
public class BandwidthLimiterUtils {

  StreamManager streamManager;

  @Value("${bandwidth.max.kbitPerSecond}")
  long bandwidthLimitKbps;
  @Value("${upload.max.kbitPerSecond}")
  long uploadLimitKbps;
  @Value("${download.max.kbitPerSecond}")
  long downloadLimitKbps;

  public BandwidthLimiterUtils(StreamManager streamManager) {
    this.streamManager = streamManager;
  }

  public BandwidthLimiterConfigurationDTO getConfiguration(){
    return BandwidthLimiterConfigurationDTO.builder()
        .isActive(streamManager.isActive())
        .maxThresholdKbps(bandwidthLimitKbps)
        .downstreamKbps(downloadLimitKbps)
        .upstreamKbps(uploadLimitKbps)
        .build();
  }

  public void setConfiguration(BandwidthLimiterConfigurationDTO configurationDTO){
    this.bandwidthLimitKbps = configurationDTO.getMaxThresholdKbps();
    this.downloadLimitKbps = configurationDTO.getDownstreamKbps();
    this.uploadLimitKbps = configurationDTO.getUpstreamKbps();

    this.streamManager.setActive(configurationDTO.isActive());
    this.streamManager.setMaxBitsPerSecondThreshold(this.bandwidthLimitKbps * 1000);
    this.streamManager.setUpstreamKbps(this.uploadLimitKbps);
    this.streamManager.setDownstreamKbps(this.downloadLimitKbps);
  }
}
