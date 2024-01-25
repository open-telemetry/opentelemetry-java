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

  private final Otel2PrometheusConverter converter = new Otel2PrometheusConverter(
      true,
      /* addResourceAttributesAsLabels= */ false,
      /* allowedResourceAttributesRegexp= */ Pattern.compile(".*"));

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

  void resourceAttributesAddition()
  private static Stream<Arguments> metricMetadataArgs() {
    return Stream.of(
        // the unity unit "1" is translated to "ratio"
        Arguments.of(
            createSampleMetricData("sample", "1", MetricDataType.LONG_GAUGE),
            "sample_ratio gauge",
            "sample_ratio description",
            "sample_ratio"),
        // unit is appended to metric name
        Arguments.of(
            createSampleMetricData("sample", "unit", MetricDataType.LONG_GAUGE),
            "sample_unit gauge",
            "sample_unit description",
            "sample_unit"),
        // units in curly braces are dropped
        Arguments.of(
            createSampleMetricData("sample", "1{dropped}", MetricDataType.LONG_GAUGE),
            "sample_ratio gauge",
            "sample_ratio description",
            "sample_ratio"),
        // monotonic sums always include _total suffix
        Arguments.of(
            createSampleMetricData("sample", "unit", MetricDataType.LONG_SUM),
            "sample_unit_total counter",
            "sample_unit_total description",
            "sample_unit_total"),
        Arguments.of(
            createSampleMetricData("sample", "1", MetricDataType.LONG_SUM),
            "sample_ratio_total counter",
            "sample_ratio_total description",
            "sample_ratio_total"),
        // units expressed as numbers other than 1 are retained
        Arguments.of(
            createSampleMetricData("sample", "2", MetricDataType.LONG_SUM),
            "sample_2_total counter",
            "sample_2_total description",
            "sample_2_total"),
        Arguments.of(
            createSampleMetricData("metric_name", "2", MetricDataType.SUMMARY),
            "metric_name_2 summary",
            "metric_name_2 description",
            "metric_name_2_count"),
        // unsupported characters are translated to "_", repeated "_" are dropped
        Arguments.of(
            createSampleMetricData("s%%ple", "%/min", MetricDataType.SUMMARY),
            "s_ple_percent_per_minute summary",
            "s_ple_percent_per_minute description",
            "s_ple_percent_per_minute_count"),
        // metric unit is not appended if the name already contains the unit
        Arguments.of(
            createSampleMetricData("metric_name_total", "total", MetricDataType.LONG_SUM),
            "metric_name_total counter",
            "metric_name_total description",
            "metric_name_total"),
        // total suffix is stripped because total is a reserved suffixed for monotonic sums
        Arguments.of(
            createSampleMetricData("metric_name_total", "total", MetricDataType.SUMMARY),
            "metric_name summary",
            "metric_name description",
            "metric_name_count"),
        // if metric name ends with unit the unit is omitted
        Arguments.of(
            createSampleMetricData("metric_name_ratio", "1", MetricDataType.LONG_GAUGE),
            "metric_name_ratio gauge",
            "metric_name_ratio description",
            "metric_name_ratio"),
        Arguments.of(
            createSampleMetricData("metric_name_ratio", "1", MetricDataType.SUMMARY),
            "metric_name_ratio summary",
            "metric_name_ratio description",
            "metric_name_ratio_count"),
        Arguments.of(
            createSampleMetricData("metric_hertz", "hertz", MetricDataType.LONG_GAUGE),
            "metric_hertz gauge",
            "metric_hertz description",
            "metric_hertz"),
        Arguments.of(
            createSampleMetricData("metric_hertz", "hertz", MetricDataType.LONG_SUM),
            "metric_hertz_total counter",
            "metric_hertz_total description",
            "metric_hertz_total"),
        // if metric name ends with unit the unit is omitted - order matters
        Arguments.of(
            createSampleMetricData("metric_total_hertz", "hertz_total", MetricDataType.LONG_SUM),
            "metric_total_hertz_hertz_total counter",
            "metric_total_hertz_hertz_total description",
            "metric_total_hertz_hertz_total"),
        // metric name cannot start with a number
        Arguments.of(
            createSampleMetricData("2_metric_name", "By", MetricDataType.SUMMARY),
            "_metric_name_bytes summary",
            "_metric_name_bytes description",
            "_metric_name_bytes_count"));
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
