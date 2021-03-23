package com.lambda_expressions.package_manager.v1.controllers;

import com.lambda_expressions.package_manager.exceptions.FrameworkInstallationException;
import com.lambda_expressions.package_manager.exceptions.IOFileException;
import com.lambda_expressions.package_manager.exceptions.UnauthenticatedRequestException;
import com.lambda_expressions.package_manager.services.AuthenticationService;
import com.lambda_expressions.package_manager.services.ConfigurationService;
import com.lambda_expressions.package_manager.v1.model.BandwidthLimiterConfigurationDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by steccothal
 * on Monday 22 March 2021
 * at 2:23 PM
 */
@RestController
@RequestMapping("api/v1/configuration/")
public class ConfigurationController {
  AuthenticationService authService;
  ConfigurationService configurationService;

  public ConfigurationController(AuthenticationService authService, ConfigurationService configurationService) {
    this.authService = authService;
    this.configurationService = configurationService;
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = {"uploadFramework", "uploadFramework/", "uploadFramework/{frameworkTag}/", "uploadFramework/{frameworkTag}"},
      consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public void uploadFramework(HttpServletRequest httpRequest, @RequestBody byte[] file, @PathVariable(required = false) String frameworkTag)
      throws UnauthenticatedRequestException, FrameworkInstallationException {
    this.authService.authenticateRequest(httpRequest);

    this.configurationService.installFramework(frameworkTag, file);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = {"installedFrameworks/", "installedFrameworks"})
  public List<String> listInstalledFrameworks(HttpServletRequest httpRequest)
      throws UnauthenticatedRequestException, IOFileException {
    this.authService.authenticateRequest(httpRequest);

    return this.configurationService.listFrameworks();
  }

  @ResponseStatus(HttpStatus.OK)
  @PatchMapping(value = {"bandwidthLimiter", "bandwidthLimiter/"},
      consumes = MediaType.APPLICATION_JSON_VALUE)
  public void configureBandwidthLimiter(HttpServletRequest httpRequest, @RequestBody BandwidthLimiterConfigurationDTO configurationDTO)
      throws UnauthenticatedRequestException {
    this.authService.authenticateRequest(httpRequest);

    this.configurationService.updateBandwidthLimiterConfiguration(configurationDTO);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = {"bandwidthLimiter", "bandwidthLimiter/"})
  public BandwidthLimiterConfigurationDTO getBandwidthLimiterConfiguration(HttpServletRequest httpRequest)
      throws UnauthenticatedRequestException {
    this.authService.authenticateRequest(httpRequest);

    return this.configurationService.getBandwidthLimiterConfiguration();
  }
}
