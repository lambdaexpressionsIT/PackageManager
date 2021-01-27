package com.lambda_expressions.package_manager.services.impl;

import com.lambda_expressions.package_manager.domain.Package;
import com.lambda_expressions.package_manager.exceptions.IOFileException;
import com.lambda_expressions.package_manager.exceptions.InvalidPackageException;
import com.lambda_expressions.package_manager.exceptions.PackageNotFoundException;
import com.lambda_expressions.package_manager.repositories.PackageRepository;
import com.lambda_expressions.package_manager.services.utils.FileIOUtils;
import com.lambda_expressions.package_manager.services.utils.PackageUtils;
import com.lambda_expressions.package_manager.v1.model.PackageDTO;
import com.lambda_expressions.package_manager.v1.model.PackageListDTO;
import com.lambda_expressions.package_manager.v1.model.VersionDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Created by steccothal
 * on Saturday 23 January 2021
 * at 2:08 PM
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
class PackageServiceImplTest {

  private static final String PACKAGES_FILE_EXTENSION = "bib";
  private static final String PACKAGES_WEBSERVER_BASEURL = "http://bob.bub";
  private static final String PACKAGE_FILENAME = "fileName";
  private static final String PACKAGE_APPNAME = "appName";
  private static final long PACKAGE_V1_ID = 100;
  private static final long PACKAGE_V2_ID = 200;
  private static final int PACKAGE_VERSION_1 = 1;
  private static final int PACKAGE_VERSION_2 = 2;
  private static final byte[] DUMMY_BYTE_ARRAY = "Some dummy string converted to byte array".getBytes();

  private static final Package PACKAGE_V1_INFO = Package.builder()
      .id(PACKAGE_V1_ID)
      .filename(PACKAGE_FILENAME)
      .path(PACKAGE_APPNAME + File.separator + PACKAGE_VERSION_1 + File.separator + PACKAGE_FILENAME + "." + PACKAGES_FILE_EXTENSION)
      .valid(true)
      .version(PACKAGE_VERSION_1)
      .appname(PACKAGE_APPNAME)
      .build();

  private static final Package PACKAGE_V2_INFO = Package.builder()
      .id(PACKAGE_V2_ID)
      .filename(PACKAGE_FILENAME)
      .path(PACKAGE_APPNAME + File.separator + PACKAGE_VERSION_2 + File.separator + PACKAGE_FILENAME + "." + PACKAGES_FILE_EXTENSION)
      .valid(true)
      .version(PACKAGE_VERSION_2)
      .appname(PACKAGE_APPNAME)
      .build();

  private static final PackageDTO PACKAGE_DTO = PackageDTO.builder()
      .id(PACKAGE_V2_ID)
      .fileName(PACKAGE_FILENAME)
      .url(PACKAGES_WEBSERVER_BASEURL + "/" + PACKAGE_APPNAME + "/" + PACKAGE_VERSION_2 + "/" + PACKAGE_FILENAME + "." + PACKAGES_FILE_EXTENSION)
      .valid(true)
      .appVersion(PACKAGE_VERSION_2)
      .appName(PACKAGE_APPNAME)
      .build();

  private static final VersionDTO VERSION_1_DTO = VersionDTO.builder()
      .id(PACKAGE_V1_ID)
      .fileName(PACKAGE_FILENAME)
      .url((PACKAGES_WEBSERVER_BASEURL + "/" + PACKAGE_APPNAME + "/" + PACKAGE_VERSION_1 + "/" + PACKAGE_FILENAME + "." + PACKAGES_FILE_EXTENSION))
      .valid(true)
      .appVersion(PACKAGE_VERSION_1)
      .build();

  private static final VersionDTO VERSION_2_DTO = VersionDTO.builder()
      .id(PACKAGE_V2_ID)
      .fileName(PACKAGE_FILENAME)
      .url((PACKAGES_WEBSERVER_BASEURL + "/" + PACKAGE_APPNAME + "/" + PACKAGE_VERSION_2 + "/" + PACKAGE_FILENAME + "." + PACKAGES_FILE_EXTENSION))
      .valid(true)
      .appVersion(PACKAGE_VERSION_2)
      .build();

