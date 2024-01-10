/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSummaryData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSummaryPointData;
import io.opentelemetry.sdk.resources.Resource;
import io.prometheus.metrics.expositionformats.ExpositionFormats;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class Otel2PrometheusConverterTest {

  private static final Pattern PATTERN =
      Pattern.compile(
          "# HELP (?<help>.*)\n# TYPE (?<type>.*)\n(?<metricName>.*)\\{otel_scope_name=\"scope\"}(.|\\n)*");

  private final Otel2PrometheusConverter converter = new Otel2PrometheusConverter(true);

  @ParameterizedTest
  @MethodSource("metricMetadataArgs")
  void metricMetadata(
      MetricData metricData, String expectedType, String expectedHelp, String expectedMetricName)
      throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    MetricSnapshots snapshots = converter.convert(Collections.singletonList(metricData));
    ExpositionFormats.init().getPrometheusTextFormatWriter().write(baos, snapshots);
    String expositionFormat = new String(baos.toByteArray(), StandardCharsets.UTF_8);

    // Uncomment to debug exposition format output
    // System.out.println(expositionFormat);

    Matcher matcher = PATTERN.matcher(expositionFormat);
    assertThat(matcher.matches()).isTrue();
    assertThat(matcher.group("help")).isEqualTo(expectedHelp);
    assertThat(matcher.group("type")).isEqualTo(expectedType);
    // Note: Summaries and histograms produce output which matches METRIC_NAME_PATTERN multiple
    // times. The pattern ends up matching against the first.
    assertThat(matcher.group("metricName")).isEqualTo(expectedMetricName);
  }

  private static Stream<Arguments> metricMetadataArgs() {
    // TODO (jack-berg): delete "Previously metricName was .." comments before merging, update
    // comments to reflect new logic
    return Stream.of(
        // special case for gauge
        // Previously metricName was "sample_ratio"
        Arguments.of(
            createSampleMetricData("sample", "1", MetricDataType.LONG_GAUGE),
            "sample_ratio gauge",
            "sample_ratio description",
            "sample_ratio"),
        // special case for gauge with drop - metric unit should match "1" to be converted to
        // "ratio"
        // Previously metricName was "sample"
        Arguments.of(
            createSampleMetricData("sample", "1{dropped}", MetricDataType.LONG_GAUGE),
            "sample_ratio gauge",
            "sample_ratio description",
            "sample_ratio"),
        // Gauge without "1" as unit
        // Previously metricName was "sample_unit"
        Arguments.of(
            createSampleMetricData("sample", "unit", MetricDataType.LONG_GAUGE),
            "sample_unit gauge",
            "sample_unit description",
            "sample_unit"),
        // special case with counter
        // Previously metricName was "sample_unit_total"
        Arguments.of(
            createSampleMetricData("sample", "unit", MetricDataType.LONG_SUM),
            "sample_unit_total counter",
            "sample_unit_total description",
            "sample_unit_total"),
        // special case unit "1", but not gauge - "1" is dropped
        // Previously metricName was "sample_total"
        Arguments.of(
            createSampleMetricData("sample", "1", MetricDataType.LONG_SUM),
            "sample_ratio_total counter",
            "sample_ratio_total description",
            "sample_ratio_total"),
        // units expressed as numbers other than 1 are retained
        // Previously metricName was "sample_2_total"
        Arguments.of(
            createSampleMetricData("sample", "2", MetricDataType.LONG_SUM),
            "sample_2_total counter",
            "sample_2_total description",
            "sample_2_total"),
        // metric name with unsupported characters
        // Previously metricName was "s_ple_percent_per_minute"
        Arguments.of(
            createSampleMetricData("s%%ple", "%/min", MetricDataType.SUMMARY),
            "s__ple_percent_per_minute summary",
            "s__ple_percent_per_minute description",
            "s__ple_percent_per_minute_count"),
        // metric name with dropped portions
        // Previously metricName was "s_ple_percent_per_minute"
        Arguments.of(
            createSampleMetricData("s%%ple", "%/min", MetricDataType.SUMMARY),
            "s__ple_percent_per_minute summary",
            "s__ple_percent_per_minute description",
            "s__ple_percent_per_minute_count"),
        // metric unit as a number other than 1 is not treated specially
        // Previously metricName was "metric_name_2"
        Arguments.of(
            createSampleMetricData("metric_name", "2", MetricDataType.SUMMARY),
            "metric_name_2 summary",
            "metric_name_2 description",
            "metric_name_2_count"),
        // metric unit is not appended if the name already contains the unit
        // Previously metricName was "metric_name_total"
        Arguments.of(
            createSampleMetricData("metric_name_total", "total", MetricDataType.LONG_SUM),
            "metric_name_total counter",
            "metric_name_total description",
            "metric_name_total"),
        // metric unit is not appended if the name already contains the unit - special case for
        // total with non-counter type
        // Previously metricName was "metric_name_total"
        Arguments.of(
            createSampleMetricData("metric_name_total", "total", MetricDataType.SUMMARY),
            "metric_name summary",
            "metric_name description",
            "metric_name_count"),
        // metric unit not appended if present in metric name - special case for ratio
        // Previously metricName was "metric_name_ratio"
        Arguments.of(
            createSampleMetricData("metric_name_ratio", "1", MetricDataType.LONG_GAUGE),
            "metric_name_ratio gauge",
            "metric_name_ratio description",
            "metric_name_ratio"),
        // metric unit not appended if present in metric name - special case for ratio - unit not
        // gauge
        // Previously metricName was "metric_name_ratio"
        Arguments.of(
            createSampleMetricData("metric_name_ratio", "1", MetricDataType.SUMMARY),
            "metric_name_ratio summary",
            "metric_name_ratio description",
            "metric_name_ratio_count"),
        // metric unit is not appended if the name already contains the unit - unit can be anywhere
        // Previously metricName was "metric_hertz"
        Arguments.of(
            createSampleMetricData("metric_hertz", "hertz", MetricDataType.LONG_GAUGE),
            "metric_hertz gauge",
            "metric_hertz description",
            "metric_hertz"),
        // metric unit is not appended if the name already contains the unit - applies to every unit
        // Previously metricName was "metric_hertz_total"
        Arguments.of(
            createSampleMetricData("metric_hertz", "hertz", MetricDataType.LONG_SUM),
            "metric_hertz_total counter",
            "metric_hertz_total description",
            "metric_hertz_total"),
        // metric unit is not appended if the name already contains the unit - order matters
        // Previously metricName was "metric_total_hertz_hertz_total_total"
        Arguments.of(
            createSampleMetricData("metric_total_hertz", "hertz_total", MetricDataType.LONG_SUM),
            "metric_total_hertz_hertz_total counter",
            "metric_total_hertz_hertz_total description",
            "metric_total_hertz_hertz_total"),
        // metric name cannot start with a number
        // Previously metricName was "_metric_name_bytes"
        Arguments.of(
            createSampleMetricData("2_metric_name", "By", MetricDataType.SUMMARY),
            "__metric_name_bytes summary",
            "__metric_name_bytes description",
            "__metric_name_bytes_count"));
  }

  static MetricData createSampleMetricData(
      String metricName, String metricUnit, MetricDataType metricDataType) {
    switch (metricDataType) {
      case SUMMARY:
        return ImmutableMetricData.createDoubleSummary(
            Resource.getDefault(),
            InstrumentationScopeInfo.create("scope"),
            metricName,
            "description",
            metricUnit,
            ImmutableSummaryData.create(
                Collections.singletonList(
                    ImmutableSummaryPointData.create(
                        0, 1, Attributes.empty(), 1, 1, Collections.emptyList()))));
      case LONG_SUM:
        return ImmutableMetricData.createLongSum(
            Resource.getDefault(),
            InstrumentationScopeInfo.create("scope"),
            metricName,
            "description",
            metricUnit,
            ImmutableSumData.create(
                true,
                AggregationTemporality.CUMULATIVE,
                Collections.singletonList(
                    ImmutableLongPointData.create(0, 1, Attributes.empty(), 1L))));
      case LONG_GAUGE:
        return ImmutableMetricData.createLongGauge(
            Resource.getDefault(),
            InstrumentationScopeInfo.create("scope"),
            metricName,
            "description",
            metricUnit,
            ImmutableGaugeData.create(
                Collections.singletonList(
                    ImmutableLongPointData.create(0, 1, Attributes.empty(), 1L))));
      case HISTOGRAM:
        return ImmutableMetricData.createDoubleHistogram(
            Resource.getDefault(),
            InstrumentationScopeInfo.create("scope"),
            metricName,
            "description",
            metricUnit,
            ImmutableHistogramData.create(
                AggregationTemporality.CUMULATIVE,
                Collections.singletonList(
                    ImmutableHistogramPointData.create(
                        0,
                        1,
                        Attributes.empty(),
                        1,
                        false,
                        -1,
                        false,
                        -1,
                        Collections.singletonList(1.0),
                        Arrays.asList(0L, 1L)))));
      default:
        throw new IllegalArgumentException("Unsupported metric data type: " + metricDataType);
    }
  }
}
