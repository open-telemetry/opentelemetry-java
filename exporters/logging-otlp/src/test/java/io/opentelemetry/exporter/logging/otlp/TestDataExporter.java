/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import com.google.common.io.Resources;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.logs.TestLogRecordData;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

abstract class TestDataExporter<T> {

  private final String expectedFileNoWrapper;
  private final String expectedFileWrapper;
  private static final Resource RESOURCE =
      Resource.create(Attributes.builder().put("key", "value").build());

  private static final LogRecordData LOG1 =
      TestLogRecordData.builder()
          .setResource(RESOURCE)
          .setInstrumentationScopeInfo(
              InstrumentationScopeInfo.builder("instrumentation")
                  .setVersion("1")
                  .setAttributes(Attributes.builder().put("key", "value").build())
                  .build())
          .setBody("body1")
          .setSeverity(Severity.INFO)
          .setSeverityText("INFO")
          .setTimestamp(100L, TimeUnit.NANOSECONDS)
          .setObservedTimestamp(200L, TimeUnit.NANOSECONDS)
          .setAttributes(Attributes.of(stringKey("animal"), "cat", longKey("lives"), 9L))
          .setSpanContext(
              SpanContext.create(
                  "12345678876543211234567887654322",
                  "8765432112345876",
                  TraceFlags.getDefault(),
                  TraceState.getDefault()))
          .build();

  private static final LogRecordData LOG2 =
      TestLogRecordData.builder()
          .setResource(RESOURCE)
          .setInstrumentationScopeInfo(
              InstrumentationScopeInfo.builder("instrumentation2").setVersion("2").build())
          .setBody("body2")
          .setSeverity(Severity.INFO)
          .setSeverityText("INFO")
          .setTimestamp(100L, TimeUnit.NANOSECONDS)
          .setObservedTimestamp(200L, TimeUnit.NANOSECONDS)
          .setAttributes(Attributes.of(booleanKey("important"), true))
          .setSpanContext(
              SpanContext.create(
                  "12345678876543211234567887654322",
                  "8765432112345875",
                  TraceFlags.getDefault(),
                  TraceState.getDefault()))
          .build();

  public TestDataExporter(String expectedFileNoWrapper, String expectedFileWrapper) {
    this.expectedFileNoWrapper = expectedFileNoWrapper;
    this.expectedFileWrapper = expectedFileWrapper;
  }

  public String getExpectedJson(boolean withWrapper) throws IOException {
    String file = withWrapper ? expectedFileWrapper : expectedFileNoWrapper;
    return Resources.toString(Resources.getResource(file), StandardCharsets.UTF_8);
  }

  abstract CompletableResultCode export(T exporter);

  abstract CompletableResultCode flush(T exporter);

  abstract CompletableResultCode shutdown(T exporter);

  static TestDataExporter<LogRecordExporter> forLogs() {
    return new TestDataExporter<LogRecordExporter>(
        "expected-logs.json", "expected-logs-wrapper.json") {
      @Override
      public CompletableResultCode export(LogRecordExporter exporter) {
        return exporter.export(Arrays.asList(LOG1, LOG2));
      }

      @Override
      public CompletableResultCode flush(LogRecordExporter exporter) {
        return exporter.flush();
      }

      @Override
      public CompletableResultCode shutdown(LogRecordExporter exporter) {
        return exporter.shutdown();
      }
    };
  }
}
