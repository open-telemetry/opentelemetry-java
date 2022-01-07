/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.export;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/** A {@link LogExporter} implementation that can be used to test OpenTelemetry integration. */
public final class InMemoryLogExporter implements LogExporter {
  private final Queue<LogData> finishedLogItems = new ConcurrentLinkedQueue<>();
  private boolean isStopped = false;

  private InMemoryLogExporter() {}

  /**
   * Returns a new instance of the {@code InMemoryLogExporter}.
   *
   * @return a new instance of the {@code InMemoryLogExporter}.
   */
  public static InMemoryLogExporter create() {
    return new InMemoryLogExporter();
  }

  /**
   * Returns a {@code List} of the finished {@code Log}s, represented by {@code LogRecord}.
   *
   * @return a {@code List} of the finished {@code Log}s.
   */
  public List<LogData> getFinishedLogItems() {
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
  public CompletableResultCode export(Collection<LogData> logs) {
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
   * <p>Any subsequent call to export() function on this LogExporter, will return {@code
   * CompletableResultCode.ofFailure()}
   */
  @Override
  public CompletableResultCode shutdown() {
    isStopped = true;
    finishedLogItems.clear();
    return CompletableResultCode.ofSuccess();
  }
}
