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

package io.opentelemetry.trace.propagation;

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

  private static final Logger logger = Logger.getLogger(HttpTraceContext.class.getName());

  static final String TRACE_ID_HEADER = "uber-trace-id";
  // Parent span has been deprecated but Jaeger propagation protocol requires it
  static final String DEPRECATED_PARENT_SPAN = "0";
  static final String SEPARATOR = ":";

  private static final String IS_SAMPLED = "1";
  private static final String NOT_SAMPLED = "0";

  private static final int MAX_TRACE_ID_LENGTH = 2 * TraceId.getSize();
  private static final int MAX_SPAN_ID_LENGTH = 2 * SpanId.getSize();
  private static final int MAX_FLAGS_LENGTH = 2;
  private static final TraceFlags SAMPLED_FLAGS = TraceFlags.builder().setIsSampled(true).build();
  private static final TraceFlags NOT_SAMPLED_FLAGS =
      TraceFlags.builder().setIsSampled(false).build();

  private static final List<String> FIELDS = Collections.singletonList(TRACE_ID_HEADER);

  @Override
  public List<String> fields() {
    return FIELDS;
  }

  @Override
  public <C> void inject(Context context, C carrier, Setter<C> setter) {
    checkNotNull(context, "context");
    checkNotNull(setter, "setter");
    checkNotNull(carrier, "carrier");

    Span span = TracingContextUtils.getSpanWithoutDefault(context);
    if (span == null) {
      return;
    }

    SpanContext spanContext = span.getContext();
    String sampled = spanContext.getTraceFlags().isSampled() ? IS_SAMPLED : NOT_SAMPLED;

    setter.set(
        carrier,
        TRACE_ID_HEADER,
        spanContext.getTraceId().toLowerBase16()
            + SEPARATOR
            + spanContext.getSpanId().toLowerBase16()
            + SEPARATOR
            + DEPRECATED_PARENT_SPAN
            + SEPARATOR
            + sampled);
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
    String value = getter.get(carrier, TRACE_ID_HEADER);
    if (StringUtils.isNullOrEmpty(value)) {
      return SpanContext.getInvalid();
    }

    try {
      value = URLDecoder.decode(value, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      logger.info(
          "Error decoding '"
              + TRACE_ID_HEADER
              + "' with value "
              + value
              + ". Returning INVALID span context.");
      return SpanContext.getInvalid();
    }

    String[] parts = value.split(SEPARATOR);
    if (parts.length != 4) {
      logger.info(
          "Invalid header '"
              + TRACE_ID_HEADER
              + "' with value "
              + value
              + ". Returning INVALID span context.");
      return SpanContext.getInvalid();
    }

    String traceId = parts[0];
    if (!isTraceIdValid(traceId)) {
      logger.info(
          "Invalid TraceId in Jaeger header: '"
              + TRACE_ID_HEADER
              + "' with traceId "
              + traceId
              + ". Returning INVALID span context.");
      return SpanContext.getInvalid();
    }

    String spanId = parts[1];
    if (!isSpanIdValid(spanId)) {
      logger.info(
          "Invalid SpanId in Jaeger header: '"
              + TRACE_ID_HEADER
              + "'. Returning INVALID span context.");
      return SpanContext.getInvalid();
    }

    String flags = parts[3];
    if (!isFlagsValid(flags)) {
      logger.info(
          "Invalid Flags in Jaeger header: '"
              + TRACE_ID_HEADER
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
          "Error parsing '" + TRACE_ID_HEADER + "' header. Returning INVALID span context.",
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
