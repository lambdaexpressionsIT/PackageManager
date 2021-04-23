package com.lambda_expressions.package_manager.services;

import com.lambda_expressions.package_manager.exceptions.IOFileException;
import com.lambda_expressions.package_manager.exceptions.PackageNotFoundException;
import org.springframework.core.io.FileSystemResource;

/**
 * Created by steccothal
 * on Friday 23 April 2021
 * at 4:58 AM
 */
public interface WarehouseService {

  FileSystemResource getPackageFile(String appName, String version, String fileName) throws IOFileException, PackageNotFoundException;

}
