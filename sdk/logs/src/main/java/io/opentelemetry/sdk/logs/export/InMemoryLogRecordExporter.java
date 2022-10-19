/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.export;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A {@link LogRecordExporter} implementation that can be used to test OpenTelemetry integration.
 */
public final class InMemoryLogRecordExporter implements LogRecordExporter {
  private final Queue<LogRecordData> finishedLogItems = new ConcurrentLinkedQueue<>();
  private boolean isStopped = false;

  private InMemoryLogRecordExporter() {}

  /**
   * Returns a new instance of the {@link InMemoryLogRecordExporter}.
   *
   * @return a new instance of the {@link InMemoryLogRecordExporter}.
   */
  public static InMemoryLogRecordExporter create() {
    return new InMemoryLogRecordExporter();
  }

  /**
   * Returns a {@code List} of the finished {@code Log}s, represented by {@code LogRecord}.
   *
   * @return a {@code List} of the finished {@code Log}s.
   */
  public List<LogRecordData> getFinishedLogItems() {
    return Collections.unmodifiableList(new ArrayList<>(finishedLogItems));
  }

  /**
   * Clears the internal {@code List} of finished {@code Log}s.
   *
   * <p>Does not reset the state of this exporter if already shutdown.
   */
  public void reset() {
    finishedLogItems.clear();
  }

  /**
   * Exports the collection of {@code Log}s into the inmemory queue.
   *
   * <p>If this is called after {@code shutdown}, this will return {@code ResultCode.FAILURE}.
   */
  @Override
  public CompletableResultCode export(Collection<LogRecordData> logs) {
    if (isStopped) {
      return CompletableResultCode.ofFailure();
    }
    finishedLogItems.addAll(logs);
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  /**
   * Clears the internal {@code List} of finished {@code Log}s.
   *
   * <p>Any subsequent call to export() function on this exporter, will return {@code
   * CompletableResultCode.ofFailure()}
   */
  @Override
  public CompletableResultCode shutdown() {
    isStopped = true;
    finishedLogItems.clear();
    return CompletableResultCode.ofSuccess();
  }
}
