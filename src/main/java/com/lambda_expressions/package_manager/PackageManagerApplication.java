package com.lambda_expressions.package_manager;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.security.Security;

@SpringBootApplication
public class PackageManagerApplication extends SpringBootServletInitializer {

  public static void main(String[] args) {
    SpringApplication.run(PackageManagerApplication.class, args);
  }

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    Security.addProvider(new BouncyCastleProvider());
    return application.sources(PackageManagerApplication.class);
  }

}
