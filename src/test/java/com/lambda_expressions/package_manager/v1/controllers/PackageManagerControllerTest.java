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
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by steccothal
 * on Saturday 23 January 2021
 * at 10:42 AM
 */
@ExtendWith({MockitoExtension.class, RestDocumentationExtension.class})
@AutoConfigureRestDocs(outputDir = "target/snippets")
class PackageManagerControllerTest {
  private static final String PACKAGES_FILE_EXTENSION = ".apk";
  private static final String PACKAGES_WEBSERVER_BASEURL = "http://localhost:8080";
  private static final String PACKAGE_FILENAME = "Spending_1.9";
  private static final String PACKAGE_APPNAME = "Spending";
  private static final long PACKAGE_V1_ID = 100;
  private static final long PACKAGE_V2_ID = 200;
  private static final int PACKAGE_VERSION_1 = 1;
  private static final int PACKAGE_VERSION_2 = 2;
  private static final byte[] DUMMY_BYTE_ARRAY = "This is the payload of an uploadPackage request, the byte array of a package file".getBytes();

  private static final String REST_SERVICES_BASE_URL = "/api/v1";

  private static final String LIST_ALL_PACKAGES_URL = REST_SERVICES_BASE_URL + "/listPackages";
  private static final String LIST_PACKAGE_VERSIONS_URL = REST_SERVICES_BASE_URL + "/listPackages/Spending";
  private static final String LIST_VERSION_INFO_URL = REST_SERVICES_BASE_URL + "/listPackages/Spending/2";
  private static final String GET_PACKAGE_BY_ID_URL = REST_SERVICES_BASE_URL + "/getPackage/100";
  private static final String INVALIDATE_PACKAGE_URL = REST_SERVICES_BASE_URL + "/invalidatePackage/Spending/2";
  private static final String UPLOAD_PACKAGE_URL = REST_SERVICES_BASE_URL + "/uploadPackage/Spending/1/Spending_1.9";
  private static final String GET_PACKAGE_FILE_URL = REST_SERVICES_BASE_URL + "/downloadPackage/Spending/2";

  private static final String LIST_VERSION_INFO_URL_MALFORMED = REST_SERVICES_BASE_URL + "/listPackages/Spending/NaN";
  private static final String GET_PACKAGE_BY_ID_URL_MALFORMED = REST_SERVICES_BASE_URL + "/getPackage/NaN";
  private static final String INVALIDATE_PACKAGE_URL_MALFORMED = REST_SERVICES_BASE_URL + "/invalidatePackage/Spending/NaN";
  private static final String UPLOAD_PACKAGE_URL_MALFORMED = REST_SERVICES_BASE_URL + "/uploadPackage/Spending/NaN/Spending_1.9";
  private static final String GET_PACKAGE_FILE_URL_MALFORMED = REST_SERVICES_BASE_URL + "/downloadPackage/Spending/NaN";

  private static final VersionDTO version1DTO = VersionDTO.builder()
      .id(PACKAGE_V1_ID)
      .fileName(PACKAGE_FILENAME)
      .url((PACKAGES_WEBSERVER_BASEURL + "/" + PACKAGE_APPNAME + "/" + PACKAGE_VERSION_1 + "/" + PACKAGE_FILENAME + PACKAGES_FILE_EXTENSION))
      .valid(true)
      .appVersion(PACKAGE_VERSION_1)
      .build();

  private static final VersionDTO version2DTO = VersionDTO.builder()
      .id(PACKAGE_V2_ID)
      .fileName("Spending_2.5")
      .url((PACKAGES_WEBSERVER_BASEURL + "/" + PACKAGE_APPNAME + "/" + PACKAGE_VERSION_2 + "/" + "Spending_2.5" + PACKAGES_FILE_EXTENSION))
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
    ArrayList<VersionDTO> lisXTeVersionDTOArrayList = new ArrayList<>();
    ArrayList<PackageListDTO> packageListDTOS = new ArrayList<>();
    PackageListDTO spendingPackageListDTO = PackageListDTO.builder()
        .appName(PACKAGE_APPNAME)
        .versions(versionDTOArrayList)
        .build();
    PackageListDTO lisXTePackageListDTO = PackageListDTO.builder()
        .appName("LisXTe")
        .versions(lisXTeVersionDTOArrayList)
        .build();
    VersionDTO lisXTeVersion1 = VersionDTO.builder()
        .id(300L)
        .appVersion(1)
        .fileName("LisXTe_1.0.10_b1.apk")
        .url(PACKAGES_WEBSERVER_BASEURL + "/LisXTe/1/LisXTe_1.0.10_b1.apk")
        .valid(true)
        .build();

