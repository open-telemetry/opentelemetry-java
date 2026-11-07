/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.equalTo;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.satisfies;

import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.internal.SemConvConstants;
import io.opentelemetry.sdk.metrics.data.HistogramPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricProducer;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricExporter;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class SdkMeterProviderMetricsTest {
  @Test
  void componentNamesAreUniqueAndStable() {
    InMemoryMetricReader observer = InMemoryMetricReader.create();
    try (SdkMeterProvider provider =
        SdkMeterProvider.builder()
            .registerMetricReader(PeriodicMetricReader.create(InMemoryMetricExporter.create()))
            .registerMetricReader(PeriodicMetricReader.create(InMemoryMetricExporter.create()))
            .registerMetricReader(observer)
            .build()) {
      List<String> previousNames = null;
      for (int count = 1; count <= 2; count++) {
        assertThat(provider.forceFlush().join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
        MetricData metric =
            observer.collectAllMetrics().stream()
                .filter(m -> m.getName().equals("otel.sdk.metric_reader.collection.duration"))
                .findFirst()
                .get();
        assertThat(metric.getHistogramData().getPoints()).hasSize(2);
        List<String> names =
            metric.getHistogramData().getPoints().stream()
                .map(point -> point.getAttributes().get(SemConvConstants.OTEL_COMPONENT_NAME))
                .collect(Collectors.toList());
        assertThat(names).doesNotHaveDuplicates().doesNotContainNull();
        if (previousNames != null) {
          assertThat(names).containsExactlyInAnyOrderElementsOf(previousNames);
        }
        previousNames = names;
        for (HistogramPointData point : metric.getHistogramData().getPoints()) {
          assertThat(point.getCount()).isEqualTo(count);
        }
      }
    }
  }

  @Test
  void simple() {
    InMemoryMetricExporter metricExporter = InMemoryMetricExporter.create();
    try (SdkMeterProvider meterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(PeriodicMetricReader.create(metricExporter))
            .build()) {
      Meter meter = meterProvider.get("test");

      LongCounter counter = meter.counterBuilder("counter").build();

      counter.add(1);

      meterProvider.forceFlush().join(10, TimeUnit.SECONDS);
      metricExporter.reset();
      // Export again to export the metric reader's metric.
      meterProvider.forceFlush().join(10, TimeUnit.SECONDS);

      List<MetricData> metrics = metricExporter.getFinishedMetricItems();
      assertThat(metrics)
          .satisfiesExactlyInAnyOrder(
              m -> assertThat(m).hasName("counter"),
              m -> {
                assertThat(m)
                    .hasName("otel.sdk.metric_reader.collection.duration")
                    .hasHistogramSatisfying(
                        h ->
                            h.hasPointsSatisfying(
                                p ->
                                    p.hasCount(1)
                                        .hasAttributesSatisfyingExactly(
                                            equalTo(
                                                SemConvConstants.OTEL_COMPONENT_TYPE,
                                                "periodic_metric_reader"),
                                            satisfies(
                                                SemConvConstants.OTEL_COMPONENT_NAME,
                                                name ->
                                                    name.matches(
                                                        "periodic_metric_reader/[0-9]+")))));
              });
    }
  }

  @Test
  @SuppressLogger(PeriodicMetricReader.class)
  void collectionFailureIsRecordedWithErrorType() {
    InMemoryMetricExporter metricExporter = InMemoryMetricExporter.create();
    AtomicBoolean shouldFail = new AtomicBoolean(true);
    // Fail the first collection, then succeed so the recorded self-observability sample can be
    // exported on the following flush.
    MetricProducer failingProducer =
        resource -> {
          if (shouldFail.getAndSet(false)) {
            throw new IllegalStateException("boom");
          }
          return Collections.emptyList();
        };
    try (SdkMeterProvider meterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(PeriodicMetricReader.create(metricExporter))
            .registerMetricProducer(failingProducer)
            .build()) {

      meterProvider.forceFlush().join(10, TimeUnit.SECONDS);
      metricExporter.reset();
      // Export again to export the metric reader's self-observability metric.
      meterProvider.forceFlush().join(10, TimeUnit.SECONDS);

      assertThat(metricExporter.getFinishedMetricItems())
          .anySatisfy(
              m ->
                  assertThat(m)
                      .hasName("otel.sdk.metric_reader.collection.duration")
                      .hasHistogramSatisfying(
                          h ->
                              h.hasPointsSatisfying(
                                  p ->
                                      p.hasAttributesSatisfying(
                                          equalTo(
                                              SemConvConstants.ERROR_TYPE,
                                              "java.lang.IllegalStateException")))));
    }
  }
}
