package com.lambda_expressions.package_manager.v1.controllers;

import com.lambda_expressions.package_manager.exceptions.*;
import com.lambda_expressions.package_manager.services.AuthenticationService;
import com.lambda_expressions.package_manager.services.PackageService;
import com.lambda_expressions.package_manager.v1.controllers.utils.ControllerUtils;
import com.lambda_expressions.package_manager.v1.model.PackageDTO;
import com.lambda_expressions.package_manager.v1.model.PackageListDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
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
  @PostMapping(value = {"uploadPackage/{appName}/{version}/{fileName}", "uploadPackage/{appName}/{version}/{fileName}/"},
      consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public void uploadPackage(HttpServletRequest httpRequest, @PathVariable String appName,
                            @PathVariable String version, @PathVariable String fileName, @RequestBody byte[] file)
      throws UnauthenticatedRequestException, MalformedURLException, IOFileException {
    this.authService.authenticateRequest(httpRequest);
    int intVersion = ControllerUtils.checkVersionParameter(appName, version);

    this.packageService.installPackageFile(appName, intVersion, fileName, file);
  }

  @ResponseStatus(HttpStatus.OK)
  @PatchMapping(value = {"invalidatePackage/{appName}/{version}", "invalidatePackage/{appName}/{version}/"})
  public void invalidatePackage(HttpServletRequest httpRequest, @PathVariable String appName, @PathVariable String version)
      throws UnauthenticatedRequestException, MalformedURLException, IOFileException, PackageNotFoundException {
    this.authService.authenticateRequest(httpRequest);
    int intVersion = ControllerUtils.checkVersionParameter(appName, version);

    this.packageService.invalidatePackage(appName, intVersion);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = {"downloadPackage/{appName}/{version}", "downloadPackage/{appName}/{version}/"},
      produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public @ResponseBody
  byte[] downloadPackage(HttpServletRequest httpRequest, @PathVariable String appName, @PathVariable String version)
      throws UnauthenticatedRequestException, MalformedURLException, PackageNotFoundException, IOFileException, InvalidPackageException {
    this.authService.authenticateRequest(httpRequest);
    int intVersion = ControllerUtils.checkVersionParameter(appName, version);

    return this.packageService.getPackageFile(appName, intVersion);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value={"listPackages", "listPackages/"})
  public Collection<PackageListDTO> listPackages(HttpServletRequest httpRequest)
      throws UnauthenticatedRequestException, PackageNotFoundException {
    this.authService.authenticateRequest(httpRequest);

    return this.packageService.listAllPackages();
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = {"listPackages/{appName}", "listPackages/{appName}/"})
  public PackageListDTO listVersions(HttpServletRequest httpRequest, @PathVariable String appName)
      throws UnauthenticatedRequestException, PackageNotFoundException {
    this.authService.authenticateRequest(httpRequest);

    return this.packageService.listAllVersions(appName);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value={"listPackages/{appName}/{version}", "listPackages/{appName}/{version}/"})
  public PackageDTO getPackageUrl(HttpServletRequest httpRequest, @PathVariable String appName, @PathVariable String version)
      throws UnauthenticatedRequestException, MalformedURLException, PackageNotFoundException {
    this.authService.authenticateRequest(httpRequest);
    int intVersion = ControllerUtils.checkVersionParameter(appName, version);

    return this.packageService.getPackageInfo(appName, intVersion);
  }

}
