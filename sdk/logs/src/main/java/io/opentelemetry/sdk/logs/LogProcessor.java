/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.sdk.common.CompletableResultCode;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.ThreadSafe;

/**
 * {@link LogProcessor} is the interface to allow synchronous hooks for log records emitted by
 * {@link LogEmitter}s.
 */
@ThreadSafe
public interface LogProcessor extends Closeable {

  /**
   * Returns a {@link LogProcessor} which simply delegates to all processing to the {@code
   * processors} in order.
   */
  static LogProcessor composite(LogProcessor... processors) {
    return composite(Arrays.asList(processors));
  }

  /**
   * Returns a {@link LogProcessor} which simply delegates to all processing to the {@code
   * processors} in order.
   */
  static LogProcessor composite(Iterable<LogProcessor> processors) {
    List<LogProcessor> processorList = new ArrayList<>();
    for (LogProcessor processor : processors) {
      processorList.add(processor);
    }
    if (processorList.isEmpty()) {
      return NoopLogProcessor.getInstance();
    }
    if (processorList.size() == 1) {
      return processorList.get(0);
    }
    return MultiLogProcessor.create(processorList);
  }

  /**
   * Called when a {@link LogEmitter} {@link LogRecordBuilder#emit()}s a log record.
   *
   * @param logRecord the log record
   */
  void onEmit(ReadWriteLogRecord logRecord);

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
   * Closes this {@link LogProcessor} after processing any remaining log records, releasing any
   * resources.
   */
  @Override
  default void close() {
    shutdown().join(10, TimeUnit.SECONDS);
  }
}
