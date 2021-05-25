package com.lambda_expressions.package_manager.services;

import com.lambda_expressions.package_manager.exceptions.*;
import com.lambda_expressions.package_manager.v1.model.PackageDTO;
import com.lambda_expressions.package_manager.v1.model.PackageListDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;

/**
 * Created by steccothal
 * on Monday 18 January 2021
 * at 9:34 AM
 */
public interface PackageService {

  Collection<PackageListDTO> listAllPackages() throws PackageNotFoundException;

  PackageListDTO listAllVersions(String appName) throws PackageNotFoundException;

  PackageDTO getPackageInfo(String appName, String version) throws PackageNotFoundException;

  PackageDTO getPackageInfoById(long id) throws PackageNotFoundException;

  Collection<PackageListDTO> getPackagesById(List<Long> idList) throws PackageNotFoundException;

  byte[] getPackageFile(String appName, String version) throws PackageNotFoundException, IOFileException, InvalidPackageException;

  void installPackageFile(String packageName, String appName, long versionNumber, String version, String fileName, byte[] file) throws IOFileException, WrongAppNameException;

  PackageDTO installPackageFile(String filename, MultipartFile file) throws IOFileException, AutoDetectionException, MissingFrameworkException, WrongAppNameException;

  void invalidatePackage(String appName, String version) throws PackageNotFoundException;

  void deleteVersionPackage(String appName, String version) throws PackageNotFoundException;

  void deleteAllVersions(String appName) throws PackageNotFoundException;

  void deletePackagesList(List<Long> idList) throws PackageNotFoundException;
}
