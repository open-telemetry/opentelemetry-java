/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.export;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link LogExporter} that forwards all received logs to a list of {@link LogExporter}.
 *
 * <p>Can be used to export to multiple backends using the same {@link LogExporter} like a {@link
 * SimpleLogProcessor} or a {@link BatchLogProcessor}.
 */
final class MultiLogExporter implements LogExporter {
  private static final Logger logger = Logger.getLogger(MultiLogExporter.class.getName());

  private final LogExporter[] logExporters;

  /**
   * Constructs and returns an instance of this class.
   *
   * @param logExporters the exporters logs should be sent to
   * @return the aggregate log exporter
   */
  static LogExporter create(List<LogExporter> logExporters) {
    return new MultiLogExporter(logExporters.toArray(new LogExporter[0]));
  }

  @Override
  public CompletableResultCode export(Collection<LogData> logs) {
    List<CompletableResultCode> results = new ArrayList<>(logExporters.length);
    for (LogExporter logExporter : logExporters) {
      final CompletableResultCode exportResult;
      try {
        exportResult = logExporter.export(logs);
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
   * Flushes the data of all registered {@link LogExporter}s.
   *
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode flush() {
    List<CompletableResultCode> results = new ArrayList<>(logExporters.length);
    for (LogExporter logExporter : logExporters) {
      final CompletableResultCode flushResult;
      try {
        flushResult = logExporter.flush();
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
    List<CompletableResultCode> results = new ArrayList<>(logExporters.length);
    for (LogExporter logExporter : logExporters) {
      final CompletableResultCode shutdownResult;
      try {
        shutdownResult = logExporter.shutdown();
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

  private MultiLogExporter(LogExporter[] logExporters) {
    this.logExporters = logExporters;
  }
}
