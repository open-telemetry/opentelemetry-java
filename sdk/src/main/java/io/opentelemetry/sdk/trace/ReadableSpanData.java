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

package io.opentelemetry.sdk.trace;

import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanData;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

/** Class that can transform SpanData into Proto Spans. */
@ThreadSafe
@Immutable
final class ReadableSpanData implements ReadableSpan {

  private final SpanData spanData;

  private ReadableSpanData(SpanData spanData) {
    this.spanData = spanData;
  }

  static ReadableSpanData create(SpanData spanData) {
    return new ReadableSpanData(spanData);
  }

  @Override
  public SpanContext getSpanContext() {
    return spanData.getContext();
  }

  @Override
  public String getName() {
    return spanData.getName();
  }

  @Override
  public Span toSpanProto() {
    SpanContext context = spanData.getContext();
    io.opentelemetry.proto.trace.v1.Span.Builder builder =
        io.opentelemetry.proto.trace.v1.Span.newBuilder()
            .setTraceId(TraceProtoUtils.toProtoTraceId(context.getTraceId()))
            .setSpanId(TraceProtoUtils.toProtoSpanId(context.getSpanId()))
            .setTracestate(TraceProtoUtils.toProtoTracestate(context.getTracestate()))
            .setResource(TraceProtoUtils.toProtoResource(spanData.getResource()))
            .setName(spanData.getName())
            .setKind(TraceProtoUtils.toProtoKind(spanData.getKind()))
            .setStartTime(TraceProtoUtils.toProtoTimestamp(spanData.getStartTimestamp()))
            .setEndTime(TraceProtoUtils.toProtoTimestamp(spanData.getEndTimestamp()))
            .setAttributes(TraceProtoUtils.toProtoAttributes(spanData.getAttributes(), 0))
            .setTimeEvents(
                TraceProtoUtils.spanDataEventsToProtoTimedEvents(spanData.getTimedEvents()))
            .setLinks(TraceProtoUtils.toProtoLinks(spanData.getLinks(), 0))
            .setStatus(TraceProtoUtils.toProtoStatus(spanData.getStatus()));
    if (spanData.getParentSpanId() != null) {
      builder.setParentSpanId(TraceProtoUtils.toProtoSpanId(spanData.getParentSpanId()));
    }
    return builder.build();
  }
}
