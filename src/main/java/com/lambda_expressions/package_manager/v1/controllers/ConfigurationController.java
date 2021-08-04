package com.lambda_expressions.package_manager.v1.controllers;

import com.lambda_expressions.package_manager.exceptions.FrameworkInstallationException;
import com.lambda_expressions.package_manager.exceptions.IOFileException;
import com.lambda_expressions.package_manager.services.ConfigurationService;
import com.lambda_expressions.package_manager.v1.model.BandwidthLimiterConfigurationDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import java.util.List;

/**
 * Created by steccothal
 * on Monday 22 March 2021
 * at 2:23 PM
 */
@RestController
@RequestMapping("api/v1/configuration/")
public class ConfigurationController {
  ConfigurationService configurationService;

  public ConfigurationController(ConfigurationService configurationService) {
    this.configurationService = configurationService;
  }

  @RolesAllowed("viewer")
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = {"uploadFramework", "uploadFramework/", "uploadFramework/{frameworkTag}/", "uploadFramework/{frameworkTag}"},
      consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public void uploadFramework(@RequestBody byte[] file, @PathVariable(required = false) String frameworkTag)
      throws FrameworkInstallationException {

    this.configurationService.installFramework(frameworkTag, file);
  }

  @RolesAllowed("viewer")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = {"installedFrameworks/", "installedFrameworks"})
  public List<String> listInstalledFrameworks()
      throws IOFileException {

    return this.configurationService.listFrameworks();
  }

  @RolesAllowed("viewer")
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping(value = {"bandwidthLimiter", "bandwidthLimiter/"},
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public void configureBandwidthLimiter( @RequestBody BandwidthLimiterConfigurationDTO configurationDTO){

    this.configurationService.updateBandwidthLimiterConfiguration(configurationDTO);
  }

  @RolesAllowed("viewer")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = {"bandwidthLimiter", "bandwidthLimiter/"})
  public BandwidthLimiterConfigurationDTO getBandwidthLimiterConfiguration() {

    return this.configurationService.getBandwidthLimiterConfiguration();
  }
}
