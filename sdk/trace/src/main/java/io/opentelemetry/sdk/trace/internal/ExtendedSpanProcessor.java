/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.internal;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

/**
 * A specialization of {@link SpanProcessor} providing more extension points.
 *
 * <p>Note that this interface is considered experimental and therefore should only be used at the
 * risk of its methods being changed or removed. If it stabilized, the interface is likely removed
 * and the methods are promoted to {@link SpanProcessor}.
 */
public interface ExtendedSpanProcessor extends SpanProcessor {

  /**
   * Called just before a {@link io.opentelemetry.api.trace.Span} is ended, if the {@link
   * Span#isRecording()} returns true. This means that the span will still be mutable. Note that the
   * span will only be modifiable synchronously from this callback, concurrent modifications from
   * other threads will be prevented.
   *
   * <p>This method is called synchronously on the execution thread, should not throw or block the
   * execution thread.
   *
   * <p>Note: This method is experimental and might be subject to future changes.
   *
   * @param span the {@code Span} that is just about to be ended.
   */
  void onEnding(ReadWriteSpan span);

  /**
   * Returns {@code true} if this {@link SpanProcessor} requires onEnding events.
   *
   * <p>Note: This method is experimental and might be subject to future changes.
   *
   * @return {@code true} if this {@link SpanProcessor} requires onEnding events.
   */
  boolean isOnEndingRequired();
}
