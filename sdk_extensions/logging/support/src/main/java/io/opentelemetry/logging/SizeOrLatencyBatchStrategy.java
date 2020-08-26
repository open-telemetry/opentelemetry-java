/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.logging;

import io.opentelemetry.logging.api.LogRecord;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/**
 * Batching strategy that allows specifying both a maximum size and a maximum amount of time before
 * sending a batch of records. Batch will be flushed whenever the list of records queued exceeds the
 * maximum threshold (default 50), or a set timeout expires (default 5 seconds).
 */
public class SizeOrLatencyBatchStrategy implements LoggingBatchStrategy {
  private static final int DEFAULT_BATCH_SIZE = 50;
  private static final int DEFAULT_MAX_DELAY = 5;
  private static final TimeUnit DEFAULT_MAX_DELAY_UNITS = TimeUnit.SECONDS;
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

  private final int maxBatch;
  private final int maxDelay;
  private final TimeUnit maxDelayUnits;
  private final ScheduledFuture<?> schedule;

  private LoggingBatchExporter batchHandler;
  private Queue<LogRecord> batch = buildNewQueue();

  private static Queue<LogRecord> buildNewQueue() {
    return new LinkedBlockingQueue<>();
  }

  private SizeOrLatencyBatchStrategy(int maxBatch, int maxDelay, TimeUnit units) {
    this.maxBatch = maxBatch > 0 ? maxBatch : DEFAULT_BATCH_SIZE;
    this.maxDelay = maxDelay > 0 ? maxDelay : DEFAULT_MAX_DELAY;
    this.maxDelayUnits = units != null ? units : DEFAULT_MAX_DELAY_UNITS;
    this.schedule =
        executor.scheduleWithFixedDelay(
            new Runnable() {
              @Override
              public void run() {
                flush();
              }
            },
            this.maxDelay,
            this.maxDelay,
            this.maxDelayUnits);
  }

  @Override
  public void stop() {
    schedule.cancel(false);
  }

  @Override
  public void add(LogRecord record) {
    batch.add(record);
    if (batch.size() >= maxBatch) {
      flush();
    }
  }

  @Override
  public synchronized void flush() {
    if (batchHandler != null && batch.size() > 0) {
      Collection<LogRecord> completedBatch = batch;
      batch = buildNewQueue();
      batchHandler.handleLogRecordBatch(completedBatch);
    }
  }

  @Override
  public void setBatchHandler(LoggingBatchExporter handler) {
    batchHandler = handler;
  }

  public static class Builder {
    private int maxBatchSize = -1;
    private int maxDelay = -1;
    @Nullable private TimeUnit maxDelayTimeUnit = null;

    /**
     * The maximum number of LogRecord objects to accumulate before dispatching a batch.
     *
     * @param maxBatchSize max number of records
     * @return builder
     */
    public Builder withMaxBatchSize(int maxBatchSize) {
      this.maxBatchSize = maxBatchSize;
      return this;
    }

    /**
     * The length of time to wait before dispatching a batch, even fi the number of records
     * accumulated is less than the max number allowed.
     *
     * @param delay the maximum amount of time to wait before dispatching a batch
     * @param units the units the amount of time should be measured in
     * @return builder
     */
    public Builder withMaxDelay(int delay, TimeUnit units) {
      this.maxDelay = delay;
      this.maxDelayTimeUnit = units;
      return this;
    }

    /**
     * Instantiate the strategy.
     *
     * @return built strategy
     */
    public SizeOrLatencyBatchStrategy build() {
      return new SizeOrLatencyBatchStrategy(maxBatchSize, maxDelay, maxDelayTimeUnit);
    }
  }
}
