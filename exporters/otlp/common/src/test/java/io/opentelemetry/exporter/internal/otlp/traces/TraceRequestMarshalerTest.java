/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.traces;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.api.trace.propagation.internal.W3CTraceContextEncoding.encodeTraceState;
import static io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_SERVER;
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
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.ArrayValue;
import io.opentelemetry.proto.common.v1.InstrumentationScope;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.ScopeSpans;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Status;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class TraceRequestMarshalerTest {

  private static final byte[] TRACE_ID_BYTES =
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4};
  private static final String TRACE_ID = TraceId.fromBytes(TRACE_ID_BYTES);
  private static final byte[] SPAN_ID_BYTES = new byte[] {0, 0, 0, 0, 4, 3, 2, 1};
  private static final byte[] PARENT_SPAN_ID_BYTES = new byte[] {0, 0, 0, 0, 5, 6, 7, 8};
  private static final String SPAN_ID = SpanId.fromBytes(SPAN_ID_BYTES);
  private static final String PARENT_SPAN_ID = SpanId.fromBytes(PARENT_SPAN_ID_BYTES);
  private static final String TRACE_STATE_VALUE = "baz=qux,foo=bar";
  private static final SpanContext SPAN_CONTEXT =
      SpanContext.create(
          TRACE_ID,
          SPAN_ID,
          TraceFlags.getSampled(),
          TraceState.builder().put("foo", "bar").put("baz", "qux").build());

  private static final SpanContext PARENT_SPAN_CONTEXT =
      SpanContext.createFromRemoteParent(
          TRACE_ID, PARENT_SPAN_ID, TraceFlags.getSampled(), TraceState.builder().build());

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
                    .setInstrumentationScopeInfo(
                        InstrumentationScopeInfo.builder("testLib")
                            .setVersion("1.0")
                            .setSchemaUrl("http://url")
                            .setAttributes(Attributes.builder().put("key", "value").build())
                            .build())
                    .setResource(
                        Resource.builder().put("one", 1).setSchemaUrl("http://url").build())
                    .build()));

    assertThat(resourceSpansMarshalers).hasSize(1);

    ResourceSpans onlyResourceSpans =
        parse(ResourceSpans.getDefaultInstance(), resourceSpansMarshalers[0]);
    assertThat(onlyResourceSpans.getSchemaUrl()).isEqualTo("http://url");
    assertThat(onlyResourceSpans.getScopeSpansCount()).isEqualTo(1);
    ScopeSpans instrumentationLibrarySpans = onlyResourceSpans.getScopeSpans(0);
    assertThat(instrumentationLibrarySpans.getSchemaUrl()).isEqualTo("http://url");
    assertThat(instrumentationLibrarySpans.getScope())
        .isEqualTo(
            InstrumentationScope.newBuilder()
                .setName("testLib")
                .setVersion("1.0")
                .addAttributes(
                    KeyValue.newBuilder()
                        .setKey("key")
                        .setValue(AnyValue.newBuilder().setStringValue("value").build())
                        .build())
                .build());
  }

  @ParameterizedTest
  @EnumSource(MarshalerSource.class)
  void toProtoSpan(MarshalerSource marshalerSource) {
    Span protoSpan =
        parse(
            Span.getDefaultInstance(),
            marshalerSource.create(
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
                            .put("empty_string", "")
                            .put("false_value", false)
                            .put("zero_int", 0L)
                            .put("zero_double", 0.0)
                            // TODO: add empty array, empty map, empty bytes, and true empty value
                            // after https://github.com/open-telemetry/opentelemetry-java/pull/7973
                            .build())
                    .setTotalAttributeCount(13)
                    .setEvents(
                        Collections.singletonList(
                            EventData.create(12347, "my_event", Attributes.empty())))
                    .setTotalRecordedEvents(3)
                    .setLinks(Collections.singletonList(LinkData.create(SPAN_CONTEXT)))
                    .setTotalRecordedLinks(2)
                    .setStatus(StatusData.ok())
                    .build()));

    assertThat(protoSpan.getTraceId().toByteArray()).isEqualTo(TRACE_ID_BYTES);
    assertThat(protoSpan.getSpanId().toByteArray()).isEqualTo(SPAN_ID_BYTES);
    assertThat(protoSpan.getFlags() & 0xff)
        .isEqualTo((SPAN_CONTEXT.getTraceFlags().asByte() & 0xff));
    assertThat(SpanFlags.isKnownWhetherParentIsRemote(protoSpan.getFlags())).isTrue();
    assertThat(SpanFlags.isParentRemote(protoSpan.getFlags())).isFalse();
    assertThat(protoSpan.getTraceState()).isEqualTo(TRACE_STATE_VALUE);
    assertThat(protoSpan.getParentSpanId().toByteArray()).isEqualTo(new byte[] {});
    assertThat(protoSpan.getName()).isEqualTo("GET /api/endpoint");
    assertThat(protoSpan.getKind()).isEqualTo(SPAN_KIND_SERVER);
    assertThat(protoSpan.getStartTimeUnixNano()).isEqualTo(12345);
    assertThat(protoSpan.getEndTimeUnixNano()).isEqualTo(12349);
    assertThat(protoSpan.getAttributesList())
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
                .build(),
            KeyValue.newBuilder()
                .setKey("empty_string")
                .setValue(AnyValue.newBuilder().setStringValue("").build())
                .build(),
            KeyValue.newBuilder()
                .setKey("false_value")
                .setValue(AnyValue.newBuilder().setBoolValue(false).build())
                .build(),
            KeyValue.newBuilder()
                .setKey("zero_int")
                .setValue(AnyValue.newBuilder().setIntValue(0).build())
                .build(),
            KeyValue.newBuilder()
                .setKey("zero_double")
                .setValue(AnyValue.newBuilder().setDoubleValue(0.0).build())
                .build());
    assertThat(protoSpan.getDroppedAttributesCount()).isEqualTo(1);
    assertThat(protoSpan.getEventsList())
        .containsExactly(
            Span.Event.newBuilder().setTimeUnixNano(12347).setName("my_event").build());
    assertThat(protoSpan.getDroppedEventsCount()).isEqualTo(2); // 3 - 1
    assertThat(protoSpan.getLinksList())
        .containsExactly(
            Span.Link.newBuilder()
                .setTraceId(ByteString.copyFrom(TRACE_ID_BYTES))
                .setSpanId(ByteString.copyFrom(SPAN_ID_BYTES))
                .setFlags(
                    (SPAN_CONTEXT.getTraceFlags().asByte() & 0xff)
                        | SpanFlags.getHasParentIsRemoteMask())
                .setTraceState(encodeTraceState(SPAN_CONTEXT.getTraceState()))
                .build());
    assertThat(protoSpan.getDroppedLinksCount()).isEqualTo(1); // 2 - 1
    assertThat(protoSpan.getStatus())
        .isEqualTo(Status.newBuilder().setCode(STATUS_CODE_OK).build());
  }

  @ParameterizedTest
  @EnumSource(MarshalerSource.class)
  void toProtoSpan_withRemoteParent(MarshalerSource marshalerSource) {
    Span protoSpan =
        parse(
            Span.getDefaultInstance(),
            marshalerSource.create(
                TestSpanData.builder()
                    .setHasEnded(true)
                    .setSpanContext(SPAN_CONTEXT)
                    .setParentSpanContext(PARENT_SPAN_CONTEXT)
                    .setName("GET /api/endpoint")
                    .setKind(SpanKind.SERVER)
                    .setStartEpochNanos(12345)
                    .setEndEpochNanos(12349)
                    .setStatus(StatusData.ok())
                    .build()));

    assertThat(protoSpan.getTraceId().toByteArray()).isEqualTo(TRACE_ID_BYTES);
    assertThat(protoSpan.getSpanId().toByteArray()).isEqualTo(SPAN_ID_BYTES);
    assertThat(protoSpan.getFlags() & 0xff)
        .isEqualTo((SPAN_CONTEXT.getTraceFlags().asByte() & 0xff));
    assertThat(SpanFlags.isKnownWhetherParentIsRemote(protoSpan.getFlags())).isTrue();
    assertThat(SpanFlags.isParentRemote(protoSpan.getFlags())).isTrue();
    assertThat(protoSpan.getTraceState()).isEqualTo(TRACE_STATE_VALUE);
    assertThat(protoSpan.getParentSpanId().toByteArray()).isEqualTo(PARENT_SPAN_ID_BYTES);
    assertThat(protoSpan.getName()).isEqualTo("GET /api/endpoint");
    assertThat(protoSpan.getKind()).isEqualTo(SPAN_KIND_SERVER);
    assertThat(protoSpan.getStartTimeUnixNano()).isEqualTo(12345);
    assertThat(protoSpan.getEndTimeUnixNano()).isEqualTo(12349);
    assertThat(protoSpan.getStatus())
        .isEqualTo(Status.newBuilder().setCode(STATUS_CODE_OK).build());
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

  @ParameterizedTest
  @EnumSource(MarshalerSource.class)
  void toProtoStatus(MarshalerSource marshalerSource) {
    assertThat(parse(Status.getDefaultInstance(), marshalerSource.create(StatusData.unset())))
        .isEqualTo(Status.newBuilder().setCode(STATUS_CODE_UNSET).build());
    assertThat(
            parse(
                Status.getDefaultInstance(),
                SpanStatusMarshaler.create(StatusData.create(StatusCode.ERROR, "ERROR"))))
        .isEqualTo(Status.newBuilder().setCode(STATUS_CODE_ERROR).setMessage("ERROR").build());
    assertThat(
            parse(
                Status.getDefaultInstance(),
                SpanStatusMarshaler.create(StatusData.create(StatusCode.ERROR, "UNKNOWN"))))
        .isEqualTo(Status.newBuilder().setCode(STATUS_CODE_ERROR).setMessage("UNKNOWN").build());
    assertThat(
            parse(
                Status.getDefaultInstance(),
                SpanStatusMarshaler.create(StatusData.create(StatusCode.OK, "OK_OVERRIDE"))))
        .isEqualTo(Status.newBuilder().setCode(STATUS_CODE_OK).setMessage("OK_OVERRIDE").build());
  }

  @ParameterizedTest
  @EnumSource(MarshalerSource.class)
  void toProtoSpanEvent_WithoutAttributes(MarshalerSource marshalerSource) {
    assertThat(
            parse(
                Span.Event.getDefaultInstance(),
                marshalerSource.create(
                    EventData.create(12345, "test_without_attributes", Attributes.empty()))))
        .isEqualTo(
            Span.Event.newBuilder()
                .setTimeUnixNano(12345)
                .setName("test_without_attributes")
                .build());
  }

  @ParameterizedTest
  @EnumSource(MarshalerSource.class)
  void toProtoSpanEvent_WithAttributes(MarshalerSource marshalerSource) {
    assertThat(
            parse(
                Span.Event.getDefaultInstance(),
                marshalerSource.create(
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

  @ParameterizedTest
  @EnumSource(MarshalerSource.class)
  void toProtoSpanLink_WithoutAttributes(MarshalerSource marshalerSource) {
    assertThat(
            parse(
                Span.Link.getDefaultInstance(),
                marshalerSource.create(LinkData.create(SPAN_CONTEXT))))
        .isEqualTo(
            Span.Link.newBuilder()
                .setTraceId(ByteString.copyFrom(TRACE_ID_BYTES))
                .setSpanId(ByteString.copyFrom(SPAN_ID_BYTES))
                .setFlags(
                    (SPAN_CONTEXT.getTraceFlags().asByte() & 0xff)
                        | SpanFlags.getHasParentIsRemoteMask())
                .setTraceState(TRACE_STATE_VALUE)
                .build());
  }

  @ParameterizedTest
  @EnumSource(MarshalerSource.class)
  void toProtoSpanLink_WithRemoteContext(MarshalerSource marshalerSource) {
    assertThat(
            parse(
                Span.Link.getDefaultInstance(),
                marshalerSource.create(LinkData.create(PARENT_SPAN_CONTEXT))))
        .isEqualTo(
            Span.Link.newBuilder()
                .setTraceId(ByteString.copyFrom(TRACE_ID_BYTES))
                .setSpanId(ByteString.copyFrom(PARENT_SPAN_ID_BYTES))
                .setFlags(
                    (SPAN_CONTEXT.getTraceFlags().asByte() & 0xff)
                        | SpanFlags.getParentIsRemoteMask())
                .build());
  }

  @ParameterizedTest
  @EnumSource(MarshalerSource.class)
  void toProtoSpanLink_WithAttributes(MarshalerSource marshalerSource) {
    assertThat(
            parse(
                Span.Link.getDefaultInstance(),
                marshalerSource.create(
                    LinkData.create(
                        SPAN_CONTEXT, Attributes.of(stringKey("key_string"), "string"), 5))))
        .isEqualTo(
            Span.Link.newBuilder()
                .setTraceId(ByteString.copyFrom(TRACE_ID_BYTES))
                .setSpanId(ByteString.copyFrom(SPAN_ID_BYTES))
                .setFlags(
                    (SPAN_CONTEXT.getTraceFlags().asByte() & 0xff)
                        | SpanFlags.getHasParentIsRemoteMask())
                .setTraceState(TRACE_STATE_VALUE)
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
      for (ScopeSpans.Builder ss : fixed.getScopeSpansBuilderList()) {
        for (Span.Builder span : ss.getSpansBuilderList()) {
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

  private static <T> Marshaler createMarshaler(StatelessMarshaler<T> marshaler, T data) {
    return new Marshaler() {
      private final MarshalerContext context = new MarshalerContext();
      private final int size = marshaler.getBinarySerializedSize(data, context);

      @Override
      public int getBinarySerializedSize() {
        return size;
      }

      @Override
      protected void writeTo(Serializer output) throws IOException {
        context.resetReadIndex();
        marshaler.writeTo(output, data, context);
      }
    };
  }

  private enum MarshalerSource {
    STATEFUL_MARSHALER {
      @Override
      Marshaler create(SpanData spanData) {
        return SpanMarshaler.create(spanData);
      }

      @Override
      Marshaler create(StatusData statusData) {
        return SpanStatusMarshaler.create(statusData);
      }

      @Override
      Marshaler create(EventData eventData) {
        return SpanEventMarshaler.create(eventData);
      }

      @Override
      Marshaler create(LinkData linkData) {
        return SpanLinkMarshaler.create(linkData);
      }
    },
    STATELESS_MARSHALER {
      @Override
      Marshaler create(SpanData spanData) {
        return createMarshaler(SpanStatelessMarshaler.INSTANCE, spanData);
      }

      @Override
      Marshaler create(StatusData statusData) {
        return createMarshaler(SpanStatusStatelessMarshaler.INSTANCE, statusData);
      }

      @Override
      Marshaler create(EventData eventData) {
        return createMarshaler(SpanEventStatelessMarshaler.INSTANCE, eventData);
      }

      @Override
      Marshaler create(LinkData linkData) {
        return createMarshaler(SpanLinkStatelessMarshaler.INSTANCE, linkData);
      }
    };

    abstract Marshaler create(SpanData spanData);

    abstract Marshaler create(StatusData statusData);

    abstract Marshaler create(EventData eventData);

    abstract Marshaler create(LinkData linkData);
  }
}
