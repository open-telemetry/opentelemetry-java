/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.export;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import java.util.Objects;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A {@link SpanProcessor} implementation that uses {@code Disruptor} to execute all the hooks on an
 * async thread.
 *
 * @deprecated It is recommended to use the {@link
 *     io.opentelemetry.sdk.trace.export.BatchSpanProcessor}. If you know you need to use disruptor,
 *     switch to the {@code io.opentelemetry.contrib:disruptor-processor} artifact.
 */
@ThreadSafe
@Deprecated
public final class DisruptorAsyncSpanProcessor implements SpanProcessor {

  private final DisruptorEventQueue disruptorEventQueue;
  private final boolean startRequired;
  private final boolean endRequired;

  // TODO: Add metrics for dropped spans.

  @Override
  public void onStart(Context parentContext, ReadWriteSpan span) {
    if (!startRequired) {
      return;
    }
    disruptorEventQueue.enqueueStartEvent(span, parentContext);
  }

  @Override
  public boolean isStartRequired() {
    return startRequired;
  }

  @Override
  public void onEnd(ReadableSpan span) {
    if (!endRequired) {
      return;
    }
    disruptorEventQueue.enqueueEndEvent(span);
  }

  @Override
  public boolean isEndRequired() {
    return endRequired;
  }

  @Override
  public CompletableResultCode shutdown() {
    return disruptorEventQueue.shutdown();
  }

  @Override
  public CompletableResultCode forceFlush() {
    return disruptorEventQueue.forceFlush();
  }

  /**
   * Returns a new Builder for {@link DisruptorAsyncSpanProcessor}.
   *
   * @param spanProcessor the {@code List<SpanProcessor>} to where the Span's events are pushed.
   * @return a new {@link DisruptorAsyncSpanProcessor}.
   * @throws NullPointerException if the {@code spanProcessor} is {@code null}.
   * @deprecated It is recommended to use the {@link
   *     io.opentelemetry.sdk.trace.export.BatchSpanProcessor}. If you know you need to use
   *     disruptor, switch to the {@code io.opentelemetry.contrib:disruptor-processor} artifact.
   */
  @Deprecated
  public static DisruptorAsyncSpanProcessorBuilder builder(SpanProcessor spanProcessor) {
    return new DisruptorAsyncSpanProcessorBuilder(Objects.requireNonNull(spanProcessor));
  }

  DisruptorAsyncSpanProcessor(
      DisruptorEventQueue disruptorEventQueue, boolean startRequired, boolean endRequired) {
    this.disruptorEventQueue = disruptorEventQueue;
    this.startRequired = startRequired;
    this.endRequired = endRequired;
  }
}
