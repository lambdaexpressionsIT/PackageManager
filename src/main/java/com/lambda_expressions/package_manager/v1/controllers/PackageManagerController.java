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
import org.springframework.web.multipart.MultipartFile;

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
  ControllerUtils controllerUtils;

  public PackageManagerController(PackageService packageService, AuthenticationService authService, ControllerUtils controllerUtils) {
    this.packageService = packageService;
    this.authService = authService;
    this.controllerUtils = controllerUtils;
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = {"uploadPackage/{packageName}/{appName}/{version}/{fileName}", "uploadPackage/{packageName}/{appName}/{version}/{fileName}/"},
      consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public void uploadPackage(HttpServletRequest httpRequest, @PathVariable String packageName, @PathVariable String appName,
                            @PathVariable String version, @PathVariable String fileName, @RequestBody byte[] file)
      throws UnauthenticatedRequestException, IOFileException, WrongAppNameException {
    this.authService.authenticateRequest(httpRequest);

    this.packageService.installPackageFile(packageName, appName, version, fileName, file);
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = {"uploadPackage", "uploadPackage/"},
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public PackageDTO uploadPackageAutodetect(HttpServletRequest httpRequest, @RequestPart("file") MultipartFile file)
      throws UnauthenticatedRequestException, IOFileException, AutoDetectionException, MissingFrameworkException, WrongAppNameException {
    this.authService.authenticateRequest(httpRequest);
    String fileName = controllerUtils.getFileName(file);

    return this.packageService.installPackageFile(fileName, file);
  }

  @ResponseStatus(HttpStatus.OK)
  @PatchMapping(value = {"invalidatePackage/{appName}/{version}", "invalidatePackage/{appName}/{version}/"})
  public void invalidatePackage(HttpServletRequest httpRequest, @PathVariable String appName, @PathVariable String version)
      throws UnauthenticatedRequestException, PackageNotFoundException {
    this.authService.authenticateRequest(httpRequest);

    this.packageService.invalidatePackage(appName, version);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = {"downloadPackage/{appName}/{version}", "downloadPackage/{appName}/{version}/"},
      produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public @ResponseBody
  byte[] downloadPackage(HttpServletRequest httpRequest, @PathVariable String appName, @PathVariable String version)
      throws UnauthenticatedRequestException, PackageNotFoundException, IOFileException, InvalidPackageException {
    this.authService.authenticateRequest(httpRequest);

    return this.packageService.getPackageFile(appName, version);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = {"listPackages", "listPackages/"})
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
  @GetMapping(value = {"listPackages/{appName}/{version}", "listPackages/{appName}/{version}/"})
  public PackageDTO listPackageInfo(HttpServletRequest httpRequest, @PathVariable String appName, @PathVariable String version)
      throws UnauthenticatedRequestException, PackageNotFoundException {
    this.authService.authenticateRequest(httpRequest);

    return this.packageService.getPackageInfo(appName, version);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = {"getPackage/{appId}", "getPackage/{appId}/"})
  public PackageDTO getPackageInfoById(HttpServletRequest httpRequest, @PathVariable String appId)
      throws UnauthenticatedRequestException, MalformedURLException, PackageNotFoundException {
    this.authService.authenticateRequest(httpRequest);
    long longId = controllerUtils.checkIdParameter(appId);

    return this.packageService.getPackageInfoById(longId);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = {"getPackages", "getPackages/"})
  public Collection<PackageListDTO> getPackagesById(HttpServletRequest httpRequest, @RequestParam List<String> idList)
      throws UnauthenticatedRequestException, PackageNotFoundException {
    this.authService.authenticateRequest(httpRequest);
    List<Long> idLongList = controllerUtils.convertIdList(idList);

    return this.packageService.getPackagesById(idLongList);
  }

  @ResponseStatus(HttpStatus.OK)
  @DeleteMapping(value = {"deletePackage/{appName}", "deletePackage/{appName}/"})
  public void deleteAllVersions(HttpServletRequest httpRequest, @PathVariable String appName)
      throws UnauthenticatedRequestException, PackageNotFoundException {
    this.authService.authenticateRequest(httpRequest);

    this.packageService.deleteAllVersions(appName);
  }

  @ResponseStatus(HttpStatus.OK)
  @DeleteMapping(value = {"deletePackage/{appName}/{version}", "deletePackage/{appName}/{version}/"})
  public void deleteSingleVersion(HttpServletRequest httpRequest, @PathVariable String appName, @PathVariable String version)
      throws UnauthenticatedRequestException, PackageNotFoundException {
    this.authService.authenticateRequest(httpRequest);

    this.packageService.deleteVersionPackage(appName, version);
  }

  @ResponseStatus(HttpStatus.OK)
  @DeleteMapping(value = {"deletePackage", "deletePackage/"})
  public void deleteById(HttpServletRequest httpRequest, @RequestParam List<String> idList)
      throws UnauthenticatedRequestException, PackageNotFoundException {
    this.authService.authenticateRequest(httpRequest);
    List<Long> idLongList = controllerUtils.convertIdList(idList);

    this.packageService.deletePackagesList(idLongList);
  }

}
