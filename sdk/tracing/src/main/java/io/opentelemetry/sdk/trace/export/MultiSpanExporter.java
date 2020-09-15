/*
 * Copyright 2019, OpenTelemetry Authors
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
 */
public final class MultiSpanExporter implements SpanExporter {
  private static final Logger logger = Logger.getLogger(MultiSpanExporter.class.getName());

  private final SpanExporter[] spanExporters;

  /**
   * Constructs and returns an instance of this class.
   *
   * @param spanExporters the exporters spans should be sent to
   * @return the aggregate span exporter
   */
  public static SpanExporter create(List<SpanExporter> spanExporters) {
    return new MultiSpanExporter(spanExporters);
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
   *
   * @since 0.4.0
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

  private MultiSpanExporter(List<SpanExporter> spanExporters) {
    this.spanExporters = spanExporters.toArray(new SpanExporter[0]);
  }
}
