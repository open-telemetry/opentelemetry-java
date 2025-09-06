/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.slf4j.event.Level;

@SuppressLogger(OtlpJsonLoggingMetricExporter.class)
class OtlpJsonLoggingMetricExporterTest {

  private final TestDataExporter<MetricExporter> testDataExporter = TestDataExporter.forMetrics();

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(OtlpJsonLoggingMetricExporter.class);

  private MetricExporter exporter;

  @BeforeEach
  void setUp() {
    exporter = OtlpJsonLoggingMetricExporter.create();
  }

  @Test
  void getAggregationTemporality() {
    assertThat(
            OtlpJsonLoggingMetricExporter.create()
                .getAggregationTemporality(InstrumentType.COUNTER))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(
            OtlpJsonLoggingMetricExporter.create(AggregationTemporality.DELTA)
                .getAggregationTemporality(InstrumentType.COUNTER))
        .isEqualTo(AggregationTemporality.DELTA);
  }

  /**
   * Test that the new create method with useLowAllocation parameter maintains correct aggregation
   * temporality.
   */
  @Test
  void getAggregationTemporalityWithUseLowAllocation() {
    assertThat(
            OtlpJsonLoggingMetricExporter.create(AggregationTemporality.CUMULATIVE, false)
                .getAggregationTemporality(InstrumentType.COUNTER))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(
            OtlpJsonLoggingMetricExporter.create(AggregationTemporality.DELTA, false)
                .getAggregationTemporality(InstrumentType.COUNTER))
        .isEqualTo(AggregationTemporality.DELTA);
    assertThat(
            OtlpJsonLoggingMetricExporter.create(AggregationTemporality.CUMULATIVE, true)
                .getAggregationTemporality(InstrumentType.COUNTER))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(
            OtlpJsonLoggingMetricExporter.create(AggregationTemporality.DELTA, true)
                .getAggregationTemporality(InstrumentType.COUNTER))
        .isEqualTo(AggregationTemporality.DELTA);
  }

  @Test
  void log() throws Exception {
    testDataExporter.export(exporter);

    assertThat(logs.getEvents())
        .hasSize(1)
        .allSatisfy(log -> assertThat(log.getLevel()).isEqualTo(Level.INFO));
    String message = logs.getEvents().get(0).getMessage();
    String expectedJson = testDataExporter.getExpectedJson(false);
    JSONAssert.assertEquals("Got \n" + message, expectedJson, message, /* strict= */ false);
    assertThat(message).doesNotContain("\n");
  }

  @Test
  void logWithWrapperJsonObjectFalse() throws Exception {
    // Test that useLowAllocation=false produces the same output as the default create()
    MetricExporter exporterWithoutWrapper =
        OtlpJsonLoggingMetricExporter.create(AggregationTemporality.CUMULATIVE, false);
    testDataExporter.export(exporterWithoutWrapper);

    assertThat(logs.getEvents())
        .hasSize(1)
        .allSatisfy(log -> assertThat(log.getLevel()).isEqualTo(Level.INFO));
    String message = logs.getEvents().get(0).getMessage();
    String expectedJson = testDataExporter.getExpectedJson(false);
    JSONAssert.assertEquals("Got \n" + message, expectedJson, message, /* strict= */ false);
    assertThat(message).doesNotContain("\n");
  }

  @Test
  void logWithWrapperJsonObjectTrue() throws Exception {
    // Test that useLowAllocation=true produces wrapper format (enables low allocation)
    MetricExporter exporterWithWrapper =
        OtlpJsonLoggingMetricExporter.create(AggregationTemporality.CUMULATIVE, true);
    testDataExporter.export(exporterWithWrapper);

    assertThat(logs.getEvents())
        .hasSize(1)
        .allSatisfy(log -> assertThat(log.getLevel()).isEqualTo(Level.INFO));
    String message = logs.getEvents().get(0).getMessage();
    String expectedJson = testDataExporter.getExpectedJson(true);
    JSONAssert.assertEquals("Got \n" + message, expectedJson, message, /* strict= */ false);
    assertThat(message).doesNotContain("\n");
  }

  @Test
  void logWithWrapperJsonObjectTrueAndDeltaTemporality() throws Exception {
    // Test that useLowAllocation=true works with DELTA temporality too
    MetricExporter exporterWithWrapper =
        OtlpJsonLoggingMetricExporter.create(AggregationTemporality.DELTA, true);
    testDataExporter.export(exporterWithWrapper);

    assertThat(logs.getEvents())
        .hasSize(1)
        .allSatisfy(log -> assertThat(log.getLevel()).isEqualTo(Level.INFO));
    String message = logs.getEvents().get(0).getMessage();
    String expectedJson = testDataExporter.getExpectedJson(true);
    JSONAssert.assertEquals("Got \n" + message, expectedJson, message, /* strict= */ false);
    assertThat(message).doesNotContain("\n");
  }

  @Test
  void flush() {
    assertThat(exporter.flush().isSuccess()).isTrue();
  }

  @Test
  void shutdown() {
    assertThat(exporter.shutdown().isSuccess()).isTrue();
    assertThat(
            exporter
                .export(Collections.singletonList(TestDataExporter.METRIC1))
                .join(10, TimeUnit.SECONDS)
                .isSuccess())
        .isFalse();
    assertThat(logs.getEvents()).isEmpty();
    assertThat(exporter.shutdown().isSuccess()).isTrue();
    logs.assertContains("Calling shutdown() multiple times.");
  }
}
