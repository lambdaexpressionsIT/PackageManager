package com.lambda_expressions.package_manager.services.impl;

import com.lambda_expressions.package_manager.domain.Package;
import com.lambda_expressions.package_manager.exceptions.IOFileException;
import com.lambda_expressions.package_manager.exceptions.PackageNotFoundException;
import com.lambda_expressions.package_manager.repositories.PackageRepository;
import com.lambda_expressions.package_manager.services.utils.FileIOUtils;
import com.lambda_expressions.package_manager.services.utils.PackageUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

/**
 * Created by steccothal
 * on Friday 23 April 2021
 * at 5:54 AM
 */
@ExtendWith(MockitoExtension.class)
class WarehouseServiceImplTest {

  private static final String PACKAGE_FILENAME = "fileName.apk";
  private static final String PACKAGE_PACKAGENAME = "com.appName";
  private static final String PACKAGE_APPNAME = "appName";
  private static final long PACKAGE_V1_ID = 100;
  private static final String PACKAGE_VERSION_1 = "1.1";
  private static final long PACKAGE_VERSION_NUMBER_1 = 1;
  private static final String PACKAGE_VERSION_2 = "2.4";

  private static final Package PACKAGE_V1_INFO = new Package(
      PACKAGE_V1_ID,
      PACKAGE_APPNAME,
      PACKAGE_FILENAME,
      PACKAGE_VERSION_1,
      PACKAGE_VERSION_NUMBER_1,
      PACKAGE_APPNAME + File.separator + PACKAGE_VERSION_1 + File.separator + PACKAGE_FILENAME,
      true,
      PACKAGE_PACKAGENAME
  );

  @Mock
  PackageRepository repository;

  @Mock
  FileIOUtils fileIOUtils;

  @Spy
  PackageUtils packageUtils;

  @InjectMocks
  WarehouseServiceImpl warehouseService;

  @Test
  void getPackageFile() throws IOFileException {
    //given
    given(repository.findByAppnameIgnoreCaseAndVersionIgnoreCaseAndFilenameIgnoreCase(anyString(), anyString(), anyString())).willReturn(PACKAGE_V1_INFO);
    //when
    //then
    assertDoesNotThrow(()->warehouseService.getPackageFile(PACKAGE_APPNAME, PACKAGE_VERSION_1, PACKAGE_FILENAME));
  }

  @Test
  void getPackageFileNotFound() {
    //given
    given(repository.findByAppnameIgnoreCaseAndVersionIgnoreCaseAndFilenameIgnoreCase(anyString(), anyString(), anyString())).willReturn(null);
    //when
    //then
    assertThrows(PackageNotFoundException.class, () -> warehouseService.getPackageFile(PACKAGE_APPNAME, PACKAGE_VERSION_2, PACKAGE_FILENAME));
  }

  @Test
  void getPackageFileUnreadable() throws IOFileException {
    //given
    given(repository.findByAppnameIgnoreCaseAndVersionIgnoreCaseAndFilenameIgnoreCase(anyString(), anyString(), anyString())).willReturn(PACKAGE_V1_INFO);
    given(fileIOUtils.loadPackageFileResource(any(Package.class))).willThrow(IOFileException.class);
    //when
    //then
    assertThrows(IOFileException.class, () -> warehouseService.getPackageFile(PACKAGE_APPNAME, PACKAGE_VERSION_1, PACKAGE_FILENAME));
  }

}