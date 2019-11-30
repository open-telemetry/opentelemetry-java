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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static io.opentelemetry.contrib.http.core.HttpTraceConstants.HTTP_FLAVOR_1_0;
import static io.opentelemetry.contrib.http.core.HttpTraceConstants.HTTP_FLAVOR_1_1;
import static io.opentelemetry.contrib.http.core.HttpTraceConstants.HTTP_FLAVOR_2;
import static io.opentelemetry.contrib.http.core.HttpTraceConstants.HTTP_FLAVOR_QUIC;
import static io.opentelemetry.contrib.http.core.HttpTraceConstants.HTTP_FLAVOR_SPDY;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.contrib.http.core.HttpExtractor;
import java.util.Map;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Extracts span attributes from {@link HttpServletRequest} and {@link HttpServletResponse} with
 * routes determined using Spring MVC route mappings. This approach enabled mapping requests to URL
 * template-based paths.
 */
public class SpringMvcDrivenHttpExtractor
    extends HttpExtractor<HttpServletRequest, HttpServletResponse> {

  private static final Map<String, String> PROTOCOL_MAP;

  static {
    ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
    builder.put("HTTP/1.0", HTTP_FLAVOR_1_0);
    builder.put("HTTP/1.1", HTTP_FLAVOR_1_1);
    builder.put("HTTP/2.0", HTTP_FLAVOR_2);
    builder.put("SPDY", HTTP_FLAVOR_SPDY);
    builder.put("QUIC", HTTP_FLAVOR_QUIC);
    PROTOCOL_MAP = builder.build();
  }

  private RequestMappingHandlerMapping requestHandlerMappings;
  private PathMatcher pathMatcher;

  /**
   * Sets the Spring MVC controller mappings obtained from annotated controllers.
   *
   * @param requestHandlerMappings the Spring-constructed mappings
   */
  @Autowired
  public void setRequestHandlerMappings(RequestMappingHandlerMapping requestHandlerMappings) {
    checkNotNull(requestHandlerMappings, "requestHandlerMappings is required");
    this.requestHandlerMappings = requestHandlerMappings;
    this.pathMatcher = requestHandlerMappings.getPathMatcher();
  }

  @Override
  public String getMethod(HttpServletRequest request) {
    return request.getMethod();
  }

  @Override
  public String getUrl(HttpServletRequest request) {
    if (isNullOrEmpty(request.getQueryString())) {
      return request.getRequestURL().toString();
    } else {
      return request.getRequestURL().toString() + "?" + request.getQueryString();
    }
  }

  @Override
  public String getRoute(final HttpServletRequest request) {
    String route = null;
    for (RequestMappingInfo info : requestHandlerMappings.getHandlerMethods().keySet()) {
      RequestMappingInfo candidate = info.getMatchingCondition(request);
      if (candidate != null) {
        for (String pattern : candidate.getPatternsCondition().getPatterns()) {
          if (pathMatcher.match(pattern, request.getRequestURI())) {
            route = pattern;
            break;
          }
        }
      }
      if (route != null) {
        break;
      }
    }
    if (route == null) {
      route = request.getRequestURI();
    }
    return route;
  }

  @Nullable
  @Override
  public String getUserAgent(HttpServletRequest request) {
    return request.getHeader("User-Agent");
  }

  @Override
  @Nullable
  public String getHttpFlavor(HttpServletRequest request) {
    String protocol = request.getProtocol();
    if (isNullOrEmpty(protocol)) {
      return null;
    }
    if (PROTOCOL_MAP.containsKey(protocol)) {
      return PROTOCOL_MAP.get(protocol);
    }
    if (!isNullOrEmpty(request.getHeader(":method"))) {
      return HTTP_FLAVOR_2;
    }
    return null;
  }

  @Nullable
  @Override
  public String getClientIp(HttpServletRequest request) {
    String addr = request.getHeader("X-Forwarded-For");
    if (isNullOrEmpty(addr)) {
      addr = request.getRemoteAddr();
    }
    return addr;
  }

  @Override
  public int getStatusCode(@Nullable HttpServletResponse response) {
    if (response != null) {
      return response.getStatus();
    } else {
      return 0;
    }
  }
}
