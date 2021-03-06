package com.lambda_expressions.package_manager.v1.controllers;

import com.lambda_expressions.package_manager.exceptions.*;
import com.lambda_expressions.package_manager.services.PackageService;
import com.lambda_expressions.package_manager.v1.RESTExceptionHandler;
import com.lambda_expressions.package_manager.v1.controllers.utils.ControllerUtils;
import com.lambda_expressions.package_manager.v1.model.PackageDTO;
import com.lambda_expressions.package_manager.v1.model.PackageListDTO;
import com.lambda_expressions.package_manager.v1.model.VersionDTO;
import oracle.net.ano.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Created by steccothal
 * on Saturday 23 January 2021
 * at 10:42 AM
 */
@ExtendWith({MockitoExtension.class, RestDocumentationExtension.class})
class PackageManagerControllerTest {
  private static final String PACKAGES_FILE_EXTENSION = ".apk";
  private static final String PACKAGES_WEBSERVER_BASEURL = "https://www.package-manager.com/warehouse";
  private static final String PACKAGE_FILENAME = "Spending_1.9.apk";
  private static final String PACKAGE_PACKAGENAME = "com.package.spending";
  private static final String PACKAGE_APPNAME = "Spending";
  private static final long PACKAGE_V1_ID = 100;
  private static final long PACKAGE_V2_ID = 200;
  private static final String PACKAGE_VERSION_1 = "1.9";
  private static final String PACKAGE_VERSION_2 = "2.5";
  private static final long PACKAGE_VERSION_NUMBER_1 = 1;
  private static final long PACKAGE_VERSION_NUMBER_2 = 2;
  private static final byte[] DUMMY_BYTE_ARRAY = "This is the payload of an uploadPackage request, the byte array of a package file".getBytes();

  private static final String REST_SERVICES_BASE_URL = "/api/v1";
  private static final String LIST_ALL_PACKAGES_URL = REST_SERVICES_BASE_URL + "/listPackages";
  private static final String LIST_PACKAGE_VERSIONS_URL = REST_SERVICES_BASE_URL + "/listPackages/{appName}";
  private static final String LIST_VERSION_INFO_URL = REST_SERVICES_BASE_URL + "/listPackages/{appName}/{appVersion}";
  private static final String DELETE_PACKAGES_BY_ID_URL = REST_SERVICES_BASE_URL + "/deletePackage";
  private static final String DELETE_PACKAGE_VERSIONS_URL = REST_SERVICES_BASE_URL + "/deletePackage/{appName}";
  private static final String DELETE_SINGLE_VERSION_URL = REST_SERVICES_BASE_URL + "/deletePackage/{appName}/{appVersion}";
  private static final String GET_PACKAGE_BY_ID_URL = REST_SERVICES_BASE_URL + "/getPackage/{appId}";
  private static final String GET_PACKAGES_BY_ID_URL = REST_SERVICES_BASE_URL + "/getPackages/";
  private static final String INVALIDATE_PACKAGE_URL = REST_SERVICES_BASE_URL + "/invalidatePackage/{appName}/{appVersion}";
  private static final String UPLOAD_PACKAGE_URL = REST_SERVICES_BASE_URL + "/uploadPackage";
  private static final String UPLOAD_PACKAGE_WITH_PARAMS_URL = REST_SERVICES_BASE_URL + "/uploadPackage/{packageName}/{appName}/{appVersion}/{appVersionNumber}/{fileName}";
  private static final String GET_PACKAGE_FILE_URL = REST_SERVICES_BASE_URL + "/downloadPackage/{appName}/{appVersion}";

  private static final String GET_PACKAGES_PARAM_NAME = "idList";

  private static final VersionDTO version1DTO = new VersionDTO(
      PACKAGE_V1_ID,
      PACKAGE_VERSION_1,
      PACKAGE_VERSION_NUMBER_1,
      PACKAGE_FILENAME,
      PACKAGES_WEBSERVER_BASEURL + "/" + PACKAGE_APPNAME + "/" + PACKAGE_VERSION_1 + "/" + PACKAGE_FILENAME + PACKAGES_FILE_EXTENSION,
      true
  );
  private static final VersionDTO version2DTO = new VersionDTO(
      PACKAGE_V2_ID,
      PACKAGE_VERSION_2,
      PACKAGE_VERSION_NUMBER_2,
      "Spending_2.5.apk",
      PACKAGES_WEBSERVER_BASEURL + "/" + PACKAGE_APPNAME + "/" + PACKAGE_VERSION_2 + "/" + "Spending_2.5" + PACKAGES_FILE_EXTENSION,
      true
  );

  private final FieldDescriptor[] globalArrayField = new FieldDescriptor[]{
      fieldWithPath("[]").description("Array popolato dagli oggetti contenenti le informazioni delle applicazioni presenti sul server")
  };
  private final FieldDescriptor[] packageFields = new FieldDescriptor[]{
      fieldWithPath("appName").description("Nome dell'applicazione"),
      fieldWithPath("packageName").description("Nome del package del'applicazione"),
  };
  private final FieldDescriptor[] versionsField = new FieldDescriptor[]{
      fieldWithPath("versions").description("Array popolato dagli oggetti contenenti le informazioni delle versioni dell'applicazione presenti sul server")
  };
  private final FieldDescriptor[] versionFields = new FieldDescriptor[]{
      fieldWithPath("id").description("Identificativo univoco della combinazione applicazione/versione (intero)"),
      fieldWithPath("appVersion").description("Identificativo della versione dell'applicazione (valore alfanumerico)"),
      fieldWithPath("appVersionNumber").description("Valore incrementale della versione dell'applicazione (valore numerico)"),
      fieldWithPath("fileName").description("Nome del file dell'applicazione presente sul server."),
      fieldWithPath("valid").description("Flag indicante la validità di un package. Se settato a false, il file corrispondente non puo essere scaricato tramite il servizio 'downloadPackage'"),
      fieldWithPath("url").description("URL del servizio warehouse dal quale puo essere scaricato il file dell'applicazione.")

  };
  @Spy
  ControllerUtils controllerUtils;
  @Mock
  PackageService packageService;
  @InjectMocks
  PackageManagerController controller;
  MockMvc mockMvc;

