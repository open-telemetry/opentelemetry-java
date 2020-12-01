/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import javax.annotation.concurrent.Immutable;

/**
 * A class that represents a span context. A span context contains the state that must propagate to
 * child {@link Span}s and across process boundaries. It contains the identifiers (a {@link TraceId
 * trace_id} and {@link SpanId span_id}) associated with the {@link Span} and a set of options
 * (currently only whether the context is sampled or not), as well as the {@link TraceState
 * traceState} and the {@link boolean remote} flag.
 *
 * <p>Implementations of this interface *must* be immutable and have well-defined value-based
 * equals/hashCode implementations. If an implementation does not strictly conform to these
 * requirements, behavior of the OpenTelemetry APIs and default SDK cannot be guaranteed. It is
 * strongly suggested that you use the implementation that is provided here via {@link
 * #create(String, String, byte, TraceState)} or {@link #createFromRemoteParent(String, String,
 * byte, TraceState)}.
 */
@Immutable
public interface SpanContext {

  /**
   * Returns the invalid {@code SpanContext} that can be used for no-op operations.
   *
   * @return the invalid {@code SpanContext}.
   */
  static SpanContext getInvalid() {
    return ImmutableSpanContext.getInvalid();
  }

  /**
   * Creates a new {@code SpanContext} with the given identifiers and options.
   *
   * @param traceIdHex the trace identifier of the span context.
   * @param spanIdHex the span identifier of the span context.
   * @param traceFlags the byte representation of the {@link TraceFlags}
   * @param traceState the trace state for the span context.
   * @return a new {@code SpanContext} with the given identifiers and options.
   */
  static SpanContext create(
      String traceIdHex, String spanIdHex, byte traceFlags, TraceState traceState) {
    return ImmutableSpanContext.create(
        traceIdHex, spanIdHex, traceFlags, traceState, /* remote=*/ false);
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
   */
  static SpanContext createFromRemoteParent(
      String traceIdHex, String spanIdHex, byte traceFlags, TraceState traceState) {
    return ImmutableSpanContext.create(
        traceIdHex, spanIdHex, traceFlags, traceState, /* remote=*/ true);
  }

  /**
   * Returns the trace identifier associated with this {@code SpanContext}.
   *
   * @return the trace identifier associated with this {@code SpanContext}.
   */
  String getTraceIdAsHexString();

  /**
   * Returns the byte[] representation of the trace identifier associated with this {@link
   * SpanContext}.
   */
  default byte[] getTraceIdBytes() {
    return TraceId.bytesFromHex(getTraceIdAsHexString(), 0);
  }

  /**
   * Returns the span identifier associated with this {@code SpanContext}.
   *
   * @return the span identifier associated with this {@code SpanContext}.
   */
  String getSpanIdAsHexString();

  /**
   * Returns the byte[] representation of the span identifier associated with this {@link
   * SpanContext}.
   */
  default byte[] getSpanIdBytes() {
    return SpanId.bytesFromHex(getSpanIdAsHexString(), 0);
  }

  /** Whether the span in this context is sampled. */
  default boolean isSampled() {
    return (getTraceFlags() & 1) == 1;
  }

  /** The byte-representation of {@link TraceFlags}. */
  byte getTraceFlags();

  default void copyTraceFlagsHexTo(char[] dest, int destOffset) {
    BigendianEncoding.byteToBase16String(getTraceFlags(), dest, destOffset);
  }

  /**
   * Returns the {@code TraceState} associated with this {@code SpanContext}.
   *
   * @return the {@code TraceState} associated with this {@code SpanContext}.
   */
  TraceState getTraceState();

  /**
   * Returns {@code true} if this {@code SpanContext} is valid.
   *
   * @return {@code true} if this {@code SpanContext} is valid.
   */
  default boolean isValid() {
    return TraceId.isValid(getTraceIdAsHexString()) && SpanId.isValid(getSpanIdAsHexString());
  }

  /**
   * Returns {@code true} if the {@code SpanContext} was propagated from a remote parent.
   *
   * @return {@code true} if the {@code SpanContext} was propagated from a remote parent.
   */
  boolean isRemote();
}
