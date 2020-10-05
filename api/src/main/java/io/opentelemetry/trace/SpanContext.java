/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.trace;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import javax.annotation.concurrent.Immutable;

/**
 * A class that represents a span context. A span context contains the state that must propagate to
 * child {@link Span}s and across process boundaries. It contains the identifiers (a {@link TraceId
 * trace_id} and {@link SpanId span_id}) associated with the {@link Span} and a set of options
 * (currently only whether the context is sampled or not), as well as the {@link TraceState
 * traceState} and the {@link boolean remote} flag.
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
public abstract class SpanContext {

  private static final SpanContext INVALID =
      create(
          TraceId.getInvalid(),
          SpanId.getInvalid(),
          TraceFlags.getDefault(),
          TraceState.getDefault());

  /**
   * Returns the invalid {@code SpanContext} that can be used for no-op operations.
   *
   * @return the invalid {@code SpanContext}.
   */
  public static SpanContext getInvalid() {
    return INVALID;
  }

  /**
   * Creates a new {@code SpanContext} with the given identifiers and options.
   *
   * @param traceIdHex the trace identifier of the span context.
   * @param spanIdHex the span identifier of the span context.
   * @param traceFlags the byte representation of the {@link TraceFlags}
   * @param traceState the trace state for the span context.
   * @return a new {@code SpanContext} with the given identifiers and options.
   * @since 0.1.0
   */
  public static SpanContext create(
      String traceIdHex, String spanIdHex, byte traceFlags, TraceState traceState) {
    return create(traceIdHex, spanIdHex, traceFlags, traceState, /* remote=*/ false);
  }

  private static SpanContext create(
      String traceIdHex, String spanIdHex, byte traceFlags, TraceState traceState, boolean remote) {
    return new AutoValue_SpanContext(
        traceIdHex, spanIdHex, traceFlags, traceState, /* remote$=*/ remote);
  }

  /**
   * Creates a new {@code SpanContext} that was propagated from a remote parent, with the given
   * identifiers and options.
   *
   * @param traceIdHex the trace identifier of the span context.
   * @param spanIdHex the span identifier of the span context.
   * @param traceFlags the byte representation of the {@link TraceFlags}
   * @param traceState the trace state for the span context.
   * @return a new {@code SpanContext} with the given identifiers and options.
   * @since 0.1.0
   */
  public static SpanContext createFromRemoteParent(
      String traceIdHex, String spanIdHex, byte traceFlags, TraceState traceState) {
    return create(traceIdHex, spanIdHex, traceFlags, traceState, /* remote=*/ true);
  }

  abstract String getTraceIdHex();

  abstract String getSpanIdHex();

  /**
   * Returns the trace identifier associated with this {@code SpanContext}.
   *
   * @return the trace identifier associated with this {@code SpanContext}.
   * @since 0.1.0
   */
  public String getTraceIdAsHexString() {
    return getTraceIdHex();
  }

  /**
   * Returns the byte[] representation of the trace identifier associated with this {@link
   * SpanContext}.
   *
   * @since 0.8.0
   */
  @Memoized
  public byte[] getTraceIdBytes() {
    return TraceId.bytesFromHex(getTraceIdHex(), 0);
  }

  /**
   * Returns the span identifier associated with this {@code SpanContext}.
   *
   * @return the span identifier associated with this {@code SpanContext}.
   * @since 0.1.0
   */
  public String getSpanIdAsHexString() {
    return getSpanIdHex();
  }

  /**
   * Returns the byte[] representation of the span identifier associated with this {@link
   * SpanContext}.
   *
   * @since 0.8.0
   */
  @Memoized
  public byte[] getSpanIdBytes() {
    return SpanId.bytesFromHex(getSpanIdHex(), 0);
  }

  /** Whether the span in this context is sampled. */
  public boolean isSampled() {
    return (getTraceFlags() & 1) == 1;
  }

  /** The byte-representation of {@link TraceFlags}. */
  public abstract byte getTraceFlags();

  public void copyTraceFlagsHexTo(char[] dest, int destOffset) {
    dest[destOffset] = '0';
    dest[destOffset + 1] = isSampled() ? '1' : '0';
  }

  /**
   * Returns the {@code TraceState} associated with this {@code SpanContext}.
   *
   * @return the {@code TraceState} associated with this {@code SpanContext}.
   * @since 0.1.0
   */
  public abstract TraceState getTraceState();

  /**
   * Returns {@code true} if this {@code SpanContext} is valid.
   *
   * @return {@code true} if this {@code SpanContext} is valid.
   * @since 0.1.0
   */
  @Memoized
  public boolean isValid() {
    return TraceId.isValid(getTraceIdHex()) && SpanId.isValid(getSpanIdHex());
  }

  /**
   * Returns {@code true} if the {@code SpanContext} was propagated from a remote parent.
   *
   * @return {@code true} if the {@code SpanContext} was propagated from a remote parent.
   * @since 0.1.0
   */
  public abstract boolean isRemote();
}
