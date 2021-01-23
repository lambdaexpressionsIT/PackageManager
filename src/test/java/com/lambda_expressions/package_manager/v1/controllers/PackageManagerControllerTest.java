package com.lambda_expressions.package_manager.v1.controllers;

import com.lambda_expressions.package_manager.exceptions.IOFileException;
import com.lambda_expressions.package_manager.exceptions.InvalidPackageException;
import com.lambda_expressions.package_manager.exceptions.MalformedURLException;
import com.lambda_expressions.package_manager.exceptions.PackageNotFoundException;
import com.lambda_expressions.package_manager.services.AuthenticationService;
import com.lambda_expressions.package_manager.services.PackageService;
import com.lambda_expressions.package_manager.v1.RESTExceptionHandler;
import com.lambda_expressions.package_manager.v1.model.PackageDTO;
import com.lambda_expressions.package_manager.v1.model.PackageListDTO;
import com.lambda_expressions.package_manager.v1.model.VersionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by steccothal
 * on Saturday 23 January 2021
 * at 10:42 AM
 */
@ExtendWith({MockitoExtension.class, RestDocumentationExtension.class})
class PackageManagerControllerTest {
  private static final String PACKAGES_FILE_EXTENSION = ".bib";
  private static final String PACKAGES_WEBSERVER_BASEURL = "http://bob.bub";
  private static final String PACKAGE_FILENAME = "fileName";
  private static final String PACKAGE_APPNAME = "appName";
  private static final int PACKAGE_VERSION_1 = 1;
  private static final int PACKAGE_VERSION_2 = 2;
  private static final byte[] DUMMY_BYTE_ARRAY = "Some dummy string converted to byte array".getBytes();

  private static final String REST_SERVICES_BASE_URL = "/api/v1";

  private static final String LIST_ALL_PACKAGES_URL = REST_SERVICES_BASE_URL + "/listPackages";
  private static final String LIST_PACKAGE_VERSIONS_URL = REST_SERVICES_BASE_URL + "/listPackages/appName";
  private static final String LIST_VERSION_INFO_URL = REST_SERVICES_BASE_URL + "/listPackages/appName/2";
  private static final String INVALIDATE_PACKAGE_URL = REST_SERVICES_BASE_URL + "/invalidatePackage/appName/2";
  private static final String UPLOAD_PACKAGE_URL = REST_SERVICES_BASE_URL + "/uploadPackage/appName/2/fileName";
  private static final String GET_PACKAGE_FILE_URL = REST_SERVICES_BASE_URL + "/downloadPackage/appName/2";

  private static final String LIST_VERSION_INFO_URL_MALFORMED = REST_SERVICES_BASE_URL + "/listPackages/appName/NaN";
  private static final String INVALIDATE_PACKAGE_URL_MALFORMED = REST_SERVICES_BASE_URL + "/invalidatePackage/appName/NaN";
  private static final String UPLOAD_PACKAGE_URL_MALFORMED = REST_SERVICES_BASE_URL + "/uploadPackage/appName/NaN/fileName";
  private static final String GET_PACKAGE_FILE_URL_MALFORMED = REST_SERVICES_BASE_URL + "/downloadPackage/appName/NaN";


  private static final PackageDTO packageDTO = PackageDTO.builder()
      .fileName(PACKAGE_FILENAME)
      .url(PACKAGES_WEBSERVER_BASEURL + "/" + PACKAGE_APPNAME + "/" + PACKAGE_VERSION_2 + "/" + PACKAGE_FILENAME + PACKAGES_FILE_EXTENSION)
      .valid(true)
      .appVersion(PACKAGE_VERSION_2)
      .appName(PACKAGE_APPNAME)
      .build();

  private static final VersionDTO version1DTO = VersionDTO.builder()
      .fileName(PACKAGE_FILENAME)
      .url((PACKAGES_WEBSERVER_BASEURL + "/" + PACKAGE_APPNAME + "/" + PACKAGE_VERSION_1 + "/" + PACKAGE_FILENAME + PACKAGES_FILE_EXTENSION))
      .valid(true)
      .appVersion(PACKAGE_VERSION_1)
      .build();

