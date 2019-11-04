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
import static io.opentelemetry.contrib.http.core.HttpTraceConstants.INSTRUMENTATION_LIB_ID;

import com.google.errorprone.annotations.MustBeClosed;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.contrib.http.core.HttpRequestContext;
import io.opentelemetry.contrib.http.core.HttpServerHandler;
import io.opentelemetry.trace.DefaultSpan;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Provides static utility methods for working with Java Servlet objects. */
public class OtelHttpServletUtils {

  static final String CONTENT_LENGTH = "Content-Length";
  static final String OTEL_SERVLET_LISTENER = "io.opentelemetry.servlet.listener";

  static void recordMessageSentEvent(
      HttpServerHandler<HttpServletRequest, HttpServletResponse, HttpServletRequest> handler,
      HttpRequestContext context,
      HttpServletResponse response) {
    if (response != null) {
      String length = response.getHeader(CONTENT_LENGTH);
      if (!isNullOrEmpty(length)) {
        try {
          handler.handleMessageSent(context, Integer.parseInt(length));
        } catch (NumberFormatException ignore) {
          // NoOp
        }
      }
    }
  }

  /**
   * Provides for starting of a tracing span for an HTTP request of whether the request is
   * synchronous or asynchronous.
   *
   * @param request the HTTP request
   * @return the new wrapping scope
   */
  @MustBeClosed
  public static Scope withScope(ServletRequest request) {
    checkNotNull(request, "request");
    OtelHttpServletListener listener =
        (OtelHttpServletListener) request.getAttribute(OTEL_SERVLET_LISTENER);
    if (listener != null) {
      return listener.withSpan();
    }
    return OpenTelemetry.getTracerFactory()
        .get(INSTRUMENTATION_LIB_ID)
        .withSpan(DefaultSpan.getInvalid());
  }

  private OtelHttpServletUtils() {
    super(); // static fields and methods only
  }
}
