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
import static io.opentelemetry.contrib.http.servlet.MultiSchemeHttpPropagationGetter.TRACEPARENT;
import static io.opentelemetry.contrib.http.servlet.MultiSchemeHttpPropagationGetter.TRACESTATE;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

/** Extracts W3C Trace Context header values. */
public class W3cTraceContextHttpPropagationGetter extends BaseSchemeSpecificHttpPropagationGetter {

  /** Unique id of this scheme-specific getter. */
  public static final String SCHEME_NAME = "w3cTraceContext";

  private static final int MAX_LEN_TRACESTATE = 512;
  private static final Pattern REGEX_TRACEPARENT =
      Pattern.compile("^00-[0-9a-f]{32}-[0-9a-f]{16}-[0-9a-f]{2}$");
  private static final Pattern REGEX_TRACESTATE =
      Pattern.compile(
          "^[0-9a-z_*/@-]{1,255}=[!-+--<>-~]{1,255}(,[0-9a-z_*/@-]{1,255}=[!-+--<>-~]{1,255})*$");
  private static final Logger LOGGER =
      Logger.getLogger(W3cTraceContextHttpPropagationGetter.class.getName());

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
    String traceparent = request.getHeader(TRACEPARENT);
    if (isNullOrEmpty(traceparent)) {
      return null;
    }
    Matcher matcher = REGEX_TRACEPARENT.matcher(traceparent);
    if (!matcher.find()) {
      LOGGER.log(Level.WARNING, lenientFormat("Invalid W3C Trace ID: %s", traceparent));
      return null;
    }
    return traceparent;
  }

  @Nullable
  @Override
  protected String extractAndConstructTracestateHeaderValue(HttpServletRequest request) {
    String tracestate = request.getHeader(TRACESTATE);
    if (isNullOrEmpty(tracestate)) {
      return null;
    }
    Matcher matcher = REGEX_TRACESTATE.matcher(tracestate);
    if (!matcher.find() || tracestate.length() > MAX_LEN_TRACESTATE) {
      LOGGER.log(Level.WARNING, lenientFormat("Invalid W3C Trace State: %s", tracestate));
      return null;
    }
    return tracestate;
  }
}
