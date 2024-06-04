/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.sdk;

import static io.opentelemetry.sdk.testing.assertj.TracesAssert.assertThat;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import io.opentelemetry.sdk.testing.assertj.TracesAssert;
import io.opentelemetry.sdk.testing.exporter.InMemoryLogRecordExporter;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class OpenTelemetryTesting {
  protected final OpenTelemetrySdk openTelemetry;
  protected final InMemorySpanExporter spanExporter;
  protected final Supplier<Collection<MetricData>> metricReader;
  protected final InMemoryLogRecordExporter logRecordExporter;

  public OpenTelemetryTesting(OpenTelemetryTestSdk sdk) {
    this.openTelemetry = sdk.getOpenTelemetry();
    this.spanExporter = sdk.getSpanExporter();
    this.metricReader = sdk.getMetricReader();
    this.logRecordExporter = sdk.getLogRecordExporter();
  }

  /** Returns the {@link OpenTelemetrySdk} created by this extension. */
  public OpenTelemetry getOpenTelemetry() {
    return openTelemetry;
  }

  /** Returns all the exported {@link SpanData} so far. */
  public List<SpanData> getSpans() {
    return spanExporter.getFinishedSpanItems();
  }

  /**
   * Returns the current {@link MetricData} in {@link AggregationTemporality#CUMULATIVE} format.
   *
   * @since 1.15.0
   */
  public List<MetricData> getMetrics() {
    return new ArrayList<>(metricReader.get());
  }

  /**
   * Returns all the exported {@link LogRecordData} so far.
   *
   * @since 1.32.0
   */
  public List<LogRecordData> getLogRecords() {
    return new ArrayList<>(logRecordExporter.getFinishedLogRecordItems());
  }

  /**
   * Returns a {@link TracesAssert} for asserting on the currently exported traces. This method
   * requires AssertJ to be on the classpath.
   */
  public TracesAssert assertTraces() {
    return assertThat(spanExporter.getFinishedSpanItems());
  }

  /**
   * Clears the collected exported {@link SpanData}. Consider making your test smaller instead of
   * manually clearing state using this method.
   */
  public void clearSpans() {
    spanExporter.reset();
  }

  /**
   * Clears all registered metric instruments, such that {@link #getMetrics()} is empty.
   *
   * @since 1.15.0
   */
  public void clearMetrics() {
    SdkMeterProviderUtil.resetForTest(openTelemetry.getSdkMeterProvider());
  }

  /**
   * Clears the collected exported {@link LogRecordData}. Consider making your test smaller instead
   * of manually clearing state using this method.
   *
   * @since 1.32.0
   */
  public void clearLogRecords() {
    logRecordExporter.reset();
  }
}
