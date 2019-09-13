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

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.exceptions.ResponseException;
import com.newrelic.telemetry.exceptions.RetryWithBackoffException;
import com.newrelic.telemetry.exceptions.RetryWithRequestedWaitException;
import com.newrelic.telemetry.spans.SpanBatch;
import com.newrelic.telemetry.spans.SpanBatchSender;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class NewRelicSpanExporter implements SpanExporter {

  private final SpanBatchSender spanBatchSender;
  private final Attributes commonAttributes;

  public NewRelicSpanExporter(SpanBatchSender spanBatchSender, Attributes commonAttributes) {
    this.spanBatchSender = spanBatchSender;
    this.commonAttributes = commonAttributes;
  }

  @Override
  public ResultCode export(List<Span> openTracingSpans) {
    Collection<com.newrelic.telemetry.spans.Span> newRelicSpans = new HashSet<>();
    for (Span openTracingSpan : openTracingSpans) {
      newRelicSpans.add(makeNewRelicSpan(openTracingSpan));
    }
    SpanBatch spanBatch = new SpanBatch(newRelicSpans, new Attributes());
    try {
      spanBatchSender.sendBatch(spanBatch);
      return ResultCode.SUCCESS;
    } catch (RetryWithRequestedWaitException | RetryWithBackoffException e) {
      return ResultCode.FAILED_RETRYABLE;
    } catch (ResponseException e) {
      return ResultCode.FAILED_NOT_RETRYABLE;
    }
  }

  private com.newrelic.telemetry.spans.Span makeNewRelicSpan(Span span) {
    return com.newrelic.telemetry.spans.Span.builder(span.getSpanId().toStringUtf8())
        .timestamp(1000L * span.getStartTime().getSeconds())
        .attributes(commonAttributes)
        .build();
  }

  @Override
  public void shutdown() {}
}
