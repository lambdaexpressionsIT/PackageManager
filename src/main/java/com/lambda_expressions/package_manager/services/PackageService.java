package com.lambda_expressions.package_manager.services;

import com.lambda_expressions.package_manager.exceptions.IOFileException;
import com.lambda_expressions.package_manager.exceptions.InvalidPackageException;
import com.lambda_expressions.package_manager.exceptions.PackageNotFoundException;
import com.lambda_expressions.package_manager.v1.model.PackageDTO;
import com.lambda_expressions.package_manager.v1.model.PackageListDTO;

import java.util.Collection;

/**
 * Created by steccothal
 * on Monday 18 January 2021
 * at 9:34 AM
 */
public interface PackageService {

  Collection<PackageListDTO> listAllPackages() throws PackageNotFoundException;

  PackageListDTO listAllVersions(String appName) throws PackageNotFoundException;

  PackageDTO getPackageInfo(String appName, int version) throws PackageNotFoundException;

  PackageDTO getPackageInfoById(long id) throws PackageNotFoundException;

  byte[] getPackageFile(String appName, int version) throws PackageNotFoundException, IOFileException, InvalidPackageException;

  void installPackageFile(String appName, int version, String fileName, byte[] file) throws IOFileException;

  void invalidatePackage(String appName, int version) throws PackageNotFoundException;

}
