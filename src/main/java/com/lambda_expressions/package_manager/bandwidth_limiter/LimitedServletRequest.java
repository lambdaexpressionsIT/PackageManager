package com.lambda_expressions.package_manager.bandwidth_limiter;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

/**
 * Created by steccothal
 * on Tuesday 26 January 2021
 * at 3:33 PM
 */
public class LimitedServletRequest extends HttpServletRequestWrapper {

  private ServletInputStream limitedInputStream;

  public LimitedServletRequest(HttpServletRequest request) throws IOException {
    super(request);
    this.limitedInputStream = request.getInputStream();
  }

  @Override
  public ServletInputStream getInputStream(){
    return this.limitedInputStream;
  }
}
