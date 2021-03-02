package com.lambda_expressions.package_manager.services;

import com.lambda_expressions.package_manager.exceptions.IOFileException;
import com.lambda_expressions.package_manager.exceptions.InvalidPackageException;
import com.lambda_expressions.package_manager.exceptions.PackageNotFoundException;
import com.lambda_expressions.package_manager.v1.model.PackageDTO;
import com.lambda_expressions.package_manager.v1.model.PackageListDTO;

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

  void installPackageFile(String packageName, String appName, String version, String fileName, byte[] file) throws IOFileException;

  void invalidatePackage(String appName, String version) throws PackageNotFoundException;

}
