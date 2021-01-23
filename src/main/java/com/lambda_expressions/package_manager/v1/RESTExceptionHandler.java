package com.lambda_expressions.package_manager.v1;

import com.lambda_expressions.package_manager.exceptions.*;
import org.springframework.http.HttpHeaders;
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

  @ExceptionHandler(UnauthenticatedRequestException.class)
  public ResponseEntity<Object> handleUnauthenticatedRequestException() {
    return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(MalformedURLException.class)
  public ResponseEntity<Object> handleMalformedURLException() {
    return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(PackageNotFoundException.class)
  public ResponseEntity<Object> handleNotFoundException() {
    return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(IOFileException.class)
  public ResponseEntity<Object> handleIOFileException() {
    return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(InvalidPackageException.class)
  public ResponseEntity<Object> handleInvalidPackageException() {
    return new ResponseEntity<>(null, new HttpHeaders(), HttpStatus.FORBIDDEN);
  }
}
