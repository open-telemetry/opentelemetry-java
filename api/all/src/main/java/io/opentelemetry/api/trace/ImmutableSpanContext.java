/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
abstract class ImmutableSpanContext implements SpanContext {

  private static final SpanContext INVALID =
      create(
          TraceId.getInvalid(),
          SpanId.getInvalid(),
          TraceFlags.getDefault(),
          TraceState.getDefault(),
          /* remote= */ false);

  static SpanContext getInvalid() {
    return INVALID;
  }

  static SpanContext create(
      String traceIdHex,
      String spanIdHex,
      String traceFlags,
      TraceState traceState,
      boolean remote) {
    return new AutoValue_ImmutableSpanContext(
        traceIdHex, spanIdHex, traceState, remote, TraceFlags.parsedFromHex(traceFlags));
  }

  @Override
  @Memoized
  public byte[] getTraceIdBytes() {
    return SpanContext.super.getTraceIdBytes();
  }

  @Override
  @Memoized
  public byte[] getSpanIdBytes() {
    return SpanContext.super.getSpanIdBytes();
  }

  @Override
  @Memoized
  public boolean isValid() {
    return SpanContext.super.isValid();
  }

  @Override
  public final boolean isSampled() {
    return getParsedTraceFlags().sampled();
  }

  @Override
  public final String getTraceFlags() {
    return getParsedTraceFlags().hex();
  }

  abstract TraceFlags getParsedTraceFlags();
}