  private static final VersionDTO version2DTO = VersionDTO.builder()
      .fileName(PACKAGE_FILENAME)
      .url((PACKAGES_WEBSERVER_BASEURL + "/" + PACKAGE_APPNAME + "/" + PACKAGE_VERSION_2 + "/" + PACKAGE_FILENAME + PACKAGES_FILE_EXTENSION))
      .valid(true)
      .appVersion(PACKAGE_VERSION_2)
      .build();

  @Mock
  PackageService packageService;

  @Mock
  AuthenticationService authService;

  @InjectMocks
  PackageManagerController controller;

  MockMvc mockMvc;

  @BeforeEach
  public void setUp(RestDocumentationContextProvider documentationContextProvider) {
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new RESTExceptionHandler())
        .apply(documentationConfiguration(documentationContextProvider))
        .build();
  }

  /*
   * listAllPackages
   */
  @Test
  public void testGetAllPackages() throws Exception {
    ArrayList<VersionDTO> versionDTOArrayList = new ArrayList<>();
    ArrayList<PackageListDTO> packageListDTOS = new ArrayList<>();
    PackageListDTO packageListDTO = PackageListDTO.builder()
        .appName("someApp")
        .versions(versionDTOArrayList)
        .build();

    packageListDTOS.add(packageListDTO);

    versionDTOArrayList.add(version1DTO);
    versionDTOArrayList.add(version2DTO);

    //given
    given(packageService.listAllPackages()).willReturn(packageListDTOS);
    //when
    mockMvc.perform(get(LIST_ALL_PACKAGES_URL))
        //then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(packageListDTOS.size()))
        .andExpect(jsonPath("$[0].appName").value(packageListDTO.getAppName()))
        .andExpect(jsonPath("$[0].versions").isArray())
        .andExpect(jsonPath("$[0].versions.length()").value(packageListDTO.getVersions().size()))
        .andExpect(jsonPath("$[0].versions[0].appVersion").value(packageListDTO.getVersions().get(0).getAppVersion()))
        .andExpect(jsonPath("$[0].versions[0].fileName").value(packageListDTO.getVersions().get(0).getFileName()))
        .andExpect(jsonPath("$[0].versions[0].valid").value(packageListDTO.getVersions().get(0).isValid()))
        .andExpect(jsonPath("$[0].versions[0].url").value(packageListDTO.getVersions().get(0).getUrl()))
        .andExpect(jsonPath("$[0].versions[1].appVersion").value(packageListDTO.getVersions().get(1).getAppVersion()))
        .andExpect(jsonPath("$[0].versions[1].fileName").value(packageListDTO.getVersions().get(1).getFileName()))
        .andExpect(jsonPath("$[0].versions[1].valid").value(packageListDTO.getVersions().get(1).isValid()))
        .andExpect(jsonPath("$[0].versions[1].url").value(packageListDTO.getVersions().get(1).getUrl()));
  }

  @Test
  public void testGetAllPackagesEmptyList() throws Exception {
    ArrayList<PackageListDTO> packageListDTOS = new ArrayList<>();

    //given
    given(packageService.listAllPackages()).willReturn(packageListDTOS);
    //when
    mockMvc.perform(get(LIST_ALL_PACKAGES_URL))
        //then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(packageListDTOS.size()));
  }

  /*
   * listPackageVersions
   */
  @Test
  public void testGetPackageVersions() throws Exception {
    ArrayList<VersionDTO> versionDTOArrayList = new ArrayList<>();
    PackageListDTO packageListDTO = PackageListDTO.builder()
        .appName("someApp")
        .versions(versionDTOArrayList)
        .build();

    versionDTOArrayList.add(version1DTO);
    versionDTOArrayList.add(version2DTO);

    //given
    given(packageService.listAllVersions(anyString())).willReturn(packageListDTO);
    //when
    mockMvc.perform(get(LIST_PACKAGE_VERSIONS_URL))
        //then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.appName").value(packageListDTO.getAppName()))
        .andExpect(jsonPath("$.versions").isArray())
        .andExpect(jsonPath("$.versions.length()").value(packageListDTO.getVersions().size()))
        .andExpect(jsonPath("$.versions[0].appVersion").value(packageListDTO.getVersions().get(0).getAppVersion()))
        .andExpect(jsonPath("$.versions[0].fileName").value(packageListDTO.getVersions().get(0).getFileName()))
        .andExpect(jsonPath("$.versions[0].valid").value(packageListDTO.getVersions().get(0).isValid()))
        .andExpect(jsonPath("$.versions[0].url").value(packageListDTO.getVersions().get(0).getUrl()))
        .andExpect(jsonPath("$.versions[1].appVersion").value(packageListDTO.getVersions().get(1).getAppVersion()))
        .andExpect(jsonPath("$.versions[1].fileName").value(packageListDTO.getVersions().get(1).getFileName()))
        .andExpect(jsonPath("$.versions[1].valid").value(packageListDTO.getVersions().get(1).isValid()))
        .andExpect(jsonPath("$.versions[1].url").value(packageListDTO.getVersions().get(1).getUrl()));
  }

  @Test
  public void testGetPackageVersionsNotFound() throws Exception {
    //given
    given(packageService.listAllVersions(anyString())).willThrow(PackageNotFoundException.class);
    //when
    mockMvc.perform(get(LIST_PACKAGE_VERSIONS_URL))
        //then
        .andExpect(status().isNotFound())
        .andExpect(mvcResult -> assertTrue(mvcResult.getResolvedException() instanceof PackageNotFoundException));
  }

  /*
   * listPackageVersionInfo
   */
  @Test
  public void testGetPackageVersion() throws Exception {
    //given
    given(packageService.getPackageInfo(anyString(), anyInt())).willReturn(packageDTO);
    //when
    mockMvc.perform(get(LIST_VERSION_INFO_URL))
        //then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.appName").value(packageDTO.getAppName()))
        .andExpect(jsonPath("$.appVersion").value(packageDTO.getAppVersion()))
        .andExpect(jsonPath("$.fileName").value(packageDTO.getFileName()))
        .andExpect(jsonPath("$.valid").value(packageDTO.isValid()))
        .andExpect(jsonPath("$.url").value(packageDTO.getUrl()));
  }

  @Test
  public void testGetPackageVersionNotFound() throws Exception {
    //given
    given(packageService.getPackageInfo(anyString(), anyInt())).willThrow(PackageNotFoundException.class);
    //when
    mockMvc.perform(get(LIST_VERSION_INFO_URL))
        //then
        .andExpect(status().isNotFound())
        .andExpect(mvcResult -> assertTrue(mvcResult.getResolvedException() instanceof PackageNotFoundException));
  }

  @Test
  public void testGetPackageVersionMalformedURL() throws Exception {
    //given
    //when
    mockMvc.perform(get(LIST_VERSION_INFO_URL_MALFORMED))
        //then
        .andExpect(status().isBadRequest())
        .andExpect(result -> assertTrue(result.getResolvedException() instanceof MalformedURLException));
  }

  /*
   * downloadPackage
   */
  @Test
  public void testGetPackageFile() throws Exception {
    //given
    given(packageService.getPackageFile(anyString(), anyInt())).willReturn(DUMMY_BYTE_ARRAY);
    //when
    mockMvc.perform(get(GET_PACKAGE_FILE_URL))
        //then
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
        .andExpect(content().bytes(DUMMY_BYTE_ARRAY));
  }

  @Test
  public void testGetPackageFileNotFound() throws Exception {
    //given
    given(packageService.getPackageFile(anyString(), anyInt())).willThrow(PackageNotFoundException.class);
    //when
    mockMvc.perform(get(GET_PACKAGE_FILE_URL))
        //then
        .andExpect(status().isNotFound())
        .andExpect(mvcResult -> assertTrue(mvcResult.getResolvedException() instanceof PackageNotFoundException));
  }

  @Test
  public void testGetPackageFileMalformedURL() throws Exception {
    //given
    //when
    mockMvc.perform(get(GET_PACKAGE_FILE_URL_MALFORMED))
        //then
        .andExpect(status().isBadRequest())
        .andExpect(result -> assertTrue(result.getResolvedException() instanceof MalformedURLException));
  }

  @Test
  public void testGetPackageFileInvalidPackage() throws Exception {
    //given
    given(packageService.getPackageFile(anyString(), anyInt())).willThrow(InvalidPackageException.class);
    //when
    mockMvc.perform(get(GET_PACKAGE_FILE_URL))
        //then
        .andExpect(status().isForbidden())
        .andExpect(mvcResult -> assertTrue(mvcResult.getResolvedException() instanceof InvalidPackageException));
  }

  @Test
  public void testGetPackageFileUnableToReadFile() throws Exception {
    //given
    given(packageService.getPackageFile(anyString(), anyInt())).willThrow(IOFileException.class);
    //when
    mockMvc.perform(get(GET_PACKAGE_FILE_URL))
        //then
        .andExpect(status().isInternalServerError())
        .andExpect(mvcResult -> assertTrue(mvcResult.getResolvedException() instanceof IOFileException));
  }

  /*
   * invalidatePackage
   */
  @Test
  public void testInvalidatePackage() throws Exception {
    //given
    doNothing().when(packageService).invalidatePackage(anyString(), anyInt());
    //when
    mockMvc.perform(patch(INVALIDATE_PACKAGE_URL))
        //then
        .andExpect(status().isOk());
  }

  @Test
  public void testInvalidatePackageNotFound() throws Exception {
    //given
    doThrow(PackageNotFoundException.class).when(packageService).invalidatePackage(anyString(), anyInt());
    //when
    mockMvc.perform(patch(INVALIDATE_PACKAGE_URL))
        //then
        .andExpect(status().isNotFound())
        .andExpect(mvcResult -> assertTrue(mvcResult.getResolvedException() instanceof PackageNotFoundException));
  }

  @Test
  public void testInvalidatePackageMalformedURL() throws Exception {
    //given
    //when
    mockMvc.perform(patch(INVALIDATE_PACKAGE_URL_MALFORMED))
        //then
        .andExpect(status().isBadRequest())
        .andExpect(result -> assertTrue(result.getResolvedException() instanceof MalformedURLException));
  }

  /*
   * uploadPackage
   */
  @Test
  public void testUploadPackageFile() throws Exception {
    //given
    doNothing().when(packageService).installPackageFile(anyString(), anyInt(), anyString(), any(byte[].class));
    //when
    mockMvc.perform(post(UPLOAD_PACKAGE_URL).content(DUMMY_BYTE_ARRAY))
        //then
        .andExpect(status().isCreated());
  }

  @Test
  public void testUploadPackageFileMalformedURL() throws Exception {
    //given
    //when
    mockMvc.perform(post(UPLOAD_PACKAGE_URL_MALFORMED).content(DUMMY_BYTE_ARRAY))
        //then
        .andExpect(status().isBadRequest())
        .andExpect(result -> assertTrue(result.getResolvedException() instanceof MalformedURLException));
  }

  @Test
  public void testUploadPackageFileUnableToWriteFile() throws Exception {
    //given
    doThrow(IOFileException.class).when(packageService).installPackageFile(anyString(), anyInt(), anyString(), any(byte[].class));
    //when
    mockMvc.perform(post(UPLOAD_PACKAGE_URL).content(DUMMY_BYTE_ARRAY))
        //then
        .andExpect(status().isInternalServerError())
        .andExpect(mvcResult -> assertTrue(mvcResult.getResolvedException() instanceof IOFileException));
  }
}