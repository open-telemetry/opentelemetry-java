/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.contrib.trace.propagation;

import static io.opentelemetry.internal.Utils.checkNotNull;

import io.grpc.Context;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.internal.StringUtils;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceState;
import io.opentelemetry.trace.TracingContextUtils;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.Immutable;

/**
 * Implementation of the Jaeger propagation protocol. See <a
 * href=https://www.jaegertracing.io/docs/client-libraries/#propagation-format>Jaeger Propogation
 * Format</a>.
 */
@Immutable
public class JaegerPropagator implements HttpTextFormat {

  private static final Logger logger = Logger.getLogger(JaegerPropagator.class.getName());

  static final String PROPAGATION_HEADER = "uber-trace-id";
  // Parent span has been deprecated but Jaeger propagation protocol requires it
  static final char DEPRECATED_PARENT_SPAN = '0';
  static final char PROPAGATION_HEADER_DELIMITER = ':';

  private static final int MAX_TRACE_ID_LENGTH = 2 * TraceId.getSize();
  private static final int MAX_SPAN_ID_LENGTH = 2 * SpanId.getSize();
  private static final int MAX_FLAGS_LENGTH = 2;

  private static final char IS_SAMPLED = '1';
  private static final char NOT_SAMPLED = '0';
  private static final int PROPAGATION_HEADER_DELIMITER_SIZE = 1;

  private static final int TRACE_ID_HEX_SIZE = 2 * TraceId.getSize();
  private static final int SPAN_ID_HEX_SIZE = 2 * SpanId.getSize();
  private static final int PARENT_SPAN_ID_SIZE = 1;
  private static final int SAMPLED_FLAG_SIZE = 1;

  private static final int SPAN_ID_OFFSET = TRACE_ID_HEX_SIZE + PROPAGATION_HEADER_DELIMITER_SIZE;
  private static final int PARENT_SPAN_ID_OFFSET =
      SPAN_ID_OFFSET + SPAN_ID_HEX_SIZE + PROPAGATION_HEADER_DELIMITER_SIZE;
  private static final int SAMPLED_FLAG_OFFSET =
      PARENT_SPAN_ID_OFFSET + PARENT_SPAN_ID_SIZE + PROPAGATION_HEADER_DELIMITER_SIZE;
  private static final int PROPAGATION_HEADER_SIZE = SAMPLED_FLAG_OFFSET + SAMPLED_FLAG_SIZE;

  private static final TraceFlags SAMPLED_FLAGS = TraceFlags.builder().setIsSampled(true).build();
  private static final TraceFlags NOT_SAMPLED_FLAGS =
      TraceFlags.builder().setIsSampled(false).build();

  private static final List<String> FIELDS = Collections.singletonList(PROPAGATION_HEADER);

  @Override
  public List<String> fields() {
    return FIELDS;
  }

  @Override
  public <C> void inject(Context context, C carrier, Setter<C> setter) {
    checkNotNull(context, "context");
    checkNotNull(setter, "setter");

    Span span = TracingContextUtils.getSpanWithoutDefault(context);
    if (span == null) {
      return;
    }

    SpanContext spanContext = span.getContext();

    char[] chars = new char[PROPAGATION_HEADER_SIZE];
    spanContext.getTraceId().copyLowerBase16To(chars, 0);
    chars[SPAN_ID_OFFSET - 1] = PROPAGATION_HEADER_DELIMITER;
    spanContext.getSpanId().copyLowerBase16To(chars, SPAN_ID_OFFSET);
    chars[PARENT_SPAN_ID_OFFSET - 1] = PROPAGATION_HEADER_DELIMITER;
    chars[PARENT_SPAN_ID_OFFSET] = DEPRECATED_PARENT_SPAN;
    chars[SAMPLED_FLAG_OFFSET - 1] = PROPAGATION_HEADER_DELIMITER;
    chars[SAMPLED_FLAG_OFFSET] = spanContext.getTraceFlags().isSampled() ? IS_SAMPLED : NOT_SAMPLED;
    setter.set(carrier, PROPAGATION_HEADER, new String(chars));
  }

