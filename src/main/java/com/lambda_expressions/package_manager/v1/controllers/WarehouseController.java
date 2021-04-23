package com.lambda_expressions.package_manager.v1.controllers;

import com.lambda_expressions.package_manager.exceptions.IOFileException;
import com.lambda_expressions.package_manager.exceptions.PackageNotFoundException;
import com.lambda_expressions.package_manager.exceptions.UnauthenticatedRequestException;
import com.lambda_expressions.package_manager.services.AuthenticationService;
import com.lambda_expressions.package_manager.services.WarehouseService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by steccothal
 * on Friday 23 April 2021
 * at 4:49 AM
 */
@RestController
@RequestMapping("/warehouse/")
public class WarehouseController {

  private WarehouseService warehouseService;
  private AuthenticationService authService;

  public WarehouseController(WarehouseService warehouseService, AuthenticationService authService) {
    this.warehouseService = warehouseService;
    this.authService = authService;
  }

  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = {"{appName}/{version}/{fileName}", "{appName}/{version}/{fileName}/"})
  public FileSystemResource getPackageFile(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
                                           @PathVariable String appName, @PathVariable String version,
                                           @PathVariable String fileName)
      throws UnauthenticatedRequestException, PackageNotFoundException, IOFileException {
    this.authService.authenticateRequest(httpRequest);
    FileSystemResource packageFile = this.warehouseService.getPackageFile(appName, version, fileName);

    httpResponse.addHeader("Content-Disposition", "attachment; filename=" + fileName);

    return packageFile;
  }

}
