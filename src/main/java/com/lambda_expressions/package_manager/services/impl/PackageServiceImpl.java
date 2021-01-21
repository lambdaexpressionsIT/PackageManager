package com.lambda_expressions.package_manager.services.impl;

import com.lambda_expressions.package_manager.domain.Package;
import com.lambda_expressions.package_manager.exceptions.InvalidPackageException;
import com.lambda_expressions.package_manager.exceptions.PackageNotFoundException;
import com.lambda_expressions.package_manager.exceptions.IOFileException;
import com.lambda_expressions.package_manager.repositories.PackageRepository;
import com.lambda_expressions.package_manager.services.PackageService;
import com.lambda_expressions.package_manager.services.utils.PackageUtils;
import com.lambda_expressions.package_manager.v1.model.PackageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

  public PackageServiceImpl(PackageRepository packageRepo, PackageUtils packageUtils) {
    this.packageRepo = packageRepo;
    this.packageUtils = packageUtils;
  }

  @Override
  public List<PackageDTO> listAllPackages(){
    Iterable<Package> packages = this.packageRepo.findAll();
    List<PackageDTO> packagesList = new ArrayList<>();

    try {
      this.packageUtils.checkRepositoryResult(packages, "", -1)
          .forEach(packageInfo -> packagesList.add(this.packageUtils.composeDTOFromPackage(packageInfo)));
    }catch (PackageNotFoundException e){
      log.info("No packages found in DB");
    }

    return packagesList;
  }

  @Override
  public List<PackageDTO> listAllVersions(String appName) throws PackageNotFoundException {
    List<Package> packages = this.packageRepo.findByAppnameIgnoreCase(appName);

    return this.packageUtils.checkDataRetrieved(packages, appName, -1).stream()
        .map(pInfo->this.packageUtils.composeDTOFromPackage(pInfo)).collect(Collectors.toList());
  }

  @Override
  public PackageDTO getPackageInfo(String appName, int version) throws PackageNotFoundException {
    Package packageInfo = this.packageRepo.findByAppnameIgnoreCaseAndVersion(appName, version);

    this.packageUtils.checkRepositoryResult(packageInfo, appName, version);

    return this.packageUtils.composeDTOFromPackage(packageInfo);
  }

  @Override
  public byte[] getPackageFile(String appName, int version) throws PackageNotFoundException, IOFileException, InvalidPackageException {
    Package packageInfo = this.packageRepo.findByAppnameIgnoreCaseAndVersion(appName, version);

    this.packageUtils.checkRepositoryResult(packageInfo, appName, version);
    this.packageUtils.checkPackageValidity(packageInfo);

    return this.packageUtils.loadPackageFile(packageInfo);
  }

  @Override
  public void installPackageFile(String appName, int version, byte[] file) throws IOFileException {
    Package packageInfo = this.packageRepo.findByAppnameIgnoreCaseAndVersion(appName, version);

    this.packageUtils.savePackageFile(appName, version, file);
    this.persistNewPackageInfo(packageInfo, appName, version);
  }

  @Override
  public void invalidatePackage(String appName, int version) throws PackageNotFoundException {
    Package packageInfo = this.packageRepo.findByAppnameIgnoreCaseAndVersion(appName, version);

    this.packageUtils.checkRepositoryResult(packageInfo, appName, version);
    this.persistPackageInvalidation(packageInfo);
  }

  private void persistPackageInvalidation(Package packageInfo){
    packageInfo.setValid(false);
    this.packageRepo.save(packageInfo);
  }

  private void persistNewPackageInfo(Package packageInfo, String appName, int version){
    try{
      this.packageUtils.checkRepositoryResult(packageInfo, appName, version);
    } catch (PackageNotFoundException e){
      packageInfo = Package.builder()
          .appname(appName)
          .version(version)
          .valid(false)
          .path(this.packageUtils.composeLocalRelativePath(appName, version))
          .build();
    }

    if(!packageInfo.isValid()){
      packageInfo.setValid(true);
      this.packageRepo.save(packageInfo);
    }
  }

}
