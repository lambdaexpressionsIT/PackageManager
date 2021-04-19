package com.lambda_expressions.package_manager.services.impl;

import com.lambda_expressions.package_manager.domain.Package;
import com.lambda_expressions.package_manager.exceptions.*;
import com.lambda_expressions.package_manager.repositories.PackageRepository;
import com.lambda_expressions.package_manager.services.PackageService;
import com.lambda_expressions.package_manager.services.utils.APKUtils;
import com.lambda_expressions.package_manager.services.utils.FileIOUtils;
import com.lambda_expressions.package_manager.services.utils.PackageUtils;
import com.lambda_expressions.package_manager.v1.model.PackageDTO;
import com.lambda_expressions.package_manager.v1.model.PackageListDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * Created by steccothal
 * on Monday 18 January 2021
 * at 9:35 AM
 */
@Service
@Slf4j
public class PackageServiceImpl implements PackageService {

  PackageRepository packageRepo;
  PackageUtils packageUtils;
  FileIOUtils fileIOUtils;
  APKUtils apkUtils;

  public PackageServiceImpl(PackageRepository packageRepo, PackageUtils packageUtils, FileIOUtils fileIOUtils, APKUtils apkUtils) {
    this.packageRepo = packageRepo;
    this.packageUtils = packageUtils;
    this.fileIOUtils = fileIOUtils;
    this.apkUtils = apkUtils;
  }

  @Override
  public Collection<PackageListDTO> listAllPackages() throws PackageNotFoundException {
    Iterable<Package> packages = this.packageRepo.findAll();

    this.packageUtils.checkRepositoryIterableResult(packages, "", false);

    return this.packageUtils.composePackageListDTOFromPackageList(packages);
  }

  @Override
  public PackageListDTO listAllVersions(String appName) throws PackageNotFoundException {
    List<Package> packages = this.packageRepo.findByAppnameIgnoreCase(appName);

    this.packageUtils.checkRepositoryIterableResult(packages, appName, true);

    return this.packageUtils.composePackageListDTOFromPackage(packages, appName, packages.get(0).getPackagename());
  }

  @Override
  public PackageDTO getPackageInfo(String appName, String version) throws PackageNotFoundException {
    Package packageInfo = this.packageRepo.findByAppnameIgnoreCaseAndVersionIgnoreCase(appName, version);

    this.packageUtils.checkRepositoryResult(packageInfo, appName, version);

    return this.packageUtils.composePackageDTOFromPackage(packageInfo);
  }

  @Override
  public PackageDTO getPackageInfoById(long id) throws PackageNotFoundException {
    return this.packageRepo.findById(id)
        .map(packageUtils::composePackageDTOFromPackage)
        .orElseThrow(() -> new PackageNotFoundException("ID not found", Long.toString(id), ""));
  }

  @Override
  public Collection<PackageListDTO> getPackagesById(List<Long> idList) throws PackageNotFoundException {
    Iterable<Package> packages = this.packageRepo.findAllById(idList);

    this.packageUtils.checkRepositoryIterableResult(packages, "", false);

    return this.packageUtils.composePackageListDTOFromPackageList(packages);
  }

  @Override
  public byte[] getPackageFile(String appName, String version) throws PackageNotFoundException, IOFileException, InvalidPackageException {
    Package packageInfo = this.packageRepo.findByAppnameIgnoreCaseAndVersionIgnoreCase(appName, version);

    this.packageUtils.checkRepositoryResult(packageInfo, appName, version);
    this.packageUtils.checkPackageValidity(packageInfo);

    return this.fileIOUtils.loadPackageFile(packageInfo);
  }

  @Override
  public void installPackageFile(String packageName, String appName, String version, String fileName, byte[] file) throws IOFileException, WrongAppNameException {
    this.checkPackageAppName(packageName, appName, null);
    Package packageInfo = this.packageRepo.findByAppnameIgnoreCaseAndVersionIgnoreCase(appName, version);

    this.fileIOUtils.savePackageFile(appName, version, fileName, file, this.packageUtils);
    this.persistNewPackageInfo(packageInfo, packageName, appName, version, fileName);
  }

