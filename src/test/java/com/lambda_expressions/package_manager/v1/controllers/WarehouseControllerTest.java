package com.lambda_expressions.package_manager.v1.controllers;

import com.lambda_expressions.package_manager.exceptions.IOFileException;
import com.lambda_expressions.package_manager.exceptions.PackageNotFoundException;
import com.lambda_expressions.package_manager.services.WarehouseService;
import com.lambda_expressions.package_manager.v1.RESTExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.FileSystemResource;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by steccothal
 * on Friday 23 April 2021
 * at 6:25 AM
 */
@ExtendWith({MockitoExtension.class, RestDocumentationExtension.class})
class WarehouseControllerTest {
  private static final String PACKAGE_FILENAME = "Spending_1.9.apk";
  private static final String PACKAGE_APPNAME = "Spending";
  private static final String PACKAGE_VERSION_1 = "1.9";

  private static final String WAREHOUSE_GENERIC_URL = "/warehouse/{appName}/{appVersion}/{fileName}";

  private static final String DUMMY_APK = "src/test/resources/dummy.apk";

  @Mock
  WarehouseService warehouseService;
  @InjectMocks
  WarehouseController controller;
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

  @Test
  void getPackageFile() throws Exception {
    //given
    given(warehouseService.getPackageFile(anyString(), anyString(), anyString())).willReturn(new FileSystemResource(DUMMY_APK));
    //when
    mockMvc.perform(
        get(WAREHOUSE_GENERIC_URL, PACKAGE_APPNAME, PACKAGE_VERSION_1, PACKAGE_FILENAME))
        //then
        .andExpect(status().isOk())
        .andDo(document("warehouseOK",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(
                parameterWithName("appName").description("Nome dell'applicazione da scaricare"),
                parameterWithName("appVersion").description("Identificativo della versione dell'applicazione da scaricare"),
                parameterWithName("fileName").description("Nome del file dell'applicazione da scaricare")
            )));
  }

  @Test
  void getPackageFileNotFound() throws Exception {
    //given
    given(warehouseService.getPackageFile(anyString(), anyString(), anyString())).willThrow(PackageNotFoundException.class);
    //when
    mockMvc.perform(
        get(WAREHOUSE_GENERIC_URL, "appNotPresent", PACKAGE_VERSION_1, PACKAGE_FILENAME))
        //then
        .andExpect(status().isNotFound())
        .andDo(document("warehouseNotFound",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(
                parameterWithName("appName").description("Nome dell'applicazione da scaricare"),
                parameterWithName("appVersion").description("Identificativo della versione dell'applicazione da scaricare"),
                parameterWithName("fileName").description("Nome del file dell'applicazione da scaricare")
            )));
  }

  @Test
  void getPackageFileNotReadable() throws Exception {
    //given
    given(warehouseService.getPackageFile(anyString(), anyString(), anyString())).willThrow(IOFileException.class);
    //when
    mockMvc.perform(
        get(WAREHOUSE_GENERIC_URL, PACKAGE_APPNAME, PACKAGE_VERSION_1, PACKAGE_FILENAME))
        //then
        .andExpect(status().isInternalServerError())
        .andDo(document("warehouseNotReadable",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(
                parameterWithName("appName").description("Nome dell'applicazione da scaricare"),
                parameterWithName("appVersion").description("Identificativo della versione dell'applicazione da scaricare"),
                parameterWithName("fileName").description("Nome del file dell'applicazione da scaricare")
            )));
  }
}