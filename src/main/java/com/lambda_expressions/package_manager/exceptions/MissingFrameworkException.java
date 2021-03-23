package com.lambda_expressions.package_manager.exceptions;

/**
 * Created by steccothal
 * on Monday 22 March 2021
 * at 4:05 PM
 */
public class MissingFrameworkException extends Exception{
  private int id;

  public MissingFrameworkException(String message, int id) {
    super(message);
    this.id = id;
  }

  public int getId() {
    return id;
  }
}
