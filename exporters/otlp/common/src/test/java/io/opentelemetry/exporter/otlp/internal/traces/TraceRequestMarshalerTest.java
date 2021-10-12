/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.traces;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_SERVER;
import static io.opentelemetry.proto.trace.v1.Status.DeprecatedStatusCode.DEPRECATED_STATUS_CODE_OK;
import static io.opentelemetry.proto.trace.v1.Status.DeprecatedStatusCode.DEPRECATED_STATUS_CODE_UNKNOWN_ERROR;
import static io.opentelemetry.proto.trace.v1.Status.StatusCode.STATUS_CODE_ERROR;
import static io.opentelemetry.proto.trace.v1.Status.StatusCode.STATUS_CODE_OK;
import static io.opentelemetry.proto.trace.v1.Status.StatusCode.STATUS_CODE_UNSET;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.internal.OtelEncodingUtils;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.ArrayValue;
import io.opentelemetry.proto.common.v1.InstrumentationLibrary;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.InstrumentationLibrarySpans;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Status;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class TraceRequestMarshalerTest {

  private static final byte[] TRACE_ID_BYTES =
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4};
  private static final String TRACE_ID = TraceId.fromBytes(TRACE_ID_BYTES);
  private static final byte[] SPAN_ID_BYTES = new byte[] {0, 0, 0, 0, 4, 3, 2, 1};
  private static final String SPAN_ID = SpanId.fromBytes(SPAN_ID_BYTES);
  private static final SpanContext SPAN_CONTEXT =
      SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault());

  @Test
  void toProtoResourceSpans() {
    ResourceSpansMarshaler[] resourceSpansMarshalers =
        ResourceSpansMarshaler.create(
            Collections.singleton(
                TestSpanData.builder()
                    .setHasEnded(true)
                    .setSpanContext(SPAN_CONTEXT)
                    .setParentSpanContext(SpanContext.getInvalid())
                    .setName("GET /api/endpoint")
                    .setKind(SpanKind.SERVER)
                    .setStartEpochNanos(12345)
                    .setEndEpochNanos(12349)
                    .setStatus(StatusData.unset())
                    .setInstrumentationLibraryInfo(
                        InstrumentationLibraryInfo.create("testLib", "1.0", "http://url"))
                    .setResource(
                        Resource.builder().put("one", 1).setSchemaUrl("http://url").build())
                    .build()));

    assertThat(resourceSpansMarshalers).hasSize(1);

    ResourceSpans onlyResourceSpans =
        parse(ResourceSpans.getDefaultInstance(), resourceSpansMarshalers[0]);
    assertThat(onlyResourceSpans.getSchemaUrl()).isEqualTo("http://url");
    assertThat(onlyResourceSpans.getInstrumentationLibrarySpansCount()).isEqualTo(1);
    InstrumentationLibrarySpans instrumentationLibrarySpans =
        onlyResourceSpans.getInstrumentationLibrarySpans(0);
    assertThat(instrumentationLibrarySpans.getSchemaUrl()).isEqualTo("http://url");
    assertThat(instrumentationLibrarySpans.getInstrumentationLibrary())
        .isEqualTo(
            InstrumentationLibrary.newBuilder().setName("testLib").setVersion("1.0").build());
  }

  @Test
  void toProtoSpan() {
    Span span =
        parse(
            Span.getDefaultInstance(),
            SpanMarshaler.create(
                TestSpanData.builder()
                    .setHasEnded(true)
                    .setSpanContext(SPAN_CONTEXT)
                    .setParentSpanContext(SpanContext.getInvalid())
                    .setName("GET /api/endpoint")
                    .setKind(SpanKind.SERVER)
                    .setStartEpochNanos(12345)
                    .setEndEpochNanos(12349)
                    .setAttributes(
                        Attributes.builder()
                            .put("key", true)
                            .put("string", "string")
                            .put("int", 100L)
                            .put("double", 100.3)
                            .put("string_array", "string1", "string2")
                            .put("long_array", 12L, 23L)
                            .put("double_array", 12.3, 23.1)
                            .put("boolean_array", true, false)
                            .build())
                    .setTotalAttributeCount(9)
                    .setEvents(
                        Collections.singletonList(
                            EventData.create(12347, "my_event", Attributes.empty())))
                    .setTotalRecordedEvents(3)
                    .setLinks(Collections.singletonList(LinkData.create(SPAN_CONTEXT)))
                    .setTotalRecordedLinks(2)
                    .setStatus(StatusData.ok())
                    .build()));

    assertThat(span.getTraceId().toByteArray()).isEqualTo(TRACE_ID_BYTES);
    assertThat(span.getSpanId().toByteArray()).isEqualTo(SPAN_ID_BYTES);
    assertThat(span.getParentSpanId().toByteArray()).isEqualTo(new byte[] {});
    assertThat(span.getName()).isEqualTo("GET /api/endpoint");
    assertThat(span.getKind()).isEqualTo(SPAN_KIND_SERVER);
    assertThat(span.getStartTimeUnixNano()).isEqualTo(12345);
    assertThat(span.getEndTimeUnixNano()).isEqualTo(12349);
    assertThat(span.getAttributesList())
        .containsOnly(
            KeyValue.newBuilder()
                .setKey("key")
                .setValue(AnyValue.newBuilder().setBoolValue(true).build())
                .build(),
            KeyValue.newBuilder()
                .setKey("string")
                .setValue(AnyValue.newBuilder().setStringValue("string").build())
                .build(),
            KeyValue.newBuilder()
                .setKey("int")
                .setValue(AnyValue.newBuilder().setIntValue(100).build())
                .build(),
            KeyValue.newBuilder()
                .setKey("double")
                .setValue(AnyValue.newBuilder().setDoubleValue(100.3).build())
                .build(),
            KeyValue.newBuilder()
                .setKey("string_array")
                .setValue(
                    AnyValue.newBuilder()
                        .setArrayValue(
                            ArrayValue.newBuilder()
                                .addValues(AnyValue.newBuilder().setStringValue("string1").build())
                                .addValues(AnyValue.newBuilder().setStringValue("string2").build())
                                .build())
                        .build())
                .build(),
            KeyValue.newBuilder()
                .setKey("long_array")
                .setValue(
                    AnyValue.newBuilder()
                        .setArrayValue(
                            ArrayValue.newBuilder()
                                .addValues(AnyValue.newBuilder().setIntValue(12).build())
                                .addValues(AnyValue.newBuilder().setIntValue(23).build())
                                .build())
                        .build())
                .build(),
            KeyValue.newBuilder()
                .setKey("double_array")
                .setValue(
                    AnyValue.newBuilder()
                        .setArrayValue(
                            ArrayValue.newBuilder()
                                .addValues(AnyValue.newBuilder().setDoubleValue(12.3).build())
                                .addValues(AnyValue.newBuilder().setDoubleValue(23.1).build())
                                .build())
                        .build())
                .build(),
            KeyValue.newBuilder()
                .setKey("boolean_array")
                .setValue(
                    AnyValue.newBuilder()
                        .setArrayValue(
                            ArrayValue.newBuilder()
                                .addValues(AnyValue.newBuilder().setBoolValue(true).build())
                                .addValues(AnyValue.newBuilder().setBoolValue(false).build())
                                .build())
                        .build())
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
    assertThat(SpanMarshaler.toProtoSpanKind(SpanKind.INTERNAL))
        .isEqualTo(io.opentelemetry.proto.trace.v1.internal.Span.SpanKind.SPAN_KIND_INTERNAL);
    assertThat(SpanMarshaler.toProtoSpanKind(SpanKind.CLIENT))
        .isEqualTo(io.opentelemetry.proto.trace.v1.internal.Span.SpanKind.SPAN_KIND_CLIENT);
    assertThat(SpanMarshaler.toProtoSpanKind(SpanKind.SERVER))
        .isEqualTo(io.opentelemetry.proto.trace.v1.internal.Span.SpanKind.SPAN_KIND_SERVER);
    assertThat(SpanMarshaler.toProtoSpanKind(SpanKind.PRODUCER))
        .isEqualTo(io.opentelemetry.proto.trace.v1.internal.Span.SpanKind.SPAN_KIND_PRODUCER);
    assertThat(SpanMarshaler.toProtoSpanKind(SpanKind.CONSUMER))
        .isEqualTo(io.opentelemetry.proto.trace.v1.internal.Span.SpanKind.SPAN_KIND_CONSUMER);
  }

  @Test
  @SuppressWarnings("deprecation")
  // setDeprecatedCode is deprecated.
  void toProtoStatus() {
    assertThat(parse(Status.getDefaultInstance(), SpanStatusMarshaler.create(StatusData.unset())))
        .isEqualTo(
            Status.newBuilder()
                .setCode(STATUS_CODE_UNSET)
                .setDeprecatedCode(DEPRECATED_STATUS_CODE_OK)
                .build());
    assertThat(
            parse(
                Status.getDefaultInstance(),
                SpanStatusMarshaler.create(StatusData.create(StatusCode.ERROR, "ERROR"))))
        .isEqualTo(
            Status.newBuilder()
                .setCode(STATUS_CODE_ERROR)
                .setDeprecatedCode(DEPRECATED_STATUS_CODE_UNKNOWN_ERROR)
                .setMessage("ERROR")
                .build());
    assertThat(
            parse(
                Status.getDefaultInstance(),
                SpanStatusMarshaler.create(StatusData.create(StatusCode.ERROR, "UNKNOWN"))))
        .isEqualTo(
            Status.newBuilder()
                .setCode(STATUS_CODE_ERROR)
                .setDeprecatedCode(DEPRECATED_STATUS_CODE_UNKNOWN_ERROR)
                .setMessage("UNKNOWN")
                .build());
    assertThat(
            parse(
                Status.getDefaultInstance(),
                SpanStatusMarshaler.create(StatusData.create(StatusCode.OK, "OK_OVERRIDE"))))
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
            parse(
                Span.Event.getDefaultInstance(),
                SpanEventMarshaler.create(
                    EventData.create(12345, "test_without_attributes", Attributes.empty()))))
        .isEqualTo(
            Span.Event.newBuilder()
                .setTimeUnixNano(12345)
                .setName("test_without_attributes")
                .build());
  }

  @Test
  void toProtoSpanEvent_WithAttributes() {
    assertThat(
            parse(
                Span.Event.getDefaultInstance(),
                SpanEventMarshaler.create(
                    EventData.create(
                        12345,
                        "test_with_attributes",
                        Attributes.of(stringKey("key_string"), "string"),
                        5))))
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
    assertThat(
            parse(
                Span.Link.getDefaultInstance(),
                SpanLinkMarshaler.create(LinkData.create(SPAN_CONTEXT))))
        .isEqualTo(
            Span.Link.newBuilder()
                .setTraceId(ByteString.copyFrom(TRACE_ID_BYTES))
                .setSpanId(ByteString.copyFrom(SPAN_ID_BYTES))
                .build());
  }

  @Test
  void toProtoSpanLink_WithAttributes() {
    assertThat(
            parse(
                Span.Link.getDefaultInstance(),
                SpanLinkMarshaler.create(
                    LinkData.create(
                        SPAN_CONTEXT, Attributes.of(stringKey("key_string"), "string"), 5))))
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

  @SuppressWarnings("unchecked")
  private static <T extends Message> T parse(T prototype, Marshaler marshaler) {
    byte[] serialized = toByteArray(marshaler);
    T result;
    try {
      result = (T) prototype.newBuilderForType().mergeFrom(serialized).build();
    } catch (InvalidProtocolBufferException e) {
      throw new UncheckedIOException(e);
    }
    // Our marshaler should produce the exact same length of serialized output (for example, field
    // default values are not outputted), so we check that here. The output itself may have slightly
    // different ordering, mostly due to the way we don't output oneof values in field order all the
    // tieme. If the lengths are equal and the resulting protos are equal, the marshaling is
    // guaranteed to be valid.
    assertThat(result.getSerializedSize()).isEqualTo(serialized.length);

    // We don't compare JSON strings due to some differences (particularly serializing enums as
    // numbers instead of names). This may improve in the future but what matters is what we produce
    // can be parsed.
    String json = toJson(marshaler);
    Message.Builder builder = prototype.newBuilderForType();
    try {
      JsonFormat.parser().merge(json, builder);
    } catch (InvalidProtocolBufferException e) {
      throw new UncheckedIOException(e);
    }

    // Hackily swap out "hex as base64" decoded IDs with correct ones since no JSON protobuf
    // libraries currently support customizing on the parse side.
    if (result instanceof Span) {
      fixSpanJsonIds((Span.Builder) builder);
    }

    // Hackily swap out "hex as base64" decoded IDs with correct ones since no JSON protobuf
    // libraries currently support customizing on the parse side.
    if (result instanceof Span.Link) {
      fixLinkJsonIds((Span.Link.Builder) builder);
    }

    if (result instanceof ResourceSpans) {
      ResourceSpans.Builder fixed = (ResourceSpans.Builder) builder;
      for (InstrumentationLibrarySpans.Builder ils :
          fixed.getInstrumentationLibrarySpansBuilderList()) {
        for (Span.Builder span : ils.getSpansBuilderList()) {
          fixSpanJsonIds(span);
        }
      }
    }

    assertThat(builder.build()).isEqualTo(result);

    return result;
  }

  private static void fixSpanJsonIds(Span.Builder span) {
    span.setTraceId(toHex(span.getTraceId()));
    span.setSpanId(toHex(span.getSpanId()));
    span.setParentSpanId(toHex(span.getParentSpanId()));
    for (Span.Link.Builder link : span.getLinksBuilderList()) {
      fixLinkJsonIds(link);
    }
  }

  private static void fixLinkJsonIds(Span.Link.Builder link) {
    link.setTraceId(toHex(link.getTraceId()));
    link.setSpanId(toHex(link.getSpanId()));
  }

  @SuppressWarnings("UnusedMethod")
  private static ByteString toHex(ByteString hexReadAsBase64) {
    String hex =
        Base64.getEncoder().encodeToString(hexReadAsBase64.toByteArray()).toLowerCase(Locale.ROOT);
    return ByteString.copyFrom(OtelEncodingUtils.bytesFromBase16(hex, hex.length()));
  }

  private static byte[] toByteArray(Marshaler marshaler) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      marshaler.writeBinaryTo(bos);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return bos.toByteArray();
  }

  private static String toJson(Marshaler marshaler) {

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      marshaler.writeJsonTo(bos);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return new String(bos.toByteArray(), StandardCharsets.UTF_8);
  }
}
