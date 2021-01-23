package com.lambda_expressions.package_manager.services.utils;

import com.lambda_expressions.package_manager.domain.Package;
import com.lambda_expressions.package_manager.exceptions.InvalidPackageException;
import com.lambda_expressions.package_manager.exceptions.PackageNotFoundException;
import com.lambda_expressions.package_manager.v1.model.PackageDTO;
import com.lambda_expressions.package_manager.v1.model.PackageListDTO;
import com.lambda_expressions.package_manager.v1.model.VersionDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by steccothal
 * on Monday 18 January 2021
 * at 3:10 PM
 */
@Component
public class PackageUtils {
  @Value("${packages.web.base.url}")
  private String PACKAGES_WEBSERVER_BASEURL;

  @Value("${packages.file.extension}")
  private String PACKAGES_FILE_EXTENSION;

  public PackageListDTO composePackageListDTOFromPackage(List<Package> packageVersions, String appName) {
    return PackageListDTO.builder()
        .appName(appName)
        .versions(packageVersions.stream()
            .map(pInfo -> VersionDTO.builder()
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
          .versions(new ArrayList<>())
          .build());
      packagesMap.get(pInfo.getAppname()).getVersions().add(VersionDTO.builder()
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
        .appName(packageInfo.getAppname())
        .appVersion(packageInfo.getVersion())
        .fileName(packageInfo.getFilename())
        .valid(packageInfo.isValid())
        .url(composeURLFromLocalPath(packageInfo.getPath()))
        .build();
  }

  public String composeLocalRelativePath(String appName, int version, String fileName) {
    return String.format("%s%s%s%s%s%s", appName, File.separator, version, File.separator, fileName, PACKAGES_FILE_EXTENSION);
  }

  public String composeURLFromLocalPath(String localPath) {
    String localPathToUrl = localPath.replace(File.separator, "/");
    return String.format("%s/%s", PACKAGES_WEBSERVER_BASEURL, localPathToUrl);
  }

  public void checkPackageValidity(Package packageInfo) throws InvalidPackageException {
    if (!packageInfo.isValid()) {
      throw new InvalidPackageException("Invalid package, can't download it", packageInfo.getAppname(), packageInfo.getVersion());
    }
  }

  public void checkRepositoryIterableResult(Iterable<Package> packageList, String appName, boolean breakOnEmpty) throws PackageNotFoundException {
    this.checkRepositoryResult(packageList, appName, -1);

    if (!packageList.iterator().hasNext() && breakOnEmpty) {
      throw new PackageNotFoundException("Package not found", appName, -1);
    }
  }

  public <T> void checkRepositoryResult(T packageInfo, String appName, int version) throws PackageNotFoundException {
    Optional.ofNullable(packageInfo)
        .orElseThrow(() -> new PackageNotFoundException("Package not found", appName, version));
  }
}
