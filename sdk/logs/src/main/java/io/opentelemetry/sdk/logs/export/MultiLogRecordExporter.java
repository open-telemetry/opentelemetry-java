/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.export;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link LogRecordExporter} that forwards all received logs to a list of {@link LogRecordExporter}.
 *
 * <p>Can be used to export to multiple backends using the same {@link LogRecordExporter} like a
 * {@link SimpleLogRecordProcessor} or a {@link BatchLogRecordProcessor}.
 */
final class MultiLogRecordExporter implements LogRecordExporter {
  private static final Logger logger = Logger.getLogger(MultiLogRecordExporter.class.getName());

  private final LogRecordExporter[] logRecordExporters;

  private MultiLogRecordExporter(LogRecordExporter[] logRecordExporters) {
    this.logRecordExporters = logRecordExporters;
  }

  /**
   * Constructs and returns an instance of this class.
   *
   * @param logRecordExporters the exporters logs should be sent to
   * @return the aggregate log exporter
   */
  static LogRecordExporter create(List<LogRecordExporter> logRecordExporters) {
    return new MultiLogRecordExporter(logRecordExporters.toArray(new LogRecordExporter[0]));
  }

  @Override
  public CompletableResultCode export(Collection<LogRecordData> logs) {
    List<CompletableResultCode> results = new ArrayList<>(logRecordExporters.length);
    for (LogRecordExporter logRecordExporter : logRecordExporters) {
      CompletableResultCode exportResult;
      try {
        exportResult = logRecordExporter.export(logs);
      } catch (RuntimeException e) {
        // If an exception was thrown by the exporter
        logger.log(Level.WARNING, "Exception thrown by the export.", e);
        results.add(CompletableResultCode.ofFailure());
        continue;
      }
      results.add(exportResult);
    }
    return CompletableResultCode.ofAll(results);
  }

  /**
   * Flushes the data of all registered {@link LogRecordExporter}s.
   *
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode flush() {
    List<CompletableResultCode> results = new ArrayList<>(logRecordExporters.length);
    for (LogRecordExporter logRecordExporter : logRecordExporters) {
      CompletableResultCode flushResult;
      try {
        flushResult = logRecordExporter.flush();
      } catch (RuntimeException e) {
        // If an exception was thrown by the exporter
        logger.log(Level.WARNING, "Exception thrown by the flush.", e);
        results.add(CompletableResultCode.ofFailure());
        continue;
      }
      results.add(flushResult);
    }
    return CompletableResultCode.ofAll(results);
  }

  @Override
  public CompletableResultCode shutdown() {
    List<CompletableResultCode> results = new ArrayList<>(logRecordExporters.length);
    for (LogRecordExporter logRecordExporter : logRecordExporters) {
      CompletableResultCode shutdownResult;
      try {
        shutdownResult = logRecordExporter.shutdown();
      } catch (RuntimeException e) {
        // If an exception was thrown by the exporter
        logger.log(Level.WARNING, "Exception thrown by the shutdown.", e);
        results.add(CompletableResultCode.ofFailure());
        continue;
      }
      results.add(shutdownResult);
    }
    return CompletableResultCode.ofAll(results);
  }

  @Override
  public String toString() {
    return "MultiLogRecordExporter{"
        + "logRecordExporters="
        + Arrays.toString(logRecordExporters)
        + '}';
  }
}
