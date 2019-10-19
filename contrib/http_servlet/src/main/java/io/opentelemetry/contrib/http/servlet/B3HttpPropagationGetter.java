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
import static com.google.common.base.Strings.padStart;
import static com.google.common.base.Strings.repeat;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

/** Extracts B3 Propagation header values. */
public class B3HttpPropagationGetter extends BaseSchemeSpecificHttpPropagationGetter {

  /** Unique id of this scheme-specific getter. */
  public static final String SCHEME_NAME = "b3Propagation";
  /** B3 Propagation tracing id header. */
  public static final String B3TRACEID = "X-B3-TraceId";
  /** B3 Propagation parent span id header. */
  public static final String B3PARENTSPANID = "X-B3-ParentSpanId";
  /** B3 Propagation span id header. */
  public static final String B3SPANID = "X-B3-SpanId";
  /** B3 Propagation sampled header. */
  public static final String B3SAMPLED = "X-B3-Sampled";
  /** B3 Propagation flags header. */
  public static final String B3FLAGS = "X-B3-Flags";
  /** B3 Propagation combined ids header. */
  public static final String B3COMBINED = "b3";
  /** Tracestate key for B3 parent span id. */
  public static final String B3PARENTSPANIDKEY = "b3ParentId";

  private static final String B3_ISSAMPLED = "1";
  private static final String TRACE_FLAGS_DEFAULT = "00";
  private static final String TRACE_FLAGS_SAMPLED = "01";
  private static final String W3C_VERSION = "00";
  private static final char W3C_SEPARATOR = '-';
  private static final String NOPARENT_SAMPLED =
      W3C_VERSION
          + W3C_SEPARATOR
          + repeat("0", 32)
          + W3C_SEPARATOR
          + repeat("0", 16)
          + W3C_SEPARATOR
          + TRACE_FLAGS_SAMPLED;
  private static final String NOPARENT_DEFAULT =
      W3C_VERSION
          + W3C_SEPARATOR
          + repeat("0", 32)
          + W3C_SEPARATOR
          + repeat("0", 16)
          + W3C_SEPARATOR
          + TRACE_FLAGS_DEFAULT;
  private static final String B3_DEFAULT_SPAN = "0000000000000002";
  private static final Pattern REGEX_TRACEID = Pattern.compile("^([0-9a-f]{32}|[0-9a-f]{16})$");
  private static final Pattern REGEX_SPANID = Pattern.compile("^[0-9a-f]{16}$");
  private static final Pattern REGEX_SAMPLE = Pattern.compile("^[01]$");
  private static final Pattern REGEX_COMBINED =
      Pattern.compile("^([0-9a-f]{32}|[0-9a-f]{16})(-[0-9a-f]{16})(-[01])?(-[0-9a-f]{16})?$");
  private static final Logger LOGGER = Logger.getLogger(B3HttpPropagationGetter.class.getName());

  /** Constructs a getter object. */
  public B3HttpPropagationGetter() {
    super(SCHEME_NAME);
  }

  @Override
  public boolean canProvideValues(HttpServletRequest request) {
    return request.getHeader(B3TRACEID) != null
        || request.getHeader(B3COMBINED) != null
        || request.getHeader(B3SAMPLED) != null
        || request.getHeader(B3FLAGS) != null;
  }

  @Nullable
  @Override
  protected String extractAndConstructTraceparentHeaderValue(HttpServletRequest request) {
    String b3Combined = request.getHeader(B3COMBINED);
    if (isNullOrEmpty(b3Combined)) {
      return convertB3Headers2Traceparent(request);
    } else {
      return convertB32Traceparent(b3Combined);
    }
  }

  @Nullable
  @Override
  protected String extractAndConstructTracestateHeaderValue(HttpServletRequest request) {
    String b3Combined = request.getHeader(B3COMBINED);
    if (isNullOrEmpty(b3Combined)) {
      return convertB32Tracestate(request);
    } else {
      return convertB32Tracestate(b3Combined);
    }
  }

