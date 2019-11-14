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

package io.opentelemetry.contrib.web.servlet.filter;

import io.opentelemetry.context.propagation.HttpExtractor;
import javax.servlet.http.HttpServletRequest;

/** Tracer extract adapter for {@link HttpServletRequest}. */
public final class HttpServletRequestGetter implements HttpExtractor.Getter<HttpServletRequest> {
  private static final HttpServletRequestGetter INSTANCE = new HttpServletRequestGetter();

  public static HttpServletRequestGetter getInstance() {
    return INSTANCE;
  }

  @Override
  public String get(HttpServletRequest carrier, String key) {
    return carrier.getHeader(key);
  }

  private HttpServletRequestGetter() {}
}
