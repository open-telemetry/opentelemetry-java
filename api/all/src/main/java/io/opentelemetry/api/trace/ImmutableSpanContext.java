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
      String traceIdHex, String spanIdHex, byte traceFlags, TraceState traceState, boolean remote) {
    return new AutoValue_ImmutableSpanContext(
        traceIdHex, spanIdHex, traceFlags, traceState, remote);
  }

  @Override
  @Memoized
  public boolean isValid() {
    return SpanContext.super.isValid();
  }
}