  @Mock
  PackageRepository repository;

  @Mock
  FileIOUtils fileIOUtils;

  @Spy
  PackageUtils packageUtils;

  @InjectMocks
  PackageServiceImpl packageService;

  @BeforeEach
  public void setUp() {
//    ReflectionTestUtils.setField(packageUtils, "PACKAGES_FILESYSTEM_BASELOCATION", PACKAGES_FILESYSTEM_BASELOCATION);
    ReflectionTestUtils.setField(packageUtils, "PACKAGES_WEBSERVER_BASEURL", PACKAGES_WEBSERVER_BASEURL);
    ReflectionTestUtils.setField(packageUtils, "PACKAGES_FILE_EXTENSION", PACKAGES_FILE_EXTENSION);
  }

  @Test
  void listAllPackages() throws PackageNotFoundException {
    List<Package> packages = new ArrayList<>();

    packages.add(PACKAGE_V1_INFO);
    packages.add(PACKAGE_V2_INFO);

    //given
    given(repository.findAll()).willReturn(packages);
    //when
    Collection<PackageListDTO> listDTOS = packageService.listAllPackages();
    //then
    assertEquals(listDTOS.size(), 1);
    assertEquals(listDTOS.iterator().next().getAppName(), PACKAGE_V1_INFO.getAppname());
    assertEquals(listDTOS.iterator().next().getVersions().size(), 2);
    assertEquals(listDTOS.iterator().next().getVersions().get(0).getId(), VERSION_1_DTO.getId());
    assertEquals(listDTOS.iterator().next().getVersions().get(0).isValid(), VERSION_1_DTO.isValid());
    assertEquals(listDTOS.iterator().next().getVersions().get(0).getAppVersion(), VERSION_1_DTO.getAppVersion());
    assertEquals(listDTOS.iterator().next().getVersions().get(0).getFileName(), VERSION_1_DTO.getFileName());
    assertEquals(listDTOS.iterator().next().getVersions().get(0).getUrl(), VERSION_1_DTO.getUrl());
    assertEquals(listDTOS.iterator().next().getVersions().get(1).getId(), VERSION_2_DTO.getId());
    assertEquals(listDTOS.iterator().next().getVersions().get(1).isValid(), VERSION_2_DTO.isValid());
    assertEquals(listDTOS.iterator().next().getVersions().get(1).getAppVersion(), VERSION_2_DTO.getAppVersion());
    assertEquals(listDTOS.iterator().next().getVersions().get(1).getFileName(), VERSION_2_DTO.getFileName());
    assertEquals(listDTOS.iterator().next().getVersions().get(1).getUrl(), VERSION_2_DTO.getUrl());
  }

  @Test
  void listAllPackagesEmpty() throws PackageNotFoundException {
    List<Package> emptyList = new ArrayList<>();
    //given
    given(repository.findAll()).willReturn(emptyList);
    //when
    Collection<PackageListDTO> listDTOS = packageService.listAllPackages();
    //then
    assertEquals(listDTOS.size(), 0);
  }

  @Test
  void listAllVersions() throws PackageNotFoundException {
    List<Package> packages = new ArrayList<>();

    packages.add(PACKAGE_V1_INFO);
    packages.add(PACKAGE_V2_INFO);

    //given
    given(repository.findByAppnameIgnoreCase(anyString())).willReturn(packages);
    //when
    PackageListDTO packageDTO = packageService.listAllVersions(PACKAGE_APPNAME);
    //then
    assertEquals(packageDTO.getAppName(), PACKAGE_DTO.getAppName());
    assertEquals(packageDTO.getVersions().size(), 2);
    assertEquals(packageDTO.getVersions().get(0).getId(), VERSION_1_DTO.getId());
    assertEquals(packageDTO.getVersions().get(0).isValid(), VERSION_1_DTO.isValid());
    assertEquals(packageDTO.getVersions().get(0).getAppVersion(), VERSION_1_DTO.getAppVersion());
    assertEquals(packageDTO.getVersions().get(0).getFileName(), VERSION_1_DTO.getFileName());
    assertEquals(packageDTO.getVersions().get(0).getUrl(), VERSION_1_DTO.getUrl());
    assertEquals(packageDTO.getVersions().get(1).getId(), VERSION_2_DTO.getId());
    assertEquals(packageDTO.getVersions().get(1).isValid(), VERSION_2_DTO.isValid());
    assertEquals(packageDTO.getVersions().get(1).getAppVersion(), VERSION_2_DTO.getAppVersion());
    assertEquals(packageDTO.getVersions().get(1).getFileName(), VERSION_2_DTO.getFileName());
    assertEquals(packageDTO.getVersions().get(1).getUrl(), VERSION_2_DTO.getUrl());
  }

