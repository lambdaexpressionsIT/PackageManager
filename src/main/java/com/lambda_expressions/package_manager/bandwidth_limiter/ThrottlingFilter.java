package com.lambda_expressions.package_manager.bandwidth_limiter;

import com.lambda_expressions.package_manager.bandwidth_limiter.utils.StreamManager;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by steccothal
 * on Tuesday 26 January 2021
 * at 3:19 PM
 */
@Component
@Order(1)
public class ThrottlingFilter implements Filter {

  private final StreamManager streamManager;

  public ThrottlingFilter(StreamManager streamManager) {
    this.streamManager = streamManager;
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    ServletRequest request = servletRequest;
    ServletResponse response = servletResponse;

    if (this.streamManager.isActive()) {
      request = new LimitedServletRequest((HttpServletRequest) servletRequest);
      response = new LimitedServletResponse((HttpServletResponse) servletResponse);

      this.streamManager.registerStream(request.getInputStream());
      this.streamManager.registerStream(response.getOutputStream());
    }

    filterChain.doFilter(request, response);
  }
}
