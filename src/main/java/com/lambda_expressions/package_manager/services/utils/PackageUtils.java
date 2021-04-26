package com.lambda_expressions.package_manager.services.utils;

import com.lambda_expressions.package_manager.domain.Package;
import com.lambda_expressions.package_manager.exceptions.InvalidPackageException;
import com.lambda_expressions.package_manager.exceptions.PackageNotFoundException;
import com.lambda_expressions.package_manager.v1.model.PackageDTO;
import com.lambda_expressions.package_manager.v1.model.PackageListDTO;
import com.lambda_expressions.package_manager.v1.model.VersionDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
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
  private final String WAREHOUSE_PREFIX = "warehouse";

  @Value("${application.public.base.url}")
  private String PUBLIC_BASE_URL;

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
                .url(composeWarehouseURLFromLocalPath(pInfo.getPath()))
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
          .url(composeWarehouseURLFromLocalPath(pInfo.getPath()))
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
        .url(composeWarehouseURLFromLocalPath(packageInfo.getPath()))
        .build();
  }

  public String composeLocalRelativePath(String appName, String version, String fileName) {
    return String.format("%s%s%s%s%s", appName, File.separator, version, File.separator, fileName);
  }

  public String composeWarehouseURLFromLocalPath(String localPath) {
    String localPathToUrl = localPath.replace(File.separator, "/");
    String baseURL;

    if (StringUtils.isBlank(PUBLIC_BASE_URL)){
      baseURL = ServletUriComponentsBuilder.fromCurrentContextPath().build().toString();
    } else{
      String deploymentContext = ServletUriComponentsBuilder.fromCurrentContextPath().build().getPath();
      baseURL = String.format("%s%s", PUBLIC_BASE_URL, deploymentContext);
    }

    return String.format("%s/%s/%s", baseURL, WAREHOUSE_PREFIX, localPathToUrl);
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