  @Test
  void listAllVersionsEmpty() {
    //given
    given(repository.findByAppnameIgnoreCase(anyString())).willReturn(null);
    //when
    //then
    assertThrows(PackageNotFoundException.class, () -> packageService.listAllVersions(PACKAGE_APPNAME));
  }

  @Test
  void getPackageInfo() throws PackageNotFoundException {
    //given
    given(repository.findByAppnameIgnoreCaseAndVersion(anyString(), anyInt())).willReturn(PACKAGE_V2_INFO);
    //when
    PackageDTO packageDTO = packageService.getPackageInfo(PACKAGE_APPNAME, PACKAGE_VERSION_2);
    //then
    assertEquals(packageDTO.getId(), PACKAGE_DTO.getId());
    assertEquals(packageDTO.getAppName(), PACKAGE_DTO.getAppName());
    assertEquals(packageDTO.isValid(), PACKAGE_DTO.isValid());
    assertEquals(packageDTO.getAppVersion(), PACKAGE_DTO.getAppVersion());
    assertEquals(packageDTO.getFileName(), PACKAGE_DTO.getFileName());
    assertEquals(packageDTO.getUrl(), PACKAGE_DTO.getUrl());
  }

  @Test
  void getPackageInfoNotFound() {
    //given
    given(repository.findByAppnameIgnoreCaseAndVersion(anyString(), anyInt())).willReturn(null);
    //when
    //then
    assertThrows(PackageNotFoundException.class, () -> packageService.getPackageInfo(PACKAGE_APPNAME, PACKAGE_VERSION_2));
  }

  @Test
  void getPackagesById() throws PackageNotFoundException {
    List<Package> packageList = new ArrayList<>();
    Package anotherPackage = Package.builder()
        .id(PACKAGE_V2_ID)
        .filename("anotherAppName")
        .path("anotherAppName" + File.separator + PACKAGE_VERSION_2 + File.separator + "anotherAppName" + "." + PACKAGES_FILE_EXTENSION)
        .valid(true)
        .version(PACKAGE_VERSION_2)
        .appname("anotherAppName")
        .build();
    String anotherPackageDTOUrl = (PACKAGES_WEBSERVER_BASEURL + "/" + "anotherAppName" + "/" +
        PACKAGE_VERSION_2 + "/" + "anotherAppName" + "." + PACKAGES_FILE_EXTENSION);

    packageList.add(PACKAGE_V1_INFO);
    packageList.add(PACKAGE_V2_INFO);
    packageList.add(anotherPackage);
    //given
    given(repository.findAllById(anyList())).willReturn(packageList);
    //when
    List<PackageListDTO> listDTOS = new ArrayList<>(packageService.getPackagesById(anyList()));
    //then
    assertEquals(listDTOS.size(), 2);
    assertEquals(listDTOS.get(0).getAppName(), anotherPackage.getAppname());
    assertEquals(listDTOS.get(0).getVersions().size(), 1);
    assertEquals(listDTOS.get(0).getVersions().get(0).getId(), anotherPackage.getId());
    assertEquals(listDTOS.get(0).getVersions().get(0).isValid(), anotherPackage.isValid());
    assertEquals(listDTOS.get(0).getVersions().get(0).getAppVersion(), anotherPackage.getVersion());
    assertEquals(listDTOS.get(0).getVersions().get(0).getFileName(), anotherPackage.getFilename());
    assertEquals(listDTOS.get(0).getVersions().get(0).getUrl(), anotherPackageDTOUrl);
    assertEquals(listDTOS.get(1).getAppName(), PACKAGE_V1_INFO.getAppname());
    assertEquals(listDTOS.get(1).getVersions().size(), 2);
    assertEquals(listDTOS.get(1).getVersions().get(0).getId(), VERSION_1_DTO.getId());
    assertEquals(listDTOS.get(1).getVersions().get(0).isValid(), VERSION_1_DTO.isValid());
    assertEquals(listDTOS.get(1).getVersions().get(0).getAppVersion(), VERSION_1_DTO.getAppVersion());
    assertEquals(listDTOS.get(1).getVersions().get(0).getFileName(), VERSION_1_DTO.getFileName());
    assertEquals(listDTOS.get(1).getVersions().get(0).getUrl(), VERSION_1_DTO.getUrl());
    assertEquals(listDTOS.get(1).getVersions().get(1).getId(), VERSION_2_DTO.getId());
    assertEquals(listDTOS.get(1).getVersions().get(1).isValid(), VERSION_2_DTO.isValid());
    assertEquals(listDTOS.get(1).getVersions().get(1).getAppVersion(), VERSION_2_DTO.getAppVersion());
    assertEquals(listDTOS.get(1).getVersions().get(1).getFileName(), VERSION_2_DTO.getFileName());
    assertEquals(listDTOS.get(1).getVersions().get(1).getUrl(), VERSION_2_DTO.getUrl());
  }

