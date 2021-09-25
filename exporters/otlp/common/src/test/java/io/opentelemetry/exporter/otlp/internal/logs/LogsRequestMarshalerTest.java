/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.logs;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.internal.OtelEncodingUtils;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.InstrumentationLibrary;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.logs.v1.InstrumentationLibraryLogs;
import io.opentelemetry.proto.logs.v1.LogRecord;
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class LogsRequestMarshalerTest {
  private static final byte[] TRACE_ID_BYTES =
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4};
  private static final String TRACE_ID = TraceId.fromBytes(TRACE_ID_BYTES);
  private static final byte[] SPAN_ID_BYTES = new byte[] {0, 0, 0, 0, 4, 3, 2, 1};
  private static final String SPAN_ID = SpanId.fromBytes(SPAN_ID_BYTES);
  private static final String NAME = "GET /api/endpoint";
  private static final String BODY = "Hello world from this log...";

  @Test
  void toProtoResourceLogs() {
    ResourceLogsMarshaler[] resourceLogsMarshalers =
        ResourceLogsMarshaler.create(
            Collections.singleton(
                io.opentelemetry.sdk.logging.data.LogRecord.builder(
                        Resource.builder().put("one", 1).setSchemaUrl("http://url").build(),
                        InstrumentationLibraryInfo.create("testLib", "1.0", "http://url"))
                    .setName(NAME)
                    .setBody(BODY)
                    .setSeverity(io.opentelemetry.sdk.logging.data.LogRecord.Severity.INFO)
                    .setSeverityText("INFO")
                    .setTraceId(TRACE_ID)
                    .setSpanId(SPAN_ID)
                    .setAttributes(Attributes.of(AttributeKey.booleanKey("key"), true))
                    .setUnixTimeNano(12345)
                    .build()));

    assertThat(resourceLogsMarshalers).hasSize(1);

    ResourceLogs onlyResourceLogs =
        parse(ResourceLogs.getDefaultInstance(), resourceLogsMarshalers[0]);
    assertThat(onlyResourceLogs.getSchemaUrl()).isEqualTo("http://url");
    assertThat(onlyResourceLogs.getInstrumentationLibraryLogsCount()).isEqualTo(1);
    InstrumentationLibraryLogs instrumentationLibraryLogs =
        onlyResourceLogs.getInstrumentationLibraryLogs(0);
    assertThat(instrumentationLibraryLogs.getSchemaUrl()).isEqualTo("http://url");
    assertThat(instrumentationLibraryLogs.getInstrumentationLibrary())
        .isEqualTo(
            InstrumentationLibrary.newBuilder().setName("testLib").setVersion("1.0").build());
  }

  @Test
  void toProtoLogRecord() {
    LogRecord logRecord =
        parse(
            LogRecord.getDefaultInstance(),
            LogMarshaler.create(
                io.opentelemetry.sdk.logging.data.LogRecord.builder(
                        Resource.create(Attributes.builder().put("testKey", "testValue").build()),
                        InstrumentationLibraryInfo.create("instrumentation", "1"))
                    .setName(NAME)
                    .setBody(BODY)
                    .setSeverity(io.opentelemetry.sdk.logging.data.LogRecord.Severity.INFO)
                    .setSeverityText("INFO")
                    .setTraceId(TRACE_ID)
                    .setSpanId(SPAN_ID)
                    .setAttributes(Attributes.of(AttributeKey.booleanKey("key"), true))
                    .setUnixTimeNano(12345)
                    .build()));

    assertThat(logRecord.getTraceId().toByteArray()).isEqualTo(TRACE_ID_BYTES);
    assertThat(logRecord.getSpanId().toByteArray()).isEqualTo(SPAN_ID_BYTES);
    assertThat(logRecord.getName()).isEqualTo(NAME);
    assertThat(logRecord.getBody()).isEqualTo(AnyValue.newBuilder().setStringValue(BODY).build());
    assertThat(logRecord.getAttributesList())
        .containsExactly(
            KeyValue.newBuilder()
                .setKey("key")
                .setValue(AnyValue.newBuilder().setBoolValue(true).build())
                .build());
    assertThat(logRecord.getTimeUnixNano()).isEqualTo(12345);
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
    if (result instanceof LogRecord) {
      fixSpanJsonIds((LogRecord.Builder) builder);
    }

    if (result instanceof ResourceLogs) {
      ResourceLogs.Builder fixed = (ResourceLogs.Builder) builder;
      for (InstrumentationLibraryLogs.Builder ill :
          fixed.getInstrumentationLibraryLogsBuilderList()) {
        for (LogRecord.Builder span : ill.getLogsBuilderList()) {
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
}
