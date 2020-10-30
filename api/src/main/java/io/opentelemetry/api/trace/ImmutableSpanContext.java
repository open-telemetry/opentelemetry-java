/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import javax.annotation.concurrent.Immutable;

/**
 * A class that represents a span context. A span context contains the state that must propagate to
 * child {@link Span}s and across process boundaries. It contains the identifiers (a {@link TraceId
 * trace_id} and {@link SpanId span_id}) associated with the {@link Span} and a set of options
 * (currently only whether the context is sampled or not), as well as the {@link TraceState
 * traceState} and the {@link boolean remote} flag.
 */
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

  /**
   * Returns the invalid {@code SpanContext} that can be used for no-op operations.
   *
   * @return the invalid {@code SpanContext}.
   */
  static SpanContext getInvalid() {
    return INVALID;
  }

  static SpanContext create(
      String traceIdHex, String spanIdHex, byte traceFlags, TraceState traceState, boolean remote) {
    return new AutoValue_ImmutableSpanContext(
        traceIdHex, spanIdHex, traceFlags, traceState, remote);
  }

  /**
   * Returns the byte[] representation of the trace identifier associated with this {@link
   * SpanContext}.
   */
  @Override
  @Memoized
  public byte[] getTraceIdBytes() {
    return SpanContext.super.getTraceIdBytes();
  }

  /**
   * Returns the byte[] representation of the span identifier associated with this {@link
   * SpanContext}.
   */
  @Override
  @Memoized
  public byte[] getSpanIdBytes() {
    return SpanContext.super.getSpanIdBytes();
  }

  /**
   * Returns {@code true} if this {@code SpanContext} is valid.
   *
   * @return {@code true} if this {@code SpanContext} is valid.
   */
  @Override
  @Memoized
  public boolean isValid() {
    return SpanContext.super.isValid();
  }
}
