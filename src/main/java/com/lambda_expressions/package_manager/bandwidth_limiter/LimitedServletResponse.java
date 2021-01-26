package com.lambda_expressions.package_manager.bandwidth_limiter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

/**
 * Created by steccothal
 * on Tuesday 26 January 2021
 * at 3:33 PM
 */
public class LimitedServletResponse extends HttpServletResponseWrapper {

  private ServletOutputStream limitedOutputStream;

  public LimitedServletResponse(HttpServletResponse response) throws IOException {
    super(response);
    this.limitedOutputStream = response.getOutputStream();
  }

  @Override
  public ServletOutputStream getOutputStream(){
    return this.limitedOutputStream;
  }

}
