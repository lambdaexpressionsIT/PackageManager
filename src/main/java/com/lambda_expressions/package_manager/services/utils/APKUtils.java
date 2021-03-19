package com.lambda_expressions.package_manager.services.utils;

import apktool.brut.androlib.AndrolibException;
import apktool.brut.androlib.ApkDecoder;
import apktool.brut.androlib.res.data.ResResSpec;
import apktool.brut.androlib.res.data.ResTable;
import apktool.brut.androlib.res.data.value.ResStringValue;
import apktool.brut.directory.DirectoryException;
import com.lambda_expressions.package_manager.exceptions.AutoDetectionException;
import com.lambda_expressions.package_manager.v1.model.PackageDTO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.UUID;


/**
 * Created by steccothal
 * on Wednesday 17 March 2021
 * at 10:22 AM
 */
@Component
public class APKUtils {
  private static final String TMP_FILE_NAME = "tmp";
  private static final String TMP_FILE_EXTENSION = ".apk";

  public PackageDTO autodetectPackageInfo(byte[] fileBytes) throws AutoDetectionException {
    File tmpWorkDir = setUpWorkingDirectory();
    PackageDTO partialDTO;

    try {
      ResTable restab = getResourceTable(fileBytes, tmpWorkDir.getAbsolutePath());
      partialDTO = getPackageInfo(restab);
    } catch (AutoDetectionException e) {
      throw e;
    } catch (Exception e) {
      throw new AutoDetectionException("Cannot autodetect package information", "", "");
    } finally {
      FileUtils.deleteQuietly(tmpWorkDir);
    }

    return partialDTO;
  }

  private File setUpWorkingDirectory() {
    UUID tmpUUID = UUID.randomUUID();
    String tmpWorkDirPath = String.format("%s%s", FileUtils.getTempDirectoryPath(), tmpUUID);

    return new File(tmpWorkDirPath);
  }

  private ResTable getResourceTable(byte[] fileBytes, String workingDirPath) throws IOException, AndrolibException, DirectoryException {
    String tmpFileName = String.format("%s%s%s%s", workingDirPath, File.separator, TMP_FILE_NAME, TMP_FILE_EXTENSION);
    File tmpAPKFile = new File(tmpFileName);
    ApkDecoder apkDecoder = new ApkDecoder();

    FileUtils.writeByteArrayToFile(tmpAPKFile, fileBytes);

    apkDecoder.setApkFile(tmpAPKFile);
    apkDecoder.setOutDir(new File(String.format("%s%s%s", workingDirPath, File.separator, TMP_FILE_NAME)));
    apkDecoder.decode();

    return apkDecoder.getResTable();
  }

  private PackageDTO getPackageInfo(ResTable resTable) throws AutoDetectionException, AndrolibException {
    String packageName = resTable.getPackageRenamed();
    String versionName = resTable.getVersionInfo().versionName;
    String appName = resTable.getAppName();

    if (StringUtils.isEmpty(appName)) {
      ResResSpec resResSpec = resTable.getCurrentResPackage().listResSpecs().stream()
          .filter(spec -> spec.getFullName(true, false).equalsIgnoreCase(resTable.getAppNameResource()))
          .findFirst()
          .orElseThrow(() -> new AutoDetectionException("Cannot get app name", packageName, versionName));

      appName = ((ResStringValue) resResSpec.getDefaultResource().getValue()).encodeAsResXmlValue();
    }

    if (StringUtils.isBlank(packageName) || StringUtils.isBlank(versionName) || StringUtils.isBlank(appName)) {
      throw new AutoDetectionException("Cannot get app info", appName, packageName);
    }

    return PackageDTO.builder()
        .packageName(packageName)
        .appName(appName)
        .appVersion(versionName)
        .build();
  }
}
