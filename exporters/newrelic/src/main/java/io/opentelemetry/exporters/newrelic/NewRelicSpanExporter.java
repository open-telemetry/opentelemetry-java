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

package io.opentelemetry.exporters.newrelic;

import com.newrelic.telemetry.exceptions.ResponseException;
import com.newrelic.telemetry.exceptions.RetryWithBackoffException;
import com.newrelic.telemetry.exceptions.RetryWithRequestedWaitException;
import com.newrelic.telemetry.spans.SpanBatch;
import com.newrelic.telemetry.spans.SpanBatchSender;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.List;

/**
 * The NewRelicSpanExporter takes a list of Span objects, converts them into a New Relic SpanBatch
 * instance and then sends it to the New Relic trace ingest API via a SpanBatchSender.
 *
 * @since 0.1.0
 */
public class NewRelicSpanExporter implements SpanExporter {

  private final SpanBatchAdapter adapter;
  private final SpanBatchSender spanBatchSender;

  /**
   * Constructor for the NewRelicSpanExporter.
   *
   * @param adapter An instance of SpanBatchAdapter that can turn list of open telemetry spans into
   *     New Relic SpanBatch.
   * @param spanBatchSender An instance that sends a SpanBatch to the New Relic trace ingest API
   * @since 0.1.0
   */
  public NewRelicSpanExporter(SpanBatchAdapter adapter, SpanBatchSender spanBatchSender) {
    this.adapter = adapter;
    this.spanBatchSender = spanBatchSender;
  }

  /**
   * export() is the primary interface action method of all SpanExporters.
   *
   * @param openTracingSpans A list of spans to export to New Relic trace ingest API
   * @return A ResultCode that indicates the execution status of the export operation
   */
  @Override
  public ResultCode export(List<Span> openTracingSpans) {
    try {
      SpanBatch spanBatch = adapter.adaptToSpanBatch(openTracingSpans);
      spanBatchSender.sendBatch(spanBatch);
      return ResultCode.SUCCESS;
    } catch (RetryWithRequestedWaitException | RetryWithBackoffException e) {
      return ResultCode.FAILED_RETRYABLE;
    } catch (ResponseException e) {
      return ResultCode.FAILED_NOT_RETRYABLE;
    }
  }

  @Override
  public void shutdown() {}
}
