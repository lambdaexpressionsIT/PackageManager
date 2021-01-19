package com.lambda_expressions.package_manager.services;

import com.lambda_expressions.package_manager.exceptions.InvalidPackageException;
import com.lambda_expressions.package_manager.exceptions.PackageNotFoundException;
import com.lambda_expressions.package_manager.exceptions.IOFileException;
import com.lambda_expressions.package_manager.v1.model.PackageDTO;

import java.util.List;

/**
 * Created by steccothal
 * on Monday 18 January 2021
 * at 9:34 AM
 */
public interface PackageService {

  List<PackageDTO> listAllPackages() throws PackageNotFoundException;

  List<PackageDTO> listAllVersions(String appName) throws PackageNotFoundException;

  PackageDTO getPackageInfo(String appName, int version) throws PackageNotFoundException;

  byte[] getPackageFile(String appName, int version) throws PackageNotFoundException, IOFileException, InvalidPackageException;

  void installPackageFile(String appName, int version, byte[] file) throws  IOFileException;

  void invalidatePackage(String appName, int version) throws PackageNotFoundException;

}
