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

import io.opentelemetry.contrib.http.core.HttpExtractor;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Extracts span attributes from {@link HttpServletRequest} and {@link HttpServletResponse} with
 * routes being the URI path.
 */
public class UriPathDrivenHttpServletExtractor
    extends HttpExtractor<HttpServletRequest, HttpServletResponse> {

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
  public String getRoute(HttpServletRequest request) {
    return request.getRequestURI();
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
