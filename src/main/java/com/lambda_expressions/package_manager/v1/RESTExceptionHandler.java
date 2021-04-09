package com.lambda_expressions.package_manager.v1;

import com.lambda_expressions.package_manager.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Created by steccothal
 * on Monday 18 January 2021
 * at 9:43 AM
 */
@ControllerAdvice
public class RESTExceptionHandler extends ResponseEntityExceptionHandler {

  public static String MISSING_FRAMEWORK_MESSAGE = "Cannot decode apk file, missing resource framework with packageId: %d";

  @ExceptionHandler(UnauthenticatedRequestException.class)
  public ResponseEntity handleUnauthenticatedRequestException() {
    return new ResponseEntity(HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(MalformedURLException.class)
  public ResponseEntity handleMalformedURLException() {
    return new ResponseEntity(HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(PackageNotFoundException.class)
  public ResponseEntity handleNotFoundException() {
    return new ResponseEntity(HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(IOFileException.class)
  public ResponseEntity handleIOFileException() {
    return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(InvalidPackageException.class)
  public ResponseEntity handleInvalidPackageException() {
    return new ResponseEntity(HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(AutoDetectionException.class)
  public ResponseEntity handleAutoDetectionException() {
    return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
  }

  @ExceptionHandler(FrameworkInstallationException.class)
  public ResponseEntity handleFrameworkInstallationException() {
    return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
  }

  @ExceptionHandler(WrongAppNameException.class)
  public ResponseEntity handleWrongAppNameException() {
    return new ResponseEntity(HttpStatus.CONFLICT);
  }

  @ExceptionHandler(MissingFrameworkException.class)
  public ResponseEntity<String> handleMissingFrameworkException(MissingFrameworkException e) {
    return new ResponseEntity<>(String.format(RESTExceptionHandler.MISSING_FRAMEWORK_MESSAGE, e.getId()), HttpStatus.UNPROCESSABLE_ENTITY);
  }
}
