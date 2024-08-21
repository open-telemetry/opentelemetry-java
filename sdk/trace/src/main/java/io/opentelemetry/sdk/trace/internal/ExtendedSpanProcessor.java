/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.internal;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

/**
 * Extended {@link SpanProcessor} with experimental APIs.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface ExtendedSpanProcessor extends SpanProcessor {

  /**
   * Called when a {@link io.opentelemetry.api.trace.Span} is ended, but before {@link
   * SpanProcessor#onEnd(ReadableSpan)} is invoked with an immutable variant of this span. This
   * means that the span will still be mutable. Note that the span will only be modifiable
   * synchronously from this callback, concurrent modifications from other threads will be
   * prevented. Only called if {@link Span#isRecording()} returns true.
   *
   * <p>This method is called synchronously on the execution thread, should not throw or block the
   * execution thread.
   *
   * @param span the {@code Span} that is just about to be ended.
   */
  void onEnding(ReadWriteSpan span);

  /**
   * Returns {@code true} if this {@link SpanProcessor} requires onEnding events.
   *
   * @return {@code true} if this {@link SpanProcessor} requires onEnding events.
   */
  boolean isOnEndingRequired();
}
