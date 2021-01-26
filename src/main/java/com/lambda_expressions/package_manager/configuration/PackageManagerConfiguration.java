package com.lambda_expressions.package_manager.configuration;

import com.lambda_expressions.package_manager.bandwidth_limiter.utils.StreamManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by steccothal
 * on Tuesday 26 January 2021
 * at 3:42 PM
 */
@Configuration
public class PackageManagerConfiguration {

  @Value("${bandwidth.limitation.enabled}")
  boolean enableBandwidthLimitation;
  @Value("${bandwidth.max.kbitPerSecond}")
  long bandwidthLimitKbps;
  @Value("${upload.max.kbitPerSecond}")
  long uploadLimitKbps;
  @Value("${download.max.kbitPerSecond}")
  long downloadLimitKbps;

  @Bean
  public StreamManager getStreamManager() {
    StreamManager streamManager = new StreamManager(bandwidthLimitKbps * 1000);

    streamManager.setDownstreamKbps(downloadLimitKbps);
    streamManager.setUpstreamKbps(uploadLimitKbps);
    streamManager.setActive(enableBandwidthLimitation);

    return streamManager;
  }

}
