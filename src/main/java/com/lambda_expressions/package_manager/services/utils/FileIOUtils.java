package com.lambda_expressions.package_manager.services.utils;

import com.lambda_expressions.package_manager.domain.Package;
import com.lambda_expressions.package_manager.exceptions.AutoDetectionException;
import com.lambda_expressions.package_manager.exceptions.IOFileException;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * Created by steccothal
 * on Saturday 23 January 2021
 * at 1:51 PM
 */
@Component
public class FileIOUtils {

  @Value("${packages.filesystem.base.path}")
  private String PACKAGES_FILESYSTEM_BASE_LOCATION;

  public void deleteSingleFile(Package packageInfo) {
    String absolutePath = composeAbsoluteLocalPath(packageInfo.getPath());
    FileUtils.deleteQuietly(new File(absolutePath));
  }

  public void deletePackageList(Iterable<Package> packages) {
    packages.forEach(packageInfo -> {
      deleteSingleFile(packageInfo);
    });
  }

  public void savePackageFile(String appName, String version, String fileName, byte[] file, PackageUtils packageUtils) throws IOFileException {
    try {
      String localRelativePath = packageUtils.composeLocalRelativePath(appName, version, fileName);
      FileUtils.writeByteArrayToFile(new File(composeAbsoluteLocalPath(localRelativePath)), file);
    } catch (Exception e) {
      throw new IOFileException("can't write file", appName, version);
    }
  }

  public byte[] getMultipartFileBytes(MultipartFile file) throws AutoDetectionException {
    try {
      return file.getBytes();
    } catch (IOException e) {
      throw new AutoDetectionException("Invalid file", "", "");
    }
  }

  public byte[] loadPackageFile(Package packageInfo) throws IOFileException {
    byte[] file;

    try {
      String absolutePath = composeAbsoluteLocalPath(packageInfo.getPath());
      file = FileUtils.readFileToByteArray(new File(absolutePath));
    } catch (Exception e) {
      throw new IOFileException("can't read file", packageInfo.getAppname(), packageInfo.getVersion());
    }

    return file;
  }

  public FileSystemResource loadPackageFileResource(Package packageInfo) throws IOFileException {
    File file;

    try {
      String absolutePath = composeAbsoluteLocalPath(packageInfo.getPath());
      file = new File(absolutePath);
      if(!file.exists() || !file.isFile()){
        throw new Exception();
      }
    } catch (Exception e) {
      throw new IOFileException("can't read file", packageInfo.getAppname(), packageInfo.getVersion());
    }

    return new FileSystemResource(file);
  }

  private String composeAbsoluteLocalPath(String relativeLocalPath) {
    return String.format("%s%s%s", PACKAGES_FILESYSTEM_BASE_LOCATION, File.separator, relativeLocalPath);
  }
}
