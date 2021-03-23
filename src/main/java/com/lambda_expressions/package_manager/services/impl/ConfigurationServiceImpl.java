package com.lambda_expressions.package_manager.services.impl;

import com.lambda_expressions.package_manager.exceptions.FrameworkInstallationException;
import com.lambda_expressions.package_manager.exceptions.IOFileException;
import com.lambda_expressions.package_manager.services.ConfigurationService;
import com.lambda_expressions.package_manager.services.utils.APKUtils;
import com.lambda_expressions.package_manager.services.utils.BandwidthLimiterUtils;
import com.lambda_expressions.package_manager.v1.model.BandwidthLimiterConfigurationDTO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by steccothal
 * on Monday 22 March 2021
 * at 2:44 PM
 */
@Service
public class ConfigurationServiceImpl implements ConfigurationService {

  BandwidthLimiterUtils bandwidthLimiterUtils;
  APKUtils apkUtils;

  public ConfigurationServiceImpl(APKUtils apkUtils, BandwidthLimiterUtils bandwidthLimiterUtils) {
    this.apkUtils = apkUtils;
    this.bandwidthLimiterUtils = bandwidthLimiterUtils;
  }

  @Override
  public void installFramework(String frameworkTag, byte[] frameworkFile) throws FrameworkInstallationException {
    this.apkUtils.installFramework(frameworkFile, frameworkTag);
  }

  @Override
  public List<String> listFrameworks() throws IOFileException {
    return this.apkUtils.listInstalledFrameworks();
  }

  @Override
  public void updateBandwidthLimiterConfiguration(BandwidthLimiterConfigurationDTO configurationDTO) {
    this.bandwidthLimiterUtils.setConfiguration(configurationDTO);
  }

  @Override
  public BandwidthLimiterConfigurationDTO getBandwidthLimiterConfiguration() {
    return this.bandwidthLimiterUtils.getConfiguration();
  }
}
