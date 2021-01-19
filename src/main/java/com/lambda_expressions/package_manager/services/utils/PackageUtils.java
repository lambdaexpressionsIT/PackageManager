package com.lambda_expressions.package_manager.services.utils;

import com.lambda_expressions.package_manager.domain.Package;
import com.lambda_expressions.package_manager.exceptions.IOFileException;
import com.lambda_expressions.package_manager.exceptions.InvalidPackageException;
import com.lambda_expressions.package_manager.exceptions.PackageNotFoundException;
import com.lambda_expressions.package_manager.v1.model.PackageDTO;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * Created by steccothal
 * on Monday 18 January 2021
 * at 3:10 PM
 */
@Component
public class PackageUtils {
  @Value("${packages.filesystem.base.path}")
  private String PACKAGES_FILESYSTEM_BASELOCATION;

  @Value("${packages.web.base.url}")
  private String PACKAGES_WEBSERVER_BASEURL;

  @Value("${packages.file.extension}")
  private String PACKAGES_FILE_EXTENSION;

  public void savePackageFile(String appName, int version, byte[] file) throws IOFileException {
    try {
      String localRelativePath = this.composeLocalRelativePath(appName, version);
      FileUtils.writeByteArrayToFile(new File(this.composeAbsoluteLocalPath(localRelativePath)), file);
    } catch (Exception e) {
      throw new IOFileException("can't write file", appName, version);
    }
  }

  public byte[] loadPackageFile(Package packageInfo) throws IOFileException {
    byte[] file;
    try {
      String absolutePath = this.composeAbsoluteLocalPath(packageInfo.getPath());
      file = FileUtils.readFileToByteArray(new File(absolutePath));
    } catch (Exception e) {
      throw new IOFileException("can't read file", packageInfo.getAppname(), packageInfo.getVersion());
    }

    return file;
  }

  public PackageDTO composeDTOFromPackage(Package packageInfo) {
    PackageDTO packageDTO = PackageDTO.builder()
        .appName(packageInfo.getAppname())
        .appVersion(packageInfo.getVersion())
        .url(composeURLFromLocalPath(packageInfo.getPath()))
        .valid(packageInfo.isValid())
        .build();

    return packageDTO;
  }

  public String composeLocalRelativePath(String appName, int version) {
    return String.format("%s%s%s%s%s%s", appName, File.separator, version, File.separator, appName, PACKAGES_FILE_EXTENSION);
  }

  public String composeAbsoluteLocalPath(String relativeLocalPath) {
    return String.format("%s%s%s", PACKAGES_FILESYSTEM_BASELOCATION, File.separator, relativeLocalPath);
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

  public List<Package> checkDataRetrieved(List<Package> packageList, String appName, int version) throws PackageNotFoundException {
    List<Package> packages = this.checkRepositoryResult(packageList, appName, version);

    if(packages.isEmpty()){
      throw new PackageNotFoundException("Package not found", appName, version);
    }

    return packages;
  }

  public <T> T checkRepositoryResult(T packageInfo, String appName, int version) throws PackageNotFoundException {
    return Optional.ofNullable(packageInfo)
        .orElseThrow(() -> new PackageNotFoundException("Package not found", appName, version));
  }
}
