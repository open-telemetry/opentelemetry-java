/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.InstrumentationLibrary;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.logs.v1.InstrumentationLibraryLogs;
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class LogAdapterTest {
  private static final byte[] TRACE_ID_BYTES =
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4};
  private static final String TRACE_ID = TraceId.fromBytes(TRACE_ID_BYTES);
  private static final byte[] SPAN_ID_BYTES = new byte[] {0, 0, 0, 0, 4, 3, 2, 1};
  private static final String SPAN_ID = SpanId.fromBytes(SPAN_ID_BYTES);
  private static final String NAME = "GET /api/endpoint";
  private static final String BODY = "Hello world from this log...";

  @Test
  void toProtoResourceLogs() {
    List<ResourceLogs> resourceLogs =
        LogAdapter.toProtoResourceLogs(
            Collections.singleton(
                io.opentelemetry.sdk.logging.data.LogRecord.builder()
                    .setName(NAME)
                    .setBody(BODY)
                    .setSeverity(io.opentelemetry.sdk.logging.data.LogRecord.Severity.INFO)
                    .setSeverityText("INFO")
                    .setTraceId(TRACE_ID)
                    .setSpanId(SPAN_ID)
                    .setAttributes(Attributes.of(AttributeKey.booleanKey("key"), true))
                    .setUnixTimeNano(12345)
                    .setResource(
                        Resource.builder().put("one", 1).setSchemaUrl("http://url").build())
                    .setInstrumentationLibraryInfo(
                        InstrumentationLibraryInfo.create("testLib", "1.0", "http://url"))
                    .build()));

    assertThat(resourceLogs).hasSize(1);
    ResourceLogs onlyResourceLogs = resourceLogs.get(0);
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
    io.opentelemetry.proto.logs.v1.LogRecord logRecord =
        LogAdapter.toProtoLogRecord(
            io.opentelemetry.sdk.logging.data.LogRecord.builder()
                .setName(NAME)
                .setBody(BODY)
                .setSeverity(io.opentelemetry.sdk.logging.data.LogRecord.Severity.INFO)
                .setSeverityText("INFO")
                .setTraceId(TRACE_ID)
                .setSpanId(SPAN_ID)
                .setAttributes(Attributes.of(AttributeKey.booleanKey("key"), true))
                .setUnixTimeNano(12345)
                .setResource(
                    Resource.create(Attributes.builder().put("testKey", "testValue").build()))
                .setInstrumentationLibraryInfo(
                    InstrumentationLibraryInfo.create("instrumentation", "1"))
                .build());

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
}
