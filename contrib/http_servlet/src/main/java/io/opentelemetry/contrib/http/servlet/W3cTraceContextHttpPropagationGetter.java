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

import static io.opentelemetry.contrib.http.servlet.MultiSchemeHttpPropagationGetter.TRACEPARENT;
import static io.opentelemetry.contrib.http.servlet.MultiSchemeHttpPropagationGetter.TRACESTATE;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

/** Extracts W3C Trace Context header values. */
public class W3cTraceContextHttpPropagationGetter extends BaseSchemeSpecificHttpPropagationGetter {

  /** Unique id of this scheme-specific getter. */
  public static final String SCHEME_NAME = "w3cTraceContext";

  /** Constructs a getter object. */
  public W3cTraceContextHttpPropagationGetter() {
    super(SCHEME_NAME);
  }

  @Override
  public boolean canProvideValues(HttpServletRequest request) {
    return request.getHeader(TRACEPARENT) != null;
  }

  @Nullable
  @Override
  protected String extractAndConstructTraceparentHeaderValue(HttpServletRequest request) {
    return request.getHeader(TRACEPARENT);
  }

  @Nullable
  @Override
  protected String extractAndConstructTracestateHeaderValue(HttpServletRequest request) {
    return request.getHeader(TRACESTATE);
  }
}