    packageListDTOS.add(spendingPackageListDTO);
    packageListDTOS.add(lisXTePackageListDTO);

    versionDTOArrayList.add(version1DTO);
    versionDTOArrayList.add(version2DTO);

    lisXTeVersionDTOArrayList.add(lisXTeVersion1);

    //given
    given(packageService.listAllPackages()).willReturn(packageListDTOS);
    //when
    mockMvc.perform(get(LIST_ALL_PACKAGES_URL))
        //then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(packageListDTOS.size()))
        .andExpect(jsonPath("$[0].appName").value(spendingPackageListDTO.getAppName()))
        .andExpect(jsonPath("$[0].versions").isArray())
        .andExpect(jsonPath("$[0].versions.length()").value(spendingPackageListDTO.getVersions().size()))
        .andExpect(jsonPath("$[0].versions[0].id").value(spendingPackageListDTO.getVersions().get(0).getId()))
        .andExpect(jsonPath("$[0].versions[0].appVersion").value(spendingPackageListDTO.getVersions().get(0).getAppVersion()))
        .andExpect(jsonPath("$[0].versions[0].fileName").value(spendingPackageListDTO.getVersions().get(0).getFileName()))
        .andExpect(jsonPath("$[0].versions[0].valid").value(spendingPackageListDTO.getVersions().get(0).isValid()))
        .andExpect(jsonPath("$[0].versions[0].url").value(spendingPackageListDTO.getVersions().get(0).getUrl()))
        .andExpect(jsonPath("$[0].versions[1].id").value(spendingPackageListDTO.getVersions().get(1).getId()))
        .andExpect(jsonPath("$[0].versions[1].appVersion").value(spendingPackageListDTO.getVersions().get(1).getAppVersion()))
        .andExpect(jsonPath("$[0].versions[1].fileName").value(spendingPackageListDTO.getVersions().get(1).getFileName()))
        .andExpect(jsonPath("$[0].versions[1].valid").value(spendingPackageListDTO.getVersions().get(1).isValid()))
        .andExpect(jsonPath("$[0].versions[1].url").value(spendingPackageListDTO.getVersions().get(1).getUrl()))
        .andDo(document("listAllPackages", responseFields(
            fieldWithPath("[]").description("Array popolato dagli oggetti contenenti le informazioni delle applicazioni presenti sul server"),
            fieldWithPath("[].appName").description("Nome dell'applicazione"),
            fieldWithPath("[].versions").description("Array popolato dagli oggetti contenenti le informazioni delle versioni dell'applicazione presenti sul server"),
            fieldWithPath("[].versions[].id").description("Identificativo univoco della combinazione applicazione/versione (intero)"),
            fieldWithPath("[].versions[].appVersion").description("Numero progressivo della versione dell'applicazione (intero)"),
            fieldWithPath("[].versions[].fileName").description("Nome del file dell'applicazione presente sul server. L'estensione dei file delle applicazioni viene definita nel file 'application.properties' alla voce 'packages.file.extension'"),
            fieldWithPath("[].versions[].valid").description("Flag indicante la validità di un package. Se settato a false, il file corrispondente non puo essere scaricato tramite il servizio 'downloadPackage'"),
            fieldWithPath("[].versions[].url").description("URL pubblico al quale puo essere scaricato il file dell'applicazione. Questo URL e composto da un indirizzo base definito alla voce 'packages.web.base.url' nel file 'application.properties' e dal path relativo in cui e salvato il file dell'applicazione")
        )));
  }

  @Test
  public void testGetAllPackagesEmptyList() throws Exception {
    ArrayList<PackageListDTO> packageListDTOS = new ArrayList<>();
    //given
    //when
    mockMvc.perform(get(LIST_ALL_PACKAGES_URL))
        //then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(packageListDTOS.size()))
        .andDo(document("listAllPackagesEmpty", responseFields(
            fieldWithPath("[]").description("Nel caso in cui nessun package sia presente sul server, il servizio ritorna un array vuoto")
        )));
  }

  /*
   * listPackageVersions
   */
  @Test
  public void testGetPackageVersions() throws Exception {
    ArrayList<VersionDTO> versionDTOArrayList = new ArrayList<>();
    PackageListDTO packageListDTO = PackageListDTO.builder()
        .appName(PACKAGE_APPNAME)
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
        .andExpect(jsonPath("$.versions[0].id").value(packageListDTO.getVersions().get(0).getId()))
        .andExpect(jsonPath("$.versions[0].appVersion").value(packageListDTO.getVersions().get(0).getAppVersion()))
        .andExpect(jsonPath("$.versions[0].fileName").value(packageListDTO.getVersions().get(0).getFileName()))
        .andExpect(jsonPath("$.versions[0].valid").value(packageListDTO.getVersions().get(0).isValid()))
        .andExpect(jsonPath("$.versions[0].url").value(packageListDTO.getVersions().get(0).getUrl()))
        .andExpect(jsonPath("$.versions[1].id").value(packageListDTO.getVersions().get(1).getId()))
        .andExpect(jsonPath("$.versions[1].appVersion").value(packageListDTO.getVersions().get(1).getAppVersion()))
        .andExpect(jsonPath("$.versions[1].fileName").value(packageListDTO.getVersions().get(1).getFileName()))
        .andExpect(jsonPath("$.versions[1].valid").value(packageListDTO.getVersions().get(1).isValid()))
        .andExpect(jsonPath("$.versions[1].url").value(packageListDTO.getVersions().get(1).getUrl()))
        .andDo(document("listPackageVersions", responseFields(
            fieldWithPath("appName").description("Nome dell'applicazione. In questo servizio questo campo e unico, in quanto ritorna le informazioni di una singola applicazione"),
            fieldWithPath("versions").description("Array popolato dagli oggetti contenenti le informazioni delle versioni dell'applicazione presenti sul server"),
            fieldWithPath("versions[].id").description("Identificativo univoco della combinazione applicazione/versione (intero)"),
            fieldWithPath("versions[].appVersion").description("Numero progressivo della versione dell'applicazione (intero)"),
            fieldWithPath("versions[].fileName").description("Nome del file dell'applicazione presente sul server. L'estensione dei file delle applicazioni viene definita nel file 'application.properties' alla voce 'packages.file.extension'"),
            fieldWithPath("versions[].valid").description("Flag indicante la validità di un package. Se settato a false, il file corrispondente non puo essere scaricato tramite il servizio 'downloadPackage'"),
            fieldWithPath("versions[].url").description("URL pubblico al quale puo essere scaricato il file dell'applicazione. Questo URL e composto da un indirizzo base definito alla voce 'packages.web.base.url' nel file 'application.properties' e dal path relativo in cui e salvato il file dell'applicazione")
        )));
  }

  @Test
  public void testGetPackageVersionsNotFound() throws Exception {
    //given
    given(packageService.listAllVersions(anyString())).willThrow(PackageNotFoundException.class);
    //when
    mockMvc.perform(get(REST_SERVICES_BASE_URL + "/listPackages/notPresentAppName"))
        //then
        .andExpect(status().isNotFound())
        .andExpect(mvcResult -> assertTrue(mvcResult.getResolvedException() instanceof PackageNotFoundException))
        .andDo(document("listNotFoundPackageVersions"));
  }

  /*
   * listPackageVersionInfo
   */
  @Test
  public void testGetPackageVersion() throws Exception {
    PackageDTO packageDTO = PackageDTO.builder()
        .id(PACKAGE_V1_ID)
        .fileName(PACKAGE_FILENAME)
        .url(PACKAGES_WEBSERVER_BASEURL + "/" + PACKAGE_APPNAME + "/" + PACKAGE_VERSION_1 + "/" + PACKAGE_FILENAME + PACKAGES_FILE_EXTENSION)
        .valid(true)
        .appVersion(PACKAGE_VERSION_1)
        .appName(PACKAGE_APPNAME)
        .build();
    //given
    given(packageService.getPackageInfo(anyString(), anyInt())).willReturn(packageDTO);
    //when
    mockMvc.perform(get(LIST_VERSION_INFO_URL))
        //then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(packageDTO.getId()))
        .andExpect(jsonPath("$.appName").value(packageDTO.getAppName()))
        .andExpect(jsonPath("$.appVersion").value(packageDTO.getAppVersion()))
        .andExpect(jsonPath("$.fileName").value(packageDTO.getFileName()))
        .andExpect(jsonPath("$.valid").value(packageDTO.isValid()))
        .andExpect(jsonPath("$.url").value(packageDTO.getUrl()))
        .andDo(document("listPackageVersionInfo", responseFields(
            fieldWithPath("id").description("Identificativo univoco della combinazione applicazione/versione (intero)"),
            fieldWithPath("appName").description("Nome dell'applicazione. In questo servizio non viene ritornato un array di versioni, in quanto ritorna le informazioni di una singola versione dell'applicazione"),
            fieldWithPath("appVersion").description("Numero progressivo della versione dell'applicazione (intero)"),
            fieldWithPath("fileName").description("Nome del file dell'applicazione presente sul server. L'estensione dei file delle applicazioni viene definita nel file 'application.properties' alla voce 'packages.file.extension'"),
            fieldWithPath("valid").description("Flag indicante la validità di un package. Se settato a false, il file corrispondente non puo essere scaricato tramite il servizio 'downloadPackage'"),
            fieldWithPath("url").description("URL pubblico al quale puo essere scaricato il file dell'applicazione. Questo URL e composto da un indirizzo base definito alla voce 'packages.web.base.url' nel file 'application.properties' e dal path relativo in cui e salvato il file dell'applicazione")
        )));
  }

  @Test
  public void testGetPackageVersionNotFound() throws Exception {
    //given
    given(packageService.getPackageInfo(anyString(), anyInt())).willThrow(PackageNotFoundException.class);
    //when
    mockMvc.perform(get(REST_SERVICES_BASE_URL + "/listPackages/Spending/1010101"))
        //then
        .andExpect(status().isNotFound())
        .andExpect(mvcResult -> assertTrue(mvcResult.getResolvedException() instanceof PackageNotFoundException))
        .andDo(document("listNotfoundPackageVersionInfo"));
  }

  @Test
  public void testGetPackageVersionMalformedURL() throws Exception {
    //given
    //when
    mockMvc.perform(get(LIST_VERSION_INFO_URL_MALFORMED))
        //then
        .andExpect(status().isBadRequest())
        .andExpect(result -> assertTrue(result.getResolvedException() instanceof MalformedURLException))
        .andDo(document("listPackageVersionInfoMalformedURL"));
  }

  /*
   * getPackageById
   */
  @Test
  public void testGetPackageById() throws Exception {
    PackageDTO packageDTO = PackageDTO.builder()
        .id(PACKAGE_V1_ID)
        .fileName(PACKAGE_FILENAME)
        .url(PACKAGES_WEBSERVER_BASEURL + "/" + PACKAGE_APPNAME + "/" + PACKAGE_VERSION_1 + "/" + PACKAGE_FILENAME + PACKAGES_FILE_EXTENSION)
        .valid(true)
        .appVersion(PACKAGE_VERSION_1)
        .appName(PACKAGE_APPNAME)
        .build();
    //given
    given(packageService.getPackageInfoById(anyLong())).willReturn(packageDTO);
    //when
    mockMvc.perform(get(GET_PACKAGE_BY_ID_URL))
        //then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(packageDTO.getId()))
        .andExpect(jsonPath("$.appName").value(packageDTO.getAppName()))
        .andExpect(jsonPath("$.appVersion").value(packageDTO.getAppVersion()))
        .andExpect(jsonPath("$.fileName").value(packageDTO.getFileName()))
        .andExpect(jsonPath("$.valid").value(packageDTO.isValid()))
        .andExpect(jsonPath("$.url").value(packageDTO.getUrl()))
        .andDo(document("getPackageById", responseFields(
            fieldWithPath("id").description("Identificativo univoco della combinazione applicazione/versione (intero)"),
            fieldWithPath("appName").description("Nome dell'applicazione. In questo servizio non viene ritornato un array di versioni, in quanto ritorna le informazioni di una singola versione dell'applicazione"),
            fieldWithPath("appVersion").description("Numero progressivo della versione dell'applicazione (intero)"),
            fieldWithPath("fileName").description("Nome del file dell'applicazione presente sul server. L'estensione dei file delle applicazioni viene definita nel file 'application.properties' alla voce 'packages.file.extension'"),
            fieldWithPath("valid").description("Flag indicante la validità di un package. Se settato a false, il file corrispondente non puo essere scaricato tramite il servizio 'downloadPackage'"),
            fieldWithPath("url").description("URL pubblico al quale puo essere scaricato il file dell'applicazione. Questo URL e composto da un indirizzo base definito alla voce 'packages.web.base.url' nel file 'application.properties' e dal path relativo in cui e salvato il file dell'applicazione")
        )));
  }

  @Test
  public void testGetPackageByIdNotFound() throws Exception {
    //given
    given(packageService.getPackageInfoById(anyLong())).willThrow(PackageNotFoundException.class);
    //when
    mockMvc.perform(get(REST_SERVICES_BASE_URL + "/getPackage/1010101"))
        //then
        .andExpect(status().isNotFound())
        .andExpect(mvcResult -> assertTrue(mvcResult.getResolvedException() instanceof PackageNotFoundException))
        .andDo(document("getPackageByIdNotFound"));
  }

  @Test
  public void testGetPackageByIdMalformedURL() throws Exception {
    //given
    //when
    mockMvc.perform(get(GET_PACKAGE_BY_ID_URL_MALFORMED))
        //then
        .andExpect(status().isBadRequest())
        .andExpect(result -> assertTrue(result.getResolvedException() instanceof MalformedURLException))
        .andDo(document("getPackageByIdMalformedURL"));
  }

  /*
   * getPackagesById
   */
  @Test
  public void testGetPackagesByIdListParam() throws Exception {
    this.performGetPackagesById(REST_SERVICES_BASE_URL+"/getPackages?idList=100,200,300",
        anyList(), "getPackagesByIdListParams");
  }

  @Test
  public void testGetPackagesByIdRepeatedParamNames() throws Exception {
    this.performGetPackagesById(REST_SERVICES_BASE_URL+"/getPackages?idList=100&idList=200&idList=300",
        anyList(), "getPackagesByIdRepeatedParamNames");
  }

  @Test
  public void testGetPackagesByIdMalformedURL() throws Exception {
    this.performGetPackagesById(REST_SERVICES_BASE_URL+"/getPackages?idList=100,Nan,200,NaN,300",
        anyList(), "getPackagesByIdMalformedURL");
  }


  private void performGetPackagesById(String url, List<Long> idList, String docName) throws Exception {
    ArrayList<VersionDTO> versionDTOArrayList = new ArrayList<>();
    ArrayList<VersionDTO> lisXTeVersionDTOArrayList = new ArrayList<>();
    ArrayList<PackageListDTO> packageListDTOS = new ArrayList<>();
    PackageListDTO spendingPackageListDTO = PackageListDTO.builder()
        .appName(PACKAGE_APPNAME)
        .versions(versionDTOArrayList)
        .build();
    PackageListDTO lisXTePackageListDTO = PackageListDTO.builder()
        .appName("LisXTe")
        .versions(lisXTeVersionDTOArrayList)
        .build();
    VersionDTO lisXTeVersion1 = VersionDTO.builder()
        .id(300L)
        .appVersion(1)
        .fileName("LisXTe_1.0.10_b1.apk")
        .url(PACKAGES_WEBSERVER_BASEURL + "/LisXTe/1/LisXTe_1.0.10_b1.apk")
        .valid(true)
        .build();

    packageListDTOS.add(spendingPackageListDTO);
    packageListDTOS.add(lisXTePackageListDTO);

    versionDTOArrayList.add(version1DTO);
    versionDTOArrayList.add(version2DTO);

    lisXTeVersionDTOArrayList.add(lisXTeVersion1);
    //given
    given(packageService.getPackagesById(idList)).willReturn(packageListDTOS);
    //when
    mockMvc.perform(get(url))
        //then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(packageListDTOS.size()))
        .andExpect(jsonPath("$[0].appName").value(spendingPackageListDTO.getAppName()))
        .andExpect(jsonPath("$[0].versions").isArray())
        .andExpect(jsonPath("$[0].versions.length()").value(spendingPackageListDTO.getVersions().size()))
        .andExpect(jsonPath("$[0].versions[0].id").value(spendingPackageListDTO.getVersions().get(0).getId()))
        .andExpect(jsonPath("$[0].versions[0].appVersion").value(spendingPackageListDTO.getVersions().get(0).getAppVersion()))
        .andExpect(jsonPath("$[0].versions[0].fileName").value(spendingPackageListDTO.getVersions().get(0).getFileName()))
        .andExpect(jsonPath("$[0].versions[0].valid").value(spendingPackageListDTO.getVersions().get(0).isValid()))
        .andExpect(jsonPath("$[0].versions[0].url").value(spendingPackageListDTO.getVersions().get(0).getUrl()))
        .andExpect(jsonPath("$[0].versions[1].id").value(spendingPackageListDTO.getVersions().get(1).getId()))
        .andExpect(jsonPath("$[0].versions[1].appVersion").value(spendingPackageListDTO.getVersions().get(1).getAppVersion()))
        .andExpect(jsonPath("$[0].versions[1].fileName").value(spendingPackageListDTO.getVersions().get(1).getFileName()))
        .andExpect(jsonPath("$[0].versions[1].valid").value(spendingPackageListDTO.getVersions().get(1).isValid()))
        .andExpect(jsonPath("$[0].versions[1].url").value(spendingPackageListDTO.getVersions().get(1).getUrl()))
        .andDo(document(docName, responseFields(
            fieldWithPath("[]").description("Array popolato dagli oggetti contenenti le informazioni delle applicazioni presenti sul server"),
            fieldWithPath("[].appName").description("Nome dell'applicazione"),
            fieldWithPath("[].versions").description("Array popolato dagli oggetti contenenti le informazioni delle versioni dell'applicazione presenti sul server"),
            fieldWithPath("[].versions[].id").description("Identificativo univoco della combinazione applicazione/versione (intero)"),
            fieldWithPath("[].versions[].appVersion").description("Numero progressivo della versione dell'applicazione (intero)"),
            fieldWithPath("[].versions[].fileName").description("Nome del file dell'applicazione presente sul server. L'estensione dei file delle applicazioni viene definita nel file 'application.properties' alla voce 'packages.file.extension'"),
            fieldWithPath("[].versions[].valid").description("Flag indicante la validità di un package. Se settato a false, il file corrispondente non puo essere scaricato tramite il servizio 'downloadPackage'"),
            fieldWithPath("[].versions[].url").description("URL pubblico al quale puo essere scaricato il file dell'applicazione. Questo URL e composto da un indirizzo base definito alla voce 'packages.web.base.url' nel file 'application.properties' e dal path relativo in cui e salvato il file dell'applicazione")
        )));
  }

  @Test
  public void testGetPackagesByIdNotFound() throws Exception {
    ArrayList<PackageListDTO> packageListDTOS = new ArrayList<>();
    //given
    //when
    mockMvc.perform(get(REST_SERVICES_BASE_URL+"/getPackages?idList=1010101,2020202"))
        //then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(packageListDTOS.size()))
        .andDo(document("getPackagesByIdEmpty", responseFields(
            fieldWithPath("[]").description("Nel caso in cui nessun package corrisponda a nessuno degli id richiesti, il servizio ritorna un array vuoto.")
        )));
  }

  @Test
  public void testGetPackagesByIdEmptyParamList() throws Exception {
    ArrayList<PackageListDTO> packageListDTOS = new ArrayList<>();
    //given
    //when
    mockMvc.perform(get(REST_SERVICES_BASE_URL+"/getPackages?idList="))
        //then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(packageListDTOS.size()))
        .andDo(document("getPackagesByIdEmptyParamList", responseFields(
            fieldWithPath("[]").description("Nel caso in cui nessun package corrisponda a nessuno degli id richiesti, il servizio ritorna un array vuoto.")
        )));
  }

  @Test
  public void testGetPackagesByIdWithoutParams() throws Exception {
    ArrayList<PackageListDTO> packageListDTOS = new ArrayList<>();
    //given
    //when
    mockMvc.perform(get(REST_SERVICES_BASE_URL + "/getPackages"))
        //then
        .andExpect(status().isBadRequest())
        .andDo(document("getPackagesByIdNoParams"));
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
        .andExpect(content().bytes(DUMMY_BYTE_ARRAY))
        .andDo(document("downloadPackageFile"));
  }

  @Test
  public void testGetPackageFileNotFound() throws Exception {
    //given
    given(packageService.getPackageFile(anyString(), anyInt())).willThrow(PackageNotFoundException.class);
    //when
    mockMvc.perform(get(GET_PACKAGE_FILE_URL))
        //then
        .andExpect(status().isNotFound())
        .andExpect(mvcResult -> assertTrue(mvcResult.getResolvedException() instanceof PackageNotFoundException))
        .andDo(document("downloadNotFoundPackageFile"));
  }

  @Test
  public void testGetPackageFileMalformedURL() throws Exception {
    //given
    //when
    mockMvc.perform(get(GET_PACKAGE_FILE_URL_MALFORMED))
        //then
        .andExpect(status().isBadRequest())
        .andExpect(result -> assertTrue(result.getResolvedException() instanceof MalformedURLException))
        .andDo(document("downloadPackageFileMalformedURL"));
  }

  @Test
  public void testGetPackageFileInvalidPackage() throws Exception {
    //given
    given(packageService.getPackageFile(anyString(), anyInt())).willThrow(InvalidPackageException.class);
    //when
    mockMvc.perform(get(GET_PACKAGE_FILE_URL))
        //then
        .andExpect(status().isForbidden())
        .andExpect(mvcResult -> assertTrue(mvcResult.getResolvedException() instanceof InvalidPackageException))
        .andDo(document("downloadPackageInvalidFile"));
  }

  @Test
  public void testGetPackageFileUnableToReadFile() throws Exception {
    //given
    given(packageService.getPackageFile(anyString(), anyInt())).willThrow(IOFileException.class);
    //when
    mockMvc.perform(get(GET_PACKAGE_FILE_URL))
        //then
        .andExpect(status().isInternalServerError())
        .andExpect(mvcResult -> assertTrue(mvcResult.getResolvedException() instanceof IOFileException))
        .andDo(document("downloadPackageUnreadableFile"));
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
        .andExpect(status().isOk())
        .andDo(document("invalidatePackage"));
  }

  @Test
  public void testInvalidatePackageNotFound() throws Exception {
    //given
    doThrow(PackageNotFoundException.class).when(packageService).invalidatePackage(anyString(), anyInt());
    //when
    mockMvc.perform(patch(INVALIDATE_PACKAGE_URL))
        //then
        .andExpect(status().isNotFound())
        .andExpect(mvcResult -> assertTrue(mvcResult.getResolvedException() instanceof PackageNotFoundException))
        .andDo(document("invalidateNotFoundPackage"));
  }

  @Test
  public void testInvalidatePackageMalformedURL() throws Exception {
    //given
    //when
    mockMvc.perform(patch(INVALIDATE_PACKAGE_URL_MALFORMED))
        //then
        .andExpect(status().isBadRequest())
        .andExpect(result -> assertTrue(result.getResolvedException() instanceof MalformedURLException))
        .andDo(document("invalidatePackageMalformedURL"));
  }

  /*
   * uploadPackage
   */
  @Test
  public void testUploadPackageFile() throws Exception {
    //given
    doNothing().when(packageService).installPackageFile(anyString(), anyInt(), anyString(), any(byte[].class));
    //when
    mockMvc.perform(post(UPLOAD_PACKAGE_URL).content(DUMMY_BYTE_ARRAY).contentType(MediaType.APPLICATION_OCTET_STREAM))
        //then
        .andExpect(status().isCreated())
        .andDo(document("uploadPackage",
            requestHeaders(headerWithName("content-type").description("Deve essere settato a 'application/octet-stream' affinché la request sia accettata")),
            requestBody()));
  }

  @Test
  public void testUploadPackageFileMalformedURL() throws Exception {
    //given
    //when
    mockMvc.perform(post(UPLOAD_PACKAGE_URL_MALFORMED)
        .content(DUMMY_BYTE_ARRAY)
        .contentType(MediaType.APPLICATION_OCTET_STREAM))
        //then
        .andExpect(status().isBadRequest())
        .andExpect(result -> assertTrue(result.getResolvedException() instanceof MalformedURLException))
        .andDo(document("uploadPackageMalformedURL"));
  }

  @Test
  public void testUploadPackageFileUnableToWriteFile() throws Exception {
    //given
    doThrow(IOFileException.class).when(packageService).installPackageFile(anyString(), anyInt(), anyString(),
        any(byte[].class));
    //when
    mockMvc.perform(post(UPLOAD_PACKAGE_URL).content(DUMMY_BYTE_ARRAY).contentType(MediaType.APPLICATION_OCTET_STREAM))
        //then
        .andExpect(status().isInternalServerError())
        .andExpect(mvcResult -> assertTrue(mvcResult.getResolvedException() instanceof IOFileException))
        .andDo(document("uploadNotWritablePackage"));
  }
}