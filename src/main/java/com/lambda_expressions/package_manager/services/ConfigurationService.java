package com.lambda_expressions.package_manager.services;

import com.lambda_expressions.package_manager.exceptions.FrameworkInstallationException;
import com.lambda_expressions.package_manager.exceptions.IOFileException;
import com.lambda_expressions.package_manager.v1.model.BandwidthLimiterConfigurationDTO;

import java.util.List;

/**
 * Created by steccothal
 * on Monday 22 March 2021
 * at 2:42 PM
 */
public interface ConfigurationService {

  void installFramework(String frameworkTag, byte[] frameworkFile) throws FrameworkInstallationException;

  List<String> listFrameworks() throws IOFileException;

  void updateBandwidthLimiterConfiguration(BandwidthLimiterConfigurationDTO configurationDTO);
  BandwidthLimiterConfigurationDTO getBandwidthLimiterConfiguration();


}
