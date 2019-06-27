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

import static com.google.common.truth.Truth.assertThat;

import com.google.protobuf.Timestamp;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Span.Attributes;
import io.opentelemetry.proto.trace.v1.Span.SpanKind;
import io.opentelemetry.proto.trace.v1.Span.TimedEvent.Event;
import io.opentelemetry.proto.trace.v1.Span.TimedEvents;
import io.opentelemetry.resources.Resource;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanData;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceOptions;
import io.opentelemetry.trace.Tracestate;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link ReadableSpanData}. */
@RunWith(JUnit4.class)
public class ReadableSpanDataTest {

  private static final String SPAN_NAME = "MySpanName";
  private final TraceId traceId = TestUtils.generateRandomTraceId();
  private final SpanId spanId = TestUtils.generateRandomSpanId();
  private final SpanId parentSpanId = TestUtils.generateRandomSpanId();
  private final SpanContext spanContext =
      SpanContext.create(traceId, spanId, TraceOptions.getDefault(), Tracestate.getDefault());
  private final Resource resource = Resource.getEmpty();
  private final SpanData.Timestamp startTime = SpanData.Timestamp.create(10, 0);
  private final SpanData.Timestamp endTime = SpanData.Timestamp.create(20, 0);
  private final SpanData.TimedEvent timedEvent =
      SpanData.TimedEvent.create(
          SpanData.Timestamp.create(10, 20),
          SpanData.Event.create("event", Collections.<String, AttributeValue>emptyMap()));

  @Test
  public void getNameAndGetSpanContext() {
    SpanData spanData =
        SpanData.create(
            spanContext,
            null,
            resource,
            SPAN_NAME,
            Kind.CLIENT,
            startTime,
            Collections.<String, AttributeValue>emptyMap(),
            Collections.<SpanData.TimedEvent>emptyList(),
            Collections.<Link>emptyList(),
            Status.OK,
            endTime);
    ReadableSpanData readableSpanData = ReadableSpanData.create(spanData);
    assertThat(readableSpanData.getName()).isEqualTo(SPAN_NAME);
    assertThat(readableSpanData.getSpanContext()).isEqualTo(spanContext);
  }

  @Test
  public void toSpanProto() {
    SpanData spanData =
        SpanData.create(
            spanContext,
            parentSpanId,
            resource,
            SPAN_NAME,
            Kind.CLIENT,
            startTime,
            Collections.<String, AttributeValue>emptyMap(),
            Collections.singletonList(timedEvent),
            Collections.<Link>emptyList(),
            Status.OK,
            endTime);
    ReadableSpanData readableSpanData = ReadableSpanData.create(spanData);
    Span spanProto = readableSpanData.toSpanProto();
    assertThat(spanProto.getTraceId()).isEqualTo(TraceProtoUtils.toProtoTraceId(traceId));
    assertThat(spanProto.getSpanId()).isEqualTo(TraceProtoUtils.toProtoSpanId(spanId));
    assertThat(spanProto.getParentSpanId()).isEqualTo(TraceProtoUtils.toProtoSpanId(parentSpanId));
    assertThat(spanProto.getTracestate())
        .isEqualTo(TraceProtoUtils.toProtoTracestate(Tracestate.getDefault()));
    assertThat(spanProto.getResource()).isEqualTo(TraceProtoUtils.toProtoResource(resource));
    assertThat(spanProto.getName()).isEqualTo(SPAN_NAME);
    assertThat(spanProto.getKind()).isEqualTo(SpanKind.CLIENT);
    assertThat(spanProto.getTimeEvents())
        .isEqualTo(
            TimedEvents.newBuilder()
                .addTimedEvent(
                    Span.TimedEvent.newBuilder()
                        .setTime(Timestamp.newBuilder().setSeconds(10).setNanos(20).build())
                        .setEvent(
                            Event.newBuilder()
                                .setName("event")
                                .setAttributes(Attributes.getDefaultInstance())
                                .build()))
                .build());
    assertThat(spanProto.getStartTime()).isEqualTo(Timestamp.newBuilder().setSeconds(10).build());
    assertThat(spanProto.getEndTime()).isEqualTo(Timestamp.newBuilder().setSeconds(20).build());
    assertThat(spanProto.getStatus().getCode()).isEqualTo(Status.OK.getCanonicalCode().value());
  }
}
