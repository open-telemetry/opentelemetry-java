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

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.protobuf.ByteString;
import com.google.protobuf.Duration;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.exceptions.ResponseException;
import com.newrelic.telemetry.exceptions.RetryWithBackoffException;
import com.newrelic.telemetry.exceptions.RetryWithRequestedWaitException;
import com.newrelic.telemetry.spans.Span.SpanBuilder;
import com.newrelic.telemetry.spans.SpanBatch;
import com.newrelic.telemetry.spans.SpanBatchSender;
import io.opentelemetry.proto.trace.v1.AttributeValue;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Status;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;

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
    SpanBatch spanBatch = new SpanBatch(newRelicSpans, commonAttributes);
    try {
      spanBatchSender.sendBatch(spanBatch);
      return ResultCode.SUCCESS;
    } catch (RetryWithRequestedWaitException | RetryWithBackoffException e) {
      return ResultCode.FAILED_RETRYABLE;
    } catch (ResponseException e) {
      return ResultCode.FAILED_NOT_RETRYABLE;
    }
  }

  private static com.newrelic.telemetry.spans.Span makeNewRelicSpan(Span span) {
    SpanBuilder spanBuilder =
        com.newrelic.telemetry.spans.Span.builder(makeSpanId(span.getSpanId()))
            .name(span.getName().isEmpty() ? null : span.getName())
            .parentId(makeSpanId(span.getParentSpanId()))
            .traceId(makeTraceId(span.getTraceId()))
            .attributes(generateSpanAttributes(span));

    if (span.hasStartTime()) {
      spanBuilder.timestamp(calculateTimestampMillis(span));
    }

    Double duration = calculateDuration(span);
    if (duration != null) {
      spanBuilder.durationMs(duration);
    }
    return spanBuilder.build();
  }

  private static Attributes generateSpanAttributes(Span span) {
    Attributes attributes = new Attributes();
    if (span.hasStatus()) {
      Status status = span.getStatus();
      if (status.getMessage() != null && !status.getMessage().isEmpty()) {
        attributes.put("error.message", status.getMessage());
      }
    }
    Span.Attributes originalAttributes = span.getAttributes();
    Map<String, AttributeValue> attributeMap = originalAttributes.getAttributeMapMap();
    for (Entry<String, AttributeValue> stringAttributeValueEntry : attributeMap.entrySet()) {
      AttributeValue value = stringAttributeValueEntry.getValue();
      switch (value.getValueCase()) {
        case STRING_VALUE:
          attributes.put(stringAttributeValueEntry.getKey(), value.getStringValue());
          break;
        case INT_VALUE:
          attributes.put(stringAttributeValueEntry.getKey(), value.getIntValue());
          break;
        case BOOL_VALUE:
          attributes.put(stringAttributeValueEntry.getKey(), value.getBoolValue());
          break;
        case DOUBLE_VALUE:
          attributes.put(stringAttributeValueEntry.getKey(), value.getDoubleValue());
          break;
        case VALUE_NOT_SET:
          // ignore this. NR doesn't support valueless attributes.
          break;
      }
    }
    return attributes;
  }

  @Nullable
  private static Double calculateDuration(Span span) {
    if (!span.hasEndTime() || !span.hasStartTime()) {
      return null;
    }
    Timestamp startTime = span.getStartTime();
    Timestamp endTime = span.getEndTime();

    Duration duration = Timestamps.between(startTime, endTime);
    double nanoPart = duration.getNanos() / 1000000d;
    return nanoPart + SECONDS.toMillis(duration.getSeconds());
  }

  @Nullable
  private static String makeTraceId(ByteString byteString) {
    if (byteString.isEmpty()) {
      return null;
    }
    return TraceId.asBase16(byteString.toByteArray());
  }

  @Nullable
  private static String makeSpanId(ByteString byteString) {
    if (byteString.isEmpty()) {
      return null;
    }
    return SpanId.asBase16(byteString.toByteArray());
  }

  private static long calculateTimestampMillis(Span span) {
    Timestamp spanStartTime = span.getStartTime();
    long millis = NANOSECONDS.toMillis(spanStartTime.getNanos());
    long seconds = SECONDS.toMillis(spanStartTime.getSeconds());
    return seconds + millis;
  }

  @Override
  public void shutdown() {}
}
