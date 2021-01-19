package com.lambda_expressions.package_manager.v1.controllers;

import com.lambda_expressions.package_manager.exceptions.*;
import com.lambda_expressions.package_manager.services.AuthenticationService;
import com.lambda_expressions.package_manager.services.PackageService;
import com.lambda_expressions.package_manager.v1.model.PackageDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by steccothal
 * on Monday 18 January 2021
 * at 9:31 AM
 */

@RestController
@RequestMapping("api/v1/")
public class PackageManagerController {
  PackageService packageService;
  AuthenticationService authService;

  public PackageManagerController(PackageService packageService, AuthenticationService authService) {
    this.packageService = packageService;
    this.authService = authService;
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = {"uploadPackage/{appName}/{version}", "uploadPackage/{appName}/{version}/"},
      consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public void uploadPackage(HttpServletRequest httpRequest, @PathVariable String appName, @PathVariable String version, @RequestBody byte[] file)
      throws UnauthenticatedRequestException, MalformedURLException, IOFileException {
    int intVersion = this.checkVersionParameter(appName, version);
    this.authService.authenticateRequest(httpRequest);

    this.packageService.installPackageFile(appName, intVersion, file);
  }

  @ResponseStatus(HttpStatus.OK)
  @PatchMapping(value = {"invalidatePackage/{appName}/{version}", "invalidatePackage/{appName}/{version}/"})
  public void invalidatePackage(HttpServletRequest httpRequest, @PathVariable String appName, @PathVariable String version)
      throws UnauthenticatedRequestException, MalformedURLException, IOFileException, PackageNotFoundException {
    int intVersion = this.checkVersionParameter(appName, version);
    this.authService.authenticateRequest(httpRequest);

    this.packageService.invalidatePackage(appName, intVersion);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = {"downloadPackage/{appName}/{version}", "downloadPackage/{appName}/{version}/"},
      produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public @ResponseBody
  byte[] downloadPackage(HttpServletRequest httpRequest, @PathVariable String appName, @PathVariable String version)
      throws UnauthenticatedRequestException, MalformedURLException, PackageNotFoundException, IOFileException, InvalidPackageException {
    int intVersion = this.checkVersionParameter(appName, version);
    this.authService.authenticateRequest(httpRequest);

    return this.packageService.getPackageFile(appName, intVersion);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value={"listPackages", "listPackages/"})
  public List<PackageDTO> listPackages(HttpServletRequest httpRequest)
      throws UnauthenticatedRequestException, PackageNotFoundException {
    this.authService.authenticateRequest(httpRequest);

    return this.packageService.listAllPackages();
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = {"listPackages/{appName}", "listPackages/{appName}/"})
  public List<PackageDTO> listVersions(HttpServletRequest httpRequest, @PathVariable String appName)
      throws UnauthenticatedRequestException, PackageNotFoundException {
    this.authService.authenticateRequest(httpRequest);

    return this.packageService.listAllVersions(appName);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value={"listPackages/{appName}/{version}", "listPackages/{appName}/{version}/"})
  public PackageDTO getPackageUrl(HttpServletRequest httpRequest, @PathVariable String appName, @PathVariable String version)
      throws UnauthenticatedRequestException, MalformedURLException, PackageNotFoundException {
    int intVersion = this.checkVersionParameter(appName, version);
    this.authService.authenticateRequest(httpRequest);

    return this.packageService.getPackageInfo(appName, intVersion);
  }

  private int checkVersionParameter(String appName, String version) throws MalformedURLException {
    int intVersion = 0;

    try {
      intVersion = Integer.parseInt(version);
    } catch (NumberFormatException formatException) {
      throw new MalformedURLException("Version is not a number", appName, version);
    }

    return intVersion;
  }

}
