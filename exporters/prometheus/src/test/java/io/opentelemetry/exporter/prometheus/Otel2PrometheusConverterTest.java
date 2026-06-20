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
import static io.opentelemetry.api.common.AttributeKey.valueKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.KeyValue;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoubleExemplarData;
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
import io.opentelemetry.sdk.metrics.internal.data.ImmutableValueAtQuantile;
import io.opentelemetry.sdk.resources.Resource;
import io.prometheus.metrics.expositionformats.ExpositionFormats;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot.GaugeDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.InfoSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricMetadata;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.model.snapshots.NativeHistogramBuckets;
import io.prometheus.metrics.model.snapshots.SummarySnapshot;
import io.prometheus.metrics.model.snapshots.Unit;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

  private final Otel2PrometheusConverter converter =
      new Otel2PrometheusConverter(
          /* otelScopeLabelsEnabled= */ true,
          /* targetInfoMetricEnabled= */ true,
          TranslationStrategy.UNDERSCORE_ESCAPING_WITH_SUFFIXES,
          /* allowedResourceAttributesFilter= */ null);

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
        Arguments.argumentSet(
            "gauge unitless 1",
            createSampleMetricData("sample", "1", MetricDataType.LONG_GAUGE),
            "sample gauge",
            "sample description",
            "sample"),
        Arguments.argumentSet(
            "gauge with unit",
            createSampleMetricData("sample", "unit", MetricDataType.LONG_GAUGE),
            "sample_unit gauge",
            "sample_unit description",
            "sample_unit"),
        Arguments.argumentSet(
            "gauge curly braces dropped",
            createSampleMetricData("sample", "1{dropped}", MetricDataType.LONG_GAUGE),
            "sample gauge",
            "sample description",
            "sample"),
        Arguments.argumentSet(
            "sum with unit total suffix",
            createSampleMetricData("sample", "unit", MetricDataType.LONG_SUM),
            "sample_unit_total counter",
            "sample_unit_total description",
            "sample_unit_total"),
        Arguments.argumentSet(
            "sum unitless 1 total suffix",
            createSampleMetricData("sample", "1", MetricDataType.LONG_SUM),
            "sample_total counter",
            "sample_total description",
            "sample_total"),
        Arguments.argumentSet(
            "sum numeric unit 2",
            createSampleMetricData("sample", "2", MetricDataType.LONG_SUM),
            "sample_2_total counter",
            "sample_2_total description",
            "sample_2_total"),
        Arguments.argumentSet(
            "summary numeric unit 2",
            createSampleMetricData("metric_name", "2", MetricDataType.SUMMARY),
            "metric_name_2 summary",
            "metric_name_2 description",
            "metric_name_2_count"),
        // unsupported characters are translated to "_", repeated "_" are dropped
        Arguments.argumentSet(
            "summary special chars translated",
            createSampleMetricData("s%%ple", "%/min", MetricDataType.SUMMARY),
            "s_ple_percent_per_minute summary",
            "s_ple_percent_per_minute description",
            "s_ple_percent_per_minute_count"),
        Arguments.argumentSet(
            "sum name contains unit",
            createSampleMetricData("metric_name_total", "total", MetricDataType.LONG_SUM),
            "metric_name_total counter",
            "metric_name_total description",
            "metric_name_total"),
        // total suffix is stripped because total is a reserved suffixed for monotonic sums
        Arguments.argumentSet(
            "summary total suffix stripped",
            createSampleMetricData("metric_name_total", "total", MetricDataType.SUMMARY),
            "metric_name summary",
            "metric_name description",
            "metric_name_count"),
        Arguments.argumentSet(
            "gauge name ends with ratio unit omitted",
            createSampleMetricData("metric_name_ratio", "1", MetricDataType.LONG_GAUGE),
            "metric_name_ratio gauge",
            "metric_name_ratio description",
            "metric_name_ratio"),
        Arguments.argumentSet(
            "summary name ends with ratio unit omitted",
            createSampleMetricData("metric_name_ratio", "1", MetricDataType.SUMMARY),
            "metric_name_ratio summary",
            "metric_name_ratio description",
            "metric_name_ratio_count"),
        Arguments.argumentSet(
            "gauge name ends with hertz unit omitted",
            createSampleMetricData("metric_hertz", "hertz", MetricDataType.LONG_GAUGE),
            "metric_hertz gauge",
            "metric_hertz description",
            "metric_hertz"),
        Arguments.argumentSet(
            "sum name ends with hertz unit omitted",
            createSampleMetricData("metric_hertz", "hertz", MetricDataType.LONG_SUM),
            "metric_hertz_total counter",
            "metric_hertz_total description",
            "metric_hertz_total"),
        Arguments.argumentSet(
            "sum name total hertz order matters",
            createSampleMetricData("metric_total_hertz", "hertz_total", MetricDataType.LONG_SUM),
            "metric_total_hertz_total counter",
            "metric_total_hertz_total description",
            "metric_total_hertz_total"),
        Arguments.argumentSet(
            "summary name starts with number",
            createSampleMetricData("2_metric_name", "By", MetricDataType.SUMMARY),
            "_metric_name_bytes summary",
            "_metric_name_bytes description",
            "_metric_name_bytes_count"));
  }

  @ParameterizedTest
  @MethodSource("translationStrategyArgs")
  void metricMetadata_translationStrategy(
      TranslationStrategy translationStrategy,
      String expectedName,
      String expectedExpositionBaseName,
      String expectedOriginalName) {
    Otel2PrometheusConverter converter =
        new Otel2PrometheusConverter(
            /* otelScopeLabelsEnabled= */ true,
            /* targetInfoMetricEnabled= */ true,
            translationStrategy,
            /* allowedResourceAttributesFilter= */ null);

    MetricSnapshots snapshots =
        converter.convert(
            Collections.singletonList(
                createSampleMetricData("sample.name", "By", MetricDataType.LONG_SUM)));

    MetricMetadata metadata = snapshots.get(0).getMetadata();
    assertThat(metadata.getName()).isEqualTo(expectedName);
    assertThat(metadata.getExpositionBaseName()).isEqualTo(expectedExpositionBaseName);
    assertThat(metadata.getOriginalName()).isEqualTo(expectedOriginalName);
  }

  private static Stream<Arguments> translationStrategyArgs() {
    return Stream.of(
        Arguments.argumentSet(
            "underscore escaping with suffixes",
            TranslationStrategy.UNDERSCORE_ESCAPING_WITH_SUFFIXES,
            "sample_name_bytes",
            "sample_name_bytes",
            "sample_name_bytes"),
        Arguments.argumentSet(
            "underscore escaping without suffixes",
            TranslationStrategy.UNDERSCORE_ESCAPING_WITHOUT_SUFFIXES,
            "sample_name",
            "sample_name",
            "sample_name"),
        Arguments.argumentSet(
            "no utf8 escaping with suffixes",
            TranslationStrategy.NO_UTF8_ESCAPING_WITH_SUFFIXES,
            "sample.name_bytes",
            "sample.name_bytes_total",
            "sample.name_bytes"),
        Arguments.argumentSet(
            "no translation",
            TranslationStrategy.NO_TRANSLATION,
            "sample.name",
            "sample.name",
            "sample.name"));
  }

  @Test
  void metricMetadata_underscoreEscapingCollapsesRepeatedUnderscores() {
    MetricSnapshots snapshots =
        converter.convert(
            Collections.singletonList(
                createSampleMetricData("sample__name", "By", MetricDataType.LONG_SUM)));

    MetricMetadata metadata =
        snapshots.stream()
            .filter(snapshot -> snapshot instanceof CounterSnapshot)
            .findFirst()
            .orElseThrow(AssertionError::new)
            .getMetadata();
    assertThat(metadata.getName()).isEqualTo("sample_name_bytes");
    assertThat(metadata.getExpositionBaseName()).isEqualTo("sample_name_bytes");
    assertThat(metadata.getOriginalName()).isEqualTo("sample_name_bytes");
  }

  @ParameterizedTest
  @MethodSource("legacyLabelNameTranslationArgs")
  void labelNameTranslation_underscoreEscaping(String labelName, String expectedLabelName) {
    Labels labels =
        convertAttributeLabels(labelName, TranslationStrategy.UNDERSCORE_ESCAPING_WITH_SUFFIXES);

    assertThat(labels.size()).isEqualTo(1);
    assertThat(labels.getName(0)).isEqualTo(expectedLabelName);
    assertThat(labels.getValue(0)).isEqualTo("value");
  }

  private static Stream<Arguments> legacyLabelNameTranslationArgs() {
    return Stream.of(
        Arguments.argumentSet("colons", "label:with:colons", "label_with_colons"),
        Arguments.argumentSet(
            "capital letters", "LabelWithCapitalLetters", "LabelWithCapitalLetters"),
        Arguments.argumentSet(
            "special chars", "label!with&special$chars)", "label_with_special_chars_"),
        Arguments.argumentSet(
            "foreign characters",
            "label_with_foreign_characters_字符",
            "label_with_foreign_characters_"),
        Arguments.argumentSet("dots", "label.with.dots", "label_with_dots"),
        Arguments.argumentSet("leading digit", "123label", "key_123label"),
        Arguments.argumentSet(
            "leading underscore",
            "_label_starting_with_underscore",
            "_label_starting_with_underscore"),
        Arguments.argumentSet(
            "leading double underscore",
            "__label_starting_with_2underscores",
            "_label_starting_with_2underscores"),
        Arguments.argumentSet(
            "double underscores",
            "label__with__double__underscores",
            "label_with_double_underscores"),
        Arguments.argumentSet(
            "mixed special", "label.name__with&&special##chars", "label_name_with_special_chars"),
        // Prometheus Java rejects user labels starting with "__".
        Arguments.argumentSet(
            "reserved name", "__reserved__label__name__", "_reserved_label_name_"),
        Arguments.argumentSet(
            "trailing underscores", "trailing_underscores___", "trailing_underscores_"));
  }

  @Test
  void labelNameTranslation_legacyDropsMetricWithInvalidNormalizedName() {
    Otel2PrometheusConverter converter =
        new Otel2PrometheusConverter(
            /* otelScopeLabelsEnabled= */ false,
            /* targetInfoMetricEnabled= */ false,
            TranslationStrategy.UNDERSCORE_ESCAPING_WITH_SUFFIXES,
            /* allowedResourceAttributesFilter= */ null);

    MetricSnapshots snapshots =
        converter.convert(
            Collections.singletonList(
                createSampleMetricData(
                    "sample",
                    "1",
                    MetricDataType.LONG_SUM,
                    Attributes.of(stringKey("ようこそ"), "value"),
                    Resource.empty())));

    assertThat(snapshots).isEmpty();
  }

  @Test
  void metricNameTranslation_legacyDropsMetricWithInvalidNormalizedName() {
    Otel2PrometheusConverter converter =
        new Otel2PrometheusConverter(
            /* otelScopeLabelsEnabled= */ false,
            /* targetInfoMetricEnabled= */ false,
            TranslationStrategy.UNDERSCORE_ESCAPING_WITHOUT_SUFFIXES,
            /* allowedResourceAttributesFilter= */ null);

    MetricSnapshots snapshots =
        converter.convert(
            Collections.singletonList(
                createSampleMetricData("ようこそ", "1", MetricDataType.LONG_SUM)));

    assertThat(snapshots).isEmpty();
  }

  @Test
  void metricNameTranslation_legacyDropsMetricWithEmptyName() {
    Otel2PrometheusConverter converter =
        new Otel2PrometheusConverter(
            /* otelScopeLabelsEnabled= */ false,
            /* targetInfoMetricEnabled= */ false,
            TranslationStrategy.UNDERSCORE_ESCAPING_WITHOUT_SUFFIXES,
            /* allowedResourceAttributesFilter= */ null);

    MetricSnapshots snapshots =
        converter.convert(
            Collections.singletonList(createSampleMetricData("", "1", MetricDataType.LONG_SUM)));

    assertThat(snapshots).isEmpty();
  }

  @ParameterizedTest
  @MethodSource("nonEscapingTranslationStrategyArgs")
  void labelNameTranslation_nonEscapingStrategiesPreserveLabels(
      TranslationStrategy translationStrategy) {
    Labels labels = convertAttributeLabels("label:with:colons", translationStrategy);

    assertThat(labels.size()).isEqualTo(1);
    assertThat(labels.getName(0)).isEqualTo("label:with:colons");
    assertThat(labels.getValue(0)).isEqualTo("value");
  }

  private static Stream<Arguments> nonEscapingTranslationStrategyArgs() {
    return Stream.of(
        Arguments.argumentSet(
            "no utf8 escaping with suffixes", TranslationStrategy.NO_UTF8_ESCAPING_WITH_SUFFIXES),
        Arguments.argumentSet("no translation", TranslationStrategy.NO_TRANSLATION));
  }

  @Test
  void convertReturnsEmptySnapshotsForNullOrEmptyInput() {
    assertThat(converter.convert(null)).isEmpty();
    assertThat(converter.convert(Collections.emptyList())).isEmpty();
  }

  @Test
  void convertDoesNotAddTargetInfoWhenAllMetricsAreDropped() {
    MetricSnapshots snapshots =
        converter.convert(
            Collections.singletonList(
                createSampleMetricData(
                    "sample",
                    "1",
                    MetricDataType.LONG_SUM,
                    Attributes.of(stringKey("ようこそ"), "value"),
                    Resource.empty())));

    assertThat(snapshots).isEmpty();
  }

  @ParameterizedTest
  @MethodSource("deltaMetricDataArgs")
  void convertDropsDeltaMetrics(MetricData metricData) {
    assertThat(converter.convert(Collections.singletonList(metricData))).isEmpty();
  }

  private static Stream<Arguments> deltaMetricDataArgs() {
    return Stream.of(
        Arguments.argumentSet(
            "delta long sum", createDeltaMetricData("sample", "1", MetricDataType.LONG_SUM)),
        Arguments.argumentSet(
            "delta double sum", createDeltaMetricData("sample", "1", MetricDataType.DOUBLE_SUM)),
        Arguments.argumentSet(
            "delta histogram", createDeltaMetricData("sample", "1", MetricDataType.HISTOGRAM)),
        Arguments.argumentSet(
            "delta exponential histogram",
            createDeltaMetricData("sample", "1", MetricDataType.EXPONENTIAL_HISTOGRAM)));
  }

  @Test
  void nonMonotonicDoubleSumConvertsToGauge() {
    MetricSnapshots snapshots =
        converter.convert(
            Collections.singletonList(
                createMetricDataWithTemporality(
                    "sample",
                    "1",
                    MetricDataType.DOUBLE_SUM,
                    null,
                    null,
                    /* cumulative= */ true,
                    /* monotonic= */ false)));

    assertThat(snapshots).hasSize(2);
    assertThat(snapshots.stream().map(snapshot -> snapshot.getMetadata().getName()))
        .contains("sample", "target")
        .doesNotContain("sample_total");
  }

  @Test
  void convertDropsExponentialHistogramWithUnsupportedScale() {
    MetricData metricData = createExponentialHistogramMetricData(-5);

    assertThat(converter.convert(Collections.singletonList(metricData))).isEmpty();
  }

  @Test
  void summaryExportsConfiguredQuantiles() {
    MetricData metricData = createSummaryMetricDataWithQuantiles("summary");

    MetricSnapshots snapshots = converter.convert(Collections.singletonList(metricData));

    SummarySnapshot snapshot =
        (SummarySnapshot)
            snapshots.stream()
                .filter(SummarySnapshot.class::isInstance)
                .findFirst()
                .orElseThrow(AssertionError::new);
    assertThat(snapshot.getDataPoints().get(0).getQuantiles()).hasSize(2);
  }

  @Test
  void mergeSummaryMetricsWithSameName() {
    MetricSnapshots snapshots =
        converter.convert(
            Arrays.asList(
                createSummaryMetricDataWithQuantiles(
                    "summary", Attributes.of(stringKey("id"), "a")),
                createSummaryMetricDataWithQuantiles(
                    "summary", Attributes.of(stringKey("id"), "b"))));

    SummarySnapshot snapshot =
        (SummarySnapshot)
            snapshots.stream()
                .filter(SummarySnapshot.class::isInstance)
                .findFirst()
                .orElseThrow(AssertionError::new);
    assertThat(snapshot.getDataPoints()).hasSize(2);
  }

  @Test
  void mergeHistogramMetricsWithSameName() {
    MetricSnapshots snapshots =
        converter.convert(
            Arrays.asList(
                createSampleMetricData(
                    "histogram",
                    "1",
                    MetricDataType.HISTOGRAM,
                    Attributes.of(stringKey("id"), "a"),
                    null),
                createSampleMetricData(
                    "histogram",
                    "1",
                    MetricDataType.HISTOGRAM,
                    Attributes.of(stringKey("id"), "b"),
                    null)));

    HistogramSnapshot snapshot =
        (HistogramSnapshot)
            snapshots.stream()
                .filter(HistogramSnapshot.class::isInstance)
                .findFirst()
                .orElseThrow(AssertionError::new);
    assertThat(snapshot.getDataPoints()).hasSize(2);
  }

  @Test
  void mergeGaugeMetricsWithSameName() {
    MetricSnapshots snapshots =
        converter.convert(
            Arrays.asList(
                createSampleMetricData(
                    "gauge",
                    "1",
                    MetricDataType.LONG_GAUGE,
                    Attributes.of(stringKey("id"), "a"),
                    null),
                createSampleMetricData(
                    "gauge",
                    "1",
                    MetricDataType.LONG_GAUGE,
                    Attributes.of(stringKey("id"), "b"),
                    null)));

    GaugeSnapshot snapshot =
        (GaugeSnapshot)
            snapshots.stream()
                .filter(GaugeSnapshot.class::isInstance)
                .findFirst()
                .orElseThrow(AssertionError::new);
    assertThat(snapshot.getDataPoints()).hasSize(2);
  }

  private static Labels convertAttributeLabels(
      String labelName, TranslationStrategy translationStrategy) {
    Otel2PrometheusConverter converter =
        new Otel2PrometheusConverter(
            /* otelScopeLabelsEnabled= */ false,
            /* targetInfoMetricEnabled= */ false,
            translationStrategy,
            /* allowedResourceAttributesFilter= */ null);

    MetricSnapshots snapshots =
        converter.convert(
            Collections.singletonList(
                createSampleMetricData(
                    "sample",
                    "1",
                    MetricDataType.LONG_SUM,
                    Attributes.of(stringKey(labelName), "value"),
                    Resource.empty())));

    assertThat(snapshots).hasSize(1);
    return snapshots.get(0).getDataPoints().get(0).getLabels();
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
        new Otel2PrometheusConverter(
            /* otelScopeLabelsEnabled= */ true,
            /* targetInfoMetricEnabled= */ true,
            TranslationStrategy.UNDERSCORE_ESCAPING_WITH_SUFFIXES,
            allowedResourceAttributesFilter);

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
          Arguments.argumentSet(
              "resource attribute added " + metricDataType,
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
        Arguments.argumentSet(
            "resource attribute not duplicated",
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
        Arguments.argumentSet(
            "empty metric attributes",
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

    // Array-valued resource attribute is serialized as a JSON string, matching the point attribute
    // path
    arguments.add(
        Arguments.argumentSet(
            "array-valued resource attribute serialized as json",
            createSampleMetricData(
                "my.metric",
                "units",
                MetricDataType.LONG_SUM,
                Attributes.empty(),
                Resource.create(
                    Attributes.of(stringArrayKey("clusters"), Arrays.asList("a", "b")))),
            /* allowedResourceAttributesFilter= */ Predicates.startsWith("clu"),
            "my_metric_units",
            "clusters=\"[\\\"a\\\",\\\"b\\\"]\",otel_scope_foo=\"bar\",otel_scope_name=\"scope\",otel_scope_schema_url=\"schemaUrl\",otel_scope_version=\"version\""));

    return arguments.stream();
  }

  @Test
  void arrayValuedScopeAttributeSerializedAsJson() {
    // Array-valued scope attribute is serialized as a JSON string, matching the point attribute
    // path
    InstrumentationScopeInfo scope =
        InstrumentationScopeInfo.builder("scope")
            .setAttributes(Attributes.of(stringArrayKey("foo"), Arrays.asList("a", "b")))
            .build();
    MetricData metricData =
        ImmutableMetricData.createLongSum(
            Resource.getDefault(),
            scope,
            "sample",
            "description",
            "1",
            ImmutableSumData.create(
                /* isMonotonic= */ true,
                AggregationTemporality.CUMULATIVE,
                Collections.singletonList(
                    ImmutableLongPointData.create(0, 1, Attributes.empty(), 1L))));

    MetricSnapshots snapshots = converter.convert(Collections.singletonList(metricData));

    Optional<MetricSnapshot> metricSnapshot =
        snapshots.stream().filter(snapshot -> snapshot instanceof CounterSnapshot).findFirst();
    assertThat(metricSnapshot).isPresent();

    Labels labels = metricSnapshot.get().getDataPoints().get(0).getLabels();
    assertThat(labels.get("otel_scope_foo")).isEqualTo("[\"a\",\"b\"]");
  }

  @Test
  void metricNameCollisionTest_Issue6277() {
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
  void labelValueSerialization(Attributes attributes, String expectedValue) {
    MetricData metricData =
        createSampleMetricData("sample", "1", MetricDataType.LONG_SUM, attributes, null);

    MetricSnapshots snapshots = converter.convert(Collections.singletonList(metricData));

    Optional<MetricSnapshot> metricSnapshot =
        snapshots.stream().filter(snapshot -> snapshot instanceof CounterSnapshot).findFirst();
    assertThat(metricSnapshot).isPresent();

    Labels labels = metricSnapshot.get().getDataPoints().get(0).getLabels();
    String labelValue = labels.get("key");
    assertThat(labelValue).isEqualTo(expectedValue);
  }

  private static Stream<Arguments> labelValueSerializationArgs() {
    return Stream.of(
        Arguments.argumentSet(
            "string value", Attributes.of(stringKey("key"), "stringValue"), "stringValue"),
        Arguments.argumentSet("boolean value", Attributes.of(booleanKey("key"), true), "true"),
        Arguments.argumentSet(
            "long value", Attributes.of(longKey("key"), Long.MAX_VALUE), "9223372036854775807"),
        Arguments.argumentSet("double value", Attributes.of(doubleKey("key"), 0.12345), "0.12345"),
        Arguments.argumentSet(
            "string array value",
            Attributes.of(
                stringArrayKey("key"),
                Arrays.asList("stringValue1", "\"+\\\\\\+\b+\f+\n+\r+\t+" + (char) 0)),
            "[\"stringValue1\",\"\\\"+\\\\\\\\\\\\+\\b+\\f+\\n+\\r+\\t+\\u0000\"]"),
        Arguments.argumentSet(
            "boolean array value",
            Attributes.of(booleanArrayKey("key"), Arrays.asList(true, false)),
            "[true,false]"),
        Arguments.argumentSet(
            "long array value",
            Attributes.of(longArrayKey("key"), Arrays.asList(Long.MIN_VALUE, Long.MAX_VALUE)),
            "[-9223372036854775808,9223372036854775807]"),
        Arguments.argumentSet(
            "double array value",
            Attributes.of(doubleArrayKey("key"), Arrays.asList(Double.MIN_VALUE, Double.MAX_VALUE)),
            "[4.9E-324,1.7976931348623157E308]"),
        Arguments.argumentSet(
            "bytes value", Attributes.of(valueKey("key"), Value.of(new byte[] {1, 2, 3})), "AQID"),
        Arguments.argumentSet(
            "nested key-value value",
            Attributes.of(valueKey("key"), Value.of(KeyValue.of("nested", Value.of("value")))),
            "{\"nested\":\"value\"}"),
        Arguments.argumentSet(
            "list value",
            Attributes.of(valueKey("key"), Value.of(Value.of("string"), Value.of(123L))),
            "[\"string\",123]"),
        Arguments.argumentSet("empty value", Attributes.of(valueKey("key"), Value.empty()), ""));
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
    return createMetricDataWithTemporality(
        metricName,
        metricUnit,
        metricDataType,
        attributes,
        resource,
        /* cumulative= */ true,
        /* monotonic= */ true);
  }

  private static MetricData createDeltaMetricData(
      String metricName, String metricUnit, MetricDataType metricDataType) {
    return createMetricDataWithTemporality(
        metricName,
        metricUnit,
        metricDataType,
        null,
        null,
        /* cumulative= */ false,
        /* monotonic= */ true);
  }

  private static MetricData createSummaryMetricDataWithQuantiles(String metricName) {
    return createSummaryMetricDataWithQuantiles(metricName, Attributes.empty());
  }

  private static MetricData createSummaryMetricDataWithQuantiles(
      String metricName, Attributes attributes) {
    InstrumentationScopeInfo scope =
        InstrumentationScopeInfo.builder("scope")
            .setVersion("version")
            .setSchemaUrl("schemaUrl")
            .setAttributes(Attributes.of(stringKey("foo"), "bar"))
            .build();
    return ImmutableMetricData.createDoubleSummary(
        Resource.getDefault(),
        scope,
        metricName,
        "description",
        "1",
        ImmutableSummaryData.create(
            Collections.singletonList(
                ImmutableSummaryPointData.create(
                    0,
                    1,
                    attributes,
                    2,
                    3,
                    Arrays.asList(
                        ImmutableValueAtQuantile.create(0.5, 1.5),
                        ImmutableValueAtQuantile.create(0.9, 2.5))))));
  }

  private static MetricData createExponentialHistogramMetricData(int scale) {
    InstrumentationScopeInfo scope =
        InstrumentationScopeInfo.builder("scope")
            .setVersion("version")
            .setSchemaUrl("schemaUrl")
            .setAttributes(Attributes.of(stringKey("foo"), "bar"))
            .build();
    return ImmutableMetricData.createExponentialHistogram(
        Resource.getDefault(),
        scope,
        "histogram",
        "description",
        "1",
        ImmutableExponentialHistogramData.create(
            AggregationTemporality.CUMULATIVE,
            Collections.singletonList(
                ImmutableExponentialHistogramPointData.create(
                    0,
                    1,
                    scale,
                    false,
                    1,
                    false,
                    1,
                    ImmutableExponentialHistogramBuckets.create(2, 5, Arrays.asList(1L, 2L)),
                    ImmutableExponentialHistogramBuckets.create(2, 5, Arrays.asList(1L, 2L)),
                    0,
                    10,
                    Attributes.empty(),
                    Collections.emptyList()))));
  }

  private static MetricData createMetricDataWithTemporality(
      String metricName,
      String metricUnit,
      MetricDataType metricDataType,
      @Nullable Attributes attributes,
      @Nullable Resource resource,
      boolean cumulative,
      boolean monotonic) {
    Attributes attributesToUse = attributes == null ? Attributes.empty() : attributes;
    Resource resourceToUse = resource == null ? Resource.getDefault() : resource;
    AggregationTemporality aggregationTemporality =
        cumulative ? AggregationTemporality.CUMULATIVE : AggregationTemporality.DELTA;

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
                monotonic,
                aggregationTemporality,
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
                monotonic,
                aggregationTemporality,
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
                aggregationTemporality,
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
                aggregationTemporality,
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
        new Otel2PrometheusConverter(
            /* otelScopeLabelsEnabled= */ true,
            /* targetInfoMetricEnabled= */ true,
            TranslationStrategy.UNDERSCORE_ESCAPING_WITH_SUFFIXES,
            /* allowedResourceAttributesFilter= */ countPredicate);

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

  @Test
  void mergeInfoSnapshotsWithSameName() throws Exception {
    InfoSnapshot merged =
        (InfoSnapshot)
            invokePrivateStatic(
                "merge",
                new Class<?>[] {MetricSnapshot.class, MetricSnapshot.class},
                makeInfoSnapshot("a"),
                makeInfoSnapshot("b"));

    assertThat(merged.getDataPoints()).hasSize(2);
  }

  @Test
  void mergeConflictingTypesReturnsNull() throws Exception {
    Object merged =
        invokePrivateStatic(
            "merge",
            new Class<?>[] {MetricSnapshot.class, MetricSnapshot.class},
            makeInfoSnapshot("a"),
            new GaugeSnapshot(
                MetricMetadata.builder().name("target").build(),
                Collections.singletonList(new GaugeDataPointSnapshot(1.0, Labels.EMPTY, null))));

    assertThat(merged).isNull();
  }

  @Test
  void mergeMetadataReturnsNullForDifferentUnits() throws Exception {
    Object merged =
        invokePrivateStatic(
            "mergeMetadata",
            new Class<?>[] {MetricMetadata.class, MetricMetadata.class},
            MetricMetadata.builder().name("sample").unit(new Unit("seconds")).build(),
            MetricMetadata.builder().name("sample").unit(new Unit("milliseconds")).build());

    assertThat(merged).isNull();
  }

  @Test
  void convertLegacyLabelNameRejectsEmptyName() {
    assertThatThrownBy(
            () -> invokePrivateStatic("convertLegacyLabelName", new Class<?>[] {String.class}, ""))
        .hasCauseInstanceOf(IllegalArgumentException.class)
        .hasRootCauseMessage("label name is empty");
  }

  @Test
  void stripReservedMetricSuffixesHandlesReservedNameOnly() throws Exception {
    assertThat(
            invokePrivateStatic(
                "stripReservedMetricSuffixes", new Class<?>[] {String.class}, "_total"))
        .isEqualTo("total");
  }

  @Test
  void validateNormalizedMetricNameRejectsEmptyName() {
    assertThatThrownBy(
            () ->
                invokePrivateStatic(
                    "validateNormalizedMetricName",
                    new Class<?>[] {String.class, String.class},
                    "orig",
                    ""))
        .hasCauseInstanceOf(IllegalArgumentException.class)
        .hasRootCauseMessage("normalization for metric \"orig\" resulted in empty name");
  }

  @Test
  void convertExponentialHistogramBucketsReturnsEmptyForNoBuckets() throws Exception {
    NativeHistogramBuckets buckets =
        (NativeHistogramBuckets)
            invokePrivateStatic(
                "convertExponentialHistogramBuckets",
                new Class<?>[] {ExponentialHistogramBuckets.class, int.class},
                ImmutableExponentialHistogramBuckets.create(0, 0, Collections.emptyList()),
                0);

    assertThat(buckets).isSameAs(NativeHistogramBuckets.EMPTY);
  }

  @Test
  void typeStringUsesLowerCaseClassName() throws Exception {
    assertThat(
            invokePrivateStatic(
                "typeString", new Class<?>[] {MetricSnapshot.class}, makeInfoSnapshot("a")))
        .isEqualTo("info");
  }

  @ParameterizedTest
  @MethodSource("exemplarLabelLimitArgs")
  void exemplarLabelLimit(
      SpanContext spanContext,
      Attributes filteredAttributes,
      String[] expectedPresentKeys,
      String[] expectedAbsentKeys) {
    ImmutableDoubleExemplarData exemplar =
        (ImmutableDoubleExemplarData)
            ImmutableDoubleExemplarData.create(filteredAttributes, 1000L, spanContext, 1.0);

    MetricData metricData =
        ImmutableMetricData.createDoubleGauge(
            Resource.getDefault(),
            InstrumentationScopeInfo.create("test"),
            "my.gauge",
            "desc",
            "unit",
            ImmutableGaugeData.create(
                Collections.singletonList(
                    ImmutableDoublePointData.create(
                        0, 1000, Attributes.empty(), 1.0, Collections.singletonList(exemplar)))));

    MetricSnapshots snapshots = converter.convert(Collections.singletonList(metricData));
    assertThat(snapshots).isNotNull();
    GaugeDataPointSnapshot point = (GaugeDataPointSnapshot) snapshots.get(0).getDataPoints().get(0);
    Labels exemplarLabels = point.getExemplar().getLabels();
    for (String key : expectedPresentKeys) {
      assertThat(exemplarLabels.get(key)).as("expected label '%s' to be present", key).isNotNull();
    }
    for (String key : expectedAbsentKeys) {
      assertThat(exemplarLabels.get(key)).as("expected label '%s' to be absent", key).isNull();
    }
  }

  private static Stream<Arguments> exemplarLabelLimitArgs() {
    SpanContext validSpanContext =
        SpanContext.create(
            "00000000000000000000000000000001",
            "0000000000000001",
            TraceFlags.getSampled(),
            TraceState.getDefault());

    char[] chars = new char[100];
    Arrays.fill(chars, 'x');
    String longValue100 = new String(chars);

    chars = new char[150];
    Arrays.fill(chars, 'x');
    String longValue150 = new String(chars);

    return Stream.of(
        Arguments.argumentSet(
            "withSpanContext withinLimit",
            validSpanContext,
            Attributes.of(stringKey("short_attr"), "val"),
            new String[] {"trace_id", "span_id", "short_attr"},
            new String[] {}),
        Arguments.argumentSet(
            "withSpanContext exceedingLimit",
            validSpanContext,
            Attributes.of(stringKey("long_attr"), longValue100),
            new String[] {"trace_id", "span_id"},
            new String[] {"long_attr"}),
        Arguments.argumentSet(
            "withoutSpanContext exceedingLimit",
            SpanContext.getInvalid(),
            Attributes.of(stringKey("long_attr"), longValue150),
            new String[] {},
            new String[] {"long_attr"}),
        Arguments.argumentSet(
            "withoutSpanContext withinLimit",
            SpanContext.getInvalid(),
            Attributes.of(stringKey("short_attr"), "val"),
            new String[] {"short_attr"},
            new String[] {}));
  }

  private static InfoSnapshot makeInfoSnapshot(String id) {
    return new InfoSnapshot(
        MetricMetadata.builder().name("target").build(),
        Collections.singletonList(
            new InfoSnapshot.InfoDataPointSnapshot(
                Labels.of(new String[] {"id"}, new String[] {id}))));
  }

  private static Object invokePrivateStatic(
      String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
    Method method = Otel2PrometheusConverter.class.getDeclaredMethod(methodName, parameterTypes);
    method.setAccessible(true);
    try {
      return method.invoke(null, args);
    } catch (InvocationTargetException e) {
      throw e;
    }
  }
}
