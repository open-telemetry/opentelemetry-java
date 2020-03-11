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

package io.opentelemetry.trace.propagation;

import static io.opentelemetry.internal.Utils.checkNotNull;

import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.internal.StringUtils;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceState;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.Immutable;

/**
 * Implementation of the B3 propagation protocol. See <a
 * href=https://github.com/openzipkin/b3-propagation>openzipkin/b3-propagation</a>.
 */
@Immutable
public class B3Propagator implements HttpTextFormat<SpanContext> {
  private static final Logger logger = Logger.getLogger(HttpTraceContext.class.getName());

  static final String TRACE_ID_HEADER = "X-B3-TraceId";
  static final String SPAN_ID_HEADER = "X-B3-SpanId";
  static final String SAMPLED_HEADER = "X-B3-Sampled";
  static final String TRUE_INT = "1";
  static final String FALSE_INT = "0";
  static final String COMBINED_HEADER = "b3";
  static final String COMBINED_HEADER_DELIMITER = "-";

  private static final int MAX_TRACE_ID_LENGTH = 2 * TraceId.getSize();
  private static final int MAX_SPAN_ID_LENGTH = 2 * SpanId.getSize();
  private static final TraceFlags SAMPLED_FLAGS = TraceFlags.builder().setIsSampled(true).build();
  private static final TraceFlags NOT_SAMPLED_FLAGS =
      TraceFlags.builder().setIsSampled(false).build();

  private static final List<String> FIELDS =
      Collections.unmodifiableList(Arrays.asList(TRACE_ID_HEADER, SPAN_ID_HEADER, SAMPLED_HEADER));

  private final boolean singleHeader;

  /** Creates a new instance of {@link B3Propagator}. Defaults to use multiple headers. */
  public B3Propagator() {
    this(false);
  }

  /**
   * Creates a new instance of {@link B3Propagator}.
   *
   * @param singleHeader whether to use single or multiple headers.
   */
  public B3Propagator(boolean singleHeader) {
    this.singleHeader = singleHeader;
  }

  @Override
  public List<String> fields() {
    return FIELDS;
  }

  @Override
  public <C> void inject(SpanContext spanContext, C carrier, Setter<C> setter) {
    checkNotNull(spanContext, "spanContext");
    checkNotNull(setter, "setter");
    checkNotNull(carrier, "carrier");

    String sampled = spanContext.getTraceFlags().isSampled() ? TRUE_INT : FALSE_INT;

    if (singleHeader) {
      setter.set(
          carrier,
          COMBINED_HEADER,
          spanContext.getTraceId().toLowerBase16()
              + COMBINED_HEADER_DELIMITER
              + spanContext.getSpanId().toLowerBase16()
              + COMBINED_HEADER_DELIMITER
              + sampled);
    } else {
      setter.set(carrier, TRACE_ID_HEADER, spanContext.getTraceId().toLowerBase16());
      setter.set(carrier, SPAN_ID_HEADER, spanContext.getSpanId().toLowerBase16());
      setter.set(carrier, SAMPLED_HEADER, sampled);
    }
  }

  @Override
  public <C> SpanContext extract(C carrier, Getter<C> getter) {
    checkNotNull(carrier, "carrier");
    checkNotNull(getter, "getter");

    if (singleHeader) {
      return getSpanContextFromSingleHeader(carrier, getter);
    } else {
      return getSpanContextFromMultipleHeaders(carrier, getter);
    }
  }

  @SuppressWarnings("StringSplitter")
  private static <C> SpanContext getSpanContextFromSingleHeader(C carrier, Getter<C> getter) {
    String value = getter.get(carrier, COMBINED_HEADER);
    if (StringUtils.isNullOrEmpty(value)) {
      logger.info(
          "Missing or empty combined header: "
              + COMBINED_HEADER
              + ". Returning INVALID span context.");
      return SpanContext.getInvalid();
    }

    // must have between 2 and 4 hyphen delimieted parts:
    //   traceId-spanId-sampled-parentSpanId (last two are optional)
    // NOTE: we do not use parentSpanId
    String[] parts = value.split(COMBINED_HEADER_DELIMITER);
    if (parts.length < 2 || parts.length > 4) {
      logger.info(
          "Invalid combined header '" + COMBINED_HEADER + ". Returning INVALID span context.");
      return SpanContext.getInvalid();
    }

    String traceId = parts[0];
    if (!isTraceIdValid(traceId)) {
      logger.info(
          "Invalid TraceId in B3 header: " + COMBINED_HEADER + ". Returning INVALID span context.");
      return SpanContext.getInvalid();
    }

    String spanId = parts[1];
    if (!isSpanIdValid(spanId)) {
      logger.info(
          "Invalid SpanId in B3 header: " + COMBINED_HEADER + ". Returning INVALID span context.");
      return SpanContext.getInvalid();
    }

    String sampled = parts.length == 3 ? parts[2] : null;

    return buildSpanContext(traceId, spanId, sampled);
  }

  private static <C> SpanContext getSpanContextFromMultipleHeaders(C carrier, Getter<C> getter) {
    String traceId = getter.get(carrier, TRACE_ID_HEADER);
    if (!isTraceIdValid(traceId)) {
      logger.info(
          "Invalid TraceId in B3 header: "
              + TRACE_ID_HEADER
              + "'. Returning INVALID span context.");
      return SpanContext.getInvalid();
    }

    String spanId = getter.get(carrier, SPAN_ID_HEADER);
    if (!isSpanIdValid(spanId)) {
      logger.info(
          "Invalid SpanId in B3 header: " + SPAN_ID_HEADER + "'. Returning INVALID span context.");
      return SpanContext.getInvalid();
    }

    String sampled = getter.get(carrier, SAMPLED_HEADER);
    return buildSpanContext(traceId, spanId, sampled);
  }

  private static SpanContext buildSpanContext(String traceId, String spanId, String sampled) {
    try {
      TraceFlags traceFlags =
          TRUE_INT.equals(sampled) || Boolean.parseBoolean(sampled) // accept either "1" or "true"
              ? SAMPLED_FLAGS
              : NOT_SAMPLED_FLAGS;

      return SpanContext.createFromRemoteParent(
          TraceId.fromLowerBase16(traceId, 0),
          SpanId.fromLowerBase16(spanId, 0),
          traceFlags,
          TraceState.getDefault());
    } catch (Exception e) {
      logger.log(Level.INFO, "Error parsing B3 header. Returning INVALID span context.", e);
      return SpanContext.getInvalid();
    }
  }

  private static boolean isTraceIdValid(String value) {
    return !(StringUtils.isNullOrEmpty(value) || value.length() > MAX_TRACE_ID_LENGTH);
  }

  private static boolean isSpanIdValid(String value) {
    return !(StringUtils.isNullOrEmpty(value) || value.length() > MAX_SPAN_ID_LENGTH);
  }
}
