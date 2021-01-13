/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Implementation of the Jaeger propagation protocol. See <a
 * href=https://www.jaegertracing.io/docs/client-libraries/#propagation-format>Jaeger Propagation
 * Format</a>.
 */
@Immutable
@SuppressWarnings("deprecation") // Remove after StringUtils is made package-private
public final class JaegerPropagator implements TextMapPropagator {

  private static final Logger logger = Logger.getLogger(JaegerPropagator.class.getName());

  static final String PROPAGATION_HEADER = "uber-trace-id";
  static final String BAGGAGE_HEADER = "jaeger-baggage";
  static final String BAGGAGE_PREFIX = "uberctx-";
  // Parent span has been deprecated but Jaeger propagation protocol requires it
  static final char DEPRECATED_PARENT_SPAN = '0';
  static final char PROPAGATION_HEADER_DELIMITER = ':';

  private static final int MAX_TRACE_ID_LENGTH = TraceId.getHexLength();
  private static final int MAX_SPAN_ID_LENGTH = SpanId.getHexLength();
  private static final int MAX_FLAGS_LENGTH = 2;

  private static final char IS_SAMPLED_CHAR = '1';
  private static final char NOT_SAMPLED_CHAR = '0';
  private static final int PROPAGATION_HEADER_DELIMITER_SIZE = 1;

  private static final int TRACE_ID_HEX_SIZE = TraceId.getHexLength();
  private static final int SPAN_ID_HEX_SIZE = SpanId.getHexLength();
  private static final int PARENT_SPAN_ID_SIZE = 1;
  private static final int SAMPLED_FLAG_SIZE = 1;

  private static final int SPAN_ID_OFFSET = TRACE_ID_HEX_SIZE + PROPAGATION_HEADER_DELIMITER_SIZE;
  private static final int PARENT_SPAN_ID_OFFSET =
      SPAN_ID_OFFSET + SPAN_ID_HEX_SIZE + PROPAGATION_HEADER_DELIMITER_SIZE;
  private static final int SAMPLED_FLAG_OFFSET =
      PARENT_SPAN_ID_OFFSET + PARENT_SPAN_ID_SIZE + PROPAGATION_HEADER_DELIMITER_SIZE;
  private static final int PROPAGATION_HEADER_SIZE = SAMPLED_FLAG_OFFSET + SAMPLED_FLAG_SIZE;

  private static final byte SAMPLED = TraceFlags.getSampled();
  private static final byte NOT_SAMPLED = TraceFlags.getDefault();

  private static final Collection<String> FIELDS = Collections.singletonList(PROPAGATION_HEADER);

  private static final JaegerPropagator INSTANCE = new JaegerPropagator();

  private JaegerPropagator() {
    // singleton
  }

  public static JaegerPropagator getInstance() {
    return INSTANCE;
  }

  @Override
  public Collection<String> fields() {
    return FIELDS;
  }

  @Override
  public <C> void inject(Context context, C carrier, Setter<C> setter) {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(setter, "setter");

    SpanContext spanContext = Span.fromContext(context).getSpanContext();
    if (spanContext.isValid()) {
      injectSpan(spanContext, carrier, setter);
    }

    injectBaggage(Baggage.fromContext(context), carrier, setter);
  }

  private static <C> void injectSpan(SpanContext spanContext, C carrier, Setter<C> setter) {

    char[] chars = new char[PROPAGATION_HEADER_SIZE];

    String traceId = spanContext.getTraceIdAsHexString();
    for (int i = 0; i < traceId.length(); i++) {
      chars[i] = traceId.charAt(i);
    }

    chars[SPAN_ID_OFFSET - 1] = PROPAGATION_HEADER_DELIMITER;
    String spanId = spanContext.getSpanIdAsHexString();
    for (int i = 0; i < spanId.length(); i++) {
      chars[SPAN_ID_OFFSET + i] = spanId.charAt(i);
    }

    chars[PARENT_SPAN_ID_OFFSET - 1] = PROPAGATION_HEADER_DELIMITER;
    chars[PARENT_SPAN_ID_OFFSET] = DEPRECATED_PARENT_SPAN;
    chars[SAMPLED_FLAG_OFFSET - 1] = PROPAGATION_HEADER_DELIMITER;
    chars[SAMPLED_FLAG_OFFSET] = spanContext.isSampled() ? IS_SAMPLED_CHAR : NOT_SAMPLED_CHAR;
    setter.set(carrier, PROPAGATION_HEADER, new String(chars));
  }