  @Override
  public PackageDTO installPackageFile(String fileName, MultipartFile multipartFile) throws IOFileException, AutoDetectionException, MissingFrameworkException, WrongAppNameException {
    byte[] file = this.fileIOUtils.getMultipartFileBytes(multipartFile);
    PackageDTO partialDTO = this.apkUtils.autodetectPackageInfo(file);

    this.checkPackageAppName(partialDTO.getPackageName(), partialDTO.getAppName(), null);

    Package packageInfo = this.packageRepo.findByAppnameIgnoreCaseAndVersionIgnoreCase(partialDTO.getAppName(), partialDTO.getAppVersion());

    this.fileIOUtils.savePackageFile(partialDTO.getAppName(), partialDTO.getAppVersion(), fileName, file, this.packageUtils);
    packageInfo = this.persistNewPackageInfo(packageInfo, partialDTO.getPackageName(), partialDTO.getAppName(), partialDTO.getAppVersion(), fileName);

    return this.packageUtils.composePackageDTOFromPackage(packageInfo);
  }

  @Override
  public void invalidatePackage(String appName, String version) throws PackageNotFoundException {
    Package packageInfo = this.packageRepo.findByAppnameIgnoreCaseAndVersionIgnoreCase(appName, version);

    this.packageUtils.checkRepositoryResult(packageInfo, appName, version);
    this.persistPackageInvalidation(packageInfo);
  }

  @Override
  public void deleteVersionPackage(String appName, String version) throws PackageNotFoundException {
    Package packageInfo = this.packageRepo.findByAppnameIgnoreCaseAndVersionIgnoreCase(appName, version);

    this.packageUtils.checkRepositoryResult(packageInfo, appName, version);
    this.packageRepo.deleteById(packageInfo.getId());
    this.fileIOUtils.deleteSingleFile(packageInfo);
  }

  @Override
  public void deleteAllVersions(String appName) throws PackageNotFoundException {
    List<Package> packages = this.packageRepo.findByAppnameIgnoreCase(appName);

    this.manageDeletion(packages, appName, true);
  }

  @Override
  public void deletePackagesList(List<Long> idList) throws PackageNotFoundException {
    Iterable<Package> packages = this.packageRepo.findAllById(idList);

    this.manageDeletion(packages, "", false);
  }

  private void manageDeletion(Iterable<Package> packages, String appName, boolean breakOnEmpty) throws PackageNotFoundException {
    this.packageUtils.checkRepositoryIterableResult(packages, appName, breakOnEmpty);

    if (StreamSupport.stream(packages.spliterator(), false).count() > 0) {
      this.packageRepo.deleteAll(packages);
      this.fileIOUtils.deletePackageList(packages);
    }
  }

  private void checkPackageAppName(String packageName, String appName, @Nullable String version) throws WrongAppNameException {
    List<Package> packageList = null;

    if (version != null) {
      packageList = this.packageRepo.findByPackagenameIgnoreCaseAndVersionIgnoreCaseAndAppnameIgnoreCaseNot(packageName, version, appName);
    } else {
      packageList = this.packageRepo.findByPackagenameIgnoreCaseAndAppnameIgnoreCaseNot(packageName, appName);
    }

    if (packageList != null && packageList.size() > 0) {
      throw new WrongAppNameException(
          String.format("%s %s", "This package already exists, with appName", packageList.get(0).getAppname()),
          appName,
          version);
    }
  }

  private void persistPackageInvalidation(Package packageInfo) {
    packageInfo.setValid(false);
    this.packageRepo.save(packageInfo);
  }

  private Package persistNewPackageInfo(Package packageInfo, String packageName, String appName, String version, String fileName) {
    try {
      this.packageUtils.checkRepositoryResult(packageInfo, appName, version);
    } catch (PackageNotFoundException e) {
      packageInfo = Package.builder()
          .appname(appName)
          .packagename(packageName)
          .version(version)
          .filename(fileName)
          .valid(false)
          .path(this.packageUtils.composeLocalRelativePath(appName, version, fileName))
          .build();
    }

    if (!packageInfo.isValid()) {
      packageInfo.setValid(true);
      packageInfo = this.packageRepo.save(packageInfo);
    }

    return packageInfo;
  }

}
