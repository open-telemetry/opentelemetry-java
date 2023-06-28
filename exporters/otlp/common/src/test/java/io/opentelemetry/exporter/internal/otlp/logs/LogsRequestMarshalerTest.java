/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.logs;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.internal.OtelEncodingUtils;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.InstrumentationScope;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.logs.v1.LogRecord;
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import io.opentelemetry.proto.logs.v1.ScopeLogs;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.logs.TestLogRecordData;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class LogsRequestMarshalerTest {
  private static final byte[] TRACE_ID_BYTES =
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4};
  private static final String TRACE_ID = TraceId.fromBytes(TRACE_ID_BYTES);
  private static final byte[] SPAN_ID_BYTES = new byte[] {0, 0, 0, 0, 4, 3, 2, 1};
  private static final String SPAN_ID = SpanId.fromBytes(SPAN_ID_BYTES);
  private static final String BODY = "Hello world from this log...";

  @Test
  void toProtoResourceLogs() {
    ResourceLogsMarshaler[] resourceLogsMarshalers =
        ResourceLogsMarshaler.create(
            Collections.singleton(
                TestLogRecordData.builder()
                    .setResource(
                        Resource.builder().put("one", 1).setSchemaUrl("http://url").build())
                    .setInstrumentationScopeInfo(
                        InstrumentationScopeInfo.builder("testLib")
                            .setVersion("1.0")
                            .setSchemaUrl("http://url")
                            .setAttributes(Attributes.builder().put("key", "value").build())
                            .build())
                    .setBody(BODY)
                    .setSeverity(Severity.INFO)
                    .setSeverityText("INFO")
                    .setSpanContext(
                        SpanContext.create(
                            TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault()))
                    .setAttributes(Attributes.of(AttributeKey.booleanKey("key"), true))
                    .setTotalAttributeCount(2)
                    .setTimestamp(12345, TimeUnit.NANOSECONDS)
                    .setObservedTimestamp(6789, TimeUnit.NANOSECONDS)
                    .build()));

    assertThat(resourceLogsMarshalers).hasSize(1);

    ResourceLogs onlyResourceLogs =
        parse(ResourceLogs.getDefaultInstance(), resourceLogsMarshalers[0]);
    assertThat(onlyResourceLogs.getSchemaUrl()).isEqualTo("http://url");
    assertThat(onlyResourceLogs.getScopeLogsCount()).isEqualTo(1);
    ScopeLogs instrumentationLibraryLogs = onlyResourceLogs.getScopeLogs(0);
    assertThat(instrumentationLibraryLogs.getSchemaUrl()).isEqualTo("http://url");
    assertThat(instrumentationLibraryLogs.getScope())
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

  @Test
  void toProtoLogRecord() {
    LogRecord logRecord =
        parse(
            LogRecord.getDefaultInstance(),
            LogMarshaler.create(
                TestLogRecordData.builder()
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
                    .setAttributes(Attributes.of(AttributeKey.booleanKey("key"), true))
                    .setTotalAttributeCount(2)
                    .setTimestamp(12345, TimeUnit.NANOSECONDS)
                    .setObservedTimestamp(6789, TimeUnit.NANOSECONDS)
                    .build()));

    assertThat(logRecord.getTraceId().toByteArray()).isEqualTo(TRACE_ID_BYTES);
    assertThat(logRecord.getSpanId().toByteArray()).isEqualTo(SPAN_ID_BYTES);
    assertThat(logRecord.getSeverityText()).isEqualTo("INFO");
    assertThat(logRecord.getBody()).isEqualTo(AnyValue.newBuilder().setStringValue(BODY).build());
    assertThat(logRecord.getAttributesList())
        .containsExactly(
            KeyValue.newBuilder()
                .setKey("key")
                .setValue(AnyValue.newBuilder().setBoolValue(true).build())
                .build());
    assertThat(logRecord.getDroppedAttributesCount()).isEqualTo(1);
    assertThat(logRecord.getTimeUnixNano()).isEqualTo(12345);
    assertThat(logRecord.getObservedTimeUnixNano()).isEqualTo(6789);
  }

  @Test
  void toProtoLogRecord_MinimalFields() {
    LogRecord logRecord =
        parse(
            LogRecord.getDefaultInstance(),
            LogMarshaler.create(
                TestLogRecordData.builder()
                    .setResource(
                        Resource.create(Attributes.builder().put("testKey", "testValue").build()))
                    .setInstrumentationScopeInfo(
                        InstrumentationScopeInfo.builder("instrumentation").setVersion("1").build())
                    .setTimestamp(12345, TimeUnit.NANOSECONDS)
                    .setObservedTimestamp(6789, TimeUnit.NANOSECONDS)
                    .build()));

    assertThat(logRecord.getTraceId()).isEmpty();
    assertThat(logRecord.getSpanId()).isEmpty();
    assertThat(logRecord.getSeverityText()).isBlank();
    assertThat(logRecord.getSeverityNumber().getNumber())
        .isEqualTo(Severity.UNDEFINED_SEVERITY_NUMBER.getSeverityNumber());
    assertThat(logRecord.getBody()).isEqualTo(AnyValue.newBuilder().setStringValue("").build());
    assertThat(logRecord.getAttributesList()).isEmpty();
    assertThat(logRecord.getDroppedAttributesCount()).isZero();
    assertThat(logRecord.getTimeUnixNano()).isEqualTo(12345);
    assertThat(logRecord.getObservedTimeUnixNano()).isEqualTo(6789);
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
}
