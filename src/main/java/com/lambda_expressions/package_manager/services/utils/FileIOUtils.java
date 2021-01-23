package com.lambda_expressions.package_manager.services.utils;

import com.lambda_expressions.package_manager.domain.Package;
import com.lambda_expressions.package_manager.exceptions.IOFileException;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Created by steccothal
 * on Saturday 23 January 2021
 * at 1:51 PM
 */
@Component
public class FileIOUtils {

  @Value("${packages.filesystem.base.path}")
  private String PACKAGES_FILESYSTEM_BASE_LOCATION;

  public void savePackageFile(String appName, int version, String fileName, byte[] file, PackageUtils packageUtils) throws IOFileException {
    try {
      String localRelativePath = packageUtils.composeLocalRelativePath(appName, version, fileName);
      FileUtils.writeByteArrayToFile(new File(composeAbsoluteLocalPath(localRelativePath)), file);
    } catch (Exception e) {
      throw new IOFileException("can't write file", appName, version);
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

  private String composeAbsoluteLocalPath(String relativeLocalPath) {
    return String.format("%s%s%s", PACKAGES_FILESYSTEM_BASE_LOCATION, File.separator, relativeLocalPath);
  }
}
