/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.exporters.otlp;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.ByteString;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Span.SpanKind;
import io.opentelemetry.proto.trace.v1.Status;
import io.opentelemetry.proto.trace.v1.Status.StatusCode;
import io.opentelemetry.sdk.trace.data.EventImpl;
import io.opentelemetry.sdk.trace.data.SpanData.Link;
import io.opentelemetry.sdk.trace.data.test.TestSpanData;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceState;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link SpanAdapter}. */
class SpanAdapterTest {
  private static final byte[] TRACE_ID_BYTES =
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4};
  private static final TraceId TRACE_ID = TraceId.fromBytes(TRACE_ID_BYTES, 0);
  private static final byte[] SPAN_ID_BYTES = new byte[] {0, 0, 0, 0, 4, 3, 2, 1};
  private static final SpanId SPAN_ID = SpanId.fromBytes(SPAN_ID_BYTES, 0);
  private static final TraceState TRACE_STATE = TraceState.builder().build();
  private static final SpanContext SPAN_CONTEXT =
      SpanContext.create(
          TRACE_ID, SPAN_ID, TraceFlags.builder().setIsSampled(true).build(), TRACE_STATE);

  @Test
  void toProtoSpan() {
    Span span =
        SpanAdapter.toProtoSpan(
            TestSpanData.newBuilder()
                .setHasEnded(true)
                .setTraceId(TRACE_ID)
                .setSpanId(SPAN_ID)
                .setParentSpanId(SpanId.getInvalid())
                .setName("GET /api/endpoint")
                .setKind(Kind.SERVER)
                .setStartEpochNanos(12345)
                .setEndEpochNanos(12349)
                .setAttributes(Attributes.of("key", AttributeValue.booleanAttributeValue(true)))
                .setTotalAttributeCount(2)
                .setEvents(
                    Collections.singletonList(
                        EventImpl.create(12347, "my_event", Attributes.empty())))
                .setTotalRecordedEvents(3)
                .setLinks(Collections.singletonList(Link.create(SPAN_CONTEXT)))
                .setTotalRecordedLinks(2)
                .setStatus(io.opentelemetry.trace.Status.OK)
                .build());

    assertThat(span.getTraceId().toByteArray()).isEqualTo(TRACE_ID_BYTES);
    assertThat(span.getSpanId().toByteArray()).isEqualTo(SPAN_ID_BYTES);
    assertThat(span.getParentSpanId().toByteArray()).isEqualTo(new byte[] {});
    assertThat(span.getName()).isEqualTo("GET /api/endpoint");
    assertThat(span.getKind()).isEqualTo(SpanKind.SERVER);
    assertThat(span.getStartTimeUnixNano()).isEqualTo(12345);
    assertThat(span.getEndTimeUnixNano()).isEqualTo(12349);
    assertThat(span.getAttributesList())
        .containsExactly(
            KeyValue.newBuilder()
                .setKey("key")
                .setValue(AnyValue.newBuilder().setBoolValue(true).build())
                .build());
    assertThat(span.getDroppedAttributesCount()).isEqualTo(1);
    assertThat(span.getEventsList())
        .containsExactly(
            Span.Event.newBuilder().setTimeUnixNano(12347).setName("my_event").build());
    assertThat(span.getDroppedEventsCount()).isEqualTo(2); // 3 - 1
    assertThat(span.getLinksList())
        .containsExactly(
            Span.Link.newBuilder()
                .setTraceId(ByteString.copyFrom(TRACE_ID_BYTES))
                .setSpanId(ByteString.copyFrom(SPAN_ID_BYTES))
                .build());
    assertThat(span.getDroppedLinksCount()).isEqualTo(1); // 2 - 1
    assertThat(span.getStatus()).isEqualTo(Status.newBuilder().setCode(StatusCode.Ok).build());
  }

  @Test
  void toProtoSpanKind() {
    assertThat(SpanAdapter.toProtoSpanKind(Kind.INTERNAL)).isEqualTo(SpanKind.INTERNAL);
    assertThat(SpanAdapter.toProtoSpanKind(Kind.CLIENT)).isEqualTo(SpanKind.CLIENT);
    assertThat(SpanAdapter.toProtoSpanKind(Kind.SERVER)).isEqualTo(SpanKind.SERVER);
    assertThat(SpanAdapter.toProtoSpanKind(Kind.PRODUCER)).isEqualTo(SpanKind.PRODUCER);
    assertThat(SpanAdapter.toProtoSpanKind(Kind.CONSUMER)).isEqualTo(SpanKind.CONSUMER);
  }

  @Test
  void toProtoStatus() {
    assertThat(SpanAdapter.toStatusProto(io.opentelemetry.trace.Status.OK))
        .isEqualTo(Status.newBuilder().setCode(StatusCode.Ok).build());
    assertThat(
            SpanAdapter.toStatusProto(
                io.opentelemetry.trace.Status.CANCELLED.withDescription("CANCELLED")))
        .isEqualTo(
            Status.newBuilder().setCode(StatusCode.Cancelled).setMessage("CANCELLED").build());
    assertThat(
            SpanAdapter.toStatusProto(
                io.opentelemetry.trace.Status.UNKNOWN.withDescription("UNKNOWN")))
        .isEqualTo(
            Status.newBuilder().setCode(StatusCode.UnknownError).setMessage("UNKNOWN").build());
    assertThat(
            SpanAdapter.toStatusProto(
                io.opentelemetry.trace.Status.INVALID_ARGUMENT.withDescription("INVALID_ARGUMENT")))
        .isEqualTo(
            Status.newBuilder()
                .setCode(StatusCode.InvalidArgument)
                .setMessage("INVALID_ARGUMENT")
                .build());
    assertThat(
            SpanAdapter.toStatusProto(
                io.opentelemetry.trace.Status.DEADLINE_EXCEEDED.withDescription(
                    "DEADLINE_EXCEEDED")))
        .isEqualTo(
            Status.newBuilder()
                .setCode(StatusCode.DeadlineExceeded)
                .setMessage("DEADLINE_EXCEEDED")
                .build());
    assertThat(
            SpanAdapter.toStatusProto(
                io.opentelemetry.trace.Status.NOT_FOUND.withDescription("NOT_FOUND")))
        .isEqualTo(
            Status.newBuilder().setCode(StatusCode.NotFound).setMessage("NOT_FOUND").build());
    assertThat(
            SpanAdapter.toStatusProto(
                io.opentelemetry.trace.Status.ALREADY_EXISTS.withDescription("ALREADY_EXISTS")))
        .isEqualTo(
            Status.newBuilder()
                .setCode(StatusCode.AlreadyExists)
                .setMessage("ALREADY_EXISTS")
                .build());
    assertThat(
            SpanAdapter.toStatusProto(
                io.opentelemetry.trace.Status.PERMISSION_DENIED.withDescription(
                    "PERMISSION_DENIED")))
        .isEqualTo(
            Status.newBuilder()
                .setCode(StatusCode.PermissionDenied)
                .setMessage("PERMISSION_DENIED")
                .build());
    assertThat(
            SpanAdapter.toStatusProto(
                io.opentelemetry.trace.Status.RESOURCE_EXHAUSTED.withDescription(
                    "RESOURCE_EXHAUSTED")))
        .isEqualTo(
            Status.newBuilder()
                .setCode(StatusCode.ResourceExhausted)
                .setMessage("RESOURCE_EXHAUSTED")
                .build());
    assertThat(
            SpanAdapter.toStatusProto(
                io.opentelemetry.trace.Status.FAILED_PRECONDITION.withDescription(
                    "FAILED_PRECONDITION")))
        .isEqualTo(
            Status.newBuilder()
                .setCode(StatusCode.FailedPrecondition)
                .setMessage("FAILED_PRECONDITION")
                .build());
    assertThat(
            SpanAdapter.toStatusProto(
                io.opentelemetry.trace.Status.ABORTED.withDescription("ABORTED")))
        .isEqualTo(Status.newBuilder().setCode(StatusCode.Aborted).setMessage("ABORTED").build());
    assertThat(
            SpanAdapter.toStatusProto(
                io.opentelemetry.trace.Status.OUT_OF_RANGE.withDescription("OUT_OF_RANGE")))
        .isEqualTo(
            Status.newBuilder().setCode(StatusCode.OutOfRange).setMessage("OUT_OF_RANGE").build());
    assertThat(
            SpanAdapter.toStatusProto(
                io.opentelemetry.trace.Status.UNIMPLEMENTED.withDescription("UNIMPLEMENTED")))
        .isEqualTo(
            Status.newBuilder()
                .setCode(StatusCode.Unimplemented)
                .setMessage("UNIMPLEMENTED")
                .build());
    assertThat(
            SpanAdapter.toStatusProto(
                io.opentelemetry.trace.Status.INTERNAL.withDescription("INTERNAL")))
        .isEqualTo(
            Status.newBuilder().setCode(StatusCode.InternalError).setMessage("INTERNAL").build());
    assertThat(
            SpanAdapter.toStatusProto(
                io.opentelemetry.trace.Status.UNAVAILABLE.withDescription("UNAVAILABLE")))
        .isEqualTo(
            Status.newBuilder().setCode(StatusCode.Unavailable).setMessage("UNAVAILABLE").build());
    assertThat(
            SpanAdapter.toStatusProto(
                io.opentelemetry.trace.Status.DATA_LOSS.withDescription("DATA_LOSS")))
        .isEqualTo(
            Status.newBuilder().setCode(StatusCode.DataLoss).setMessage("DATA_LOSS").build());
    assertThat(
            SpanAdapter.toStatusProto(
                io.opentelemetry.trace.Status.UNAUTHENTICATED.withDescription("UNAUTHENTICATED")))
        .isEqualTo(
            Status.newBuilder()
                .setCode(StatusCode.Unauthenticated)
                .setMessage("UNAUTHENTICATED")
                .build());
  }

  @Test
  void toProtoSpanEvent_WithoutAttributes() {
    assertThat(
            SpanAdapter.toProtoSpanEvent(
                EventImpl.create(12345, "test_without_attributes", Attributes.empty())))
        .isEqualTo(
            Span.Event.newBuilder()
                .setTimeUnixNano(12345)
                .setName("test_without_attributes")
                .build());
  }

  @Test
  void toProtoSpanEvent_WithAttributes() {
    assertThat(
            SpanAdapter.toProtoSpanEvent(
                EventImpl.create(
                    12345,
                    "test_with_attributes",
                    Attributes.of("key_string", AttributeValue.stringAttributeValue("string")),
                    5)))
        .isEqualTo(
            Span.Event.newBuilder()
                .setTimeUnixNano(12345)
                .setName("test_with_attributes")
                .addAttributes(
                    KeyValue.newBuilder()
                        .setKey("key_string")
                        .setValue(AnyValue.newBuilder().setStringValue("string").build())
                        .build())
                .setDroppedAttributesCount(4)
                .build());
  }

  @Test
  void toProtoSpanLink_WithoutAttributes() {
    assertThat(SpanAdapter.toProtoSpanLink(Link.create(SPAN_CONTEXT)))
        .isEqualTo(
            Span.Link.newBuilder()
                .setTraceId(ByteString.copyFrom(TRACE_ID_BYTES))
                .setSpanId(ByteString.copyFrom(SPAN_ID_BYTES))
                .build());
  }

  @Test
  void toProtoSpanLink_WithAttributes() {
    assertThat(
            SpanAdapter.toProtoSpanLink(
                Link.create(
                    SPAN_CONTEXT,
                    Attributes.of("key_string", AttributeValue.stringAttributeValue("string")),
                    5)))
        .isEqualTo(
            Span.Link.newBuilder()
                .setTraceId(ByteString.copyFrom(TRACE_ID_BYTES))
                .setSpanId(ByteString.copyFrom(SPAN_ID_BYTES))
                .addAttributes(
                    KeyValue.newBuilder()
                        .setKey("key_string")
                        .setValue(AnyValue.newBuilder().setStringValue("string").build())
                        .build())
                .setDroppedAttributesCount(4)
                .build());
  }
}
