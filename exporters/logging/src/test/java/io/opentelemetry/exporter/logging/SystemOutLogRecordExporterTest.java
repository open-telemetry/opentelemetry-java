/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging;

import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.api.common.AttributeKey.valueKey;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.KeyValue;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.logs.TestLogRecordData;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class SystemOutLogRecordExporterTest {

  @Test
  void export() {
    SystemOutLogRecordExporter exporter = SystemOutLogRecordExporter.create();
    assertThat(exporter.export(singletonList(sampleLog(System.currentTimeMillis()))).isSuccess())
        .isTrue();
  }

  @Test
  void format() {
    long timestamp =
        LocalDateTime.of(1970, Month.AUGUST, 7, 10, 0).toInstant(ZoneOffset.UTC).toEpochMilli();
    LogRecordData log = sampleLog(timestamp);
    StringBuilder output = new StringBuilder();
    SystemOutLogRecordExporter.formatLog(output, log);
    assertThat(output.toString())
        .isEqualTo(
            "1970-08-07T10:00:00Z ERROR3 'message' : 00000000000000010000000000000002 0000000000000003 "
                + "[scopeInfo: logTest:1.0] {amount=1, bytes=ValueBytes{AQID}, cheese=\"cheddar\", "
                + "empty=ValueEmpty{}, heterogeneousArray=ValueArray{[\"string\",123]}, "
                + "map=KeyValueList{{\"nested\":\"value\"}}}");
  }

  @Test
  void shutdown() {
    SystemOutLogRecordExporter exporter = SystemOutLogRecordExporter.create();
    assertThat(exporter.shutdown().isSuccess()).isTrue();
    assertThat(
            exporter
                .export(Collections.singletonList(sampleLog(System.currentTimeMillis())))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isFalse();
    assertThat(exporter.shutdown().isSuccess()).isTrue();
  }

  @Test
  void stringRepresentation() {
    assertThat(SystemOutLogRecordExporter.create().toString())
        .isEqualTo("SystemOutLogRecordExporter{}");
  }

  private static LogRecordData sampleLog(long timestamp) {
    return TestLogRecordData.builder()
        .setResource(Resource.empty())
        .setInstrumentationScopeInfo(
            InstrumentationScopeInfo.builder("logTest").setVersion("1.0").build())
        .setAttributes(
            Attributes.builder()
                .put(stringKey("cheese"), "cheddar")
                .put(longKey("amount"), 1L)
                .put(valueKey("bytes"), Value.of(new byte[] {1, 2, 3}))
                .put(valueKey("map"), Value.of(KeyValue.of("nested", Value.of("value"))))
                .put(valueKey("heterogeneousArray"), Value.of(Value.of("string"), Value.of(123L)))
                .put(valueKey("empty"), Value.empty())
                .build())
        .setBody("message")
        .setSeverity(Severity.ERROR3)
        .setTimestamp(timestamp, TimeUnit.MILLISECONDS)
        .setSpanContext(
            SpanContext.create(
                "00000000000000010000000000000002",
                "0000000000000003",
                TraceFlags.getDefault(),
                TraceState.getDefault()))
        .build();
  }
}
