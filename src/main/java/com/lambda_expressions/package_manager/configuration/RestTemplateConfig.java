package com.lambda_expressions.package_manager.configuration;

import org.keycloak.adapters.springsecurity.client.KeycloakClientRequestFactory;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor;
import org.springframework.cloud.client.loadbalancer.RetryLoadBalancerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;

/**
 * Created by steccothal
 * on Thursday 09 December 2021
 * at 7:23 PM
 */
public class RestTemplateConfig {
  @Value("${ms-timeout-reading}")
  private Integer msTimeoutReading;
  @Value("${ms-timeout-connection}")
  private Integer msTimeoutConnection;

  @Bean
  @Primary
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  public RestTemplate restTemplate(KeycloakClientRequestFactory keycloakClientRequestFactory,
                                   @Nullable LoadBalancerInterceptor loadBalancerInterceptor,
                                   @Nullable RetryLoadBalancerInterceptor retryLoadBalancerInterceptor,
                                   @Value("${spring.cloud.consul.enabled:true}") boolean consulEnabled) {

    keycloakClientRequestFactory.setConnectTimeout(msTimeoutConnection);
    keycloakClientRequestFactory.setReadTimeout(msTimeoutReading);

    KeycloakRestTemplate result = new KeycloakRestTemplate(keycloakClientRequestFactory);
    if (consulEnabled) {
      result.getInterceptors().add(loadBalancerInterceptor != null ? loadBalancerInterceptor : retryLoadBalancerInterceptor);
    }
    return result;
  }

  @Bean("restTemplateNoKeycloak")
  public RestTemplate restTemplateNoKeycloak(@Nullable LoadBalancerInterceptor loadBalancerInterceptor,
                                             @Nullable RetryLoadBalancerInterceptor retryLoadBalancerInterceptor,
                                             @Value("${spring.cloud.consul.enabled:true}") boolean consulEnabled) {
    RestTemplate result = provideRestTemplate();

    if (consulEnabled) {
      result.getInterceptors().add(loadBalancerInterceptor != null ? loadBalancerInterceptor : retryLoadBalancerInterceptor);
    }
    return result;
  }

  @Bean("restTemplateExternalServices")
  public RestTemplate restTemplateExternalServices() {
    return provideRestTemplate();
  }

  private RestTemplate provideRestTemplate() {

    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(msTimeoutConnection);
    requestFactory.setReadTimeout(msTimeoutReading);

    return new RestTemplate(requestFactory);
  }
}
