package com.lambda_expressions.package_manager.services.impl;

import com.lambda_expressions.package_manager.domain.Package;
import com.lambda_expressions.package_manager.exceptions.IOFileException;
import com.lambda_expressions.package_manager.exceptions.InvalidPackageException;
import com.lambda_expressions.package_manager.exceptions.PackageNotFoundException;
import com.lambda_expressions.package_manager.repositories.PackageRepository;
import com.lambda_expressions.package_manager.services.PackageService;
import com.lambda_expressions.package_manager.services.utils.FileIOUtils;
import com.lambda_expressions.package_manager.services.utils.PackageUtils;
import com.lambda_expressions.package_manager.v1.model.PackageDTO;
import com.lambda_expressions.package_manager.v1.model.PackageListDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

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

  public PackageServiceImpl(PackageRepository packageRepo, PackageUtils packageUtils, FileIOUtils fileIOUtils) {
    this.packageRepo = packageRepo;
    this.packageUtils = packageUtils;
    this.fileIOUtils = fileIOUtils;
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

    return this.packageUtils.composePackageListDTOFromPackage(packages, appName);
  }

  @Override
  public PackageDTO getPackageInfo(String appName, int version) throws PackageNotFoundException {
    Package packageInfo = this.packageRepo.findByAppnameIgnoreCaseAndVersion(appName, version);

    this.packageUtils.checkRepositoryResult(packageInfo, appName, version);

    return this.packageUtils.composePackageDTOFromPackage(packageInfo);
  }

  @Override
  public PackageDTO getPackageInfoById(long id) throws PackageNotFoundException {
    return this.packageRepo.findById(id)
        .map(packageUtils::composePackageDTOFromPackage)
        .orElseThrow(() -> new PackageNotFoundException("ID not found", Long.toString(id), -1));
  }

  @Override
  public Collection<PackageListDTO> getPackagesById(List<Long> idList) throws PackageNotFoundException {
    Iterable<Package> packages = this.packageRepo.findAllById(idList);

    this.packageUtils.checkRepositoryIterableResult(packages, "", false);

    return this.packageUtils.composePackageListDTOFromPackageList(packages);
  }

  @Override
  public byte[] getPackageFile(String appName, int version) throws PackageNotFoundException, IOFileException, InvalidPackageException {
    Package packageInfo = this.packageRepo.findByAppnameIgnoreCaseAndVersion(appName, version);

    this.packageUtils.checkRepositoryResult(packageInfo, appName, version);
    this.packageUtils.checkPackageValidity(packageInfo);

    return this.fileIOUtils.loadPackageFile(packageInfo);
  }

  @Override
  public void installPackageFile(String appName, int version, String fileName, byte[] file) throws IOFileException {
    Package packageInfo = this.packageRepo.findByAppnameIgnoreCaseAndVersion(appName, version);

    this.fileIOUtils.savePackageFile(appName, version, fileName, file, this.packageUtils);
    this.persistNewPackageInfo(packageInfo, appName, version, fileName);
  }

  @Override
  public void invalidatePackage(String appName, int version) throws PackageNotFoundException {
    Package packageInfo = this.packageRepo.findByAppnameIgnoreCaseAndVersion(appName, version);

    this.packageUtils.checkRepositoryResult(packageInfo, appName, version);
    this.persistPackageInvalidation(packageInfo);
  }

  private void persistPackageInvalidation(Package packageInfo) {
    packageInfo.setValid(false);
    this.packageRepo.save(packageInfo);
  }

  private void persistNewPackageInfo(Package packageInfo, String appName, int version, String fileName) {
    try {
      this.packageUtils.checkRepositoryResult(packageInfo, appName, version);
    } catch (PackageNotFoundException e) {
      packageInfo = Package.builder()
          .appname(appName)
          .version(version)
          .filename(fileName)
          .valid(false)
          .path(this.packageUtils.composeLocalRelativePath(appName, version, fileName))
          .build();
    }

    if (!packageInfo.isValid()) {
      packageInfo.setValid(true);
      this.packageRepo.save(packageInfo);
    }
  }

}
