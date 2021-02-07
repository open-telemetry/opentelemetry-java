/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation;

import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.Immutable;

/**
 * This set of common propagator utils is currently only used by the OtTracerPropagator and the
 * B3Propagator.
 */
@Immutable
final class Common {
  private static final Logger logger = Logger.getLogger(Common.class.getName());

  static final String TRUE_INT = "1";
  static final String FALSE_INT = "0";
  static final int MAX_TRACE_ID_LENGTH = TraceId.getHexLength();
  static final int MIN_TRACE_ID_LENGTH = MAX_TRACE_ID_LENGTH / 2;
  private static final byte SAMPLED = TraceFlags.getSampled();
  private static final byte NOT_SAMPLED = TraceFlags.getDefault();

  private Common() {}

  static SpanContext buildSpanContext(String traceId, String spanId, String sampled) {
    try {
      byte traceFlags =
          TRUE_INT.equals(sampled) || Boolean.parseBoolean(sampled) // accept either "1" or "true"
              ? SAMPLED
              : NOT_SAMPLED;

      return SpanContext.createFromRemoteParent(
          StringUtils.padLeft(traceId, MAX_TRACE_ID_LENGTH),
          spanId,
          traceFlags,
          TraceState.getDefault());
    } catch (RuntimeException e) {
      logger.log(Level.FINE, "Error parsing header. Returning INVALID span context.", e);
      return SpanContext.getInvalid();
    }
  }

  static boolean isTraceIdValid(String value) {
    return !(StringUtils.isNullOrEmpty(value)
        || (value.length() != MIN_TRACE_ID_LENGTH && value.length() != MAX_TRACE_ID_LENGTH)
        || !TraceId.isValid(StringUtils.padLeft(value, TraceId.getHexLength())));
  }

  static boolean isSpanIdValid(String value) {
    return !StringUtils.isNullOrEmpty(value) && SpanId.isValid(value);
  }
}
