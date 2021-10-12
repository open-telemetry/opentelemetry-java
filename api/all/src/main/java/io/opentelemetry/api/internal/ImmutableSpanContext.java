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

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
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

  /**
   * Creates a new {@code SpanContext} with the given identifiers and options.
   *
   * <p>If the traceId or the spanId are invalid (ie. do not conform to the requirements for
   * hexadecimal ids of the appropriate lengths), both will be replaced with the standard "invalid"
   * versions (i.e. all '0's). See {@link SpanId#isValid(CharSequence)} and {@link
   * TraceId#isValid(CharSequence)} for details.
   *
   * @param traceIdHex the trace identifier of the {@code SpanContext}.
   * @param spanIdHex the span identifier of the {@code SpanContext}.
   * @param traceFlags the trace flags of the {@code SpanContext}.
   * @param traceState the trace state for the {@code SpanContext}.
   * @param remote the remote flag for the {@code SpanContext}.
   * @return a new {@code SpanContext} with the given identifiers and options.
   */
  public static SpanContext create(
      String traceIdHex,
      String spanIdHex,
      TraceFlags traceFlags,
      TraceState traceState,
      boolean remote,
      boolean skipIdValidation) {
    if (skipIdValidation || (SpanId.isValid(spanIdHex) && TraceId.isValid(traceIdHex))) {
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

  @Override
  public abstract boolean isValid();
}
