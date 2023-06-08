/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static io.opentelemetry.exporter.prometheus.TestConstants.DELTA_HISTOGRAM;
import static io.opentelemetry.exporter.prometheus.TestConstants.DOUBLE_GAUGE;
import static io.opentelemetry.exporter.prometheus.TestConstants.MONOTONIC_CUMULATIVE_LONG_SUM;
import static io.opentelemetry.exporter.prometheus.TestConstants.SUMMARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PrometheusMetricNameMapperTest {

  @Test
  void prometheusMetricNameMapperCaching() {
    AtomicInteger count = new AtomicInteger();
    BiFunction<MetricData, PrometheusType, String> delegate =
        (metricData, prometheusType) ->
            String.join(
                "_",
                metricData.getName(),
                prometheusType.name(),
                Integer.toString(count.incrementAndGet()));
    PrometheusMetricNameMapper mapper = new PrometheusMetricNameMapper(delegate);

    assertThat(mapper.apply(MONOTONIC_CUMULATIVE_LONG_SUM, PrometheusType.GAUGE))
        .isEqualTo("monotonic.cumulative.long.sum_GAUGE_1");
    assertThat(mapper.apply(MONOTONIC_CUMULATIVE_LONG_SUM, PrometheusType.GAUGE))
        .isEqualTo("monotonic.cumulative.long.sum_GAUGE_1");
    assertThat(mapper.apply(MONOTONIC_CUMULATIVE_LONG_SUM, PrometheusType.GAUGE))
        .isEqualTo("monotonic.cumulative.long.sum_GAUGE_1");
    assertThat(mapper.apply(MONOTONIC_CUMULATIVE_LONG_SUM, PrometheusType.GAUGE))
        .isEqualTo("monotonic.cumulative.long.sum_GAUGE_1");
    assertThat(mapper.apply(MONOTONIC_CUMULATIVE_LONG_SUM, PrometheusType.GAUGE))
        .isEqualTo("monotonic.cumulative.long.sum_GAUGE_1");
    assertThat(count).hasValue(1);
  }

  @ParameterizedTest
  @MethodSource("provideRawMetricDataForTest")
  void metricNameSerializationTest(MetricData metricData, String expectedSerializedName) {
    assertEquals(
        expectedSerializedName,
        PrometheusMetricNameMapper.INSTANCE.apply(
            metricData, PrometheusType.forMetric(metricData)));
  }

  private static Stream<Arguments> provideRawMetricDataForTest() {
    return Stream.of(
        // special case for gauge
        Arguments.of(createSampleMetricData("sample", "1", PrometheusType.GAUGE), "sample_ratio"),
        // special case for gauge with drop - metric unit should match "1" to be converted to
        // "ratio"
        Arguments.of(
            createSampleMetricData("sample", "1{dropped}", PrometheusType.GAUGE), "sample"),
        // Gauge without "1" as unit
        Arguments.of(createSampleMetricData("sample", "unit", PrometheusType.GAUGE), "sample_unit"),
        // special case with counter
        Arguments.of(
            createSampleMetricData("sample", "unit", PrometheusType.COUNTER), "sample_unit_total"),
        // special case unit "1", but no gauge - "1" is dropped
        Arguments.of(createSampleMetricData("sample", "1", PrometheusType.COUNTER), "sample_total"),
        // units expressed as numbers other than 1 are retained
        Arguments.of(
            createSampleMetricData("sample", "2", PrometheusType.COUNTER), "sample_2_total"),
        // metric name with unsupported characters
        Arguments.of(
            createSampleMetricData("s%%ple", "%/m", PrometheusType.SUMMARY),
            "s_ple_percent_per_minute"),
        // metric name with dropped portions
        Arguments.of(
            createSampleMetricData("s%%ple", "%/m", PrometheusType.SUMMARY),
            "s_ple_percent_per_minute"),
        // metric unit as a number other than 1 is not treated specially
        Arguments.of(
            createSampleMetricData("metric_name", "2", PrometheusType.SUMMARY), "metric_name_2"),
        // metric unit is not appended if the name already contains the unit
        Arguments.of(
            createSampleMetricData("metric_name_total", "total", PrometheusType.COUNTER),
            "metric_name_total"),
        // metric unit is not appended if the name already contains the unit - special case for
        // total with non-counter type
        Arguments.of(
            createSampleMetricData("metric_name_total", "total", PrometheusType.SUMMARY),
            "metric_name_total"),
        // metric unit not appended if present in metric name - special case for ratio
        Arguments.of(
            createSampleMetricData("metric_name_ratio", "1", PrometheusType.GAUGE),
            "metric_name_ratio"),
        // metric unit not appended if present in metric name - special case for ratio - unit not
        // gauge
        Arguments.of(
            createSampleMetricData("metric_name_ratio", "1", PrometheusType.SUMMARY),
            "metric_name_ratio"),
        // metric unit is not appended if the name already contains the unit - unit can be anywhere
        Arguments.of(
            createSampleMetricData("metric_hertz", "hertz", PrometheusType.GAUGE), "metric_hertz"),
        // metric unit is not appended if the name already contains the unit - applies to every unit
        Arguments.of(
            createSampleMetricData("metric_hertz_total", "hertz_total", PrometheusType.COUNTER),
            "metric_hertz_total"),
        // metric unit is not appended if the name already contains the unit - order matters
        Arguments.of(
            createSampleMetricData("metric_total_hertz", "hertz_total", PrometheusType.COUNTER),
            "metric_total_hertz_hertz_total"),
        // metric name cannot start with a number
        Arguments.of(
            createSampleMetricData("2_metric_name", "By", PrometheusType.SUMMARY),
            "_metric_name_bytes"));
  }

  static MetricData createSampleMetricData(
      String metricName, String metricUnit, PrometheusType prometheusType) {
    switch (prometheusType) {
      case SUMMARY:
        return ImmutableMetricData.createDoubleSummary(
            SUMMARY.getResource(),
            SUMMARY.getInstrumentationScopeInfo(),
            metricName,
            SUMMARY.getDescription(),
            metricUnit,
            SUMMARY.getSummaryData());
      case COUNTER:
        return ImmutableMetricData.createLongSum(
            MONOTONIC_CUMULATIVE_LONG_SUM.getResource(),
            MONOTONIC_CUMULATIVE_LONG_SUM.getInstrumentationScopeInfo(),
            metricName,
            MONOTONIC_CUMULATIVE_LONG_SUM.getDescription(),
            metricUnit,
            MONOTONIC_CUMULATIVE_LONG_SUM.getLongSumData());
      case GAUGE:
        return ImmutableMetricData.createDoubleGauge(
            DOUBLE_GAUGE.getResource(),
            DOUBLE_GAUGE.getInstrumentationScopeInfo(),
            metricName,
            DOUBLE_GAUGE.getDescription(),
            metricUnit,
            DOUBLE_GAUGE.getDoubleGaugeData());
      case HISTOGRAM:
        return ImmutableMetricData.createDoubleHistogram(
            DELTA_HISTOGRAM.getResource(),
            DELTA_HISTOGRAM.getInstrumentationScopeInfo(),
            metricName,
            DELTA_HISTOGRAM.getDescription(),
            metricUnit,
            DELTA_HISTOGRAM.getHistogramData());
    }
    throw new IllegalArgumentException();
  }
}