  @Nullable
  private static String convertB3Headers2Traceparent(HttpServletRequest request) {
    String traceId = request.getHeader(B3TRACEID);
    String sampled =
        request.getHeader(B3SAMPLED) != null
            ? request.getHeader(B3SAMPLED)
            : request.getHeader(B3FLAGS);
    if (isNullOrEmpty(traceId) && isNullOrEmpty(sampled)) {
      return null;
    }
    if (isNullOrEmpty(traceId)) {
      return B3_ISSAMPLED.equals(sampled) ? NOPARENT_SAMPLED : NOPARENT_DEFAULT;
    }
    String spanId =
        request.getHeader(B3SPANID) != null ? request.getHeader(B3SPANID) : B3_DEFAULT_SPAN;
    Matcher traceMatcher = REGEX_TRACEID.matcher(traceId);
    if (!traceMatcher.find()) {
      LOGGER.log(Level.WARNING, lenientFormat("Invalid B3 Trace ID: %s", traceId));
      return null;
    }
    Matcher spanMatcher = REGEX_SPANID.matcher(spanId);
    if (!spanMatcher.find()) {
      LOGGER.log(Level.WARNING, lenientFormat("Invalid B3 Span ID: %s", spanId));
      return null;
    }
    return constructTraceparent(traceId, spanId, sampled);
  }

  @Nullable
  private static String convertB32Traceparent(String b3Combined) {
    Matcher matcher = REGEX_COMBINED.matcher(b3Combined);
    if (!matcher.find()) {
      Matcher sampledMatcher = REGEX_SAMPLE.matcher(b3Combined);
      if (!sampledMatcher.find()) {
        LOGGER.log(Level.WARNING, lenientFormat("Invalid B3 Combined ID: %s", b3Combined));
        return null;
      } else {
        return B3_ISSAMPLED.equals(b3Combined) ? NOPARENT_SAMPLED : NOPARENT_DEFAULT;
      }
    }
    return constructTraceparent(
        matcher.group(1), matcher.group(2).substring(1), matcher.group(3).substring(1));
  }

  private static String constructTraceparent(String traceId, String spanId, String sampled) {
    return new StringBuilder()
        .append(W3C_VERSION)
        .append(W3C_SEPARATOR)
        .append(padStart(traceId, 32, '0'))
        .append(W3C_SEPARATOR)
        .append(spanId)
        .append(W3C_SEPARATOR)
        .append(B3_ISSAMPLED.equals(sampled) ? TRACE_FLAGS_SAMPLED : TRACE_FLAGS_DEFAULT)
        .toString();
  }

  @Nullable
  private static String convertB32Tracestate(HttpServletRequest request) {
    String parentSpanId = request.getHeader(B3PARENTSPANID);
    return constructTracestateFromB3ParentSpanId(parentSpanId);
  }

  @Nullable
  private static String convertB32Tracestate(String b3Combined) {
    Matcher matcher = REGEX_COMBINED.matcher(b3Combined);
    if (!matcher.find()) {
      return null;
    }
    String parentSpanId = matcher.group(4);
    if (isNullOrEmpty(parentSpanId)) {
      return null;
    } else {
      return constructTracestateFromB3ParentSpanId(parentSpanId.substring(1));
    }
  }

  @Nullable
  private static String constructTracestateFromB3ParentSpanId(String parentSpanId) {
    if (parentSpanId == null) {
      return null;
    }
    Matcher matcher = REGEX_SPANID.matcher(parentSpanId);
    if (!matcher.find()) {
      LOGGER.log(Level.WARNING, lenientFormat("Invalid B3 Parent Span ID: %s", parentSpanId));
      return null;
    }
    return new StringBuilder()
        .append(B3PARENTSPANIDKEY)
        .append('=')
        .append(parentSpanId)
        .toString();
  }
}
