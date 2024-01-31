/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoublePointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableExponentialHistogramData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableExponentialHistogramPointData;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class Otel2PrometheusConverterTest {

  private static final Pattern PATTERN =
      Pattern.compile(
          "# HELP (?<help>.*)\n# TYPE (?<type>.*)\n(?<metricName>.*)\\{otel_scope_name=\"scope\"}(.|\\n)*");

  private final Otel2PrometheusConverter converter =
      new Otel2PrometheusConverter(
          true,
          /* addResourceAttributesAsLabels= */ false,
          /* allowedResourceAttributesFilter= */ Predicates.ALLOW_ALL);

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

  @ParameterizedTest
  @MethodSource("resourceAttributesAdditionArgs")
  void resourceAttributesAddition(
      MetricData metricData,
      boolean addResourceAttributesAsLabels,
      Predicate<String> allowedResourceAttributesFilter,
      String metricName,
      String expectedMetricLabels)
      throws IOException {

    Otel2PrometheusConverter converter =
        new Otel2PrometheusConverter(
            true, addResourceAttributesAsLabels, allowedResourceAttributesFilter);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    MetricSnapshots snapshots = converter.convert(Collections.singletonList(metricData));
    ExpositionFormats.init().getPrometheusTextFormatWriter().write(out, snapshots);
    String expositionFormat = new String(out.toByteArray(), StandardCharsets.UTF_8);

    // extract the only metric line
    List<String> metricLines =
        Arrays.stream(expositionFormat.split("\n"))
            .filter(line -> line.startsWith(metricName))
            .collect(Collectors.toList());
    assertThat(metricLines).hasSize(1);
    String metricLine = metricLines.get(0);

    String metricLabels =
        metricLine.substring(metricLine.indexOf("{") + 1, metricLine.indexOf("}"));
    assertThat(metricLabels).isEqualTo(expectedMetricLabels);
  }

  private static Stream<Arguments> resourceAttributesAdditionArgs() {
    List<Arguments> arguments = new ArrayList<>();

    for (MetricDataType metricDataType : MetricDataType.values()) {
      // Check that resource attributes are added as labels, according to allowed pattern
      arguments.add(
          Arguments.of(
              createSampleMetricData(
                  "my.metric",
                  "units",
                  metricDataType,
                  Attributes.of(stringKey("foo1"), "bar1", stringKey("foo2"), "bar2"),
                  Resource.create(
                      Attributes.of(
                          stringKey("host"), "localhost", stringKey("cluster"), "mycluster"))),
              /* addResourceAttributesAsLabels= */ true,
              /* allowedResourceAttributesFilter= */ Predicates.startsWith("clu"),
              metricDataType == MetricDataType.SUMMARY
                      || metricDataType == MetricDataType.HISTOGRAM
                      || metricDataType == MetricDataType.EXPONENTIAL_HISTOGRAM
                  ? "my_metric_units_count"
                  : "my_metric_units",

              // "cluster" attribute is added (due to reg expr specified) and only it
              "cluster=\"mycluster\",foo1=\"bar1\",foo2=\"bar2\",otel_scope_name=\"scope\""));
    }

    // Resource attributes which also exists in the metric labels are not added twice
    arguments.add(
        Arguments.of(
            createSampleMetricData(
                "my.metric",
                "units",
                MetricDataType.LONG_SUM,
                Attributes.of(stringKey("cluster"), "mycluster2", stringKey("foo2"), "bar2"),
                Resource.create(
                    Attributes.of(
                        stringKey("host"), "localhost", stringKey("cluster"), "mycluster"))),
            /* addResourceAttributesAsLabels= */ true,
            /* allowedResourceAttributesRegexp= */ Predicates.startsWith("clu"),
            "my_metric_units",

            // "cluster" attribute is present only once and the value is taken
            // from the metric attributes and not the resource attributes
            "cluster=\"mycluster2\",foo2=\"bar2\",otel_scope_name=\"scope\""));

    // Empty attributes
    arguments.add(
        Arguments.of(
            createSampleMetricData(
                "my.metric",
                "units",
                MetricDataType.LONG_SUM,
                Attributes.empty(),
                Resource.create(
                    Attributes.of(
                        stringKey("host"), "localhost", stringKey("cluster"), "mycluster"))),
            /* addResourceAttributesAsLabels= */ true,
            /* allowedResourceAttributesRegexp= */ Predicates.startsWith("clu"),
            "my_metric_units",
            "cluster=\"mycluster\",otel_scope_name=\"scope\""));

    return arguments.stream();
  }

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
    return createSampleMetricData(metricName, metricUnit, metricDataType, null, null);
  }

  static MetricData createSampleMetricData(
      String metricName,
      String metricUnit,
      MetricDataType metricDataType,
      @Nullable Attributes attributes,
      @Nullable Resource resource) {
    Attributes attributesToUse = attributes == null ? Attributes.empty() : attributes;
    Resource resourceToUse = resource == null ? Resource.getDefault() : resource;

    switch (metricDataType) {
      case SUMMARY:
        return ImmutableMetricData.createDoubleSummary(
            resourceToUse,
            InstrumentationScopeInfo.create("scope"),
            metricName,
            "description",
            metricUnit,
            ImmutableSummaryData.create(
                Collections.singletonList(
                    ImmutableSummaryPointData.create(
                        0, 1, attributesToUse, 1, 1, Collections.emptyList()))));
      case LONG_SUM:
        return ImmutableMetricData.createLongSum(
            resourceToUse,
            InstrumentationScopeInfo.create("scope"),
            metricName,
            "description",
            metricUnit,
            ImmutableSumData.create(
                true,
                AggregationTemporality.CUMULATIVE,
                Collections.singletonList(
                    ImmutableLongPointData.create(0, 1, attributesToUse, 1L))));
      case DOUBLE_SUM:
        return ImmutableMetricData.createDoubleSum(
            resourceToUse,
            InstrumentationScopeInfo.create("scope"),
            metricName,
            "description",
            metricUnit,
            ImmutableSumData.create(
                true,
                AggregationTemporality.CUMULATIVE,
                Collections.singletonList(
                    ImmutableDoublePointData.create(0, 1, attributesToUse, 1.0))));
      case LONG_GAUGE:
        return ImmutableMetricData.createLongGauge(
            resourceToUse,
            InstrumentationScopeInfo.create("scope"),
            metricName,
            "description",
            metricUnit,
            ImmutableGaugeData.create(
                Collections.singletonList(
                    ImmutableLongPointData.create(0, 1, attributesToUse, 1L))));
      case DOUBLE_GAUGE:
        return ImmutableMetricData.createDoubleGauge(
            resourceToUse,
            InstrumentationScopeInfo.create("scope"),
            metricName,
            "description",
            metricUnit,
            ImmutableGaugeData.create(
                Collections.singletonList(
                    ImmutableDoublePointData.create(0, 1, attributesToUse, 1.0f))));
      case HISTOGRAM:
        return ImmutableMetricData.createDoubleHistogram(
            resourceToUse,
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
                        attributesToUse,
                        1,
                        false,
                        -1,
                        false,
                        -1,
                        Collections.singletonList(1.0),
                        Arrays.asList(0L, 1L)))));
      case EXPONENTIAL_HISTOGRAM:
        return ImmutableMetricData.createExponentialHistogram(
            resourceToUse,
            InstrumentationScopeInfo.create("scope"),
            metricName,
            "description",
            metricUnit,
            ImmutableExponentialHistogramData.create(
                AggregationTemporality.CUMULATIVE,
                Collections.singletonList(
                    ImmutableExponentialHistogramPointData.create(
                        0,
                        1,
                        5,
                        false,
                        1,
                        false,
                        1,
                        ImmutableExponentialHistogramBuckets.create(
                            2, 5, Arrays.asList(1L, 2L, 3L, 4L, 5L)),
                        ImmutableExponentialHistogramBuckets.create(
                            2, 5, Arrays.asList(1L, 2L, 3L, 4L, 5L)),
                        0,
                        10,
                        attributesToUse,
                        Collections.emptyList()))));
    }

    throw new IllegalArgumentException("Unsupported metric data type: " + metricDataType);
  }
}
