/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.exporter;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** Unit tests for {@link InMemoryMetricExporter}. */
class InMemoryMetricExporterTest {

  private final InMemoryMetricExporter exporter = InMemoryMetricExporter.create();

  private static MetricData generateFakeMetric() {
    long startNs = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
    long endNs = startNs + TimeUnit.MILLISECONDS.toNanos(900);
    return ImmutableMetricData.createLongSum(
        Resource.empty(),
        InstrumentationScopeInfo.empty(),
        "name",
        "description",
        "1",
        ImmutableSumData.create(
            /* isMonotonic= */ true,
            AggregationTemporality.CUMULATIVE,
            Collections.singletonList(
                ImmutableLongPointData.create(
                    startNs, endNs, Attributes.builder().put("k", "v").build(), 5))));
  }

  @ParameterizedTest
  @EnumSource(InstrumentType.class)
  void getAggregationTemporality(InstrumentType instrumentType) {
    assertThat(InMemoryMetricExporter.create().getAggregationTemporality(instrumentType))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(
            InMemoryMetricExporter.create(AggregationTemporality.DELTA)
                .getAggregationTemporality(instrumentType))
        .isEqualTo(AggregationTemporality.DELTA);
    assertThat(
            InMemoryMetricExporter.create(AggregationTemporality.CUMULATIVE)
                .getAggregationTemporality(instrumentType))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
  }

  @ParameterizedTest
  @EnumSource(InstrumentType.class)
  void getAggregationTemporality_deltaPreferred(InstrumentType instrumentType) {
    assertThat(
            InMemoryMetricExporter.create(AggregationTemporalitySelector.deltaPreferred())
                .getAggregationTemporality(instrumentType))
        .isEqualTo(
            instrumentType == InstrumentType.UP_DOWN_COUNTER
                    || instrumentType == InstrumentType.OBSERVABLE_UP_DOWN_COUNTER
                ? AggregationTemporality.CUMULATIVE
                : AggregationTemporality.DELTA);
  }

  @Test
  void create_nullSelector() {
    assertThatThrownBy(() -> InMemoryMetricExporter.create((AggregationTemporalitySelector) null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("aggregationTemporalitySelector");
  }

  @Test
  void export_deltaPreferred() {
    InMemoryMetricExporter deltaExporter =
        InMemoryMetricExporter.create(AggregationTemporalitySelector.deltaPreferred());
    TestClock clock = TestClock.create();
    PeriodicMetricReader reader =
        PeriodicMetricReader.builder(deltaExporter).setInterval(Duration.ofDays(1)).build();
    try (SdkMeterProvider meterProvider =
        SdkMeterProvider.builder().setClock(clock).registerMetricReader(reader).build()) {
      Meter meter = meterProvider.get("test");
      LongCounter counter = meter.counterBuilder("counter").build();
      DoubleHistogram histogram = meter.histogramBuilder("histogram").build();
      LongUpDownCounter upDownCounter = meter.upDownCounterBuilder("upDownCounter").build();
      AtomicLong observedUpDownCounter = new AtomicLong();
      meter
          .upDownCounterBuilder("observableUpDownCounter")
          .buildWithCallback(measurement -> measurement.record(observedUpDownCounter.get()));

      for (int value : new int[] {1, 2, 3}) {
        counter.add(value);
        histogram.record(value);
        upDownCounter.add(value == 3 ? -1 : 1);
        observedUpDownCounter.set(value == 3 ? 0 : 1);
        clock.advance(Duration.ofSeconds(1));

        assertThat(reader.forceFlush().join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
        assertThat(deltaExporter.getFinishedMetricItems())
            .filteredOn(metric -> metric.getInstrumentationScopeInfo().getName().equals("test"))
            .satisfiesExactlyInAnyOrder(
                metric ->
                    assertThat(metric)
                        .hasName("counter")
                        .hasLongSumSatisfying(
                            sum ->
                                sum.isDelta().hasPointsSatisfying(point -> point.hasValue(value))),
                metric ->
                    assertThat(metric)
                        .hasName("histogram")
                        .hasHistogramSatisfying(
                            data ->
                                data.isDelta()
                                    .hasPointsSatisfying(point -> point.hasCount(1).hasSum(value))),
                metric ->
                    assertThat(metric)
                        .hasName("upDownCounter")
                        .hasLongSumSatisfying(
                            sum ->
                                sum.isCumulative()
                                    .hasPointsSatisfying(
                                        point -> point.hasValue(value == 2 ? 2 : 1))),
                metric ->
                    assertThat(metric)
                        .hasName("observableUpDownCounter")
                        .hasLongSumSatisfying(
                            sum ->
                                sum.isCumulative()
                                    .hasPointsSatisfying(
                                        point -> point.hasValue(value == 3 ? 0 : 1))));
        deltaExporter.reset();
      }
    }
  }

  @Test
  void export_preservesMetricData() {
    InMemoryMetricExporter deltaExporter =
        InMemoryMetricExporter.create(AggregationTemporalitySelector.deltaPreferred());
    MetricData metric = generateFakeMetric();

    assertThat(deltaExporter.export(Collections.singletonList(metric)).isSuccess()).isTrue();
    assertThat(deltaExporter.getFinishedMetricItems()).singleElement().isSameAs(metric);
  }

  @Test
  void getFinishedMetricItems() {
    List<MetricData> metrics = new ArrayList<MetricData>();
    metrics.add(generateFakeMetric());
    metrics.add(generateFakeMetric());
    metrics.add(generateFakeMetric());

    assertThat(exporter.export(metrics).isSuccess()).isTrue();
    List<MetricData> metricItems = exporter.getFinishedMetricItems();
    assertThat(metricItems).isNotNull();
    assertThat(metricItems.size()).isEqualTo(3);
  }

  @Test
  void reset() {
    List<MetricData> metrics = new ArrayList<MetricData>();
    metrics.add(generateFakeMetric());
    metrics.add(generateFakeMetric());
    metrics.add(generateFakeMetric());

    assertThat(exporter.export(metrics).isSuccess()).isTrue();
    List<MetricData> metricItems = exporter.getFinishedMetricItems();
    assertThat(metricItems).isNotNull();
    assertThat(metricItems.size()).isEqualTo(3);
    exporter.reset();
    metricItems = exporter.getFinishedMetricItems();
    assertThat(metricItems).isNotNull();
    assertThat(metricItems.size()).isEqualTo(0);
  }

  @Test
  void shutdown() {
    List<MetricData> metrics = new ArrayList<MetricData>();
    metrics.add(generateFakeMetric());
    metrics.add(generateFakeMetric());
    metrics.add(generateFakeMetric());

    assertThat(exporter.export(metrics).isSuccess()).isTrue();
    exporter.shutdown();
    List<MetricData> metricItems = exporter.getFinishedMetricItems();
    assertThat(metricItems).isNotNull();
    assertThat(metricItems.size()).isEqualTo(0);
  }

  @Test
  void shutdown_ThenExport() {
    List<MetricData> metrics = new ArrayList<MetricData>();
    metrics.add(generateFakeMetric());
    metrics.add(generateFakeMetric());
    metrics.add(generateFakeMetric());

    assertThat(exporter.export(metrics).isSuccess()).isTrue();
    exporter.shutdown();
    assertThat(exporter.export(metrics).isSuccess()).isFalse();
  }

  @Test
  void flush() {
    assertThat(exporter.flush().isSuccess()).isTrue();
  }
}
