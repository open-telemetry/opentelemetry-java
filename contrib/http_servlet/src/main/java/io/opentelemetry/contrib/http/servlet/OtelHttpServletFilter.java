/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.contrib.http.servlet;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.lenientFormat;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.contrib.http.core.HttpExtractor;
import io.opentelemetry.contrib.http.core.HttpRequestContext;
import io.opentelemetry.contrib.http.core.HttpServerHandler;
import io.opentelemetry.contrib.http.core.HttpStatus2OtStatusConverter;
import io.opentelemetry.trace.Tracer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.AsyncContext;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This filter class implements Filter interface called by web container. The filter is used as an
 * interceptor to enable tracing of http requests.
 */
public class OtelHttpServletFilter implements Filter {

  /** Servlet filter init param name for comma-separated list of incoming propagation schemes. */
  public static final String OTEL_TRACE_PROPAGATORS =
      "io.opentelemetry.http.server.trace-propagators";
  /** Servlet filter init param name for comma-separated list of incoming propagation schemes. */
  public static final String OTEL_EXTRACTOR = "io.opentelemetry.http.server.extractors";
  /** Servlet filter init param name for ant-style path includes. */
  public static final String OTEL_PATH_INCLUDES = "io.opentelemetry.http.server.path-includes";
  /** Servlet filter init param name for ant-style path excludes. */
  public static final String OTEL_PATH_EXCLUDES = "io.opentelemetry.http.server.path-excludes";
  /** Servlet filter init param name for whether a new trace should always be created or not. */
  public static final String OTEL_PUBLIC_ENDPOINT = "io.opentelemetry.http.server.public-endpoint";

  private static final Logger LOGGER = Logger.getLogger(OtelHttpServletFilter.class.getName());

  private String tracingIncludePaths;
  private String tracingExcludePaths;
  private Boolean publicEndpoint;
  private Tracer tracer;
  private final List<SchemeSpecificHttpPropagationGetter> additionalGetters = new ArrayList<>();
  private HttpStatus2OtStatusConverter statusConverter = new HttpStatus2OtStatusConverter();
  private AntPathRequestMatcher requestMatcher;
  private HttpExtractor<HttpServletRequest, HttpServletResponse> httpExtractor;
  private HttpServerHandler<HttpServletRequest, HttpServletResponse, HttpServletRequest> handler;

  /** Constructs a filter object. */
  public OtelHttpServletFilter() {
    super();
  }

  /**
   * Sets the comma-separated, ant-style URL paths which telemetry should be collected on.
   *
   * @param tracingIncludePaths the include paths
   */
  public void setTracingIncludePaths(String tracingIncludePaths) {
    this.tracingIncludePaths = tracingIncludePaths;
  }

  /**
   * Sets the comma-separated, ant-style URL paths which telemetry should not be collected on.
   *
   * @param tracingExcludePaths the exclude paths
   */
  public void setTracingExcludePaths(String tracingExcludePaths) {
    this.tracingExcludePaths = tracingExcludePaths;
  }

  /**
   * Sets whether this application should act as a public endpoint which always creates a new trace
   * regardless of propagation info received.
   *
   * @param publicEndpoint public endpoint or not
   */
  public void setPublicEndpoint(Boolean publicEndpoint) {
    this.publicEndpoint = publicEndpoint;
  }

  /**
   * Sets additional gets for any trace propagation schemes not implemented within this package.
   *
   * @param additionalPropagationGetters the additional getters
   */
  public void setAdditionalPropagationGetters(
      List<SchemeSpecificHttpPropagationGetter> additionalPropagationGetters) {
    if (additionalPropagationGetters != null) {
      this.additionalGetters.clear();
      this.additionalGetters.addAll(additionalPropagationGetters);
    }
  }

  /**
   * Sets the HTTP status to OpenTelemetry status converter if the default should not be used.
   *
   * @param statusConverter the converter
   */
  public void setStatusConverter(HttpStatus2OtStatusConverter statusConverter) {
    if (statusConverter != null) {
      this.statusConverter = statusConverter;
    }
  }

