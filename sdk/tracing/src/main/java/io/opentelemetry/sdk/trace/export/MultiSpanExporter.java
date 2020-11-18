/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the {@code SpanExporter} that simply forwards all received spans to a list of
 * {@code SpanExporter}.
 *
 * <p>Can be used to export to multiple backends using the same {@code SpanProcessor} like a {@code
 * SimpleSampledSpansProcessor} or a {@code BatchSampledSpansProcessor}.
 *
 * @deprecated Use {@link SpanExporter#delegating(SpanExporter...)}
 */
@Deprecated
public final class MultiSpanExporter implements SpanExporter {
  private static final Logger logger = Logger.getLogger(MultiSpanExporter.class.getName());

  private final SpanExporter[] spanExporters;

  /**
   * Constructs and returns an instance of this class.
   *
   * @param spanExporters the exporters spans should be sent to
   * @return the aggregate span exporter
   * @deprecated Use {@link SpanExporter#delegating(SpanExporter...)}
   */
  @Deprecated
  public static SpanExporter create(List<SpanExporter> spanExporters) {
    return new MultiSpanExporter(spanExporters.toArray(new SpanExporter[0]));
  }

  @Override
  public CompletableResultCode export(Collection<SpanData> spans) {
    List<CompletableResultCode> results = new ArrayList<>(spanExporters.length);
    for (SpanExporter spanExporter : spanExporters) {
      final CompletableResultCode exportResult;
      try {
        exportResult = spanExporter.export(spans);
      } catch (Exception e) {
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
   * Flushes the data of all registered {@link SpanExporter}s.
   *
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode flush() {
    List<CompletableResultCode> results = new ArrayList<>(spanExporters.length);
    for (SpanExporter spanExporter : spanExporters) {
      final CompletableResultCode flushResult;
      try {
        flushResult = spanExporter.flush();
      } catch (Exception e) {
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
    List<CompletableResultCode> results = new ArrayList<>(spanExporters.length);
    for (SpanExporter spanExporter : spanExporters) {
      final CompletableResultCode shutdownResult;
      try {
        shutdownResult = spanExporter.shutdown();
      } catch (Exception e) {
        // If an exception was thrown by the exporter
        logger.log(Level.WARNING, "Exception thrown by the shutdown.", e);
        results.add(CompletableResultCode.ofFailure());
        continue;
      }
      results.add(shutdownResult);
    }
    return CompletableResultCode.ofAll(results);
  }

  private MultiSpanExporter(SpanExporter[] spanExporters) {
    this.spanExporters = spanExporters;
  }
}