  @BeforeEach
  public void setUp(RestDocumentationContextProvider documentationContextProvider) {
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new RESTExceptionHandler())
        .apply(documentationConfiguration(documentationContextProvider)
            .uris()
            .withHost("www.package-manager.com")
            .withPort(8080)
            .withScheme("https")
        )
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
    PackageListDTO spendingPackageListDTO = new PackageListDTO(
        PACKAGE_APPNAME,
        PACKAGE_PACKAGENAME,
        versionDTOArrayList
    );
    PackageListDTO lisXTePackageListDTO = new PackageListDTO(
        "LisXTe",
        "com.package.LisXTe",
        lisXTeVersionDTOArrayList
    );
    VersionDTO lisXTeVersion1 = new VersionDTO(
        300L,
        "1",
        1L,
        "LisXTe_1.0.10_b1.apk",
        PACKAGES_WEBSERVER_BASEURL + "/LisXTe/1/LisXTe_1.0.10_b1.apk",
        true
    );

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
        .andExpect(jsonPath("$[0].packageName").value(spendingPackageListDTO.getPackageName()))
        .andExpect(jsonPath("$[0].versions").isArray())
        .andExpect(jsonPath("$[0].versions.length()").value(spendingPackageListDTO.getVersions().size()))
        .andExpect(jsonPath("$[0].versions[0].id").value(spendingPackageListDTO.getVersions().get(0).getId()))
        .andExpect(jsonPath("$[0].versions[0].appVersion").value(spendingPackageListDTO.getVersions().get(0).getAppVersion()))
        .andExpect(jsonPath("$[0].versions[0].appVersionNumber").value(spendingPackageListDTO.getVersions().get(0).getAppVersionNumber()))
        .andExpect(jsonPath("$[0].versions[0].fileName").value(spendingPackageListDTO.getVersions().get(0).getFileName()))
        .andExpect(jsonPath("$[0].versions[0].valid").value(spendingPackageListDTO.getVersions().get(0).isValid()))
        .andExpect(jsonPath("$[0].versions[0].url").value(spendingPackageListDTO.getVersions().get(0).getUrl()))
        .andExpect(jsonPath("$[0].versions[1].id").value(spendingPackageListDTO.getVersions().get(1).getId()))
        .andExpect(jsonPath("$[0].versions[1].appVersion").value(spendingPackageListDTO.getVersions().get(1).getAppVersion()))
        .andExpect(jsonPath("$[0].versions[1].appVersionNumber").value(spendingPackageListDTO.getVersions().get(1).getAppVersionNumber()))
        .andExpect(jsonPath("$[0].versions[1].fileName").value(spendingPackageListDTO.getVersions().get(1).getFileName()))
        .andExpect(jsonPath("$[0].versions[1].valid").value(spendingPackageListDTO.getVersions().get(1).isValid()))
        .andExpect(jsonPath("$[0].versions[1].url").value(spendingPackageListDTO.getVersions().get(1).getUrl()))
        .andDo(document("listAllPackages",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            responseFields(globalArrayField)
                .andWithPrefix("[].", packageFields)
                .andWithPrefix("[].", versionsField)
                .andWithPrefix("[].versions[].", versionFields)
        ));
  }

  @Test
  public void testGetAllPackagesEmptyList() throws Exception {
    //given
    //when
    mockMvc.perform(get(LIST_ALL_PACKAGES_URL))
        //then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0))
        .andDo(document("listAllPackagesEmpty",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            responseFields(
                fieldWithPath("[]").description("Nel caso in cui nessun package sia presente sul server, il servizio ritorna un array vuoto")
            )
        ));
  }

  /*
   * listPackageVersions
   */
  @Test
  public void testGetPackageVersions() throws Exception {
    ArrayList<VersionDTO> versionDTOArrayList = new ArrayList<>();
    PackageListDTO packageListDTO = new PackageListDTO(
        PACKAGE_APPNAME,
        PACKAGE_PACKAGENAME,
        versionDTOArrayList
    );
    versionDTOArrayList.add(version1DTO);
    versionDTOArrayList.add(version2DTO);

    //given
    given(packageService.listAllVersions(anyString())).willReturn(packageListDTO);
    //when
    mockMvc.perform(get(LIST_PACKAGE_VERSIONS_URL, PACKAGE_APPNAME))
        //then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.appName").value(packageListDTO.getAppName()))
        .andExpect(jsonPath("$.packageName").value(packageListDTO.getPackageName()))
        .andExpect(jsonPath("$.versions").isArray())
        .andExpect(jsonPath("$.versions.length()").value(packageListDTO.getVersions().size()))
        .andExpect(jsonPath("$.versions[0].id").value(packageListDTO.getVersions().get(0).getId()))
        .andExpect(jsonPath("$.versions[0].appVersion").value(packageListDTO.getVersions().get(0).getAppVersion()))
        .andExpect(jsonPath("$.versions[0].appVersionNumber").value(packageListDTO.getVersions().get(0).getAppVersionNumber()))
        .andExpect(jsonPath("$.versions[0].fileName").value(packageListDTO.getVersions().get(0).getFileName()))
        .andExpect(jsonPath("$.versions[0].valid").value(packageListDTO.getVersions().get(0).isValid()))
        .andExpect(jsonPath("$.versions[0].url").value(packageListDTO.getVersions().get(0).getUrl()))
        .andExpect(jsonPath("$.versions[1].id").value(packageListDTO.getVersions().get(1).getId()))
        .andExpect(jsonPath("$.versions[1].appVersion").value(packageListDTO.getVersions().get(1).getAppVersion()))
        .andExpect(jsonPath("$.versions[1].appVersionNumber").value(packageListDTO.getVersions().get(1).getAppVersionNumber()))
        .andExpect(jsonPath("$.versions[1].fileName").value(packageListDTO.getVersions().get(1).getFileName()))
        .andExpect(jsonPath("$.versions[1].valid").value(packageListDTO.getVersions().get(1).isValid()))
        .andExpect(jsonPath("$.versions[1].url").value(packageListDTO.getVersions().get(1).getUrl()))
        .andDo(document("listPackageVersions",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(parameterWithName("appName").description("Nome dell'applicazione ricercata, corrispondente al campo ritornato da questo servizio")),
            responseFields(packageFields)
                .and(versionsField)
                .andWithPrefix("versions[].", versionFields)
        ));
  }

  @Test
  public void testGetPackageVersionsNotFound() throws Exception {
    //given
    given(packageService.listAllVersions(anyString())).willThrow(PackageNotFoundException.class);
    //when
    mockMvc.perform(get(LIST_PACKAGE_VERSIONS_URL, "appNotPresent"))
        //then
        .andExpect(status().isNotFound())
        .andExpect(mvcResult -> assertTrue(mvcResult.getResolvedException() instanceof PackageNotFoundException))
        .andDo(document("listNotFoundPackageVersions",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(parameterWithName("appName").description("Nome dell'applicazione ricercata"))
        ));
  }

  /*
   * listPackageVersionInfo
   */
  @Test
  public void testGetPackageVersion() throws Exception {
    PackageDTO packageDTO = new PackageDTO(
        PACKAGE_V1_ID,
        PACKAGE_VERSION_1,
        PACKAGE_VERSION_NUMBER_1,
        PACKAGE_FILENAME,
        PACKAGES_WEBSERVER_BASEURL + "/" + PACKAGE_APPNAME + "/" + PACKAGE_VERSION_1 + "/" + PACKAGE_FILENAME + PACKAGES_FILE_EXTENSION,
        true,
        PACKAGE_APPNAME,
        PACKAGE_PACKAGENAME
    );
    //given
    given(packageService.getPackageInfo(anyString(), anyString())).willReturn(packageDTO);
    //when
    mockMvc.perform(get(LIST_VERSION_INFO_URL, PACKAGE_APPNAME, PACKAGE_VERSION_1))
        //then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(packageDTO.getId()))
        .andExpect(jsonPath("$.appName").value(packageDTO.getAppName()))
        .andExpect(jsonPath("$.packageName").value(packageDTO.getPackageName()))
        .andExpect(jsonPath("$.appVersion").value(packageDTO.getAppVersion()))
        .andExpect(jsonPath("$.appVersionNumber").value(packageDTO.getAppVersionNumber()))
        .andExpect(jsonPath("$.fileName").value(packageDTO.getFileName()))
        .andExpect(jsonPath("$.valid").value(packageDTO.isValid()))
        .andExpect(jsonPath("$.url").value(packageDTO.getUrl()))
        .andDo(document("listPackageVersionInfo",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(
                parameterWithName("appName").description("Nome dell'applicazione ricercata"),
                parameterWithName("appVersion").description("Identificativo della versione dell'applicazione ricercata")
            ),
            responseFields(packageFields)
                .and(versionFields)
        ));
  }

  @Test
  public void testGetPackageVersionNotFound() throws Exception {
    //given
    given(packageService.getPackageInfo(anyString(), anyString())).willThrow(PackageNotFoundException.class);
    //when
    mockMvc.perform(get(LIST_VERSION_INFO_URL, PACKAGE_APPNAME, "100.200.300"))
        //then
        .andExpect(status().isNotFound())
        .andExpect(mvcResult -> assertTrue(mvcResult.getResolvedException() instanceof PackageNotFoundException))
        .andDo(document("listNotfoundPackageVersionInfo",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(
                parameterWithName("appName").description("Nome dell'applicazione ricercata"),
                parameterWithName("appVersion").description("Identificativo della versione dell'applicazione ricercata")
            )
        ));
  }

  /*
   * getPackageById
   */
  @Test
  public void testGetPackageById() throws Exception {
    PackageDTO packageDTO = new PackageDTO(
        PACKAGE_V1_ID,
        PACKAGE_VERSION_1,
        PACKAGE_VERSION_NUMBER_1,
        PACKAGE_FILENAME,
        PACKAGES_WEBSERVER_BASEURL + "/" + PACKAGE_APPNAME + "/" + PACKAGE_VERSION_1 + "/" + PACKAGE_FILENAME + PACKAGES_FILE_EXTENSION,
        true,
        PACKAGE_APPNAME,
        PACKAGE_PACKAGENAME
    );
    //given
    given(packageService.getPackageInfoById(anyLong())).willReturn(packageDTO);
    //when
    mockMvc.perform(get(GET_PACKAGE_BY_ID_URL, PACKAGE_V1_ID))
        //then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(packageDTO.getId()))
        .andExpect(jsonPath("$.appName").value(packageDTO.getAppName()))
        .andExpect(jsonPath("$.packageName").value(packageDTO.getPackageName()))
        .andExpect(jsonPath("$.appVersion").value(packageDTO.getAppVersion()))
        .andExpect(jsonPath("$.appVersionNumber").value(packageDTO.getAppVersionNumber()))
        .andExpect(jsonPath("$.fileName").value(packageDTO.getFileName()))
        .andExpect(jsonPath("$.valid").value(packageDTO.isValid()))
        .andExpect(jsonPath("$.url").value(packageDTO.getUrl()))
        .andDo(document("getPackageById",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(parameterWithName("appId").description("Identificativo univoco della specifica combinazione applicazione/versione ricercata")),
            responseFields(packageFields)
                .and(versionFields)
        ));
  }

  @Test
  public void testGetPackageByIdNotFound() throws Exception {
    //given
    given(packageService.getPackageInfoById(anyLong())).willThrow(PackageNotFoundException.class);
    //when
    mockMvc.perform(get(GET_PACKAGE_BY_ID_URL, "1010101"))
        //then
        .andExpect(status().isNotFound())
        .andExpect(mvcResult -> assertTrue(mvcResult.getResolvedException() instanceof PackageNotFoundException))
        .andDo(document("getPackageByIdNotFound",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(parameterWithName("appId").description("Id dell'applicazione ricercata"))
        ));
  }

  @Test
  public void testGetPackageByIdMalformedURL() throws Exception {
    //given
    //when
    mockMvc.perform(get(GET_PACKAGE_BY_ID_URL, "NaN"))
        //then
        .andExpect(status().isBadRequest())
        .andExpect(result -> assertTrue(result.getResolvedException() instanceof MalformedURLException))
        .andDo(document("getPackageByIdMalformedURL",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(parameterWithName("appId").description("Id dell'applicazione ricercata"))
        ));
  }

  /*
   * getPackagesById
   */
  @Test
  public void testGetPackagesByIdListParam() throws Exception {
    this.performGetPackagesById(
        get(GET_PACKAGES_BY_ID_URL + "?idList=100,200,300"),
        "getPackagesByIdListParams");
  }

  @Test
  public void testGetPackagesByIdRepeatedParamNames() throws Exception {
    this.performGetPackagesById(
        get(GET_PACKAGES_BY_ID_URL)
            .queryParam(GET_PACKAGES_PARAM_NAME, "100", "200", "300"),
        "getPackagesByIdRepeatedParamNames");
  }

  @Test
  public void testGetPackagesByIdMalformedURL() throws Exception {
    this.performGetPackagesById(
        get(GET_PACKAGES_BY_ID_URL)
            .queryParam(GET_PACKAGES_PARAM_NAME, "100", "NaN", "200", "NaN", "300"),
        "getPackagesByIdMalformedURL");
  }


  private void performGetPackagesById(MockHttpServletRequestBuilder get, String docName) throws Exception {
    ArrayList<VersionDTO> versionDTOArrayList = new ArrayList<>();
    ArrayList<VersionDTO> lisXTeVersionDTOArrayList = new ArrayList<>();
    ArrayList<PackageListDTO> packageListDTOS = new ArrayList<>();
    PackageListDTO spendingPackageListDTO = new PackageListDTO(
        PACKAGE_APPNAME,
        PACKAGE_PACKAGENAME,
        versionDTOArrayList
    );
    PackageListDTO lisXTePackageListDTO = new PackageListDTO(
        "LisXTe",
        "com.package.LisXTe",
        lisXTeVersionDTOArrayList
    );
    VersionDTO lisXTeVersion1 = new VersionDTO(
        300L,
        "1",
        1L,
        "LisXTe_1.0.10_b1.apk",
        PACKAGES_WEBSERVER_BASEURL + "/LisXTe/1/LisXTe_1.0.10_b1.apk",
        true
    );

    packageListDTOS.add(spendingPackageListDTO);
    packageListDTOS.add(lisXTePackageListDTO);

    versionDTOArrayList.add(version1DTO);
    versionDTOArrayList.add(version2DTO);

    lisXTeVersionDTOArrayList.add(lisXTeVersion1);
    //given
    given(packageService.getPackagesById(anyList())).willReturn(packageListDTOS);
    //when
    mockMvc.perform(get)
        //then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(packageListDTOS.size()))
        .andExpect(jsonPath("$[0].appName").value(spendingPackageListDTO.getAppName()))
        .andExpect(jsonPath("$[0].packageName").value(spendingPackageListDTO.getPackageName()))
        .andExpect(jsonPath("$[0].versions").isArray())
        .andExpect(jsonPath("$[0].versions.length()").value(spendingPackageListDTO.getVersions().size()))
        .andExpect(jsonPath("$[0].versions[0].id").value(spendingPackageListDTO.getVersions().get(0).getId()))
        .andExpect(jsonPath("$[0].versions[0].appVersion").value(spendingPackageListDTO.getVersions().get(0).getAppVersion()))
        .andExpect(jsonPath("$[0].versions[0].appVersionNumber").value(spendingPackageListDTO.getVersions().get(0).getAppVersionNumber()))
        .andExpect(jsonPath("$[0].versions[0].fileName").value(spendingPackageListDTO.getVersions().get(0).getFileName()))
        .andExpect(jsonPath("$[0].versions[0].valid").value(spendingPackageListDTO.getVersions().get(0).isValid()))
        .andExpect(jsonPath("$[0].versions[0].url").value(spendingPackageListDTO.getVersions().get(0).getUrl()))
        .andExpect(jsonPath("$[0].versions[1].id").value(spendingPackageListDTO.getVersions().get(1).getId()))
        .andExpect(jsonPath("$[0].versions[1].appVersion").value(spendingPackageListDTO.getVersions().get(1).getAppVersion()))
        .andExpect(jsonPath("$[0].versions[1].appVersionNumber").value(spendingPackageListDTO.getVersions().get(1).getAppVersionNumber()))
        .andExpect(jsonPath("$[0].versions[1].fileName").value(spendingPackageListDTO.getVersions().get(1).getFileName()))
        .andExpect(jsonPath("$[0].versions[1].valid").value(spendingPackageListDTO.getVersions().get(1).isValid()))
        .andExpect(jsonPath("$[0].versions[1].url").value(spendingPackageListDTO.getVersions().get(1).getUrl()))
        .andDo(document(docName,
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestParameters(
                parameterWithName(GET_PACKAGES_PARAM_NAME).description("Lista degli id delle applicazioni ricercate")
            ),
            responseFields(globalArrayField)
                .andWithPrefix("[].", packageFields)
                .andWithPrefix("[].", versionsField)
                .andWithPrefix("[].versions[].", versionFields)
        ));
  }

  @Test
  public void testGetPackagesByIdNotFound() throws Exception {
    //given
    given(packageService.getPackagesById(anyList())).willReturn(Collections.EMPTY_LIST);
    //when
    mockMvc.perform(
        get(GET_PACKAGES_BY_ID_URL)
            .queryParam(GET_PACKAGES_PARAM_NAME, "10101", "20202", "30303"))
        //then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0))
        .andDo(document("getPackagesByIdEmpty",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestParameters(
                parameterWithName(GET_PACKAGES_PARAM_NAME).description("Elenco degli id delle applicazioni ricercate")
            ),
            responseFields(
                fieldWithPath("[]").description("Nel caso in cui nessun package corrisponda a nessuno degli id richiesti, il servizio ritorna un array vuoto.")
            )
        ));
  }

  @Test
  public void testGetPackagesByIdEmptyParamList() throws Exception {
    //given
    //when
    mockMvc.perform(
        get(GET_PACKAGES_BY_ID_URL)
            .queryParam(GET_PACKAGES_PARAM_NAME, ""))
        //then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0))
        .andDo(document("getPackagesByIdEmptyParamList",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestParameters(
                parameterWithName(GET_PACKAGES_PARAM_NAME).description("Lista degli id delle applicazioni ricercate")
            ),
            responseFields(
                fieldWithPath("[]").description("Nel caso in cui nessun package corrisponda a nessuno degli id richiesti, il servizio ritorna un array vuoto.")
            )
        ));
  }

  @Test
  public void testGetPackagesByIdWithoutParams() throws Exception {
    //given
    //when
    mockMvc.perform(get(GET_PACKAGES_BY_ID_URL))
        //then
        .andExpect(status().isBadRequest())
        .andDo(document("getPackagesByIdNoParams",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())
        ));
  }

  /*
   * downloadPackage
   */
  @Test
  public void testGetPackageFile() throws Exception {
    //given
    given(packageService.getPackageFile(anyString(), anyString())).willReturn(DUMMY_BYTE_ARRAY);
    //when
    mockMvc.perform(get(GET_PACKAGE_FILE_URL, PACKAGE_APPNAME, PACKAGE_VERSION_1))
        //then
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
        .andExpect(content().bytes(DUMMY_BYTE_ARRAY))
        .andDo(document("downloadPackageFile",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(
                parameterWithName("appName").description("Nome dell'applicazione ricercata"),
                parameterWithName("appVersion").description("Identificativo della versione dell'applicazione ricercata")
            )
        ));
  }

  @Test
  public void testGetPackageFileNotFound() throws Exception {
    //given
    given(packageService.getPackageFile(anyString(), anyString())).willThrow(PackageNotFoundException.class);
    //when
    mockMvc.perform(get(GET_PACKAGE_FILE_URL, "appNotPresent", PACKAGE_VERSION_1))
        //then
        .andExpect(status().isNotFound())
        .andExpect(mvcResult -> assertTrue(mvcResult.getResolvedException() instanceof PackageNotFoundException))
        .andDo(document("downloadNotFoundPackageFile",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(
                parameterWithName("appName").description("Nome dell'applicazione ricercata"),
                parameterWithName("appVersion").description("Identificativo della versione dell'applicazione ricercata")
            )
        ));
  }

  @Test
  public void testGetPackageFileInvalidPackage() throws Exception {
    //given
    given(packageService.getPackageFile(anyString(), anyString())).willThrow(InvalidPackageException.class);
    //when
    mockMvc.perform(get(GET_PACKAGE_FILE_URL, "invalidApp", PACKAGE_VERSION_1))
        //then
        .andExpect(status().isForbidden())
        .andExpect(mvcResult -> assertTrue(mvcResult.getResolvedException() instanceof InvalidPackageException))
        .andDo(document("downloadPackageInvalidFile",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(
                parameterWithName("appName").description("Nome dell'applicazione ricercata"),
                parameterWithName("appVersion").description("Identificativo della versione dell'applicazione ricercata")
            )
        ));
  }

  @Test
  public void testGetPackageFileUnableToReadFile() throws Exception {
    //given
    given(packageService.getPackageFile(anyString(), anyString())).willThrow(IOFileException.class);
    //when
    mockMvc.perform(get(GET_PACKAGE_FILE_URL, PACKAGE_APPNAME, PACKAGE_VERSION_1))
        //then
        .andExpect(status().isInternalServerError())
        .andExpect(mvcResult -> assertTrue(mvcResult.getResolvedException() instanceof IOFileException))
        .andDo(document("downloadPackageUnreadableFile",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(
                parameterWithName("appName").description("Nome dell'applicazione ricercata"),
                parameterWithName("appVersion").description("Identificativo della versione dell'applicazione ricercata")
            )
        ));
  }

  /*
   * invalidatePackage
   */
  @Test
  public void testInvalidatePackage() throws Exception {
    //given
    doNothing().when(packageService).invalidatePackage(anyString(), anyString());
    //when
    mockMvc.perform(patch(INVALIDATE_PACKAGE_URL, PACKAGE_APPNAME, PACKAGE_VERSION_1))
        //then
        .andExpect(status().isOk())
        .andDo(document("invalidatePackage",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(
                parameterWithName("appName").description("Nome dell'applicazione da invalidare"),
                parameterWithName("appVersion").description("Identificativo della versione dell'applicazione da invalidare")
            )
        ));
  }

  @Test
  public void testInvalidatePackageNotFound() throws Exception {
    //given
    doThrow(PackageNotFoundException.class).when(packageService).invalidatePackage(anyString(), anyString());
    //when
    mockMvc.perform(patch(INVALIDATE_PACKAGE_URL, PACKAGE_APPNAME, PACKAGE_VERSION_1))
        //then
        .andExpect(status().isNotFound())
        .andExpect(mvcResult -> assertTrue(mvcResult.getResolvedException() instanceof PackageNotFoundException))
        .andDo(document("invalidateNotFoundPackage",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(
                parameterWithName("appName").description("Nome dell'applicazione da invalidare"),
                parameterWithName("appVersion").description("Identificativo della versione dell'applicazione da invalidare")
            )
        ));
  }

  /*
   * uploadPackage
   */
  @Test
  public void testUploadPackageFile() throws Exception {
    //given
    doNothing().when(packageService).installPackageFile(anyString(), anyString(), anyLong(), anyString(), anyString(), any(byte[].class));
    //when
    mockMvc.perform(
        post(UPLOAD_PACKAGE_WITH_PARAMS_URL, PACKAGE_PACKAGENAME, PACKAGE_APPNAME, PACKAGE_VERSION_1, PACKAGE_VERSION_NUMBER_1, PACKAGE_FILENAME)
            .accept(MediaType.APPLICATION_OCTET_STREAM)
            .content(DUMMY_BYTE_ARRAY)
            .contentType(MediaType.APPLICATION_OCTET_STREAM))
        //then
        .andExpect(status().isCreated())
        .andDo(document("uploadPackage",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(
                parameterWithName("packageName").description("Nome del package dell'applicazione da caricare"),
                parameterWithName("appName").description("Nome dell'applicazione da caricare"),
                parameterWithName("appVersion").description("Identificativo della versione dell'applicazione da caricare (valore alfanumerico)"),
                parameterWithName("appVersionNumber").description("Valore incrementale della versione dell'applicazione da caricare (valore numerico)"),
                parameterWithName("fileName").description("Nome del file dell'applicazione da caricare")
            ),
            requestHeaders(headerWithName("content-type").description("Deve essere settato a 'application/octet-stream' affinché la request sia accettata")),
            requestBody()
        ));
  }

  @Test
  public void testUploadPackageFileUnableToWriteFile() throws Exception {
    //given
    doThrow(IOFileException.class).when(packageService).installPackageFile(anyString(), anyString(), anyLong(), anyString(), anyString(),
        any(byte[].class));
    //when
    mockMvc.perform(
        post(UPLOAD_PACKAGE_WITH_PARAMS_URL, PACKAGE_PACKAGENAME, PACKAGE_APPNAME, PACKAGE_VERSION_1, PACKAGE_VERSION_NUMBER_1, PACKAGE_FILENAME)
            .accept(MediaType.APPLICATION_OCTET_STREAM)
            .content(DUMMY_BYTE_ARRAY)
            .contentType(MediaType.APPLICATION_OCTET_STREAM))
        //then
        .andExpect(status().isInternalServerError())
        .andExpect(mvcResult -> assertTrue(mvcResult.getResolvedException() instanceof IOFileException))
        .andDo(document("uploadNotWritablePackage",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(
                parameterWithName("packageName").description("Nome del package dell'applicazione da caricare"),
                parameterWithName("appName").description("Nome dell'applicazione da caricare"),
                parameterWithName("appVersion").description("Identificativo della versione dell'applicazione da caricare (valore alfanumerico)"),
                parameterWithName("appVersionNumber").description("Valore incrementale della versione dell'applicazione da caricare (valore numerico)"),
                parameterWithName("fileName").description("Nome del file dell'applicazione da caricare, verrà assegnato al file registrato sul server")
            ),
            requestHeaders(headerWithName("content-type").description("Deve essere settato a 'application/octet-stream' affinché la request sia accettata")),
            requestBody()
        ));
  }

  @Test
  public void testUploadPackageFileWrongAppVersionNumber() throws Exception {
    //given
    //when
    mockMvc.perform(
        post(UPLOAD_PACKAGE_WITH_PARAMS_URL, PACKAGE_PACKAGENAME, PACKAGE_APPNAME, PACKAGE_VERSION_1, "NaN", PACKAGE_FILENAME)
            .accept(MediaType.APPLICATION_OCTET_STREAM)
            .content(DUMMY_BYTE_ARRAY)
            .contentType(MediaType.APPLICATION_OCTET_STREAM))
        //then
        .andExpect(status().isBadRequest())
        .andExpect(mvcResult -> assertTrue(mvcResult.getResolvedException() instanceof MalformedURLException))
        .andDo(document("uploadWrongAppVersionNumber",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(
                parameterWithName("packageName").description("Nome del package dell'applicazione da caricare"),
                parameterWithName("appName").description("Nome dell'applicazione da caricare"),
                parameterWithName("appVersion").description("Identificativo della versione dell'applicazione da caricare (valore alfanumerico)"),
                parameterWithName("appVersionNumber").description("Valore incrementale della versione dell'applicazione da caricare (valore non accettabile)"),
                parameterWithName("fileName").description("Nome del file dell'applicazione da caricare, verrà assegnato al file registrato sul server")
            ),
            requestHeaders(headerWithName("content-type").description("Deve essere settato a 'application/octet-stream' affinché la request sia accettata")),
            requestBody()
        ));
  }

  @Test
  public void testUploadPackageFileWrongAppName() throws Exception {
    //given
    doThrow(WrongAppNameException.class).when(packageService).installPackageFile(anyString(), anyString(), anyLong(), anyString(), anyString(),
        any(byte[].class));
    //when
    mockMvc.perform(
        post(UPLOAD_PACKAGE_WITH_PARAMS_URL, PACKAGE_PACKAGENAME, PACKAGE_APPNAME, PACKAGE_VERSION_1, PACKAGE_VERSION_NUMBER_1, PACKAGE_FILENAME)
            .accept(MediaType.APPLICATION_OCTET_STREAM)
            .content(DUMMY_BYTE_ARRAY)
            .contentType(MediaType.APPLICATION_OCTET_STREAM))
        //then
        .andExpect(status().isConflict())
        .andExpect(mvcResult -> assertTrue(mvcResult.getResolvedException() instanceof WrongAppNameException))
        .andDo(document("uploadWrongAppNamePackage",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(
                parameterWithName("packageName").description("Nome del package dell'applicazione da caricare"),
                parameterWithName("appName").description("Nome dell'applicazione da caricare"),
                parameterWithName("appVersion").description("Identificativo della versione dell'applicazione da caricare (valore alfanumerico)"),
                parameterWithName("appVersionNumber").description("Valore incrementale della versione dell'applicazione da caricare (valore numerico)"),
                parameterWithName("fileName").description("Nome del file dell'applicazione da caricare, verrà assegnato al file registrato sul server")
            ),
            requestHeaders(headerWithName("content-type").description("Deve essere settato a 'application/octet-stream' affinché la request sia accettata")),
            requestBody()
        ));
  }

  @Test
  public void testUploadPackageAutodetect() throws Exception {
    PackageDTO packageDTO = new PackageDTO(
        PACKAGE_V1_ID,
        PACKAGE_VERSION_1,
        PACKAGE_VERSION_NUMBER_1,
        PACKAGE_FILENAME,
        PACKAGES_WEBSERVER_BASEURL + "/" + PACKAGE_APPNAME + "/" + PACKAGE_VERSION_1 + "/" + PACKAGE_FILENAME + PACKAGES_FILE_EXTENSION,
        true,
        PACKAGE_APPNAME,
        PACKAGE_PACKAGENAME
    );
    MockMultipartFile file = new MockMultipartFile("file", PACKAGE_FILENAME, MediaType.MULTIPART_FORM_DATA_VALUE, DUMMY_BYTE_ARRAY);
    //given
    given(packageService.installPackageFile(anyString(), any(MultipartFile.class))).willReturn(packageDTO);
    //when
    mockMvc.perform(
        multipart(UPLOAD_PACKAGE_URL).file(file)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .accept(MediaType.APPLICATION_JSON))
        //then
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(packageDTO.getId()))
        .andExpect(jsonPath("$.appName").value(packageDTO.getAppName()))
        .andExpect(jsonPath("$.packageName").value(packageDTO.getPackageName()))
        .andExpect(jsonPath("$.appVersion").value(packageDTO.getAppVersion()))
        .andExpect(jsonPath("$.appVersionNumber").value(packageDTO.getAppVersionNumber()))
        .andExpect(jsonPath("$.fileName").value(packageDTO.getFileName()))
        .andExpect(jsonPath("$.valid").value(packageDTO.isValid()))
        .andExpect(jsonPath("$.url").value(packageDTO.getUrl()))
        .andDo(document("uploadPackageAutodetect",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestParts(partWithName("file").description("Campo della richiesta contenente il file da installare")),
            requestPartBody("file"),
            responseFields(packageFields)
                .and(versionFields)
        ));
  }

  @Test
  public void testUploadPackageAutodetectWrongAppName() throws Exception {
    MockMultipartFile file = new MockMultipartFile("file", PACKAGE_FILENAME, MediaType.MULTIPART_FORM_DATA_VALUE, DUMMY_BYTE_ARRAY);
    //given
    doThrow(WrongAppNameException.class).when(packageService).installPackageFile(anyString(), any(MultipartFile.class));
    //when
    mockMvc.perform(
        multipart(UPLOAD_PACKAGE_URL).file(file)
            .contentType(MediaType.MULTIPART_FORM_DATA))
        //then
        .andExpect(status().isConflict())
        .andDo(document("uploadPackageAutodetectWrongAppName",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestParts(partWithName("file").description("Campo della richiesta contenente il file da installare"))
        ));
  }

  @Test
  public void testUploadPackageAutodetectWrongKey() throws Exception {
    MockMultipartFile file = new MockMultipartFile("not-file", PACKAGE_FILENAME, MediaType.MULTIPART_FORM_DATA_VALUE, DUMMY_BYTE_ARRAY);
    //given
    //when
    mockMvc.perform(
        multipart(UPLOAD_PACKAGE_URL).file(file)
            .contentType(MediaType.MULTIPART_FORM_DATA))
        //then
        .andExpect(status().isBadRequest())
        .andDo(document("uploadPackageAutodetectWrongKey",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestParts(partWithName("not-file").description("Campo della richiesta contenente il file da installare (valore errato)"))
        ));
  }

  @Test
  public void testUploadPackageAutodetectEmptyFile() throws Exception {
    byte[] emptyArray = {};
    MockMultipartFile file = new MockMultipartFile("file", "", MediaType.MULTIPART_FORM_DATA_VALUE, emptyArray);
    //given
    //when
    mockMvc.perform(
        multipart(UPLOAD_PACKAGE_URL).file(file)
            .contentType(MediaType.MULTIPART_FORM_DATA))
        //then
        .andExpect(status().isNotAcceptable())
        .andDo(document("uploadPackageAutodetectEmptyFile",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestParts(partWithName("file").description("Campo della richiesta contenente il file da installare"))
        ));
  }

  @Test
  public void testUploadPackageAutodetectEmptyFileName() throws Exception {
    MockMultipartFile file = new MockMultipartFile("file", "", MediaType.MULTIPART_FORM_DATA_VALUE, DUMMY_BYTE_ARRAY);
    //given
    //when
    mockMvc.perform(
        multipart(UPLOAD_PACKAGE_URL).file(file)
            .contentType(MediaType.MULTIPART_FORM_DATA))
        //then
        .andExpect(status().isNotAcceptable())
        .andDo(document("uploadPackageAutodetectEmptyFileName",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestParts(partWithName("file").description("Campo della richiesta contenente il file da installare"))
        ));
  }

  @Test
  public void testUploadPackageAutodetectionFailure() throws Exception {
    MockMultipartFile file = new MockMultipartFile("file", PACKAGE_FILENAME, MediaType.MULTIPART_FORM_DATA_VALUE, DUMMY_BYTE_ARRAY);
    //given
    given(packageService.installPackageFile(anyString(), any(MultipartFile.class))).willThrow(AutoDetectionException.class);
    //when
    mockMvc.perform(
        multipart(UPLOAD_PACKAGE_URL).file(file)
            .contentType(MediaType.MULTIPART_FORM_DATA))
        //then
        .andExpect(status().isNotAcceptable())
        .andDo(document("uploadPackageAutodetectionFailure",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestParts(partWithName("file").description("Campo della richiesta contenente il file da installare"))
        ));
  }

  @Test
  public void testUploadPackageAutodetectUnableToWriteFile() throws Exception {
    MockMultipartFile file = new MockMultipartFile("file", PACKAGE_FILENAME, MediaType.MULTIPART_FORM_DATA_VALUE, DUMMY_BYTE_ARRAY);
    //given
    given(packageService.installPackageFile(anyString(), any(MultipartFile.class))).willThrow(IOFileException.class);
    //when
    mockMvc.perform(
        multipart(UPLOAD_PACKAGE_URL).file(file)
            .contentType(MediaType.MULTIPART_FORM_DATA))
        //then
        .andExpect(status().isInternalServerError())
        .andDo(document("uploadPackageAutodetecUnableToWriteFile",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())
        ));
  }

  @Test
  public void testUploadPackageMissingFramework() throws Exception {
    int frameworkId = 1;
    MockMultipartFile file = new MockMultipartFile("file", PACKAGE_FILENAME, MediaType.MULTIPART_FORM_DATA_VALUE, DUMMY_BYTE_ARRAY);
    //given
    given(packageService.installPackageFile(anyString(), any(MultipartFile.class))).willThrow(new MissingFrameworkException("dummy message", frameworkId));
    //when
    mockMvc.perform(
        multipart(UPLOAD_PACKAGE_URL).file(file)
            .contentType(MediaType.MULTIPART_FORM_DATA))
        //then
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().string(String.format(RESTExceptionHandler.MISSING_FRAMEWORK_MESSAGE, frameworkId)))
        .andDo(document("uploadPackageMissingFramework",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestParts(partWithName("file").description("Campo della richiesta contenente il file da installare"))
        ));
  }

  @Test
  public void testDeleteSinglePackage() throws Exception {
    //given
    doNothing().when(packageService).deleteVersionPackage(anyString(), anyString());
    //when
    mockMvc.perform(delete(DELETE_SINGLE_VERSION_URL, PACKAGE_APPNAME, PACKAGE_VERSION_1))
        //then
        .andExpect(status().isOk())
        .andDo(document("deleteSinglePackage",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(
                parameterWithName("appName").description("Nome dell'applicazione ricercata"),
                parameterWithName("appVersion").description("Identificativo della versione dell'applicazione da cancellare")
            )
        ));
  }

  @Test
  public void testDeleteSinglePackageNotFound() throws Exception {
    //given
    doThrow(PackageNotFoundException.class).when(packageService).deleteVersionPackage(anyString(), anyString());
    //when
    mockMvc.perform(delete(DELETE_SINGLE_VERSION_URL, PACKAGE_APPNAME, "100.200.300"))
        //then
        .andExpect(status().isNotFound())
        .andExpect(mvcResult -> assertTrue(mvcResult.getResolvedException() instanceof PackageNotFoundException))
        .andDo(document("deleteSinglePackageNotFound",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(
                parameterWithName("appName").description("Nome dell'applicazione ricercata"),
                parameterWithName("appVersion").description("Identificativo della versione dell'applicazione da cancellare")
            )
        ));
  }

  @Test
  public void testDeletePackageVersions() throws Exception {
    //given
    doNothing().when(packageService).deleteAllVersions(anyString());
    //when
    mockMvc.perform(delete(DELETE_PACKAGE_VERSIONS_URL, PACKAGE_APPNAME))
        //then
        .andExpect(status().isOk())
        .andDo(document("deletePackageVersions",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(parameterWithName("appName").description("Nome dell'applicazione di cui eliminare tutte le versioni"))
        ));
  }

  @Test
  public void testDeletePackageVersionsNotFound() throws Exception {
    //given
    doThrow(PackageNotFoundException.class).when(packageService).deleteAllVersions(anyString());
    //when
    mockMvc.perform(delete(DELETE_PACKAGE_VERSIONS_URL, "appNotPresent"))
        //then
        .andExpect(status().isNotFound())
        .andExpect(mvcResult -> assertTrue(mvcResult.getResolvedException() instanceof PackageNotFoundException))
        .andDo(document("deletePackageVersionsNotFound",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(parameterWithName("appName").description("Nome dell'applicazione di cui eliminare tutte le versioni"))
        ));
  }

  @Test
  public void testDeletePackagesByIdListParam() throws Exception {
    this.performDeletePackagesById(
        delete(DELETE_PACKAGES_BY_ID_URL + "?idList=100,200,300"),
        "deletePackagesByIdListParams");
  }

  @Test
  public void testDeletePackagesByIdRepeatedParamNames() throws Exception {
    this.performDeletePackagesById(
        delete(DELETE_PACKAGES_BY_ID_URL)
            .queryParam(GET_PACKAGES_PARAM_NAME, "100", "200", "300"),
        "deletePackagesByIdRepeatedParamNames");
  }

  @Test
  public void testDeletePackagesByIdMalformedURL() throws Exception {
    this.performDeletePackagesById(
        delete(DELETE_PACKAGES_BY_ID_URL)
            .queryParam(GET_PACKAGES_PARAM_NAME, "100", "NaN", "200", "NaN", "300"),
        "deletePackagesByIdMalformedURL");
  }


  private void performDeletePackagesById(MockHttpServletRequestBuilder delete, String docName) throws Exception {
    //given
    doNothing().when(packageService).deletePackagesList(anyList());
    //when
    mockMvc.perform(delete)
        //then
        .andExpect(status().isOk())
        .andDo(document(docName,
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestParameters(
                parameterWithName(GET_PACKAGES_PARAM_NAME).description("Elenco degli id dei packages da cancellare")
            )
        ));
  }

  @Test
  public void testDeletePackagesByIdNotFound() throws Exception {
    //given
    //when
    mockMvc.perform(
        delete(DELETE_PACKAGES_BY_ID_URL)
            .queryParam(GET_PACKAGES_PARAM_NAME, "10101", "20202", "30303"))
        //then
        .andExpect(status().isOk())
        .andDo(document("deletePackagesByIdEmpty",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestParameters(
                parameterWithName(GET_PACKAGES_PARAM_NAME).description("Elenco degli id dei packages da cancellare")
            )
        ));
  }

  @Test
  public void testDeletePackagesByIdEmptyParamList() throws Exception {
    //given
    //when
    mockMvc.perform(
        delete(DELETE_PACKAGES_BY_ID_URL)
            .queryParam(GET_PACKAGES_PARAM_NAME, ""))
        //then
        .andExpect(status().isOk())
        .andDo(document("deletePackagesByIdEmptyParamList",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestParameters(
                parameterWithName(GET_PACKAGES_PARAM_NAME).description("Elenco degli id dei packages da cancellare")
            )
        ));
  }

  @Test
  public void testDeletePackagesByIdWithoutParams() throws Exception {
    //given
    //when
    mockMvc.perform(delete(DELETE_PACKAGES_BY_ID_URL))
        //then
        .andExpect(status().isBadRequest())
        .andDo(document("deletePackagesByIdNoParams",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())
        ));
  }
}