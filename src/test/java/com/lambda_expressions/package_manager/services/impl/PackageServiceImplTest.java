package com.lambda_expressions.package_manager.services.impl;

import com.lambda_expressions.package_manager.domain.Package;
import com.lambda_expressions.package_manager.exceptions.*;
import com.lambda_expressions.package_manager.repositories.PackageRepository;
import com.lambda_expressions.package_manager.services.utils.APKUtils;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;

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

  private static final String PACKAGES_WAREHOUSE_BASEURL = "http://localhost/warehouse";
  private static final String PACKAGE_FILENAME = "fileName.apk";
  private static final String PACKAGE_PACKAGENAME = "com.appName";
  private static final String PACKAGE_APPNAME = "appName";
  private static final long PACKAGE_V1_ID = 100;
  private static final long PACKAGE_V2_ID = 200;
  private static final String PACKAGE_VERSION_1 = "1.1";
  private static final String PACKAGE_VERSION_2 = "2.4";
  private static final long PACKAGE_VERSION_NUMBER_1 = 1;
  private static final long PACKAGE_VERSION_NUMBER_2 = 2;
  private static final byte[] DUMMY_BYTE_ARRAY = "Some dummy string converted to byte array".getBytes();

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

  private static final Package PACKAGE_V2_INFO = new Package(
      PACKAGE_V2_ID,
      PACKAGE_APPNAME,
      PACKAGE_FILENAME,
      PACKAGE_VERSION_2,
      PACKAGE_VERSION_NUMBER_2,
      PACKAGE_APPNAME + File.separator + PACKAGE_VERSION_2 + File.separator + PACKAGE_FILENAME,
      true,
      PACKAGE_PACKAGENAME
  );

  private static final PackageDTO PACKAGE_DTO = new PackageDTO(
      PACKAGE_V2_ID,
      PACKAGE_VERSION_2,
      PACKAGE_VERSION_NUMBER_2,
      PACKAGE_FILENAME,
      PACKAGES_WAREHOUSE_BASEURL + "/" + PACKAGE_APPNAME + "/" + PACKAGE_VERSION_2 + "/" + PACKAGE_FILENAME,
      true,
      PACKAGE_APPNAME,
      PACKAGE_PACKAGENAME
  );

  private static final VersionDTO VERSION_1_DTO = new VersionDTO(
      PACKAGE_V1_ID,
      PACKAGE_VERSION_1,
      PACKAGE_VERSION_NUMBER_1,
      PACKAGE_FILENAME,
      PACKAGES_WAREHOUSE_BASEURL + "/" + PACKAGE_APPNAME + "/" + PACKAGE_VERSION_1 + "/" + PACKAGE_FILENAME,
      true
  );

  private static final VersionDTO VERSION_2_DTO = new VersionDTO(
      PACKAGE_V2_ID,
      PACKAGE_VERSION_2,
      PACKAGE_VERSION_NUMBER_2,
      PACKAGE_FILENAME,
      PACKAGES_WAREHOUSE_BASEURL + "/" + PACKAGE_APPNAME + "/" + PACKAGE_VERSION_2 + "/" + PACKAGE_FILENAME,
      true
  );

  @Mock
  PackageRepository repository;

  @Mock
  FileIOUtils fileIOUtils;

  @Mock
  APKUtils apkUtils;

  @Spy
  PackageUtils packageUtils;

  @InjectMocks
  PackageServiceImpl packageService;

  @BeforeEach
  public void setUp() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
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
    assertEquals(listDTOS.iterator().next().getPackageName(), PACKAGE_V1_INFO.getPackagename());
    assertEquals(listDTOS.iterator().next().getVersions().size(), 2);
    assertEquals(listDTOS.iterator().next().getVersions().get(0).getId(), VERSION_1_DTO.getId());
    assertEquals(listDTOS.iterator().next().getVersions().get(0).isValid(), VERSION_1_DTO.isValid());
    assertEquals(listDTOS.iterator().next().getVersions().get(0).getAppVersion(), VERSION_1_DTO.getAppVersion());
    assertEquals(listDTOS.iterator().next().getVersions().get(0).getAppVersionNumber(), VERSION_1_DTO.getAppVersionNumber());
    assertEquals(listDTOS.iterator().next().getVersions().get(0).getFileName(), VERSION_1_DTO.getFileName());
    assertEquals(listDTOS.iterator().next().getVersions().get(0).getUrl(), VERSION_1_DTO.getUrl());
    assertEquals(listDTOS.iterator().next().getVersions().get(1).getId(), VERSION_2_DTO.getId());
    assertEquals(listDTOS.iterator().next().getVersions().get(1).isValid(), VERSION_2_DTO.isValid());
    assertEquals(listDTOS.iterator().next().getVersions().get(1).getAppVersion(), VERSION_2_DTO.getAppVersion());
    assertEquals(listDTOS.iterator().next().getVersions().get(1).getAppVersionNumber(), VERSION_2_DTO.getAppVersionNumber());
    assertEquals(listDTOS.iterator().next().getVersions().get(1).getFileName(), VERSION_2_DTO.getFileName());
    assertEquals(listDTOS.iterator().next().getVersions().get(1).getUrl(), VERSION_2_DTO.getUrl());
  }

  @Test
  void listAllPackagesEmpty() throws PackageNotFoundException {
    //given
    given(repository.findAll()).willReturn(Collections.EMPTY_LIST);
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
    assertEquals(packageDTO.getPackageName(), PACKAGE_DTO.getPackageName());
    assertEquals(packageDTO.getVersions().size(), 2);
    assertEquals(packageDTO.getVersions().get(0).getId(), VERSION_1_DTO.getId());
    assertEquals(packageDTO.getVersions().get(0).isValid(), VERSION_1_DTO.isValid());
    assertEquals(packageDTO.getVersions().get(0).getAppVersion(), VERSION_1_DTO.getAppVersion());
    assertEquals(packageDTO.getVersions().get(0).getAppVersionNumber(), VERSION_1_DTO.getAppVersionNumber());
    assertEquals(packageDTO.getVersions().get(0).getFileName(), VERSION_1_DTO.getFileName());
    assertEquals(packageDTO.getVersions().get(0).getUrl(), VERSION_1_DTO.getUrl());
    assertEquals(packageDTO.getVersions().get(1).getId(), VERSION_2_DTO.getId());
    assertEquals(packageDTO.getVersions().get(1).isValid(), VERSION_2_DTO.isValid());
    assertEquals(packageDTO.getVersions().get(1).getAppVersion(), VERSION_2_DTO.getAppVersion());
    assertEquals(packageDTO.getVersions().get(1).getAppVersionNumber(), VERSION_2_DTO.getAppVersionNumber());
    assertEquals(packageDTO.getVersions().get(1).getFileName(), VERSION_2_DTO.getFileName());
    assertEquals(packageDTO.getVersions().get(1).getUrl(), VERSION_2_DTO.getUrl());
  }

  @Test
  void listAllVersionsEmpty() {
    //given
    given(repository.findByAppnameIgnoreCase(anyString())).willReturn(Collections.EMPTY_LIST);
    //when
    //then
    assertThrows(PackageNotFoundException.class, () -> packageService.listAllVersions(PACKAGE_APPNAME));
  }

  @Test
  void getPackageInfo() throws PackageNotFoundException {
    //given
    given(repository.findByAppnameIgnoreCaseAndVersionIgnoreCase(anyString(), anyString())).willReturn(PACKAGE_V2_INFO);
    //when
    PackageDTO packageDTO = packageService.getPackageInfo(PACKAGE_APPNAME, PACKAGE_VERSION_2);
    //then
    assertEquals(packageDTO.getId(), PACKAGE_DTO.getId());
    assertEquals(packageDTO.getAppName(), PACKAGE_DTO.getAppName());
    assertEquals(packageDTO.getPackageName(), PACKAGE_DTO.getPackageName());
    assertEquals(packageDTO.isValid(), PACKAGE_DTO.isValid());
    assertEquals(packageDTO.getAppVersion(), PACKAGE_DTO.getAppVersion());
    assertEquals(packageDTO.getAppVersionNumber(), PACKAGE_DTO.getAppVersionNumber());
    assertEquals(packageDTO.getFileName(), PACKAGE_DTO.getFileName());
    assertEquals(packageDTO.getUrl(), PACKAGE_DTO.getUrl());
  }

  @Test
  void getPackageInfoNotFound() {
    //given
    given(repository.findByAppnameIgnoreCaseAndVersionIgnoreCase(anyString(), anyString())).willReturn(null);
    //when
    //then
    assertThrows(PackageNotFoundException.class, () -> packageService.getPackageInfo(PACKAGE_APPNAME, PACKAGE_VERSION_2));
  }

  @Test
  void getPackagesById() throws PackageNotFoundException {
    List<Package> packageList = new ArrayList<>();
    Package anotherPackage = new Package(
        PACKAGE_V2_ID,
        "anotherAppName",
        "anotherAppName.apk",
        PACKAGE_VERSION_2,
        PACKAGE_VERSION_NUMBER_2,
        "anotherAppName" + File.separator + PACKAGE_VERSION_2 + File.separator + "anotherAppName.apk",
        true,
        "com.package.anotherAppName"
    );

    String anotherPackageDTOUrl = (PACKAGES_WAREHOUSE_BASEURL + "/" + "anotherAppName" + "/" +
        PACKAGE_VERSION_2 + "/" + "anotherAppName.apk");

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
    assertEquals(listDTOS.get(0).getPackageName(), anotherPackage.getPackagename());
    assertEquals(listDTOS.get(0).getVersions().size(), 1);
    assertEquals(listDTOS.get(0).getVersions().get(0).getId(), anotherPackage.getId());
    assertEquals(listDTOS.get(0).getVersions().get(0).isValid(), anotherPackage.isValid());
    assertEquals(listDTOS.get(0).getVersions().get(0).getAppVersion(), anotherPackage.getVersion());
    assertEquals(listDTOS.get(0).getVersions().get(0).getAppVersionNumber(), anotherPackage.getVersionnumber());
    assertEquals(listDTOS.get(0).getVersions().get(0).getFileName(), anotherPackage.getFilename());
    assertEquals(listDTOS.get(0).getVersions().get(0).getUrl(), anotherPackageDTOUrl);
    assertEquals(listDTOS.get(1).getAppName(), PACKAGE_V1_INFO.getAppname());
    assertEquals(listDTOS.get(1).getPackageName(), PACKAGE_V1_INFO.getPackagename());
    assertEquals(listDTOS.get(1).getVersions().size(), 2);
    assertEquals(listDTOS.get(1).getVersions().get(0).getId(), VERSION_1_DTO.getId());
    assertEquals(listDTOS.get(1).getVersions().get(0).isValid(), VERSION_1_DTO.isValid());
    assertEquals(listDTOS.get(1).getVersions().get(0).getAppVersion(), VERSION_1_DTO.getAppVersion());
    assertEquals(listDTOS.get(1).getVersions().get(0).getAppVersionNumber(), VERSION_1_DTO.getAppVersionNumber());
    assertEquals(listDTOS.get(1).getVersions().get(0).getFileName(), VERSION_1_DTO.getFileName());
    assertEquals(listDTOS.get(1).getVersions().get(0).getUrl(), VERSION_1_DTO.getUrl());
    assertEquals(listDTOS.get(1).getVersions().get(1).getId(), VERSION_2_DTO.getId());
    assertEquals(listDTOS.get(1).getVersions().get(1).isValid(), VERSION_2_DTO.isValid());
    assertEquals(listDTOS.get(1).getVersions().get(1).getAppVersion(), VERSION_2_DTO.getAppVersion());
    assertEquals(listDTOS.get(1).getVersions().get(1).getAppVersionNumber(), VERSION_2_DTO.getAppVersionNumber());
    assertEquals(listDTOS.get(1).getVersions().get(1).getFileName(), VERSION_2_DTO.getFileName());
    assertEquals(listDTOS.get(1).getVersions().get(1).getUrl(), VERSION_2_DTO.getUrl());
  }

  @Test
  void getPackagesByIDNotFound() throws PackageNotFoundException {
    //given
    given(repository.findAllById(anyList())).willReturn(Collections.EMPTY_LIST);
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
    assertEquals(packageDTO.getPackageName(), PACKAGE_DTO.getPackageName());
    assertEquals(packageDTO.isValid(), PACKAGE_DTO.isValid());
    assertEquals(packageDTO.getAppVersion(), PACKAGE_DTO.getAppVersion());
    assertEquals(packageDTO.getAppVersionNumber(), PACKAGE_DTO.getAppVersionNumber());
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
    Package packageToInvalidate = new Package(
        PACKAGE_V1_ID,
        PACKAGE_APPNAME,
        PACKAGE_FILENAME,
        PACKAGE_VERSION_1,
        PACKAGE_VERSION_NUMBER_1,
        PACKAGE_APPNAME + File.separator + PACKAGE_VERSION_1 + File.separator + PACKAGE_FILENAME,
        true,
        PACKAGE_PACKAGENAME
    );

    //given
    given(repository.findByAppnameIgnoreCaseAndVersionIgnoreCase(anyString(), anyString())).willReturn(packageToInvalidate);
    //when
    packageService.invalidatePackage(PACKAGE_APPNAME, PACKAGE_VERSION_2);
    //then
    assertFalse(packageToInvalidate.isValid());
    verify(repository, times(1)).save(packageToInvalidate);
  }

  @Test
  void invalidatePackageNotFound() {
    //given
    given(repository.findByAppnameIgnoreCaseAndVersionIgnoreCase(anyString(), anyString())).willReturn(null);
    //when
    //then
    assertThrows(PackageNotFoundException.class, () -> packageService.invalidatePackage(PACKAGE_APPNAME, PACKAGE_VERSION_2));
  }

  @Test
  void getPackageFile() throws IOFileException, PackageNotFoundException, InvalidPackageException {
    //given
    given(repository.findByAppnameIgnoreCaseAndVersionIgnoreCase(anyString(), anyString())).willReturn(PACKAGE_V1_INFO);
    given(fileIOUtils.loadPackageFile(any(Package.class))).willReturn(DUMMY_BYTE_ARRAY);
    //when
    //then
    assertEquals(packageService.getPackageFile(PACKAGE_APPNAME, PACKAGE_VERSION_1), DUMMY_BYTE_ARRAY);
  }

  @Test
  void getPackageFileNotFound() {
    //given
    given(repository.findByAppnameIgnoreCaseAndVersionIgnoreCase(anyString(), anyString())).willReturn(null);
    //when
    //then
    assertThrows(PackageNotFoundException.class, () -> packageService.getPackageFile(PACKAGE_APPNAME, PACKAGE_VERSION_2));
  }

  @Test
  void getPackageFileInvalid() {
    Package invalidPackage = new Package(
        PACKAGE_V1_ID,
        PACKAGE_APPNAME,
        PACKAGE_FILENAME,
        PACKAGE_VERSION_1,
        PACKAGE_VERSION_NUMBER_1,
        PACKAGE_APPNAME + File.separator + PACKAGE_VERSION_1 + File.separator + PACKAGE_FILENAME,
        false,
        PACKAGE_PACKAGENAME
    );
    //given
    given(repository.findByAppnameIgnoreCaseAndVersionIgnoreCase(anyString(), anyString())).willReturn(invalidPackage);
    //when
    //then
    assertThrows(InvalidPackageException.class, () -> packageService.getPackageFile(PACKAGE_APPNAME, PACKAGE_VERSION_1));
  }

  @Test
  void getPackageFileUnreadable() throws IOFileException {
    //given
    given(repository.findByAppnameIgnoreCaseAndVersionIgnoreCase(anyString(), anyString())).willReturn(PACKAGE_V1_INFO);
    given(fileIOUtils.loadPackageFile(any(Package.class))).willThrow(IOFileException.class);
    //when
    //then
    assertThrows(IOFileException.class, () -> packageService.getPackageFile(PACKAGE_APPNAME, PACKAGE_VERSION_1));
  }

  @Test
  void installPackageFile() throws IOFileException, WrongAppNameException {
    ArgumentCaptor<Package> packageCaptor = ArgumentCaptor.forClass(Package.class);
    ArgumentCaptor<byte[]> fileCaptor = ArgumentCaptor.forClass(byte[].class);
    //given
    //when
    packageService.installPackageFile(PACKAGE_PACKAGENAME, PACKAGE_APPNAME, PACKAGE_VERSION_NUMBER_1, PACKAGE_VERSION_1, PACKAGE_FILENAME, DUMMY_BYTE_ARRAY);
    //then
    verify(repository, times(1)).save(packageCaptor.capture());
    assertEquals(packageCaptor.getValue().getAppname(), PACKAGE_APPNAME);
    assertEquals(packageCaptor.getValue().getPackagename(), PACKAGE_PACKAGENAME);
    assertEquals(packageCaptor.getValue().getVersion(), PACKAGE_VERSION_1);
    assertEquals(packageCaptor.getValue().getVersionnumber(), PACKAGE_VERSION_NUMBER_1);
    assertEquals(packageCaptor.getValue().getFilename(), PACKAGE_FILENAME);
    assertEquals(packageCaptor.getValue().getPath(), PACKAGE_V1_INFO.getPath());
    assertTrue(packageCaptor.getValue().isValid());

    verify(fileIOUtils, times(1)).savePackageFile(anyString(), anyString(), anyString(), fileCaptor.capture(), any(PackageUtils.class));
    assertEquals(fileCaptor.getValue(), DUMMY_BYTE_ARRAY);
  }

  @Test
  void installPackageFileIOException() throws IOFileException {
    //given
    //when
    doThrow(IOFileException.class).when(fileIOUtils).savePackageFile(anyString(), anyString(), anyString(), any(byte[].class), any(PackageUtils.class));
    //then
    assertThrows(IOFileException.class, () -> packageService.installPackageFile(PACKAGE_PACKAGENAME, PACKAGE_APPNAME, PACKAGE_VERSION_NUMBER_1, PACKAGE_VERSION_1, PACKAGE_FILENAME, DUMMY_BYTE_ARRAY));
  }

  @Test
  void installPackageWrongAppNameException() throws IOFileException {
    List<Package> packageList = new ArrayList<>();
    packageList.add(PACKAGE_V1_INFO);
    //given
    given(repository.findByPackagenameIgnoreCaseAndAppnameIgnoreCaseNot(anyString(), anyString())).willReturn(packageList);
    //when
    //then
    assertThrows(WrongAppNameException.class, () -> packageService.installPackageFile(PACKAGE_PACKAGENAME, PACKAGE_APPNAME, PACKAGE_VERSION_NUMBER_1, PACKAGE_VERSION_1, PACKAGE_FILENAME, DUMMY_BYTE_ARRAY));
  }

  @Test
  void installPackageFileAutodetect() throws AutoDetectionException, IOFileException, MissingFrameworkException, WrongAppNameException {
    MockMultipartFile file = new MockMultipartFile("file", PACKAGE_DTO.getFileName(), MediaType.MULTIPART_FORM_DATA_VALUE, DUMMY_BYTE_ARRAY);
    PackageDTO partialDto = new PackageDTO();
    partialDto.setPackageName(PACKAGE_DTO.getPackageName());
    partialDto.setAppName(PACKAGE_DTO.getAppName());
    partialDto.setAppVersion(PACKAGE_DTO.getAppVersion());
    partialDto.setAppVersionNumber(PACKAGE_DTO.getAppVersionNumber());
    Package savedPackage = new Package(
        PACKAGE_DTO.getId(),
        partialDto.getAppName(),
        PACKAGE_DTO.getFileName(),
        partialDto.getAppVersion(),
        partialDto.getAppVersionNumber(),
        String.format("%s%s%s%s%s", partialDto.getAppName(), File.separator, partialDto.getAppVersion(), File.separator, PACKAGE_DTO.getFileName()),
        true,
        partialDto.getPackageName()
    );

    //given
    given(fileIOUtils.getMultipartFileBytes(any(MultipartFile.class))).willReturn(DUMMY_BYTE_ARRAY);
    given(apkUtils.autodetectPackageInfo(any(byte[].class))).willReturn(partialDto);
    given(repository.save(any(Package.class))).willReturn(savedPackage);
    //when
    PackageDTO packageDTO = packageService.installPackageFile(PACKAGE_DTO.getFileName(), file);
    //then
    assertEquals(packageDTO.getId(), PACKAGE_DTO.getId());
    assertEquals(packageDTO.getAppName(), partialDto.getAppName());
    assertEquals(packageDTO.getPackageName(), partialDto.getPackageName());
    assertEquals(packageDTO.isValid(), true);
    assertEquals(packageDTO.getAppVersion(), partialDto.getAppVersion());
    assertEquals(packageDTO.getAppVersionNumber(), partialDto.getAppVersionNumber());
    assertEquals(packageDTO.getFileName(), PACKAGE_DTO.getFileName());
    assertEquals(packageDTO.getUrl(), PACKAGE_DTO.getUrl());
    verify(fileIOUtils, times(1)).savePackageFile(anyString(), anyString(), anyString(), any(byte[].class), any(PackageUtils.class));
  }

  @Test
  void installPackageFileAutodetectWrongAppNameException() throws IOFileException, AutoDetectionException, MissingFrameworkException {
    MockMultipartFile file = new MockMultipartFile("file", PACKAGE_DTO.getFileName(), MediaType.MULTIPART_FORM_DATA_VALUE, DUMMY_BYTE_ARRAY);
    PackageDTO partialDto = new PackageDTO();
    partialDto.setPackageName(PACKAGE_DTO.getPackageName());
    partialDto.setAppName(PACKAGE_DTO.getAppName());
    partialDto.setAppVersion(PACKAGE_DTO.getAppVersion());
    partialDto.setAppVersionNumber(PACKAGE_DTO.getAppVersionNumber());
    List<Package> packageList = new ArrayList<>();
    packageList.add(PACKAGE_V1_INFO);
    //given
    given(fileIOUtils.getMultipartFileBytes(any(MultipartFile.class))).willReturn(DUMMY_BYTE_ARRAY);
    given(apkUtils.autodetectPackageInfo(any(byte[].class))).willReturn(partialDto);
    given(repository.findByPackagenameIgnoreCaseAndAppnameIgnoreCaseNot(anyString(), anyString())).willReturn(packageList);
    //when
    //then
    assertThrows(WrongAppNameException.class, () -> packageService.installPackageFile(PACKAGE_FILENAME, file));
  }

  @Test
  void installPackageFileAutodetectIOException() throws IOFileException, AutoDetectionException, MissingFrameworkException {
    MockMultipartFile file = new MockMultipartFile("file", PACKAGE_DTO.getFileName(), MediaType.MULTIPART_FORM_DATA_VALUE, DUMMY_BYTE_ARRAY);
    PackageDTO partialDto = new PackageDTO();
    partialDto.setPackageName(PACKAGE_DTO.getPackageName());
    partialDto.setAppName(PACKAGE_DTO.getAppName());
    partialDto.setAppVersion(PACKAGE_DTO.getAppVersion());
    partialDto.setAppVersionNumber(PACKAGE_DTO.getAppVersionNumber());
    //given
    given(fileIOUtils.getMultipartFileBytes(any(MultipartFile.class))).willReturn(DUMMY_BYTE_ARRAY);
    given(apkUtils.autodetectPackageInfo(any(byte[].class))).willReturn(partialDto);
    //when
    doThrow(IOFileException.class).when(fileIOUtils).savePackageFile(anyString(), anyString(), anyString(), any(byte[].class), any(PackageUtils.class));
    //then
    assertThrows(IOFileException.class, () -> packageService.installPackageFile(PACKAGE_FILENAME, file));
  }

  @Test
  void installPackageFileMissingFrameworkException() throws MissingFrameworkException, AutoDetectionException {
    MockMultipartFile file = new MockMultipartFile("file", PACKAGE_DTO.getFileName(), MediaType.MULTIPART_FORM_DATA_VALUE, DUMMY_BYTE_ARRAY);
    //given
    given(fileIOUtils.getMultipartFileBytes(any(MultipartFile.class))).willReturn(DUMMY_BYTE_ARRAY);
    //when
    doThrow(MissingFrameworkException.class).when(apkUtils).autodetectPackageInfo(any(byte[].class));
    //then
    assertThrows(MissingFrameworkException.class, () -> packageService.installPackageFile(PACKAGE_FILENAME, file));
  }

  @Test
  void installPackageFileAutodetectionException() throws AutoDetectionException, MissingFrameworkException {
    MockMultipartFile file = new MockMultipartFile("file", PACKAGE_DTO.getFileName(), MediaType.MULTIPART_FORM_DATA_VALUE, DUMMY_BYTE_ARRAY);
    //given
    given(fileIOUtils.getMultipartFileBytes(any(MultipartFile.class))).willReturn(DUMMY_BYTE_ARRAY);
    //when
    doThrow(AutoDetectionException.class).when(apkUtils).autodetectPackageInfo(any(byte[].class));
    //then
    assertThrows(AutoDetectionException.class, () -> packageService.installPackageFile(PACKAGE_FILENAME, file));
  }

  @Test
  void testDeleteVersionPackage() throws PackageNotFoundException {
    ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
    ArgumentCaptor<Package> packageCaptor = ArgumentCaptor.forClass(Package.class);
    //given
    doNothing().when(fileIOUtils).deleteSingleFile(any(Package.class));
    given(repository.findByAppnameIgnoreCaseAndVersionIgnoreCase(anyString(), anyString())).willReturn(PACKAGE_V1_INFO);
    //when
    packageService.deleteVersionPackage(PACKAGE_APPNAME, PACKAGE_VERSION_1);
    //then
    verify(repository, times(1)).deleteById(idCaptor.capture());
    verify(fileIOUtils, times(1)).deleteSingleFile(packageCaptor.capture());
    assertEquals(idCaptor.getValue(), PACKAGE_V1_ID);
    assertEquals(PACKAGE_V1_INFO.getId(), packageCaptor.getValue().getId());
    assertEquals(PACKAGE_V1_INFO.getAppname(), packageCaptor.getValue().getAppname());
    assertEquals(PACKAGE_V1_INFO.getVersion(), packageCaptor.getValue().getVersion());
    assertEquals(PACKAGE_V1_INFO.getVersionnumber(), packageCaptor.getValue().getVersionnumber());
    assertEquals(PACKAGE_V1_INFO.getPackagename(), packageCaptor.getValue().getPackagename());
    assertEquals(PACKAGE_V1_INFO.getPath(), packageCaptor.getValue().getPath());
    assertEquals(PACKAGE_V1_INFO.getFilename(), packageCaptor.getValue().getFilename());
    assertEquals(PACKAGE_V1_INFO.isValid(), packageCaptor.getValue().isValid());
  }

  @Test
  void testDeleteVersionPackageNotFound() {
    //given
    given(repository.findByAppnameIgnoreCaseAndVersionIgnoreCase(anyString(), anyString())).willReturn(null);
    //when
    assertThrows(PackageNotFoundException.class, () -> packageService.deleteVersionPackage(anyString(), anyString()));
    //then
    verify(repository, times(0)).deleteById(any());
    verify(fileIOUtils, times(0)).deleteSingleFile(any());
  }

  @Test
  void testDeleteAllVersions() throws PackageNotFoundException {
    ArgumentCaptor<List<Package>> packageCaptor = ArgumentCaptor.forClass(List.class);
    List<Package> packages = new ArrayList<>();

    packages.add(PACKAGE_V1_INFO);
    packages.add(PACKAGE_V2_INFO);
    //given
    doNothing().when(fileIOUtils).deletePackageList(anyIterable());
    given(repository.findByAppnameIgnoreCase(anyString())).willReturn(packages);
    //when
    packageService.deleteAllVersions(PACKAGE_APPNAME);
    //then
    verify(repository, times(1)).deleteAll(packageCaptor.capture());
    assertEquals(2, packageCaptor.getValue().size());
    assertEquals(PACKAGE_V1_INFO.getId(), packageCaptor.getValue().get(0).getId());
    assertEquals(PACKAGE_V1_INFO.getAppname(), packageCaptor.getValue().get(0).getAppname());
    assertEquals(PACKAGE_V1_INFO.getVersion(), packageCaptor.getValue().get(0).getVersion());
    assertEquals(PACKAGE_V1_INFO.getVersionnumber(), packageCaptor.getValue().get(0).getVersionnumber());
    assertEquals(PACKAGE_V1_INFO.getPackagename(), packageCaptor.getValue().get(0).getPackagename());
    assertEquals(PACKAGE_V1_INFO.getPath(), packageCaptor.getValue().get(0).getPath());
    assertEquals(PACKAGE_V1_INFO.getFilename(), packageCaptor.getValue().get(0).getFilename());
    assertEquals(PACKAGE_V1_INFO.isValid(), packageCaptor.getValue().get(0).isValid());
    assertEquals(PACKAGE_V2_INFO.getId(), packageCaptor.getValue().get(1).getId());
    assertEquals(PACKAGE_V2_INFO.getAppname(), packageCaptor.getValue().get(1).getAppname());
    assertEquals(PACKAGE_V2_INFO.getVersion(), packageCaptor.getValue().get(1).getVersion());
    assertEquals(PACKAGE_V2_INFO.getVersionnumber(), packageCaptor.getValue().get(1).getVersionnumber());
    assertEquals(PACKAGE_V2_INFO.getPackagename(), packageCaptor.getValue().get(1).getPackagename());
    assertEquals(PACKAGE_V2_INFO.getPath(), packageCaptor.getValue().get(1).getPath());
    assertEquals(PACKAGE_V2_INFO.getFilename(), packageCaptor.getValue().get(1).getFilename());
    assertEquals(PACKAGE_V2_INFO.isValid(), packageCaptor.getValue().get(1).isValid());
    verify(fileIOUtils, times(1)).deletePackageList(packageCaptor.capture());
    assertEquals(2, packageCaptor.getValue().size());
    assertEquals(PACKAGE_V1_INFO.getId(), packageCaptor.getValue().get(0).getId());
    assertEquals(PACKAGE_V1_INFO.getAppname(), packageCaptor.getValue().get(0).getAppname());
    assertEquals(PACKAGE_V1_INFO.getVersion(), packageCaptor.getValue().get(0).getVersion());
    assertEquals(PACKAGE_V1_INFO.getVersionnumber(), packageCaptor.getValue().get(0).getVersionnumber());
    assertEquals(PACKAGE_V1_INFO.getPackagename(), packageCaptor.getValue().get(0).getPackagename());
    assertEquals(PACKAGE_V1_INFO.getPath(), packageCaptor.getValue().get(0).getPath());
    assertEquals(PACKAGE_V1_INFO.getFilename(), packageCaptor.getValue().get(0).getFilename());
    assertEquals(PACKAGE_V1_INFO.isValid(), packageCaptor.getValue().get(0).isValid());
    assertEquals(PACKAGE_V2_INFO.getId(), packageCaptor.getValue().get(1).getId());
    assertEquals(PACKAGE_V2_INFO.getAppname(), packageCaptor.getValue().get(1).getAppname());
    assertEquals(PACKAGE_V2_INFO.getVersion(), packageCaptor.getValue().get(1).getVersion());
    assertEquals(PACKAGE_V2_INFO.getVersionnumber(), packageCaptor.getValue().get(1).getVersionnumber());
    assertEquals(PACKAGE_V2_INFO.getPackagename(), packageCaptor.getValue().get(1).getPackagename());
    assertEquals(PACKAGE_V2_INFO.getPath(), packageCaptor.getValue().get(1).getPath());
    assertEquals(PACKAGE_V2_INFO.getFilename(), packageCaptor.getValue().get(1).getFilename());
    assertEquals(PACKAGE_V2_INFO.isValid(), packageCaptor.getValue().get(1).isValid());
  }

  @Test
  void testDeleteAllVersionsNotfound() {
    //given
    given(repository.findByAppnameIgnoreCase(anyString())).willReturn(null);
    //when
    assertThrows(PackageNotFoundException.class, () -> packageService.deleteAllVersions(anyString()));
    //then
    verify(repository, times(0)).deleteAll(any());
    verify(fileIOUtils, times(0)).deletePackageList(any());
  }

  @Test
  void testDeletePackageList() throws PackageNotFoundException {
    ArgumentCaptor<List<Package>> packageCaptor = ArgumentCaptor.forClass(List.class);
    List<Package> packages = new ArrayList<>();
    List<Long> idList = new ArrayList<>();

    packages.add(PACKAGE_V1_INFO);
    packages.add(PACKAGE_V2_INFO);
    idList.add(PACKAGE_V1_ID);
    idList.add(PACKAGE_V2_ID);
    //given
    doNothing().when(fileIOUtils).deletePackageList(anyIterable());
    given(repository.findAllById(any())).willReturn(packages);
    //when
    packageService.deletePackagesList(idList);
    //then
    verify(repository, times(1)).deleteAll(packageCaptor.capture());
    assertEquals(2, packageCaptor.getValue().size());
    assertEquals(PACKAGE_V1_INFO.getId(), packageCaptor.getValue().get(0).getId());
    assertEquals(PACKAGE_V1_INFO.getAppname(), packageCaptor.getValue().get(0).getAppname());
    assertEquals(PACKAGE_V1_INFO.getVersion(), packageCaptor.getValue().get(0).getVersion());
    assertEquals(PACKAGE_V1_INFO.getVersionnumber(), packageCaptor.getValue().get(0).getVersionnumber());
    assertEquals(PACKAGE_V1_INFO.getPackagename(), packageCaptor.getValue().get(0).getPackagename());
    assertEquals(PACKAGE_V1_INFO.getPath(), packageCaptor.getValue().get(0).getPath());
    assertEquals(PACKAGE_V1_INFO.getFilename(), packageCaptor.getValue().get(0).getFilename());
    assertEquals(PACKAGE_V1_INFO.isValid(), packageCaptor.getValue().get(0).isValid());
    assertEquals(PACKAGE_V2_INFO.getId(), packageCaptor.getValue().get(1).getId());
    assertEquals(PACKAGE_V2_INFO.getAppname(), packageCaptor.getValue().get(1).getAppname());
    assertEquals(PACKAGE_V2_INFO.getVersion(), packageCaptor.getValue().get(1).getVersion());
    assertEquals(PACKAGE_V2_INFO.getVersionnumber(), packageCaptor.getValue().get(1).getVersionnumber());
    assertEquals(PACKAGE_V2_INFO.getPackagename(), packageCaptor.getValue().get(1).getPackagename());
    assertEquals(PACKAGE_V2_INFO.getPath(), packageCaptor.getValue().get(1).getPath());
    assertEquals(PACKAGE_V2_INFO.getFilename(), packageCaptor.getValue().get(1).getFilename());
    assertEquals(PACKAGE_V2_INFO.isValid(), packageCaptor.getValue().get(1).isValid());
    verify(fileIOUtils, times(1)).deletePackageList(packageCaptor.capture());
    assertEquals(2, packageCaptor.getValue().size());
    assertEquals(PACKAGE_V1_INFO.getId(), packageCaptor.getValue().get(0).getId());
    assertEquals(PACKAGE_V1_INFO.getAppname(), packageCaptor.getValue().get(0).getAppname());
    assertEquals(PACKAGE_V1_INFO.getVersion(), packageCaptor.getValue().get(0).getVersion());
    assertEquals(PACKAGE_V1_INFO.getVersionnumber(), packageCaptor.getValue().get(0).getVersionnumber());
    assertEquals(PACKAGE_V1_INFO.getPackagename(), packageCaptor.getValue().get(0).getPackagename());
    assertEquals(PACKAGE_V1_INFO.getPath(), packageCaptor.getValue().get(0).getPath());
    assertEquals(PACKAGE_V1_INFO.getFilename(), packageCaptor.getValue().get(0).getFilename());
    assertEquals(PACKAGE_V1_INFO.isValid(), packageCaptor.getValue().get(0).isValid());
    assertEquals(PACKAGE_V2_INFO.getId(), packageCaptor.getValue().get(1).getId());
    assertEquals(PACKAGE_V2_INFO.getAppname(), packageCaptor.getValue().get(1).getAppname());
    assertEquals(PACKAGE_V2_INFO.getVersion(), packageCaptor.getValue().get(1).getVersion());
    assertEquals(PACKAGE_V2_INFO.getVersionnumber(), packageCaptor.getValue().get(1).getVersionnumber());
    assertEquals(PACKAGE_V2_INFO.getPackagename(), packageCaptor.getValue().get(1).getPackagename());
    assertEquals(PACKAGE_V2_INFO.getPath(), packageCaptor.getValue().get(1).getPath());
    assertEquals(PACKAGE_V2_INFO.getFilename(), packageCaptor.getValue().get(1).getFilename());
    assertEquals(PACKAGE_V2_INFO.isValid(), packageCaptor.getValue().get(1).isValid());
  }

  @Test
  void testDeletePackageListNotfound() {
    //given
    given(repository.findAllById(any())).willReturn(Collections.EMPTY_LIST);
    //when
    assertDoesNotThrow(() -> packageService.deletePackagesList(anyList()));
    //then
    verify(repository, times(0)).deleteAll(any());
    verify(fileIOUtils, times(0)).deletePackageList(any());
  }
}