  @Test
  void getPackagesByIDNotFound() throws PackageNotFoundException {
    List<Package> emptyList = new ArrayList<>();
    //given
    given(repository.findAllById(anyList())).willReturn(emptyList);
    //when
    Collection<PackageListDTO> listDTOS = packageService.getPackagesById(anyList());
    //then
    assertEquals(listDTOS.size(), 0);
  }

  @Test
  void getPackageInfoById() throws PackageNotFoundException {
    //given
    given(repository.findById(anyLong())).willReturn(Optional.of(PACKAGE_V2_INFO));
    //when
    PackageDTO packageDTO = packageService.getPackageInfoById(PACKAGE_V2_ID);
    //then
    assertEquals(packageDTO.getId(), PACKAGE_DTO.getId());
    assertEquals(packageDTO.getAppName(), PACKAGE_DTO.getAppName());
    assertEquals(packageDTO.isValid(), PACKAGE_DTO.isValid());
    assertEquals(packageDTO.getAppVersion(), PACKAGE_DTO.getAppVersion());
    assertEquals(packageDTO.getFileName(), PACKAGE_DTO.getFileName());
    assertEquals(packageDTO.getUrl(), PACKAGE_DTO.getUrl());
  }

  @Test
  void getPackageInfoByIdNotFound() {
    //given
    given(repository.findById(anyLong())).willReturn(Optional.empty());
    //when
    //then
    assertThrows(PackageNotFoundException.class, () -> packageService.getPackageInfoById(PACKAGE_V2_ID));
  }

  @Test
  void invalidatePackage() throws PackageNotFoundException {
    Package packageToInvalidate = Package.builder()
        .filename(PACKAGE_FILENAME)
        .path(PACKAGE_APPNAME + File.separator + PACKAGE_VERSION_1 + File.separator + PACKAGE_FILENAME + "." + PACKAGES_FILE_EXTENSION)
        .valid(true)
        .version(PACKAGE_VERSION_1)
        .appname(PACKAGE_APPNAME)
        .build();
    //given
    given(repository.findByAppnameIgnoreCaseAndVersion(anyString(), anyInt())).willReturn(packageToInvalidate);
    //when
    packageService.invalidatePackage(PACKAGE_APPNAME, PACKAGE_VERSION_2);
    //then
    assertFalse(packageToInvalidate.isValid());
    verify(repository, times(1)).save(packageToInvalidate);
  }

  @Test
  void invalidatePackageNotFound() {
    //given
    given(repository.findByAppnameIgnoreCaseAndVersion(anyString(), anyInt())).willReturn(null);
    //when
    //then
    assertThrows(PackageNotFoundException.class, () -> packageService.invalidatePackage(PACKAGE_APPNAME, PACKAGE_VERSION_2));
  }

