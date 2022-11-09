/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.ThreadSafe;

/**
 * {@link LogRecordProcessor} is the interface to allow synchronous hooks for log records emitted by
 * {@link Logger}s.
 */
@ThreadSafe
public interface LogRecordProcessor extends Closeable {

  /**
   * Returns a {@link LogRecordProcessor} which simply delegates to all processing to the {@code
   * processors} in order.
   */
  static LogRecordProcessor composite(LogRecordProcessor... processors) {
    return composite(Arrays.asList(processors));
  }

  /**
   * Returns a {@link LogRecordProcessor} which simply delegates to all processing to the {@code
   * processors} in order.
   */
  static LogRecordProcessor composite(Iterable<LogRecordProcessor> processors) {
    List<LogRecordProcessor> processorList = new ArrayList<>();
    for (LogRecordProcessor processor : processors) {
      processorList.add(processor);
    }
    if (processorList.isEmpty()) {
      return NoopLogRecordProcessor.getInstance();
    }
    if (processorList.size() == 1) {
      return processorList.get(0);
    }
    return MultiLogRecordProcessor.create(processorList);
  }

  /**
   * Called when a {@link Logger} {@link LogRecordBuilder#emit()}s a log record.
   *
   * @param context the context set via {@link LogRecordBuilder#setContext(Context)}, or {@link
   *     Context#current()} if not explicitly set
   * @param logRecord the log record
   */
  void onEmit(Context context, ReadWriteLogRecord logRecord);

  /**
   * Shutdown the log processor.
   *
   * @return result
   */
  default CompletableResultCode shutdown() {
    return forceFlush();
  }

  /**
   * Process all log records that have not yet been processed.
   *
   * @return result
   */
  default CompletableResultCode forceFlush() {
    return CompletableResultCode.ofSuccess();
  }

  /**
   * Closes this {@link LogRecordProcessor} after processing any remaining log records, releasing
   * any resources.
   */
  @Override
  default void close() {
    shutdown().join(10, TimeUnit.SECONDS);
  }
}
