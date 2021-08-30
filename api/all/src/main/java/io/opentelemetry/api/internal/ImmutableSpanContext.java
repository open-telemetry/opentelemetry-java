/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
public abstract class ImmutableSpanContext implements SpanContext {

  public static final SpanContext INVALID =
      createInternal(
          TraceId.getInvalid(),
          SpanId.getInvalid(),
          TraceFlags.getDefault(),
          TraceState.getDefault(),
          /* remote= */ false,
          /* valid= */ false);

  private static AutoValue_ImmutableSpanContext createInternal(
      String traceId,
      String spanId,
      TraceFlags traceFlags,
      TraceState traceState,
      boolean remote,
      boolean valid) {
    return new AutoValue_ImmutableSpanContext(
        traceId, spanId, traceFlags, traceState, remote, valid);
  }

  public static SpanContext create(
      String traceIdHex,
      String spanIdHex,
      TraceFlags traceFlags,
      TraceState traceState,
      boolean remote) {
    if (SpanId.isValid(spanIdHex) && TraceId.isValid(traceIdHex)) {
      return createInternal(
          traceIdHex, spanIdHex, traceFlags, traceState, remote, /* valid= */ true);
    }
    return createInternal(
        TraceId.getInvalid(),
        SpanId.getInvalid(),
        traceFlags,
        traceState,
        remote,
        /* valid= */ false);
  }

  /**
   * This method is provided as an optimization when {@code traceIdHex} and {@code spanIdHex} have
   * already been validated. Only use this method if you are sure these have both been validated,
   * e.g. when using {@code traceIdHex} from a parent {@link SpanContext} and {@code spanIdHex} from
   * an {@code IdGenerator}.
   */
  public static SpanContext createBypassingValidation(
      String traceIdHex, String spanIdHex, TraceFlags traceFlags, TraceState traceState) {
    return createInternal(
        traceIdHex, spanIdHex, traceFlags, traceState, /* remote= */ false, /* valid= */ true);
  }

  @Override
  public abstract boolean isValid();
}
