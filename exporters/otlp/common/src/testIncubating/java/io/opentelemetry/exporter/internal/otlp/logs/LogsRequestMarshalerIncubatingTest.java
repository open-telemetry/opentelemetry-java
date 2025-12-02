/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.logs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.incubator.common.ExtendedAttributeKey;
import io.opentelemetry.api.incubator.common.ExtendedAttributes;
import io.opentelemetry.api.internal.OtelEncodingUtils;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.ArrayValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.common.v1.KeyValueList;
import io.opentelemetry.proto.logs.v1.LogRecord;
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import io.opentelemetry.proto.logs.v1.ScopeLogs;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.logs.TestLogRecordData;
import io.opentelemetry.sdk.testing.logs.internal.TestExtendedLogRecordData;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@SuppressWarnings("deprecation") // Testing deprecated EXTENDED_ATTRIBUTES until removed
class LogsRequestMarshalerIncubatingTest {
  private static final byte[] TRACE_ID_BYTES =
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4};
  private static final String TRACE_ID = TraceId.fromBytes(TRACE_ID_BYTES);
  private static final byte[] SPAN_ID_BYTES = new byte[] {0, 0, 0, 0, 4, 3, 2, 1};
  private static final String SPAN_ID = SpanId.fromBytes(SPAN_ID_BYTES);
  private static final String EVENT_NAME = "hello";
  private static final String BODY = "Hello world from this log...";

  // Marshalers should not throw if the incubator is on the class path, but are called with
  // LogRecordData instances which are not ExtendedLogRecordData.
  // See: https://github.com/open-telemetry/opentelemetry-java/issues/7363
  @ParameterizedTest
  @EnumSource(MarshalerSource.class)
  void toProtoLogRecord_NotExtendedLogRecordData_DoesNotThrow(MarshalerSource marshalerSource) {
    assertThatCode(
            () ->
                parse(
                    LogRecord.getDefaultInstance(),
                    marshalerSource.create(TestLogRecordData.builder().build())))
        .doesNotThrowAnyException();
  }

  @ParameterizedTest
  @EnumSource(MarshalerSource.class)
  void toProtoLogRecord(MarshalerSource marshalerSource) {
    LogRecord logRecord =
        parse(
            LogRecord.getDefaultInstance(),
            marshalerSource.create(
                TestExtendedLogRecordData.builder()
                    .setResource(
                        Resource.create(Attributes.builder().put("testKey", "testValue").build()))
                    .setInstrumentationScopeInfo(
                        InstrumentationScopeInfo.builder("instrumentation").setVersion("1").build())
                    .setBody(BODY)
                    .setSeverity(Severity.INFO)
                    .setSeverityText("INFO")
                    .setSpanContext(
                        SpanContext.create(
                            TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault()))
                    .setTotalAttributeCount(11)
                    .setTimestamp(12345, TimeUnit.NANOSECONDS)
                    .setObservedTimestamp(6789, TimeUnit.NANOSECONDS)
                    // Extended fields
                    .setEventName(EVENT_NAME)
                    .setExtendedAttributes(
                        ExtendedAttributes.builder()
                            .put("str_key", "str_value")
                            .put("str_arr_key", "str_value1", "str_value2")
                            .put("bool_key", true)
                            .put("bool_arr_key", true, false)
                            .put("double_key", 1.1)
                            .put("double_arr_key", 1.1, 2.2)
                            .put("int_key", 1)
                            .put("int_arr_key", 1, 2)
                            .put(
                                "kv_list_key",
                                ExtendedAttributes.builder()
                                    .put("bool_key", true)
                                    .put("double_key", 1.1)
                                    .put("int_key", 1)
                                    .put(
                                        "kv_list_key",
                                        ExtendedAttributes.builder()
                                            .put("str_key", "str_value")
                                            .build())
                                    .put("str_key", "str_value")
                                    .build())
                            .put(
                                ExtendedAttributeKey.valueKey("value_key"),
                                Value.of(
                                    io.opentelemetry.api.common.KeyValue.of(
                                        "bool_key", Value.of(true)),
                                    io.opentelemetry.api.common.KeyValue.of(
                                        "double_key", Value.of(1.1)),
                                    io.opentelemetry.api.common.KeyValue.of("int_key", Value.of(1)),
                                    io.opentelemetry.api.common.KeyValue.of(
                                        "value_key",
                                        Value.of(
                                            io.opentelemetry.api.common.KeyValue.of(
                                                "str_key", Value.of("str_value")))),
                                    io.opentelemetry.api.common.KeyValue.of(
                                        "str_key", Value.of("str_value"))))
                            .build())
                    .build()));

    assertThat(logRecord.getTraceId().toByteArray()).isEqualTo(TRACE_ID_BYTES);
    assertThat(logRecord.getSpanId().toByteArray()).isEqualTo(SPAN_ID_BYTES);
    assertThat(logRecord.getSeverityText()).isEqualTo("INFO");

    assertThat(logRecord.getBody()).isEqualTo(AnyValue.newBuilder().setStringValue(BODY).build());
    assertThat(logRecord.getDroppedAttributesCount()).isEqualTo(1);
    assertThat(logRecord.getTimeUnixNano()).isEqualTo(12345);
    assertThat(logRecord.getObservedTimeUnixNano()).isEqualTo(6789);
    assertThat(logRecord.getEventName()).isEqualTo(EVENT_NAME);
    assertThat(logRecord.getAttributesList())
        .containsExactlyInAnyOrder(
            keyValue("str_key", anyValue("str_value")),
            keyValue(
                "str_arr_key",
                anyValue(Arrays.asList(anyValue("str_value1"), anyValue("str_value2")))),
            keyValue("bool_key", anyValue(true)),
            keyValue("bool_arr_key", anyValue(Arrays.asList(anyValue(true), anyValue(false)))),
            keyValue("double_key", anyValue(1.1)),
            keyValue("double_arr_key", anyValue(Arrays.asList(anyValue(1.1), anyValue(2.2)))),
            keyValue("int_key", anyValue(1)),
            keyValue("int_arr_key", anyValue(Arrays.asList(anyValue(1), anyValue(2)))),
            keyValue(
                "kv_list_key",
                AnyValue.newBuilder()
                    .setKvlistValue(
                        KeyValueList.newBuilder()
                            .addValues(keyValue("bool_key", anyValue(true)))
                            .addValues(keyValue("double_key", anyValue(1.1)))
                            .addValues(keyValue("int_key", anyValue(1)))
                            .addValues(
                                keyValue(
                                    "kv_list_key",
                                    AnyValue.newBuilder()
                                        .setKvlistValue(
                                            KeyValueList.newBuilder()
                                                .addValues(
                                                    keyValue("str_key", anyValue("str_value")))
                                                .build())
                                        .build()))
                            .addValues(keyValue("str_key", anyValue("str_value")))
                            .build())
                    .build()),
            keyValue(
                "value_key",
                AnyValue.newBuilder()
                    .setKvlistValue(
                        KeyValueList.newBuilder()
                            .addValues(keyValue("bool_key", anyValue(true)))
                            .addValues(keyValue("double_key", anyValue(1.1)))
                            .addValues(keyValue("int_key", anyValue(1)))
                            .addValues(
                                keyValue(
                                    "value_key",
                                    AnyValue.newBuilder()
                                        .setKvlistValue(
                                            KeyValueList.newBuilder()
                                                .addValues(
                                                    keyValue("str_key", anyValue("str_value")))
                                                .build())
                                        .build()))
                            .addValues(keyValue("str_key", anyValue("str_value")))
                            .build())
                    .build()));
  }

  private static AnyValue anyValue(String value) {
    return AnyValue.newBuilder().setStringValue(value).build();
  }

  private static AnyValue anyValue(long value) {
    return AnyValue.newBuilder().setIntValue(value).build();
  }

  private static AnyValue anyValue(double value) {
    return AnyValue.newBuilder().setDoubleValue(value).build();
  }

  private static AnyValue anyValue(boolean value) {
    return AnyValue.newBuilder().setBoolValue(value).build();
  }

  private static AnyValue anyValue(List<AnyValue> value) {
    return AnyValue.newBuilder()
        .setArrayValue(ArrayValue.newBuilder().addAllValues(value).build())
        .build();
  }

  private static KeyValue keyValue(String key, AnyValue anyValue) {
    return KeyValue.newBuilder().setKey(key).setValue(anyValue).build();
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
    Message.Builder builder = prototype.newBuilderForType();
    try {
      String json = toJson(marshaler);
      JsonFormat.parser().merge(json, builder);
    } catch (InvalidProtocolBufferException e) {
      throw new UncheckedIOException(e);
    }

    // Hackily swap out "hex as base64" decoded IDs with correct ones since no JSON protobuf
    // libraries currently support customizing on the parse side.
    if (result instanceof LogRecord) {
      fixSpanJsonIds((LogRecord.Builder) builder);
    }

    if (result instanceof ResourceLogs) {
      ResourceLogs.Builder fixed = (ResourceLogs.Builder) builder;
      for (ScopeLogs.Builder ill : fixed.getScopeLogsBuilderList()) {
        for (LogRecord.Builder span : ill.getLogRecordsBuilderList()) {
          fixSpanJsonIds(span);
        }
      }
    }

    assertThat(builder.build()).isEqualTo(result);

    return result;
  }

  private static void fixSpanJsonIds(LogRecord.Builder span) {
    span.setTraceId(toHex(span.getTraceId()));
    span.setSpanId(toHex(span.getSpanId()));
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
    MARSHALER {
      @Override
      Marshaler create(LogRecordData logData) {
        return LogMarshaler.create(logData);
      }
    },
    LOW_ALLOCATION_MARSHALER {
      @Override
      Marshaler create(LogRecordData logData) {
        return createMarshaler(LogStatelessMarshaler.INSTANCE, logData);
      }
    };

    abstract Marshaler create(LogRecordData logData);
  }
}