  @Test
  void getPackageFile() throws IOFileException, PackageNotFoundException, InvalidPackageException {
    //given
    given(repository.findByAppnameIgnoreCaseAndVersion(anyString(), anyInt())).willReturn(PACKAGE_V1_INFO);
    given(fileIOUtils.loadPackageFile(any(Package.class))).willReturn(DUMMY_BYTE_ARRAY);
    //when
    //then
    assertEquals(packageService.getPackageFile(PACKAGE_APPNAME, PACKAGE_VERSION_1), DUMMY_BYTE_ARRAY);
  }

  @Test
  void getPackageFileNotFound() {
    //given
    given(repository.findByAppnameIgnoreCaseAndVersion(anyString(), anyInt())).willReturn(null);
    //when
    //then
    assertThrows(PackageNotFoundException.class, () -> packageService.getPackageFile(PACKAGE_APPNAME, PACKAGE_VERSION_2));
  }

  @Test
  void getPackageFileInvalid() {
    Package invalidPackage = Package.builder()
        .filename(PACKAGE_FILENAME)
        .path(PACKAGE_APPNAME + File.separator + PACKAGE_VERSION_1 + File.separator + PACKAGE_FILENAME + "." + PACKAGES_FILE_EXTENSION)
        .valid(false)
        .version(PACKAGE_VERSION_1)
        .appname(PACKAGE_APPNAME)
        .build();
    //given
    given(repository.findByAppnameIgnoreCaseAndVersion(anyString(), anyInt())).willReturn(invalidPackage);
    //when
    //then
    assertThrows(InvalidPackageException.class, () -> packageService.getPackageFile(PACKAGE_APPNAME, PACKAGE_VERSION_1));
  }

  @Test
  void getPackageFileUnreadable() throws IOFileException {
    //given
    given(repository.findByAppnameIgnoreCaseAndVersion(anyString(), anyInt())).willReturn(PACKAGE_V1_INFO);
    given(fileIOUtils.loadPackageFile(any(Package.class))).willThrow(IOFileException.class);
    //when
    //then
    assertThrows(IOFileException.class, () -> packageService.getPackageFile(PACKAGE_APPNAME, PACKAGE_VERSION_1));
  }

  @Test
  void installPackageFile() throws IOFileException {
    ArgumentCaptor<Package> packageCaptor = ArgumentCaptor.forClass(Package.class);
    ArgumentCaptor<byte[]> fileCaptor = ArgumentCaptor.forClass(byte[].class);
    //given
    //when
    packageService.installPackageFile(PACKAGE_APPNAME, PACKAGE_VERSION_1, PACKAGE_FILENAME, DUMMY_BYTE_ARRAY);
    //then
    verify(repository, times(1)).save(packageCaptor.capture());
    assertEquals(packageCaptor.getValue().getAppname(), PACKAGE_APPNAME);
    assertEquals(packageCaptor.getValue().getVersion(), PACKAGE_VERSION_1);
    assertEquals(packageCaptor.getValue().getFilename(), PACKAGE_FILENAME);
    assertEquals(packageCaptor.getValue().getPath(), PACKAGE_V1_INFO.getPath());
    assertTrue(packageCaptor.getValue().isValid());

    verify(fileIOUtils, times(1)).savePackageFile(anyString(), anyInt(), anyString(), fileCaptor.capture(), any(PackageUtils.class));
    assertEquals(fileCaptor.getValue(), DUMMY_BYTE_ARRAY);
  }

  @Test
  void installPackageFileIOException() throws IOFileException {
    //given
    //when
    doThrow(IOFileException.class).when(fileIOUtils).savePackageFile(anyString(), anyInt(), anyString(), any(byte[].class), any(PackageUtils.class));
    //then
    assertThrows(IOFileException.class, () -> packageService.installPackageFile(PACKAGE_APPNAME, PACKAGE_VERSION_1, PACKAGE_FILENAME, DUMMY_BYTE_ARRAY));
  }
}