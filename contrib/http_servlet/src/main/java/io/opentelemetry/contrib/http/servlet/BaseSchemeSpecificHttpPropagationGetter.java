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
import static io.opentelemetry.contrib.http.servlet.MultiSchemeHttpPropagationGetter.TRACEPARENT;
import static io.opentelemetry.contrib.http.servlet.MultiSchemeHttpPropagationGetter.TRACESTATE;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

/** Abstract base class for implementations of {@link SchemeSpecificHttpPropagationGetter}. */
public abstract class BaseSchemeSpecificHttpPropagationGetter
    implements SchemeSpecificHttpPropagationGetter {

  private final String name;

  /**
   * Constructs a getter object.
   *
   * @param name the concrete getter unique id
   */
  protected BaseSchemeSpecificHttpPropagationGetter(String name) {
    checkNotNull(name, "name is required");
    this.name = name;
  }

  @Override
  public final String getName() {
    return name;
  }

  @Nullable
  @Override
  public final String get(HttpServletRequest carrier, String key) {
    if (TRACEPARENT.equalsIgnoreCase(key)) {
      return extractAndConstructTraceparentHeaderValue(carrier);
    } else if (TRACESTATE.equalsIgnoreCase(key)) {
      return extractAndConstructTracestateHeaderValue(carrier);
    } else {
      return null;
    }
  }

  /**
   * Returns a valid W3C Trace Context <code>traceparent</code> header value.
   *
   * @param request the http request to extract the value from
   * @return the header value
   */
  @Nullable
  protected abstract String extractAndConstructTraceparentHeaderValue(HttpServletRequest request);

  /**
   * Returns a valid W3C Trace Context <code>tracestate</code> header value.
   *
   * @param request the http request to extract the value from
   * @return the header value
   */
  @Nullable
  protected abstract String extractAndConstructTracestateHeaderValue(HttpServletRequest request);
}
