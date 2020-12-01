/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.export;

import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.WaitStrategy;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.ConfigBuilder;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import java.util.Map;
import java.util.Objects;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A {@link SpanProcessor} implementation that uses {@code Disruptor} to execute all the hooks on an
 * async thread.
 *
 * <p>Configuration options for {@link DisruptorAsyncSpanProcessor} can be read from system
 * properties, environment variables, or {@link java.util.Properties} objects.
 *
 * <p>For system properties and {@link java.util.Properties} objects, {@link
 * DisruptorAsyncSpanProcessor} will look for the following names:
 *
 * <ul>
 *   <li>{@code otel.disruptor.buffer.size}: number of events that can be enqueued at any one time.
 *   <li>{@code otel.disruptor.blocking}: to block the queue if no space available.
 *   <li>{@code otel.disruptor.num.retries}: number of retries for the {@link SleepingWaitStrategy}.
 *   <li>{@code otel.disruptor.sleeping.time}: waiting time in ns for the {@link
 *       SleepingWaitStrategy}.
 * </ul>
 *
 * <p>For environment variables, {@link DisruptorAsyncSpanProcessor} will look for the following
 * names:
 *
 * <ul>
 *   <li>{@code OTEL_DISRUPTOR_BUFFER_SIZE}: number of events that can be enqueued at any one time.
 *   <li>{@code OTEL_DISRUPTOR_BLOCKING}: to block the queue if no space available.
 *   <li>{@code OTEL_DISRUPTOR_NUM_RETRIES}: number of retries for the {@link SleepingWaitStrategy}.
 *   <li>{@code OTEL_DISRUPTOR_SLEEPING_TIME}: waiting time in ns for the {@link
 *       SleepingWaitStrategy}.
 * </ul>
 */
@ThreadSafe
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
   */
  public static Builder builder(SpanProcessor spanProcessor) {
    return new Builder(Objects.requireNonNull(spanProcessor));
  }

  /** Builder class for {@link DisruptorAsyncSpanProcessor}. */
  public static final class Builder extends ConfigBuilder<Builder> {

    private static final String KEY_DISRUPTOR_BUFFER_SIZE = "otel.disruptor.buffer.size";
    private static final String KEY_BLOCKING = "otel.disruptor.blocking";
    private static final String KEY_NUM_RETRIES = "otel.disruptor.num.retries";
    private static final String KEY_SLEEPING_TIME_NS = "otel.disruptor.sleeping.time";

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

    private Builder(SpanProcessor spanProcessor) {
      this.spanProcessor = spanProcessor;
    }

    /**
     * If {@code true} blocks when the Disruptor's ring buffer is full.
     *
     * @param blocking {@code true} blocks when the Disruptor's ring buffer is full.
     * @return this.
     */
    public Builder setBlocking(boolean blocking) {
      this.blocking = blocking;
      return this;
    }

    /**
     * Sets the buffer size for the Disruptor's ring buffer.
     *
     * @param bufferSize the buffer size for the Disruptor ring buffer.
     * @return this.
     */
    public Builder setBufferSize(int bufferSize) {
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
    public Builder setWaitingStrategy(WaitStrategy waitingStrategy) {
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

    @Override
    protected Builder fromConfigMap(
        Map<String, String> configMap, NamingConvention namingConvention) {
      configMap = namingConvention.normalize(configMap);
      Integer intValue = getIntProperty(KEY_DISRUPTOR_BUFFER_SIZE, configMap);
      if (intValue != null) {
        this.setBufferSize(intValue);
      }
      Boolean boolValue = getBooleanProperty(KEY_BLOCKING, configMap);
      if (boolValue != null) {
        this.setBlocking(boolValue);
      }
      Integer retries = getIntProperty(KEY_NUM_RETRIES, configMap);
      if (retries == null) {
        retries = DEFAULT_NUM_RETRIES;
      }
      Long sleepingNs = getLongProperty(KEY_SLEEPING_TIME_NS, configMap);
      if (sleepingNs == null) {
        sleepingNs = DEFAULT_SLEEPING_TIME_NS;
      }
      return setWaitingStrategy(new SleepingWaitStrategy(retries, sleepingNs));
    }
  }

  private DisruptorAsyncSpanProcessor(
      DisruptorEventQueue disruptorEventQueue, boolean startRequired, boolean endRequired) {
    this.disruptorEventQueue = disruptorEventQueue;
    this.startRequired = startRequired;
    this.endRequired = endRequired;
  }
}
