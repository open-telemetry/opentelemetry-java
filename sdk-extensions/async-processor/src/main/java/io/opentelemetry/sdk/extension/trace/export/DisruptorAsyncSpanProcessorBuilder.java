/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.export;

import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import io.opentelemetry.sdk.trace.SpanProcessor;

/** Builder class for {@link DisruptorAsyncSpanProcessor}. */
public final class DisruptorAsyncSpanProcessorBuilder {

  // Number of events that can be enqueued at any one time. If more than this are enqueued,
  // then subsequent attempts to enqueue new entries will block.
  private static final int DEFAULT_DISRUPTOR_BUFFER_SIZE = 8192;
  // The default value of the Disruptor behavior, blocks when no space available.
  private static final boolean DEFAULT_BLOCKING = true;
  // The default number of retries for the SleepingWaitingStrategy.
  private static final int DEFAULT_NUM_RETRIES = 0;
  // The default waiting time in ns for the SleepingWaitingStrategy.
  private static final long DEFAULT_SLEEPING_TIME_NS = 1000 * 1000L;

  private final SpanProcessor spanProcessor;
  private int bufferSize = DEFAULT_DISRUPTOR_BUFFER_SIZE;
  private boolean blocking = DEFAULT_BLOCKING;
  private WaitStrategy waitStrategy =
      new SleepingWaitStrategy(DEFAULT_NUM_RETRIES, DEFAULT_SLEEPING_TIME_NS);

  DisruptorAsyncSpanProcessorBuilder(SpanProcessor spanProcessor) {
    this.spanProcessor = spanProcessor;
  }

  /**
   * If {@code true} blocks when the Disruptor's ring buffer is full.
   *
   * @param blocking {@code true} blocks when the Disruptor's ring buffer is full.
   * @return this.
   */
  public DisruptorAsyncSpanProcessorBuilder setBlocking(boolean blocking) {
    this.blocking = blocking;
    return this;
  }

  /**
   * Sets the buffer size for the Disruptor's ring buffer.
   *
   * @param bufferSize the buffer size for the Disruptor ring buffer.
   * @return this.
   */
  public DisruptorAsyncSpanProcessorBuilder setBufferSize(int bufferSize) {
    if (bufferSize <= 0) {
      throw new IllegalArgumentException("bufferSize must be positive");
    }
    this.bufferSize = bufferSize;
    return this;
  }

  /**
   * Sets the {@code WaitStrategy} for the Disruptor's worker thread.
   *
   * @param waitingStrategy the {@code WaitStrategy} for the Disruptor's worker thread.
   * @return this.
   */
  public DisruptorAsyncSpanProcessorBuilder setWaitingStrategy(WaitStrategy waitingStrategy) {
    this.waitStrategy = waitingStrategy;
    return this;
  }

  /**
   * Returns a new {@link DisruptorAsyncSpanProcessor}.
   *
   * @return a new {@link DisruptorAsyncSpanProcessor}.
   */
  public DisruptorAsyncSpanProcessor build() {
    return new DisruptorAsyncSpanProcessor(
        new DisruptorEventQueue(bufferSize, waitStrategy, spanProcessor, blocking),
        spanProcessor.isStartRequired(),
        spanProcessor.isEndRequired());
  }
}
