/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_CLIENT;
import static io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_CONSUMER;
import static io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_INTERNAL;
import static io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_PRODUCER;
import static io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_SERVER;
import static io.opentelemetry.proto.trace.v1.Status.DeprecatedStatusCode.DEPRECATED_STATUS_CODE_OK;
import static io.opentelemetry.proto.trace.v1.Status.DeprecatedStatusCode.DEPRECATED_STATUS_CODE_UNKNOWN_ERROR;
import static io.opentelemetry.proto.trace.v1.Status.StatusCode.STATUS_CODE_ERROR;
import static io.opentelemetry.proto.trace.v1.Status.StatusCode.STATUS_CODE_OK;
import static io.opentelemetry.proto.trace.v1.Status.StatusCode.STATUS_CODE_UNSET;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.ByteString;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Status;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class SpanAdapterTest {
  private static final byte[] TRACE_ID_BYTES =
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4};
  private static final String TRACE_ID = TraceId.fromBytes(TRACE_ID_BYTES);
  private static final byte[] SPAN_ID_BYTES = new byte[] {0, 0, 0, 0, 4, 3, 2, 1};
  private static final String SPAN_ID = SpanId.fromBytes(SPAN_ID_BYTES);
  private static final SpanContext SPAN_CONTEXT =
      SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault());

  @Test
  void toProtoSpan() {
    Span span =
        SpanAdapter.toProtoSpan(
            TestSpanData.builder()
                .setHasEnded(true)
                .setSpanContext(SPAN_CONTEXT)
                .setParentSpanContext(SpanContext.getInvalid())
                .setName("GET /api/endpoint")
                .setKind(SpanKind.SERVER)
                .setStartEpochNanos(12345)
                .setEndEpochNanos(12349)
                .setAttributes(Attributes.of(booleanKey("key"), true))
                .setTotalAttributeCount(2)
                .setEvents(
                    Collections.singletonList(
                        EventData.create(12347, "my_event", Attributes.empty())))
                .setTotalRecordedEvents(3)
                .setLinks(Collections.singletonList(LinkData.create(SPAN_CONTEXT)))
                .setTotalRecordedLinks(2)
                .setStatus(StatusData.ok())
                .build(),
            Collections.emptyMap());

    assertThat(span.getTraceId().toByteArray()).isEqualTo(TRACE_ID_BYTES);
    assertThat(span.getSpanId().toByteArray()).isEqualTo(SPAN_ID_BYTES);
    assertThat(span.getParentSpanId().toByteArray()).isEqualTo(new byte[] {});
    assertThat(span.getName()).isEqualTo("GET /api/endpoint");
    assertThat(span.getKind()).isEqualTo(SPAN_KIND_SERVER);
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
    assertThat(span.getStatus()).isEqualTo(Status.newBuilder().setCode(STATUS_CODE_OK).build());
  }

  @Test
  void toProtoSpanKind() {
    assertThat(SpanAdapter.toProtoSpanKind(SpanKind.INTERNAL)).isEqualTo(SPAN_KIND_INTERNAL);
    assertThat(SpanAdapter.toProtoSpanKind(SpanKind.CLIENT)).isEqualTo(SPAN_KIND_CLIENT);
    assertThat(SpanAdapter.toProtoSpanKind(SpanKind.SERVER)).isEqualTo(SPAN_KIND_SERVER);
    assertThat(SpanAdapter.toProtoSpanKind(SpanKind.PRODUCER)).isEqualTo(SPAN_KIND_PRODUCER);
    assertThat(SpanAdapter.toProtoSpanKind(SpanKind.CONSUMER)).isEqualTo(SPAN_KIND_CONSUMER);
  }

  @Test
  @SuppressWarnings("deprecation") // setDeprecatedCode is deprecated.
  void toProtoStatus() {
    assertThat(SpanAdapter.toStatusProto(StatusData.unset()))
        .isEqualTo(
            Status.newBuilder()
                .setCode(STATUS_CODE_UNSET)
                .setDeprecatedCode(DEPRECATED_STATUS_CODE_OK)
                .build());
    assertThat(SpanAdapter.toStatusProto(StatusData.create(StatusCode.ERROR, "ERROR")))
        .isEqualTo(
            Status.newBuilder()
                .setCode(STATUS_CODE_ERROR)
                .setDeprecatedCode(DEPRECATED_STATUS_CODE_UNKNOWN_ERROR)
                .setMessage("ERROR")
                .build());
    assertThat(SpanAdapter.toStatusProto(StatusData.create(StatusCode.ERROR, "UNKNOWN")))
        .isEqualTo(
            Status.newBuilder()
                .setCode(STATUS_CODE_ERROR)
                .setDeprecatedCode(DEPRECATED_STATUS_CODE_UNKNOWN_ERROR)
                .setMessage("UNKNOWN")
                .build());
    assertThat(SpanAdapter.toStatusProto(StatusData.create(StatusCode.OK, "OK_OVERRIDE")))
        .isEqualTo(
            Status.newBuilder()
                .setCode(STATUS_CODE_OK)
                .setDeprecatedCode(DEPRECATED_STATUS_CODE_OK)
                .setMessage("OK_OVERRIDE")
                .build());
  }

  @Test
  void toProtoSpanEvent_WithoutAttributes() {
    assertThat(
            SpanAdapter.toProtoSpanEvent(
                EventData.create(12345, "test_without_attributes", Attributes.empty())))
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
                EventData.create(
                    12345,
                    "test_with_attributes",
                    Attributes.of(stringKey("key_string"), "string"),
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
    assertThat(SpanAdapter.toProtoSpanLink(LinkData.create(SPAN_CONTEXT), Collections.emptyMap()))
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
                LinkData.create(SPAN_CONTEXT, Attributes.of(stringKey("key_string"), "string"), 5),
                Collections.emptyMap()))
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