  private static <C> void injectBaggage(Baggage baggage, C carrier, Setter<C> setter) {
    baggage.forEach(
        (key, value, metadata) -> {
          setter.set(carrier, BAGGAGE_PREFIX + key, value);
        });
  }

  @Override
  public <C> Context extract(Context context, @Nullable C carrier, Getter<C> getter) {
    Objects.requireNonNull(getter, "getter");

    SpanContext spanContext = getSpanContextFromHeader(carrier, getter);
    if (spanContext.isValid()) {
      context = context.with(Span.wrap(spanContext));
    }

    Baggage baggage = getBaggageFromHeader(carrier, getter);
    if (baggage != null) {
      context = context.with(baggage);
    }

    return context;
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
        logger.fine(
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
      logger.fine(
          "Invalid header '"
              + PROPAGATION_HEADER
              + "' with value "
              + value
              + ". Returning INVALID span context.");
      return SpanContext.getInvalid();
    }

    String traceId = parts[0];
    if (!isTraceIdValid(traceId)) {
      logger.fine(
          "Invalid TraceId in Jaeger header: '"
              + PROPAGATION_HEADER
              + "' with traceId "
              + traceId
              + ". Returning INVALID span context.");
      return SpanContext.getInvalid();
    }

    String spanId = parts[1];
    if (!isSpanIdValid(spanId)) {
      logger.fine(
          "Invalid SpanId in Jaeger header: '"
              + PROPAGATION_HEADER
              + "'. Returning INVALID span context.");
      return SpanContext.getInvalid();
    }

    String flags = parts[3];
    if (!isFlagsValid(flags)) {
      logger.fine(
          "Invalid Flags in Jaeger header: '"
              + PROPAGATION_HEADER
              + "'. Returning INVALID span context.");
      return SpanContext.getInvalid();
    }

    return buildSpanContext(traceId, spanId, flags);
  }

  private static <C> Baggage getBaggageFromHeader(C carrier, Getter<C> getter) {
    BaggageBuilder builder = null;

    for (String key : getter.keys(carrier)) {
      if (key.startsWith(BAGGAGE_PREFIX)) {
        if (key.length() == BAGGAGE_PREFIX.length()) {
          continue;
        }

        if (builder == null) {
          builder = Baggage.builder();
        }
        builder.put(key.substring(BAGGAGE_PREFIX.length()), getter.get(carrier, key));
      } else if (key.equals(BAGGAGE_HEADER)) {
        builder = parseBaggageHeader(getter.get(carrier, key), builder);
      }
    }
    return builder == null ? null : builder.build();
  }

  @SuppressWarnings("StringSplitter")
  private static BaggageBuilder parseBaggageHeader(String header, BaggageBuilder builder) {
    for (String part : header.split("\\s*,\\s*")) {
      String[] kv = part.split("\\s*=\\s*");
      if (kv.length == 2) {
        if (builder == null) {
          builder = Baggage.builder();
        }
        builder.put(kv[0], kv[1]);
      } else {
        logger.fine("malformed token in " + BAGGAGE_HEADER + " header: " + part);
      }
    }
    return builder;
  }

  private static SpanContext buildSpanContext(String traceId, String spanId, String flags) {
    try {
      int flagsInt = Integer.parseInt(flags);
      byte traceFlags = ((flagsInt & 1) == 1) ? SAMPLED : NOT_SAMPLED;

      String otelTraceId = StringUtils.padLeft(traceId, MAX_TRACE_ID_LENGTH);
      String otelSpanId = StringUtils.padLeft(spanId, MAX_SPAN_ID_LENGTH);
      if (!TraceId.isValid(otelTraceId) || !SpanId.isValid(otelSpanId)) {
        return SpanContext.getInvalid();
      }
      return SpanContext.createFromRemoteParent(
          otelTraceId, otelSpanId, traceFlags, TraceState.getDefault());
    } catch (Exception e) {
      logger.log(
          Level.FINE,
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