  /**
   * Sets the HTTP extractor strategy implementation.
   *
   * @param httpExtractor the extractor
   */
  public void setHttpExtractor(
      HttpExtractor<HttpServletRequest, HttpServletResponse> httpExtractor) {
    if (httpExtractor != null) {
      this.httpExtractor = httpExtractor;
    }
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    tracer = OpenTelemetry.getTracer();
    constructAndInitializeRequestMatcher(filterConfig);
    constructAndInitializeHttpExtractor();
    MultiSchemeHttpPropagationGetter getter = constructAndInitializeGetter(filterConfig);
    configurePublicEndpointValue(filterConfig);
    handler =
        new HttpServerHandler<>(
            httpExtractor,
            getter,
            statusConverter,
            tracer,
            OpenTelemetry.getDistributedContextManager(),
            OpenTelemetry.getMeter(),
            publicEndpoint);
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if ((request instanceof HttpServletRequest) && (response instanceof HttpServletResponse)) {
      doFilterOfHttpRequest(chain, (HttpServletRequest) request, (HttpServletResponse) response);
    } else {
      chain.doFilter(request, response);
    }
  }

  private void constructAndInitializeRequestMatcher(FilterConfig filterConfig) {
    if (isNullOrEmpty(tracingIncludePaths)) {
      tracingIncludePaths = filterConfig.getInitParameter(OTEL_PATH_INCLUDES);
    }
    if (isNullOrEmpty(tracingExcludePaths)) {
      tracingExcludePaths = filterConfig.getInitParameter(OTEL_PATH_EXCLUDES);
    }
    requestMatcher = new AntPathRequestMatcher(tracingIncludePaths, tracingExcludePaths);
  }

  private void constructAndInitializeHttpExtractor() {
    if (httpExtractor == null) {
      httpExtractor = new UriPathDrivenHttpServletExtractor();
    }
  }

  private MultiSchemeHttpPropagationGetter constructAndInitializeGetter(FilterConfig filterConfig) {
    String internalSchemes = filterConfig.getInitParameter(OTEL_TRACE_PROPAGATORS);
    if (isNullOrEmpty(internalSchemes)) {
      internalSchemes = MultiSchemeHttpPropagationGetter.ALL_SCHEMES;
    }
    MultiSchemeHttpPropagationGetter getter = new MultiSchemeHttpPropagationGetter(internalSchemes);
    getter.initializeGetters(
        additionalGetters.toArray(
            new SchemeSpecificHttpPropagationGetter[additionalGetters.size()]));
    return getter;
  }

  private void configurePublicEndpointValue(FilterConfig filterConfig) {
    if (publicEndpoint == null) {
      String publicEndVal = filterConfig.getInitParameter(OTEL_PUBLIC_ENDPOINT);
      if (publicEndVal != null) {
        publicEndpoint = Boolean.parseBoolean(publicEndVal);
      } else {
        publicEndpoint = Boolean.FALSE;
      }
    }
  }

  private void doFilterOfHttpRequest(
      FilterChain chain, HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    if (requestMatcher.isMatch(request.getRequestURI())) {
      wrapRequestWithTelemetry(chain, request, response);
    } else {
      chain.doFilter(request, response);
    }
  }

  private void wrapRequestWithTelemetry(
      FilterChain chain, HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    HttpRequestContext context = handler.handleStart(request, request);
    OtelHttpServletListener listener = new OtelHttpServletListener(tracer, handler, context);
    request.setAttribute(OtelHttpServletUtils.OTEL_SERVLET_LISTENER, listener);

    int length = request.getContentLength();
    if (length > 0) {
      handler.handleMessageReceived(context, length);
    }

    Exception error = null;
    try (Scope scope = tracer.withSpan(handler.getSpanFromContext(context))) {
      chain.doFilter(request, response);
    } catch (ServletException | IOException | RuntimeException exception) {
      LOGGER.log(
          Level.INFO, lenientFormat("request failed: %s", exception.getMessage()), exception);
      error = exception;
      throw exception;
    } finally {
      if (request.isAsyncStarted()) {
        AsyncContext async = request.getAsyncContext();
        async.addListener(listener, request, response);
      } else {
        OtelHttpServletUtils.recordMessageSentEvent(handler, context, response);
        handler.handleEnd(context, request, response, error);
      }
    }
  }
}
