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

  static final SpanContext INVALID =
      createInternal(
          TraceId.getInvalid(),
          SpanId.getInvalid(),
          TraceFlags.getDefault(),
          TraceState.getDefault(),
          /* remote= */ false);

  private static AutoValue_ImmutableSpanContext createInternal(
      String traceId, String spanId, TraceFlags traceFlags, TraceState traceState, boolean remote) {
    return new AutoValue_ImmutableSpanContext(
        traceId, spanId, traceFlags, traceState, /* remote$= */ remote);
  }

  static SpanContext create(
      String traceIdHex,
      String spanIdHex,
      TraceFlags traceFlags,
      TraceState traceState,
      boolean remote) {
    if (SpanId.isValid(spanIdHex) && TraceId.isValid(traceIdHex)) {
      return createInternal(traceIdHex, spanIdHex, traceFlags, traceState, remote);
    }
    return createInternal(
        TraceId.getInvalid(), SpanId.getInvalid(), traceFlags, traceState, remote);
  }

  @Override
  @Memoized
  public boolean isValid() {
    return SpanContext.super.isValid();
  }
}
