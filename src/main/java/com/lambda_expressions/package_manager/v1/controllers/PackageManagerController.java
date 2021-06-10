package com.lambda_expressions.package_manager.v1.controllers;

import com.lambda_expressions.package_manager.exceptions.*;
import com.lambda_expressions.package_manager.services.PackageService;
import com.lambda_expressions.package_manager.v1.controllers.utils.ControllerUtils;
import com.lambda_expressions.package_manager.v1.model.PackageDTO;
import com.lambda_expressions.package_manager.v1.model.PackageListDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
  ControllerUtils controllerUtils;

  public PackageManagerController(PackageService packageService, ControllerUtils controllerUtils) {
    this.packageService = packageService;
    this.controllerUtils = controllerUtils;
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = {"uploadPackage/{packageName}/{appName}/{version}/{versionNumber}/{fileName}", "uploadPackage/{packageName}/{appName}/{version}/{versionNumber}/{fileName}/"},
      consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public void uploadPackage(@PathVariable String packageName, @PathVariable String appName,
                            @PathVariable String version, @PathVariable String versionNumber, @PathVariable String fileName,
                            @RequestBody byte[] file)
      throws IOFileException, WrongAppNameException, MalformedURLException {
    long longVersionNumber = controllerUtils.checkNumericParameter(versionNumber, "version number");

    this.packageService.installPackageFile(packageName, appName, longVersionNumber, version, fileName, file);
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping(value = {"uploadPackage", "uploadPackage/"},
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public PackageDTO uploadPackageAutodetect(@RequestPart("file") MultipartFile file)
      throws IOFileException, AutoDetectionException, MissingFrameworkException, WrongAppNameException {
    String fileName = controllerUtils.getFileName(file);

    return this.packageService.installPackageFile(fileName, file);
  }

  @ResponseStatus(HttpStatus.OK)
  @PatchMapping(value = {"invalidatePackage/{appName}/{version}", "invalidatePackage/{appName}/{version}/"})
  public void invalidatePackage(@PathVariable String appName, @PathVariable String version) throws PackageNotFoundException {

    this.packageService.invalidatePackage(appName, version);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = {"downloadPackage/{appName}/{version}", "downloadPackage/{appName}/{version}/"},
      produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public @ResponseBody
  byte[] downloadPackage(@PathVariable String appName, @PathVariable String version)
      throws PackageNotFoundException, IOFileException, InvalidPackageException {
    return this.packageService.getPackageFile(appName, version);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = {"listPackages", "listPackages/"})
  public Collection<PackageListDTO> listPackages() throws PackageNotFoundException {
    return this.packageService.listAllPackages();
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = {"listPackages/{appName}", "listPackages/{appName}/"})
  public PackageListDTO listVersions(@PathVariable String appName) throws PackageNotFoundException {

    return this.packageService.listAllVersions(appName);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = {"listPackages/{appName}/{version}", "listPackages/{appName}/{version}/"})
  public PackageDTO listPackageInfo( @PathVariable String appName, @PathVariable String version) throws PackageNotFoundException {

    return this.packageService.getPackageInfo(appName, version);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = {"getPackage/{appId}", "getPackage/{appId}/"})
  public PackageDTO getPackageInfoById(@PathVariable String appId) throws MalformedURLException, PackageNotFoundException {
    long longId = controllerUtils.checkNumericParameter(appId, "appId");

    return this.packageService.getPackageInfoById(longId);
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = {"getPackages", "getPackages/"})
  public Collection<PackageListDTO> getPackagesById(@RequestParam List<String> idList) throws PackageNotFoundException {
    List<Long> idLongList = controllerUtils.convertIdList(idList);

    return this.packageService.getPackagesById(idLongList);
  }

  @ResponseStatus(HttpStatus.OK)
  @DeleteMapping(value = {"deletePackage/{appName}", "deletePackage/{appName}/"})
  public void deleteAllVersions(@PathVariable String appName) throws PackageNotFoundException {

    this.packageService.deleteAllVersions(appName);
  }

  @ResponseStatus(HttpStatus.OK)
  @DeleteMapping(value = {"deletePackage/{appName}/{version}", "deletePackage/{appName}/{version}/"})
  public void deleteSingleVersion(@PathVariable String appName, @PathVariable String version) throws PackageNotFoundException {

    this.packageService.deleteVersionPackage(appName, version);
  }

  @ResponseStatus(HttpStatus.OK)
  @DeleteMapping(value = {"deletePackage", "deletePackage/"})
  public void deleteById(@RequestParam List<String> idList) throws PackageNotFoundException {
    List<Long> idLongList = controllerUtils.convertIdList(idList);

    this.packageService.deletePackagesList(idLongList);
  }

}
