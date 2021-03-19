package com.lambda_expressions.package_manager.services.utils;

import com.lambda_expressions.package_manager.domain.Package;
import com.lambda_expressions.package_manager.exceptions.InvalidPackageException;
import com.lambda_expressions.package_manager.exceptions.PackageNotFoundException;
import com.lambda_expressions.package_manager.v1.model.PackageDTO;
import com.lambda_expressions.package_manager.v1.model.PackageListDTO;
import com.lambda_expressions.package_manager.v1.model.VersionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by steccothal
 * on Monday 18 January 2021
 * at 3:10 PM
 */
@Component
@Slf4j
public class PackageUtils {
  @Value("${packages.web.base.url}")
  private String PACKAGES_WEBSERVER_BASEURL;

  public PackageListDTO composePackageListDTOFromPackage(List<Package> packageVersions, String appName, String packageName) {
    return PackageListDTO.builder()
        .appName(appName)
        .packageName(packageName)
        .versions(packageVersions.stream()
            .map(pInfo -> VersionDTO.builder()
                .id(pInfo.getId())
                .appVersion(pInfo.getVersion())
                .fileName(pInfo.getFilename())
                .valid(pInfo.isValid())
                .url(composeURLFromLocalPath(pInfo.getPath()))
                .build())
            .collect(Collectors.toList()))
        .build();
  }

  public Collection<PackageListDTO> composePackageListDTOFromPackageList(Iterable<Package> packages) {
    Map<String, PackageListDTO> packagesMap = new HashMap<>();

    packages.forEach(pInfo -> {
      packagesMap.putIfAbsent(pInfo.getAppname(), PackageListDTO.builder()
          .appName(pInfo.getAppname())
          .packageName(pInfo.getPackagename())
          .versions(new ArrayList<>())
          .build());
      packagesMap.get(pInfo.getAppname()).getVersions().add(VersionDTO.builder()
          .id(pInfo.getId())
          .appVersion(pInfo.getVersion())
          .fileName(pInfo.getFilename())
          .valid(pInfo.isValid())
          .url(composeURLFromLocalPath(pInfo.getPath()))
          .build());
    });

    return packagesMap.values();
  }

  public PackageDTO composePackageDTOFromPackage(Package packageInfo) {
    return PackageDTO.builder()
        .id(packageInfo.getId())
        .packageName(packageInfo.getPackagename())
        .appName(packageInfo.getAppname())
        .appVersion(packageInfo.getVersion())
        .fileName(packageInfo.getFilename())
        .valid(packageInfo.isValid())
        .url(composeURLFromLocalPath(packageInfo.getPath()))
        .build();
  }

  public String composeLocalRelativePath(String appName, String version, String fileName) {
    return String.format("%s%s%s%s%s", appName, File.separator, version, File.separator, fileName);
  }

  public String composeURLFromLocalPath(String localPath) {
    String localPathToUrl = localPath.replace(File.separator, "/");
    String resourceUrl = String.format("%s/%s", PACKAGES_WEBSERVER_BASEURL, localPathToUrl);

    try {
      resourceUrl = new URI(null, resourceUrl, null).toString();
    } catch (Exception e) {
      log.info("Unparsable resource URL: " + resourceUrl);
    }

    return resourceUrl;
  }

  public void checkPackageValidity(Package packageInfo) throws InvalidPackageException {
    if (!packageInfo.isValid()) {
      throw new InvalidPackageException("Invalid package, can't download it", packageInfo.getAppname(), packageInfo.getVersion());
    }
  }

  public void checkRepositoryIterableResult(Iterable<Package> packageList, String appName, boolean breakOnEmpty) throws PackageNotFoundException {
    this.checkRepositoryResult(packageList, appName, "");

    if (!packageList.iterator().hasNext() && breakOnEmpty) {
      throw new PackageNotFoundException("Package not found", appName, "");
    }
  }

  public <T> void checkRepositoryResult(T packageInfo, String appName, String version) throws PackageNotFoundException {
    Optional.ofNullable(packageInfo)
        .orElseThrow(() -> new PackageNotFoundException("Package not found", appName, version));
  }
}