  @Override
  public <C> Context extract(Context context, C carrier, Getter<C> getter) {
    checkNotNull(carrier, "carrier");
    checkNotNull(getter, "getter");

    SpanContext spanContext = getSpanContextFromHeader(carrier, getter);

    return TracingContextUtils.withSpan(DefaultSpan.create(spanContext), context);
  }

  @SuppressWarnings("StringSplitter")
  private static <C> SpanContext getSpanContextFromHeader(C carrier, Getter<C> getter) {
    String value = getter.get(carrier, PROPAGATION_HEADER);
    if (StringUtils.isNullOrEmpty(value)) {
      return SpanContext.getInvalid();
    }

    // if the delimiter (:) cannot be found then the propagation value could be URL
    // encoded, so we need to decode it before attempting to split it.
    if (value.lastIndexOf(PROPAGATION_HEADER_DELIMITER) == -1) {
      try {
        // the propagation value
        value = URLDecoder.decode(value, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        logger.info(
            "Error decoding '"
                + PROPAGATION_HEADER
                + "' with value "
                + value
                + ". Returning INVALID span context.");
        return SpanContext.getInvalid();
      }
    }

    String[] parts = value.split(String.valueOf(PROPAGATION_HEADER_DELIMITER));
    if (parts.length != 4) {
      logger.info(
          "Invalid header '"
              + PROPAGATION_HEADER
              + "' with value "
              + value
              + ". Returning INVALID span context.");
      return SpanContext.getInvalid();
    }

    String traceId = parts[0];
    if (!isTraceIdValid(traceId)) {
      logger.info(
          "Invalid TraceId in Jaeger header: '"
              + PROPAGATION_HEADER
              + "' with traceId "
              + traceId
              + ". Returning INVALID span context.");
      return SpanContext.getInvalid();
    }

    String spanId = parts[1];
    if (!isSpanIdValid(spanId)) {
      logger.info(
          "Invalid SpanId in Jaeger header: '"
              + PROPAGATION_HEADER
              + "'. Returning INVALID span context.");
      return SpanContext.getInvalid();
    }

    String flags = parts[3];
    if (!isFlagsValid(flags)) {
      logger.info(
          "Invalid Flags in Jaeger header: '"
              + PROPAGATION_HEADER
              + "'. Returning INVALID span context.");
      return SpanContext.getInvalid();
    }

    return buildSpanContext(traceId, spanId, flags);
  }

  private static SpanContext buildSpanContext(String traceId, String spanId, String flags) {
    try {
      int flagsInt = Integer.parseInt(flags);
      TraceFlags traceFlags = ((flagsInt & 1) == 1) ? SAMPLED_FLAGS : NOT_SAMPLED_FLAGS;

      return SpanContext.createFromRemoteParent(
          TraceId.fromLowerBase16(StringUtils.padLeft(traceId, MAX_TRACE_ID_LENGTH), 0),
          SpanId.fromLowerBase16(StringUtils.padLeft(spanId, MAX_SPAN_ID_LENGTH), 0),
          traceFlags,
          TraceState.getDefault());
    } catch (Exception e) {
      logger.log(
          Level.INFO,
          "Error parsing '" + PROPAGATION_HEADER + "' header. Returning INVALID span context.",
          e);
      return SpanContext.getInvalid();
    }
  }

  private static boolean isTraceIdValid(String value) {
    return !(StringUtils.isNullOrEmpty(value) || value.length() > MAX_TRACE_ID_LENGTH);
  }

  private static boolean isSpanIdValid(String value) {
    return !(StringUtils.isNullOrEmpty(value) || value.length() > MAX_SPAN_ID_LENGTH);
  }

  private static boolean isFlagsValid(String value) {
    return !(StringUtils.isNullOrEmpty(value) || value.length() > MAX_FLAGS_LENGTH);
  }
}
