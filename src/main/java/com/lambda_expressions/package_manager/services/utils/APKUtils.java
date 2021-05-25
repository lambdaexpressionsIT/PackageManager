package com.lambda_expressions.package_manager.services.utils;

import apktool.brut.androlib.Androlib;
import apktool.brut.androlib.AndrolibException;
import apktool.brut.androlib.ApkDecoder;
import apktool.brut.androlib.ApkOptions;
import apktool.brut.androlib.err.CantFindFrameworkResException;
import apktool.brut.androlib.res.data.ResResSpec;
import apktool.brut.androlib.res.data.ResTable;
import apktool.brut.androlib.res.data.value.ResStringValue;
import com.lambda_expressions.package_manager.exceptions.*;
import com.lambda_expressions.package_manager.v1.model.PackageDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;


/**
 * Created by steccothal
 * on Wednesday 17 March 2021
 * at 10:22 AM
 */
@Slf4j
public class APKUtils {
  private static final String TMP_FILE_NAME = "tmp";
  private static final String TMP_FILE_EXTENSION = ".apk";

  public List<String> listInstalledFrameworks() throws IOFileException {
    try {
      return new Androlib().listFrameworks();
    } catch (AndrolibException e) {
      throw new IOFileException("Can't retrieve installed framework", "", "");
    }
  }

  public void installFramework(byte[] fileBytes, String frameworkTag) throws FrameworkInstallationException {
    File tmpWorkDir = setUpWorkDirectory();

    try {
      installApkToolFramework(fileBytes, tmpWorkDir.getAbsolutePath(), frameworkTag);
    } catch (Exception e) {
      throw new FrameworkInstallationException("Cannot install framework", frameworkTag);
    } finally {
      deleteWorkDirectory(tmpWorkDir);
    }
  }

  public PackageDTO autodetectPackageInfo(byte[] fileBytes) throws AutoDetectionException, MissingFrameworkException {
    File tmpWorkDir = setUpWorkDirectory();
    PackageDTO partialDTO;

    try {
      ResTable resourcesTable = getResourceTable(fileBytes, tmpWorkDir.getAbsolutePath());
      partialDTO = getPackageInfo(resourcesTable);
    } catch (MissingFrameworkException | AutoDetectionException e) {
      throw e;
    } catch (Exception e) {
      throw new AutoDetectionException("Cannot autodetect package information", "", "");
    } finally {
      deleteWorkDirectory(tmpWorkDir);
    }

    return partialDTO;
  }

  private void installApkToolFramework(byte[] fileBytes, String workingDirPath, String frameworkTag) throws IOException, AndrolibException {
    String tmpFileName = String.format("%s%s%s%s", workingDirPath, File.separator, TMP_FILE_NAME, TMP_FILE_EXTENSION);
    File tmpAPKFile = new File(tmpFileName);
    ApkOptions apkOptions = new ApkOptions();
    Androlib androlib = new Androlib(apkOptions);

    if (!StringUtils.isBlank(frameworkTag)) {
      apkOptions.frameworkTag = frameworkTag;
    }

    FileUtils.writeByteArrayToFile(tmpAPKFile, fileBytes);

    androlib.installFramework(tmpAPKFile);
  }

  private ResTable getResourceTable(byte[] fileBytes, String workingDirPath) throws AutoDetectionException, IOException, MissingFrameworkException {
    String tmpFileName = String.format("%s%s%s%s", workingDirPath, File.separator, TMP_FILE_NAME, TMP_FILE_EXTENSION);
    File tmpAPKFile = new File(tmpFileName);
    File apkToolsWorkingDir = new File(String.format("%s%s%s", workingDirPath, File.separator, TMP_FILE_NAME));
    ApkDecoder apkDecoder = new ApkDecoder();
    ResTable resourceTable;

    FileUtils.writeByteArrayToFile(tmpAPKFile, fileBytes);

    try {
      apkDecoder.setOutDir(apkToolsWorkingDir);
      apkDecoder.setApkFile(tmpAPKFile);
      apkDecoder.decode();

      resourceTable = apkDecoder.getResTable();
    } catch (CantFindFrameworkResException e) {
      log.error(e.getMessage());
      throw new MissingFrameworkException(e.getMessage(), e.getPkgId());
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new AutoDetectionException("Cannot autodetect package information", "", "");
    } finally {
      try {
        apkDecoder.close();
      } catch (IOException e) {
        log.error("Can't close apkDecoder");
      }
    }

    return resourceTable;
  }

  private PackageDTO getPackageInfo(ResTable resourcesTable) throws AutoDetectionException, AndrolibException {

    if (resourcesTable == null) {
      throw new AutoDetectionException("Cannot autodetect package information, resource table is null", "", "");
    }

    String packageName = resourcesTable.getPackageRenamed();
    String versionName = resourcesTable.getVersionInfo().versionName;
    String appName = resourcesTable.getAppName();
    long versionCode;

    try {
      versionCode = Long.parseLong(resourcesTable.getVersionInfo().versionCode);
    } catch (NumberFormatException e) {
      throw new AutoDetectionException("Cannot get app version code", appName, packageName);
    }

    if (StringUtils.isEmpty(appName)) {
      ResResSpec resResSpec = resourcesTable.getCurrentResPackage().listResSpecs().stream()
          .filter(spec -> spec.getFullName(true, false).equalsIgnoreCase(resourcesTable.getAppNameResource()))
          .findFirst()
          .orElseThrow(() -> new AutoDetectionException("Cannot get app name", packageName, versionName));

      appName = ((ResStringValue) resResSpec.getDefaultResource().getValue()).encodeAsResXmlValue();
    }

    if (StringUtils.isBlank(packageName) || StringUtils.isBlank(versionName) || StringUtils.isBlank(appName) || versionCode <= 0) {
      throw new AutoDetectionException("Cannot get app info", appName, packageName);
    }

    PackageDTO partialDTO = new PackageDTO();

    partialDTO.setPackageName(packageName);
    partialDTO.setAppName(appName);
    partialDTO.setAppVersion(versionName);
    partialDTO.setAppVersionNumber(versionCode);

    return partialDTO;
  }

  private File setUpWorkDirectory() {
    UUID tmpUUID = UUID.randomUUID();
    String tmpWorkDirPath = String.format("%s%s%s", FileUtils.getTempDirectoryPath(), File.separator, tmpUUID);
    File workDir = new File(tmpWorkDirPath);

    log.info("Created work directory for apk decompiling:" + tmpWorkDirPath);

    return workDir;
  }

  private void deleteWorkDirectory(File workDir) {
    try {
      FileUtils.deleteDirectory(workDir);
      log.info("Deleted work directory for apk decompiling:" + workDir.getAbsolutePath());
    } catch (IOException e) {
      log.error("Unable to delete work directory for apk:" + workDir.getAbsolutePath());
    }
  }
}
