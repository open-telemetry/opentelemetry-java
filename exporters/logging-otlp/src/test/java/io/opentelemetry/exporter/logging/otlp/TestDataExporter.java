/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.api.common.AttributeKey.valueKey;

import com.google.common.io.Resources;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.KeyValue;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoublePointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.logs.internal.TestExtendedLogRecordData;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

abstract class TestDataExporter<T> {

  private final String expectedFileNoWrapper;
  private final String expectedFileWrapper;
  private static final Resource RESOURCE =
      Resource.create(Attributes.builder().put("key", "value").build());

  private static final LogRecordData LOG1 =
      TestExtendedLogRecordData.builder()
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
          .setAttributes(
              Attributes.builder()
                  .put(stringKey("animal"), "cat")
                  .put(longKey("lives"), 9L)
                  .put(valueKey("bytes"), Value.of(new byte[] {1, 2, 3}))
                  .put(valueKey("map"), Value.of(KeyValue.of("nested", Value.of("value"))))
                  .put(valueKey("heterogeneousArray"), Value.of(Value.of("string"), Value.of(123L)))
                  .put(valueKey("empty"), Value.empty())
                  .build())
          .setTotalAttributeCount(6)
          .setSpanContext(
              SpanContext.create(
                  "12345678876543211234567887654322",
                  "8765432112345876",
                  TraceFlags.getDefault(),
                  TraceState.getDefault()))
          .build();

  private static final LogRecordData LOG2 =
      TestExtendedLogRecordData.builder()
          .setResource(RESOURCE)
          .setInstrumentationScopeInfo(
              InstrumentationScopeInfo.builder("instrumentation2").setVersion("2").build())
          .setBody("body2")
          .setSeverity(Severity.INFO)
          .setSeverityText("INFO")
          .setTimestamp(100L, TimeUnit.NANOSECONDS)
          .setObservedTimestamp(200L, TimeUnit.NANOSECONDS)
          .setAttributes(Attributes.of(booleanKey("important"), true))
          .setTotalAttributeCount(1)
          .setSpanContext(
              SpanContext.create(
                  "12345678876543211234567887654322",
                  "8765432112345875",
                  TraceFlags.getDefault(),
                  TraceState.getDefault()))
          .build();

  static final SpanData SPAN1 =
      TestSpanData.builder()
          .setHasEnded(true)
          .setSpanContext(
              SpanContext.create(
                  "12345678876543211234567887654321",
                  "8765432112345678",
                  TraceFlags.getSampled(),
                  TraceState.getDefault()))
          .setStartEpochNanos(100)
          .setEndEpochNanos(100 + 1000)
          .setStatus(StatusData.ok())
          .setName("testSpan1")
          .setKind(SpanKind.INTERNAL)
          .setAttributes(
              Attributes.builder()
                  .put(stringKey("animal"), "cat")
                  .put(longKey("lives"), 9L)
                  .put(valueKey("bytes"), Value.of(new byte[] {1, 2, 3}))
                  .put(valueKey("map"), Value.of(KeyValue.of("nested", Value.of("value"))))
                  .put(valueKey("heterogeneousArray"), Value.of(Value.of("string"), Value.of(123L)))
                  .put(valueKey("empty"), Value.empty())
                  .build())
          .setEvents(
              Collections.singletonList(
                  EventData.create(
                      100 + 500,
                      "somethingHappenedHere",
                      Attributes.of(booleanKey("important"), true))))
          .setTotalAttributeCount(2)
          .setTotalRecordedEvents(1)
          .setTotalRecordedLinks(0)
          .setInstrumentationScopeInfo(
              InstrumentationScopeInfo.builder("instrumentation")
                  .setVersion("1")
                  .setAttributes(Attributes.builder().put("key", "value").build())
                  .build())
          .setResource(RESOURCE)
          .build();

  private static final SpanData SPAN2 =
      TestSpanData.builder()
          .setHasEnded(false)
          .setSpanContext(
              SpanContext.create(
                  "12340000000043211234000000004321",
                  "8765000000005678",
                  TraceFlags.getSampled(),
                  TraceState.getDefault()))
          .setStartEpochNanos(500)
          .setEndEpochNanos(500 + 1001)
          .setStatus(StatusData.error())
          .setName("testSpan2")
          .setKind(SpanKind.CLIENT)
          .setResource(RESOURCE)
          .setInstrumentationScopeInfo(
              InstrumentationScopeInfo.builder("instrumentation2").setVersion("2").build())
          .build();

  static final MetricData METRIC1 =
      ImmutableMetricData.createDoubleSum(
          RESOURCE,
          InstrumentationScopeInfo.builder("instrumentation")
              .setVersion("1")
              .setAttributes(Attributes.builder().put("key", "value").build())
              .build(),
          "metric1",
          "metric1 description",
          "m",
          ImmutableSumData.create(
              true,
              AggregationTemporality.CUMULATIVE,
              Collections.singletonList(
                  ImmutableDoublePointData.create(
                      1,
                      2,
                      Attributes.builder()
                          .put(stringKey("cat"), "meow")
                          .put(valueKey("bytes"), Value.of(new byte[] {1, 2, 3}))
                          .put(valueKey("map"), Value.of(KeyValue.of("nested", Value.of("value"))))
                          .put(
                              valueKey("heterogeneousArray"),
                              Value.of(Value.of("string"), Value.of(123L)))
                          .put(valueKey("empty"), Value.empty())
                          .build(),
                      4))));

  private static final MetricData METRIC2 =
      ImmutableMetricData.createDoubleSum(
          RESOURCE,
          InstrumentationScopeInfo.builder("instrumentation2").setVersion("2").build(),
          "metric2",
          "metric2 description",
          "s",
          ImmutableSumData.create(
              true,
              AggregationTemporality.CUMULATIVE,
              Collections.singletonList(
                  ImmutableDoublePointData.create(
                      1, 2, Attributes.of(stringKey("cat"), "meow"), 4))));

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

  static TestDataExporter<SpanExporter> forSpans() {
    return new TestDataExporter<SpanExporter>(
        "expected-spans.json", "expected-spans-wrapper.json") {
      @Override
      public CompletableResultCode export(SpanExporter exporter) {
        return exporter.export(Arrays.asList(SPAN1, SPAN2));
      }

      @Override
      public CompletableResultCode flush(SpanExporter exporter) {
        return exporter.flush();
      }

      @Override
      public CompletableResultCode shutdown(SpanExporter exporter) {
        return exporter.shutdown();
      }
    };
  }

  static TestDataExporter<MetricExporter> forMetrics() {
    return new TestDataExporter<MetricExporter>(
        "expected-metrics.json", "expected-metrics-wrapper.json") {
      @Override
      public CompletableResultCode export(MetricExporter exporter) {
        return exporter.export(Arrays.asList(METRIC1, METRIC2));
      }

      @Override
      public CompletableResultCode flush(MetricExporter exporter) {
        return exporter.flush();
      }

      @Override
      public CompletableResultCode shutdown(MetricExporter exporter) {
        return exporter.shutdown();
      }
    };
  }
}
