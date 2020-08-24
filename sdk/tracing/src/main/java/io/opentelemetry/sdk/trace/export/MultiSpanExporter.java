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

import io.opentelemetry.sdk.common.export.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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
    final CompletableResultCode compositeResultCode = new CompletableResultCode();
    final AtomicInteger completionsToProcess = new AtomicInteger(spanExporters.length);
    for (SpanExporter spanExporter : spanExporters) {
      try {
        final CompletableResultCode singleResult = spanExporter.export(spans);
        mergeResultCode(compositeResultCode, singleResult, completionsToProcess);
      } catch (Exception e) {
        // If an exception was thrown by the exporter
        logger.log(Level.WARNING, "Exception thrown by the export.", e);
        compositeResultCode.fail();
      }
    }
    return compositeResultCode;
  }

  /**
   * Flushes the data of all registered {@link SpanExporter}s.
   *
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode flush() {
    final CompletableResultCode compositeResultCode = new CompletableResultCode();
    final AtomicInteger completionsToProcess = new AtomicInteger(spanExporters.length);
    for (SpanExporter spanExporter : spanExporters) {
      try {
        mergeResultCode(compositeResultCode, spanExporter.flush(), completionsToProcess);
      } catch (Exception e) {
        // If an exception was thrown by the exporter
        logger.log(Level.WARNING, "Exception thrown by the flush.", e);
        compositeResultCode.fail();
      }
    }
    return compositeResultCode;
  }

  @Override
  public void shutdown() {
    for (SpanExporter spanExporter : spanExporters) {
      spanExporter.shutdown();
    }
  }

  private static void mergeResultCode(
      final CompletableResultCode compositeResultCode,
      final CompletableResultCode singleResultCode,
      final AtomicInteger completionsToProcess) {
    singleResultCode.whenComplete(
        new Runnable() {
          @Override
          public void run() {
            int completionsRemaining = completionsToProcess.decrementAndGet();
            if (singleResultCode.isSuccess()) {
              if (completionsRemaining == 0) {
                compositeResultCode.succeed();
              }
            } else {
              compositeResultCode.fail();
            }
          }
        });
  }

  private MultiSpanExporter(List<SpanExporter> spanExporters) {
    this.spanExporters = spanExporters.toArray(new SpanExporter[0]);
  }
}
