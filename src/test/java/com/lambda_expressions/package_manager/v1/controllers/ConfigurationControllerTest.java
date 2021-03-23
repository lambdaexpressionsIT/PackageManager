package com.lambda_expressions.package_manager.v1.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambda_expressions.package_manager.exceptions.FrameworkInstallationException;
import com.lambda_expressions.package_manager.services.AuthenticationService;
import com.lambda_expressions.package_manager.services.ConfigurationService;
import com.lambda_expressions.package_manager.v1.RESTExceptionHandler;
import com.lambda_expressions.package_manager.v1.model.BandwidthLimiterConfigurationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by steccothal
 * on Monday 22 March 2021
 * at 6:08 PM
 */
@ExtendWith({MockitoExtension.class, RestDocumentationExtension.class})
class ConfigurationControllerTest {
  private static final String REST_SERVICES_BASE_URL = "/api/v1/configuration/";
  private static final String UPLOAD_FRAMEWORK_URL = REST_SERVICES_BASE_URL + "uploadFramework";
  private static final String UPLOAD_FRAMEWORK_WITH_TAG_URL = REST_SERVICES_BASE_URL + "uploadFramework/{frameworkTag}";
  private static final String LIST_FRAMEWORKS_URL = REST_SERVICES_BASE_URL + "installedFrameworks";
  private static final String BANDWIDTH_LIMITER_URL = REST_SERVICES_BASE_URL + "bandwidthLimiter";

  private static byte[] DUMMY_FILE = "This is the payload of an uploadFramework request, the byte array of a framework file".getBytes();
  private static BandwidthLimiterConfigurationDTO CONFIGURATION_DTO = BandwidthLimiterConfigurationDTO.builder()
      .upstreamKbps(1000)
      .downstreamKbps(500)
      .maxThresholdKbps(1000)
      .isActive(true)
      .build();

  private final FieldDescriptor[] bandwidthConfigurationFields = new FieldDescriptor[]{
      fieldWithPath("maxThresholdKbps").description("Banda massima allocata per l'applicazione (in kbps). Se inferiore a uno dei due seguenti valori, ne limita il valore sostituendovi il proprio, se superiore o uguale ai due seguenti, non ne modifica i valori"),
      fieldWithPath("upstreamKbps").description("Velocità massima consentita per upload verso l'applicazione (in kbps)"),
      fieldWithPath("downstreamKbps").description("Velocità massima consentita di download dall'applicazione (in kbps)"),
      fieldWithPath("active").description("Permette di attivare il servizio di limitazione della banda")};


  @Mock
  ConfigurationService configurationService;
  @Mock
  AuthenticationService authService;
  @InjectMocks
  ConfigurationController controller;
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
  void testUploadFramework() throws Exception {
    //given
    //when
    mockMvc.perform(
        post(UPLOAD_FRAMEWORK_URL)
            .content(DUMMY_FILE)
            .contentType(MediaType.APPLICATION_OCTET_STREAM))
        //then
        .andExpect(status().isCreated())
        .andDo(document("uploadFramework",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())
        ));
  }

  @Test
  void testUploadFrameworkWithTag() throws Exception {
    //given
    //when
    mockMvc.perform(
        post(UPLOAD_FRAMEWORK_WITH_TAG_URL, "htc")
            .content(DUMMY_FILE)
            .contentType(MediaType.APPLICATION_OCTET_STREAM))
        //then
        .andExpect(status().isCreated())
        .andDo(document("uploadFrameworkWithTag",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(parameterWithName("frameworkTag").description("Tag assegnato al framework da installare (opzionale)"))
        ));
  }

  @Test
  void testUploadFrameworkError() throws Exception {
    //given
    doThrow(FrameworkInstallationException.class).when(configurationService).installFramework(anyString(), any(byte[].class));
    //when
    mockMvc.perform(
        post(UPLOAD_FRAMEWORK_WITH_TAG_URL, "htc")
            .content(DUMMY_FILE)
            .contentType(MediaType.APPLICATION_OCTET_STREAM))
        //then
        .andExpect(status().isNotAcceptable())
        .andDo(document("uploadFrameworkError",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint())
        ));
  }

  @Test
  void listInstalledFrameworks() throws Exception {
    String framework1 = "1.apk";
    String framework2 = "2-htc.apk";
    String framework3 = "3.apk";
    List<String> frameworks = new ArrayList<>();

    frameworks.add(framework1);
    frameworks.add(framework2);
    frameworks.add(framework3);
    //given
    given(configurationService.listFrameworks()).willReturn(frameworks);
    //when
    mockMvc.perform(get(LIST_FRAMEWORKS_URL))
        //then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(3))
        .andExpect((jsonPath("$[0]").value(framework1)))
        .andExpect((jsonPath("$[1]").value(framework2)))
        .andExpect((jsonPath("$[2]").value(framework3)))
        .andDo(document("listInstalledFrameworks",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            responseFields(
                fieldWithPath("[]").description("Array popolato dai nomi dei files dei frameworks installati. Il nome dei files è composto da " +
                    "{packageId}-{frameworkTag}.apk. Il packageId viene determinato automaticamente dal tool di decompilazione degli apk, il " +
                    "frameworkTag invece viene definito tramite il parametro opzionale dell'indirizzo della richiesta HTTP." +
                    "Il primo framework trovato (1.apk) è il framework AOSP, preinstallato di default")
            )));
  }

  @Test
  void configureBandwidthLimiter() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    //given
    //when
    mockMvc.perform(
        patch(BANDWIDTH_LIMITER_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(CONFIGURATION_DTO)))
        //then
        .andExpect(status().isOk())
        .andDo(document("configureBandwidthLimiter",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestFields(bandwidthConfigurationFields)
        ));
  }

  @Test
  void getBandwidthLimiterConfiguration() throws Exception {
    //given
    given(configurationService.getBandwidthLimiterConfiguration()).willReturn(CONFIGURATION_DTO);
    //when
    mockMvc.perform(get(BANDWIDTH_LIMITER_URL))
        //then
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.active").value(CONFIGURATION_DTO.isActive()))
        .andExpect(jsonPath("$.maxThresholdKbps").value(CONFIGURATION_DTO.getMaxThresholdKbps()))
        .andExpect(jsonPath("$.downstreamKbps").value(CONFIGURATION_DTO.getDownstreamKbps()))
        .andExpect(jsonPath("$.upstreamKbps").value(CONFIGURATION_DTO.getUpstreamKbps()))
        .andDo(document("getBandwidthLimiterConfiguration",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            responseFields(bandwidthConfigurationFields)
        ));
  }
}