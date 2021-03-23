package com.lambda_expressions.package_manager.bandwidth_limiter;

import com.lambda_expressions.package_manager.bandwidth_limiter.utils.StreamManager;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ThrottlingFilter implements Filter {

  private StreamManager streamManager;

  public ThrottlingFilter(StreamManager streamManager) {
    this.streamManager = streamManager;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    log.info("Initializing bandwidth limiter filter");
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    ServletRequest request = servletRequest;
    ServletResponse response = servletResponse;

    if (this.streamManager.isActive()) {
      ServletInputStream limitedInputStream = this.streamManager.registerStream(request.getInputStream());
      ServletOutputStream limitedOutputStream = this.streamManager.registerStream(response.getOutputStream());

      request = new LimitedServletRequest((HttpServletRequest) servletRequest, limitedInputStream);
      response = new LimitedServletResponse((HttpServletResponse) servletResponse, limitedOutputStream);
    }

    filterChain.doFilter(request, response);
  }
}
