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

import io.opentelemetry.context.propagation.HttpTextFormat;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

/**
 * Provides support for integrating with multiple external tracing system which use various HTTP
 * headers to propagate distributed tracing information. Keys requested are expected to be those
 * defined by the W3C Trace Context specification.
 */
public class MultiSchemeHttpPropagationGetter implements HttpTextFormat.Getter<HttpServletRequest> {

  /** W3C Trace Context traceparent header name. */
  public static final String TRACEPARENT = "traceparent";
  /** W3C Trace Context tracestate header name. */
  public static final String TRACESTATE = "tracestate";

  @Nullable
  @Override
  public String get(HttpServletRequest carrier, String key) {
    return null;
  }
}
