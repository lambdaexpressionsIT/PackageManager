package com.lambda_expressions.package_manager.services.impl;

import com.lambda_expressions.package_manager.domain.Package;
import com.lambda_expressions.package_manager.exceptions.IOFileException;
import com.lambda_expressions.package_manager.exceptions.PackageNotFoundException;
import com.lambda_expressions.package_manager.repositories.PackageRepository;
import com.lambda_expressions.package_manager.services.WarehouseService;
import com.lambda_expressions.package_manager.services.utils.FileIOUtils;
import com.lambda_expressions.package_manager.services.utils.PackageUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

/**
 * Created by steccothal
 * on Friday 23 April 2021
 * at 5:01 AM
 */
@Service
public class WarehouseServiceImpl implements WarehouseService {

  private PackageRepository packageRepository;
  private FileIOUtils fileIOUtils;
  private PackageUtils packageUtils;

  public WarehouseServiceImpl(PackageRepository packageRepository, FileIOUtils fileIOUtils, PackageUtils packageUtils) {
    this.packageRepository = packageRepository;
    this.fileIOUtils = fileIOUtils;
    this.packageUtils = packageUtils;
  }

  @Override
  public FileSystemResource getPackageFile(String appName, String version, String fileName) throws IOFileException, PackageNotFoundException {
    Package packageInfo = this.packageRepository.findByAppnameIgnoreCaseAndVersionIgnoreCaseAndFilenameIgnoreCase(appName, version, fileName);
    this.packageUtils.checkRepositoryResult(packageInfo, appName, version);

    return fileIOUtils.loadPackageFileResource(packageInfo);
  }
}
