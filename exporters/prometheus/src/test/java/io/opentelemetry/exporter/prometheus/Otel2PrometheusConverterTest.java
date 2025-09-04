/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static io.opentelemetry.api.common.AttributeKey.booleanArrayKey;
import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleArrayKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longArrayKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.common.AttributeType;
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
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class Otel2PrometheusConverterTest {

  private static final Pattern PATTERN =
      Pattern.compile(
          "(.|\\n)*# HELP (?<help>.*)\n# TYPE (?<type>.*)\n(?<metricName>.*)\\{"
              + "otel_scope_foo=\"bar\",otel_scope_name=\"scope\","
              + "otel_scope_schema_url=\"schemaUrl\",otel_scope_version=\"version\"}(.|\\n)*");
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final Otel2PrometheusConverter converter =
      new Otel2PrometheusConverter(/* allowedResourceAttributesFilter= */ null);

  @ParameterizedTest
  @MethodSource("metricMetadataArgs")
  void metricMetadata(
      MetricData metricData, String expectedType, String expectedHelp, String expectedMetricName)
      throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    MetricSnapshots snapshots = converter.convert(Collections.singletonList(metricData));
    ExpositionFormats.init().getPrometheusTextFormatWriter().write(out, snapshots);
    String expositionFormat = new String(out.toByteArray(), StandardCharsets.UTF_8);

    assertThat(expositionFormat)
        .matchesSatisfying(
            PATTERN,
            matcher -> {
              assertThat(matcher.group("help")).isEqualTo(expectedHelp);
              assertThat(matcher.group("type")).isEqualTo(expectedType);
              // Note: Summaries and histograms produce output which matches METRIC_NAME_PATTERN
              // multiple
              // times. The pattern ends up matching against the first.
              assertThat(matcher.group("metricName")).isEqualTo(expectedMetricName);
            });
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
            "metric_total_hertz_total counter",
            "metric_total_hertz_total description",
            "metric_total_hertz_total"),
        // metric name cannot start with a number
        Arguments.of(
            createSampleMetricData("2_metric_name", "By", MetricDataType.SUMMARY),
            "_metric_name_bytes summary",
            "_metric_name_bytes description",
            "_metric_name_bytes_count"));
  }

  @ParameterizedTest
  @MethodSource("resourceAttributesAdditionArgs")
  void resourceAttributesAddition(
      MetricData metricData,
      @Nullable Predicate<String> allowedResourceAttributesFilter,
      String metricName,
      String expectedMetricLabels)
      throws IOException {

    Otel2PrometheusConverter converter =
        new Otel2PrometheusConverter(allowedResourceAttributesFilter);

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
              /* allowedResourceAttributesFilter= */ Predicates.startsWith("clu"),
              metricDataType == MetricDataType.SUMMARY
                      || metricDataType == MetricDataType.HISTOGRAM
                      || metricDataType == MetricDataType.EXPONENTIAL_HISTOGRAM
                  ? "my_metric_units_count"
                  : "my_metric_units",

              // "cluster" attribute is added (due to reg expr specified) and only it
              "cluster=\"mycluster\",foo1=\"bar1\",foo2=\"bar2\",otel_scope_foo=\"bar\",otel_scope_name=\"scope\",otel_scope_schema_url=\"schemaUrl\",otel_scope_version=\"version\""));
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
            /* allowedResourceAttributesFilter= */ Predicates.startsWith("clu"),
            "my_metric_units",

            // "cluster" attribute is present only once and the value is taken
            // from the metric attributes and not the resource attributes
            "cluster=\"mycluster2\",foo2=\"bar2\",otel_scope_foo=\"bar\",otel_scope_name=\"scope\",otel_scope_schema_url=\"schemaUrl\",otel_scope_version=\"version\""));

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
            /* allowedResourceAttributesFilter= */ Predicates.startsWith("clu"),
            "my_metric_units",
            "cluster=\"mycluster\",otel_scope_foo=\"bar\",otel_scope_name=\"scope\",otel_scope_schema_url=\"schemaUrl\",otel_scope_version=\"version\""));

    return arguments.stream();
  }

  @Test
  void prometheusNameCollisionTest_Issue6277() {
    // NOTE: Metrics with the same resolved prometheus name should merge. However,
    // Otel2PrometheusConverter is not responsible for merging individual series, so the merge will
    // fail if the two different metrics contain overlapping series. Users should deal with this by
    // adding a view that renames one of the two metrics such that the conflict does not occur.
    MetricData dotName =
        createSampleMetricData(
            "my.metric",
            "units",
            MetricDataType.LONG_SUM,
            Attributes.builder().put("key", "a").build(),
            Resource.create(Attributes.empty()));
    MetricData underscoreName =
        createSampleMetricData(
            "my_metric",
            "units",
            MetricDataType.LONG_SUM,
            Attributes.builder().put("key", "b").build(),
            Resource.create(Attributes.empty()));

    List<MetricData> metricData = new ArrayList<>();
    metricData.add(dotName);
    metricData.add(underscoreName);

    assertThatCode(() -> converter.convert(metricData)).doesNotThrowAnyException();
  }

  @ParameterizedTest
  @MethodSource("labelValueSerializationArgs")
  void labelValueSerialization(Attributes attributes) {
    MetricData metricData =
        createSampleMetricData("sample", "1", MetricDataType.LONG_SUM, attributes, null);

    MetricSnapshots snapshots = converter.convert(Collections.singletonList(metricData));

    Optional<MetricSnapshot> metricSnapshot =
        snapshots.stream().filter(snapshot -> snapshot instanceof CounterSnapshot).findFirst();
    assertThat(metricSnapshot).isPresent();

    Labels labels = metricSnapshot.get().getDataPoints().get(0).getLabels();
    attributes.forEach(
        (key, value) -> {
          String labelValue = labels.get(key.getKey());
          try {
            String expectedValue =
                key.getType() == AttributeType.STRING
                    ? (String) value
                    : OBJECT_MAPPER.writeValueAsString(value);
            assertThat(labelValue).isEqualTo(expectedValue);
          } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
          }
        });
  }

  private static Stream<Arguments> labelValueSerializationArgs() {
    return Stream.of(
        Arguments.of(Attributes.of(stringKey("key"), "stringValue")),
        Arguments.of(Attributes.of(booleanKey("key"), true)),
        Arguments.of(Attributes.of(longKey("key"), Long.MAX_VALUE)),
        Arguments.of(Attributes.of(doubleKey("key"), 0.12345)),
        Arguments.of(
            Attributes.of(
                stringArrayKey("key"),
                Arrays.asList("stringValue1", "\"+\\\\\\+\b+\f+\n+\r+\t+" + (char) 0))),
        Arguments.of(Attributes.of(booleanArrayKey("key"), Arrays.asList(true, false))),
        Arguments.of(
            Attributes.of(longArrayKey("key"), Arrays.asList(Long.MIN_VALUE, Long.MAX_VALUE))),
        Arguments.of(
            Attributes.of(
                doubleArrayKey("key"), Arrays.asList(Double.MIN_VALUE, Double.MAX_VALUE))));
  }

  static MetricData createSampleMetricData(
      String metricName, Resource resource, Attributes attributes) {
    return createSampleMetricData(
        metricName, "unit", MetricDataType.LONG_SUM, attributes, resource);
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

    InstrumentationScopeInfo scope =
        InstrumentationScopeInfo.builder("scope")
            .setVersion("version")
            .setSchemaUrl("schemaUrl")
            .setAttributes(Attributes.of(stringKey("foo"), "bar"))
            .build();
    switch (metricDataType) {
      case SUMMARY:
        return ImmutableMetricData.createDoubleSummary(
            resourceToUse,
            scope,
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
            scope,
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
            scope,
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
            scope,
            metricName,
            "description",
            metricUnit,
            ImmutableGaugeData.create(
                Collections.singletonList(
                    ImmutableLongPointData.create(0, 1, attributesToUse, 1L))));
      case DOUBLE_GAUGE:
        return ImmutableMetricData.createDoubleGauge(
            resourceToUse,
            scope,
            metricName,
            "description",
            metricUnit,
            ImmutableGaugeData.create(
                Collections.singletonList(
                    ImmutableDoublePointData.create(0, 1, attributesToUse, 1.0f))));
      case HISTOGRAM:
        return ImmutableMetricData.createDoubleHistogram(
            resourceToUse,
            scope,
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
            scope,
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

  @Test
  void validateCacheIsBounded() {
    AtomicInteger predicateCalledCount = new AtomicInteger();
    Predicate<String> countPredicate =
        s -> {
          predicateCalledCount.addAndGet(1);
          return true;
        };

    Otel2PrometheusConverter otel2PrometheusConverter =
        new Otel2PrometheusConverter(/* allowedResourceAttributesFilter= */ countPredicate);

    // Create 20 different metric data objects with 2 different resource attributes;
    Resource resource1 = Resource.builder().put("cluster", "cluster1").build();
    Resource resource2 = Resource.builder().put("cluster", "cluster2").build();

    List<MetricData> metricDataList = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      Attributes attributes = Attributes.of(stringKey("foo" + i), "bar" + i);
      metricDataList.add(createSampleMetricData("metric1", resource1, attributes));
      metricDataList.add(createSampleMetricData("metric2", resource2, attributes));
    }

    otel2PrometheusConverter.convert(metricDataList);

    // The predicate should be called only once for each resource attribute, and we have
    // 2 unique resources, each with 1 attribute, so 2.
    assertThat(predicateCalledCount.get()).isEqualTo(2);

    metricDataList.clear();

    // Create 20 different metric data objects with 20 different resource attributes;
    // This should cause the cache to be full, and then subsequently cleared
    for (int i = 0; i < Otel2PrometheusConverter.MAX_CACHE_SIZE; i++) {
      Attributes attributes = Attributes.of(stringKey("foo" + i), "bar" + i);
      Resource resource = Resource.builder().put("cluster", "different-cluster" + i).build();
      metricDataList.add(createSampleMetricData("metric1", resource, attributes));
      metricDataList.add(createSampleMetricData("metric2", resource, attributes));
    }
    otel2PrometheusConverter.convert(metricDataList);

    // Now lets put metrics with the same resource attributes as before
    metricDataList.clear();
    predicateCalledCount.set(0);
    for (int i = 0; i < 10; i++) {
      Attributes attributes = Attributes.of(stringKey("foo" + i), "bar" + i);
      metricDataList.add(createSampleMetricData("metric1", resource1, attributes));
      metricDataList.add(createSampleMetricData("metric2", resource2, attributes));
    }
    otel2PrometheusConverter.convert(metricDataList);

    // If the cache was unbounded, the predicate should be 0, since it's all in the cache,
    // but if the cache was cleared, it used the predicate for each resource, since it as if
    // it never saw those resources before.
    assertThat(predicateCalledCount.get()).isEqualTo(2);
  }
}
