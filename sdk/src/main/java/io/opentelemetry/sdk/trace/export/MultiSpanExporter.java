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

import static io.opentelemetry.sdk.trace.export.SpanExporter.ResultCode.FAILURE;
import static io.opentelemetry.sdk.trace.export.SpanExporter.ResultCode.SUCCESS;

import io.opentelemetry.sdk.trace.data.SpanData;
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
  public ResultCode export(Collection<SpanData> spans) {
    ResultCode currentResultCode = SUCCESS;
    for (SpanExporter spanExporter : spanExporters) {
      try {
        currentResultCode = mergeResultCode(currentResultCode, spanExporter.export(spans));
      } catch (Throwable t) {
        // If an exception was thrown by the exporter
        logger.log(Level.WARNING, "Exception thrown by the export.", t);
        currentResultCode = FAILURE;
      }
    }
    return currentResultCode;
  }

  /**
   * Flushes the data of all registered {@link SpanExporter}s.
   *
   * @return the result of the operation
   */
  @Override
  public ResultCode flush() {
    ResultCode currentResultCode = SUCCESS;
    for (SpanExporter spanExporter : spanExporters) {
      try {
        currentResultCode = mergeResultCode(currentResultCode, spanExporter.flush());
      } catch (Throwable t) {
        // If an exception was thrown by the exporter
        logger.log(Level.WARNING, "Exception thrown by the export.", t);
        currentResultCode = FAILURE;
      }
    }
    return currentResultCode;
  }

  @Override
  public void shutdown() {
    for (SpanExporter spanExporter : spanExporters) {
      spanExporter.shutdown();
    }
  }

  // Returns a merged error code, see the rules in the code.
  private static ResultCode mergeResultCode(
      ResultCode currentResultCode, ResultCode newResultCode) {
    // If both errors are success then return success.
    if (currentResultCode == SUCCESS && newResultCode == SUCCESS) {
      return SUCCESS;
    }

    return FAILURE;
  }

  private MultiSpanExporter(List<SpanExporter> spanExporters) {
    this.spanExporters = spanExporters.toArray(new SpanExporter[spanExporters.size()]);
  }
}
