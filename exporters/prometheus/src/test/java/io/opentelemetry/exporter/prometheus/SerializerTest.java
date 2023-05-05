/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoubleExemplarData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoublePointData;
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SerializerTest {

  private static final AttributeKey<String> TYPE = stringKey("type");

  private static final MetricData MONOTONIC_CUMULATIVE_DOUBLE_SUM =
      ImmutableMetricData.createDoubleSum(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationScopeInfo.builder("full")
              .setVersion("version")
              .setAttributes(Attributes.of(stringKey("ks"), "vs"))
              .build(),
          "monotonic.cumulative.double.sum",
          "description",
          "1",
          ImmutableSumData.create(
              /* isMonotonic= */ true,
              AggregationTemporality.CUMULATIVE,
              Collections.singletonList(
                  ImmutableDoublePointData.create(
                      1633947011000000000L,
                      1633950672000000000L,
                      Attributes.of(TYPE, "mcds"),
                      5))));

  private static final MetricData MONOTONIC_CUMULATIVE_DOUBLE_SUM_WITH_SUFFIX_TOTAL =
      ImmutableMetricData.createDoubleSum(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationScopeInfo.builder("full")
              .setVersion("version")
              .setAttributes(Attributes.of(stringKey("ks"), "vs"))
              .build(),
          "monotonic.cumulative.double.sum.suffix.total",
          "description",
          "1",
          ImmutableSumData.create(
              /* isMonotonic= */ true,
              AggregationTemporality.CUMULATIVE,
              Collections.singletonList(
                  ImmutableDoublePointData.create(
                      1633947011000000000L,
                      1633950672000000000L,
                      Attributes.of(TYPE, "mcds"),
                      5))));
  private static final MetricData NON_MONOTONIC_CUMULATIVE_DOUBLE_SUM =
      ImmutableMetricData.createDoubleSum(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationScopeInfo.builder("full")
              .setVersion("version")
              .setAttributes(Attributes.of(stringKey("ks"), "vs"))
              .build(),
          "non.monotonic.cumulative.double.sum",
          "description",
          "1",
          ImmutableSumData.create(
              /* isMonotonic= */ false,
              AggregationTemporality.CUMULATIVE,
              Collections.singletonList(
                  ImmutableDoublePointData.create(
                      1633947011000000000L,
                      1633950672000000000L,
                      Attributes.of(TYPE, "nmcds"),
                      5))));
  private static final MetricData DELTA_DOUBLE_SUM =
      ImmutableMetricData.createDoubleSum(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationScopeInfo.builder("full")
              .setVersion("version")
              .setAttributes(Attributes.of(stringKey("ks"), "vs"))
              .build(),
          "delta.double.sum",
          "unused",
          "1",
          ImmutableSumData.create(
              /* isMonotonic= */ true,
              AggregationTemporality.DELTA,
              Collections.singletonList(
                  ImmutableDoublePointData.create(
                      1633947011000000000L,
                      1633950672000000000L,
                      Attributes.of(TYPE, "mdds"),
                      5))));
  private static final MetricData MONOTONIC_CUMULATIVE_LONG_SUM =
      ImmutableMetricData.createLongSum(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationScopeInfo.builder("full")
              .setVersion("version")
              .setAttributes(Attributes.of(stringKey("ks"), "vs"))
              .build(),
          "monotonic.cumulative.long.sum",
          "unused",
          "1",
          ImmutableSumData.create(
              /* isMonotonic= */ true,
              AggregationTemporality.CUMULATIVE,
              Collections.singletonList(
                  ImmutableLongPointData.create(
                      1633947011000000000L,
                      1633950672000000000L,
                      Attributes.of(TYPE, "mcls"),
                      5))));
  private static final MetricData NON_MONOTONIC_CUMULATIVE_LONG_SUM =
      ImmutableMetricData.createLongSum(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationScopeInfo.builder("full")
              .setVersion("version")
              .setAttributes(Attributes.of(stringKey("ks"), "vs"))
              .build(),
          "non.monotonic.cumulative.long_sum",
          "unused",
          "1",
          ImmutableSumData.create(
              /* isMonotonic= */ false,
              AggregationTemporality.CUMULATIVE,
              Collections.singletonList(
                  ImmutableLongPointData.create(
                      1633947011000000000L,
                      1633950672000000000L,
                      Attributes.of(TYPE, "nmcls"),
                      5))));
  private static final MetricData DELTA_LONG_SUM =
      ImmutableMetricData.createLongSum(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationScopeInfo.builder("full")
              .setVersion("version")
              .setAttributes(Attributes.of(stringKey("ks"), "vs"))
              .build(),
          "delta.long.sum",
          "unused",
          "1",
          ImmutableSumData.create(
              /* isMonotonic= */ true,
              AggregationTemporality.DELTA,
              Collections.singletonList(
                  ImmutableLongPointData.create(
                      1633947011000000000L,
                      1633950672000000000L,
                      Attributes.of(TYPE, "mdls"),
                      5))));

  private static final MetricData DOUBLE_GAUGE =
      ImmutableMetricData.createDoubleGauge(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationScopeInfo.builder("full")
              .setVersion("version")
              .setAttributes(Attributes.of(stringKey("ks"), "vs"))
              .build(),
          "double.gauge",
          "unused",
          "1",
          ImmutableGaugeData.create(
              Collections.singletonList(
                  ImmutableDoublePointData.create(
                      1633947011000000000L, 1633950672000000000L, Attributes.of(TYPE, "dg"), 5))));
  private static final MetricData LONG_GAUGE =
      ImmutableMetricData.createLongGauge(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationScopeInfo.builder("full")
              .setVersion("version")
              .setAttributes(Attributes.of(stringKey("ks"), "vs"))
              .build(),
          "long.gauge",
          "unused",
          "1",
          ImmutableGaugeData.create(
              Collections.singletonList(
                  ImmutableLongPointData.create(
                      1633947011000000000L, 1633950672000000000L, Attributes.of(TYPE, "lg"), 5))));
  private static final MetricData SUMMARY =
      ImmutableMetricData.createDoubleSummary(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationScopeInfo.builder("full")
              .setVersion("version")
              .setAttributes(Attributes.of(stringKey("ks"), "vs"))
              .build(),
          "summary",
          "unused",
          "1",
          ImmutableSummaryData.create(
              Collections.singletonList(
                  ImmutableSummaryPointData.create(
                      1633947011000000000L,
                      1633950672000000000L,
                      Attributes.of(TYPE, "s"),
                      5,
                      7,
                      Arrays.asList(
                          ImmutableValueAtQuantile.create(0.9, 0.1),
                          ImmutableValueAtQuantile.create(0.99, 0.3))))));
  private static final MetricData DELTA_HISTOGRAM =
      ImmutableMetricData.createDoubleHistogram(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationScopeInfo.builder("full")
              .setVersion("version")
              .setAttributes(Attributes.of(stringKey("ks"), "vs"))
              .build(),
          "delta.histogram",
          "unused",
          "1",
          ImmutableHistogramData.create(
              AggregationTemporality.DELTA,
              Collections.singletonList(
                  ImmutableHistogramPointData.create(
                      1633947011000000000L,
                      1633950672000000000L,
                      Attributes.empty(),
                      1.0,
                      /* hasMin= */ false,
                      0,
                      /* hasMax= */ false,
                      0,
                      Collections.emptyList(),
                      Collections.singletonList(2L),
                      Collections.emptyList()))));
  private static final MetricData CUMULATIVE_HISTOGRAM_NO_ATTRIBUTES =
      ImmutableMetricData.createDoubleHistogram(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationScopeInfo.builder("full")
              .setVersion("version")
              .setAttributes(Attributes.of(stringKey("ks"), "vs"))
              .build(),
          "cumulative.histogram.no.attributes",
          "unused",
          "1",
          ImmutableHistogramData.create(
              AggregationTemporality.CUMULATIVE,
              Collections.singletonList(
                  ImmutableHistogramPointData.create(
                      1633947011000000000L,
                      1633950672000000000L,
                      Attributes.empty(),
                      1.0,
                      /* hasMin= */ false,
                      0,
                      /* hasMax= */ false,
                      0,
                      Collections.emptyList(),
                      Collections.singletonList(2L),
                      Collections.singletonList(
                          ImmutableDoubleExemplarData.create(
                              Attributes.empty(),
                              TimeUnit.MILLISECONDS.toNanos(1L),
                              SpanContext.create(
                                  "00000000000000000000000000000001",
                                  "0000000000000002",
                                  TraceFlags.getDefault(),
                                  TraceState.getDefault()),
                              /* value= */ 4))))));
  private static final MetricData CUMULATIVE_HISTOGRAM_SINGLE_ATTRIBUTE =
      ImmutableMetricData.createDoubleHistogram(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationScopeInfo.builder("full")
              .setVersion("version")
              .setAttributes(Attributes.of(stringKey("ks"), "vs"))
              .build(),
          "cumulative.histogram.single.attribute",
          "unused",
          "1",
          ImmutableHistogramData.create(
              AggregationTemporality.CUMULATIVE,
              Collections.singletonList(
                  ImmutableHistogramPointData.create(
                      1633947011000000000L,
                      1633950672000000000L,
                      Attributes.of(TYPE, "hs"),
                      1.0,
                      /* hasMin= */ false,
                      0,
                      /* hasMax= */ false,
                      0,
                      Collections.emptyList(),
                      Collections.singletonList(2L),
                      Collections.singletonList(
                          ImmutableDoubleExemplarData.create(
                              Attributes.empty(),
                              TimeUnit.MILLISECONDS.toNanos(1L),
                              SpanContext.create(
                                  "00000000000000000000000000000001",
                                  "0000000000000002",
                                  TraceFlags.getDefault(),
                                  TraceState.getDefault()),
                              /* value= */ 4))))));
  private static final MetricData DOUBLE_GAUGE_NO_ATTRIBUTES =
      ImmutableMetricData.createDoubleGauge(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationScopeInfo.builder("full")
              .setVersion("version")
              .setAttributes(Attributes.of(stringKey("ks"), "vs"))
              .build(),
          "double.gauge.no.attributes",
          "unused",
          "1",
          ImmutableGaugeData.create(
              Collections.singletonList(
                  ImmutableDoublePointData.create(
                      1633947011000000000L, 1633950672000000000L, Attributes.empty(), 7))));
  private static final MetricData DOUBLE_GAUGE_MULTIPLE_ATTRIBUTES =
      ImmutableMetricData.createDoubleGauge(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationScopeInfo.builder("full")
              .setVersion("version")
              .setAttributes(Attributes.of(stringKey("ks"), "vs"))
              .build(),
          "double.gauge.multiple.attributes",
          "unused",
          "1",
          ImmutableGaugeData.create(
              Collections.singletonList(
                  ImmutableDoublePointData.create(
                      1633947011000000000L,
                      1633950672000000000L,
                      Attributes.of(TYPE, "dgma", stringKey("animal"), "bear"),
                      8))));

  @Test
  void prometheus004() {
    // Same output as prometheus client library except for these changes which are compatible with
    // Prometheus
    // TYPE / HELP line order reversed
    // Attributes do not end in trailing comma
    assertThat(
            serialize004(
                MONOTONIC_CUMULATIVE_DOUBLE_SUM,
                MONOTONIC_CUMULATIVE_DOUBLE_SUM_WITH_SUFFIX_TOTAL,
                NON_MONOTONIC_CUMULATIVE_DOUBLE_SUM,
                DELTA_DOUBLE_SUM, // Deltas are dropped
                MONOTONIC_CUMULATIVE_LONG_SUM,
                NON_MONOTONIC_CUMULATIVE_LONG_SUM,
                DELTA_LONG_SUM, // Deltas are dropped
                DOUBLE_GAUGE,
                LONG_GAUGE,
                SUMMARY,
                DELTA_HISTOGRAM, // Deltas are dropped
                CUMULATIVE_HISTOGRAM_NO_ATTRIBUTES,
                CUMULATIVE_HISTOGRAM_SINGLE_ATTRIBUTE,
                DOUBLE_GAUGE_NO_ATTRIBUTES,
                DOUBLE_GAUGE_MULTIPLE_ATTRIBUTES))
        .isEqualTo(
            "# TYPE target info\n"
                + "# HELP target Target metadata\n"
                + "target_info{kr=\"vr\"} 1\n"
                + "# TYPE otel_scope_info info\n"
                + "# HELP otel_scope_info Scope metadata\n"
                + "otel_scope_info{otel_scope_name=\"full\",otel_scope_version=\"version\",ks=\"vs\"} 1\n"
                + "# TYPE monotonic_cumulative_double_sum_total counter\n"
                + "# HELP monotonic_cumulative_double_sum_total description\n"
                + "monotonic_cumulative_double_sum_total{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"mcds\"} 5.0 1633950672000\n"
                + "# TYPE monotonic_cumulative_double_sum_suffix_total counter\n"
                + "# HELP monotonic_cumulative_double_sum_suffix_total description\n"
                + "monotonic_cumulative_double_sum_suffix_total{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"mcds\"} 5.0 1633950672000\n"
                + "# TYPE non_monotonic_cumulative_double_sum gauge\n"
                + "# HELP non_monotonic_cumulative_double_sum description\n"
                + "non_monotonic_cumulative_double_sum_ratio{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"nmcds\"} 5.0 1633950672000\n"
                + "# TYPE monotonic_cumulative_long_sum_total counter\n"
                + "# HELP monotonic_cumulative_long_sum_total unused\n"
                + "monotonic_cumulative_long_sum_total{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"mcls\"} 5.0 1633950672000\n"
                + "# TYPE non_monotonic_cumulative_long_sum gauge\n"
                + "# HELP non_monotonic_cumulative_long_sum unused\n"
                + "non_monotonic_cumulative_long_sum_ratio{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"nmcls\"} 5.0 1633950672000\n"
                + "# TYPE double_gauge gauge\n"
                + "# HELP double_gauge unused\n"
                + "double_gauge_ratio{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"dg\"} 5.0 1633950672000\n"
                + "# TYPE long_gauge gauge\n"
                + "# HELP long_gauge unused\n"
                + "long_gauge_ratio{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"lg\"} 5.0 1633950672000\n"
                + "# TYPE summary summary\n"
                + "# HELP summary unused\n"
                + "summary_count{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"s\"} 5.0 1633950672000\n"
                + "summary_sum{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"s\"} 7.0 1633950672000\n"
                + "summary{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"s\",quantile=\"0.9\"} 0.1 1633950672000\n"
                + "summary{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"s\",quantile=\"0.99\"} 0.3 1633950672000\n"
                + "# TYPE cumulative_histogram_no_attributes histogram\n"
                + "# HELP cumulative_histogram_no_attributes unused\n"
                + "cumulative_histogram_no_attributes_count{otel_scope_name=\"full\",otel_scope_version=\"version\"} 2.0 1633950672000\n"
                + "cumulative_histogram_no_attributes_sum{otel_scope_name=\"full\",otel_scope_version=\"version\"} 1.0 1633950672000\n"
                + "cumulative_histogram_no_attributes_bucket{otel_scope_name=\"full\",otel_scope_version=\"version\",le=\"+Inf\"} 2.0 1633950672000\n"
                + "# TYPE cumulative_histogram_single_attribute histogram\n"
                + "# HELP cumulative_histogram_single_attribute unused\n"
                + "cumulative_histogram_single_attribute_count{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"hs\"} 2.0 1633950672000\n"
                + "cumulative_histogram_single_attribute_sum{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"hs\"} 1.0 1633950672000\n"
                + "cumulative_histogram_single_attribute_bucket{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"hs\",le=\"+Inf\"} 2.0 1633950672000\n"
                + "# TYPE double_gauge_no_attributes gauge\n"
                + "# HELP double_gauge_no_attributes unused\n"
                + "double_gauge_no_attributes_ratio{otel_scope_name=\"full\",otel_scope_version=\"version\"} 7.0 1633950672000\n"
                + "# TYPE double_gauge_multiple_attributes gauge\n"
                + "# HELP double_gauge_multiple_attributes unused\n"
                + "double_gauge_multiple_attributes_ratio{otel_scope_name=\"full\",otel_scope_version=\"version\",animal=\"bear\",type=\"dgma\"} 8.0 1633950672000\n");
  }

  @Test
  void openMetrics() {
    assertThat(
            serializeOpenMetrics(
                MONOTONIC_CUMULATIVE_DOUBLE_SUM,
                MONOTONIC_CUMULATIVE_DOUBLE_SUM_WITH_SUFFIX_TOTAL,
                NON_MONOTONIC_CUMULATIVE_DOUBLE_SUM,
                DELTA_DOUBLE_SUM, // Deltas are dropped
                MONOTONIC_CUMULATIVE_LONG_SUM,
                NON_MONOTONIC_CUMULATIVE_LONG_SUM,
                DELTA_LONG_SUM, // Deltas are dropped
                DOUBLE_GAUGE,
                LONG_GAUGE,
                SUMMARY,
                DELTA_HISTOGRAM, // Deltas are dropped
                CUMULATIVE_HISTOGRAM_NO_ATTRIBUTES,
                CUMULATIVE_HISTOGRAM_SINGLE_ATTRIBUTE,
                DOUBLE_GAUGE_NO_ATTRIBUTES,
                DOUBLE_GAUGE_MULTIPLE_ATTRIBUTES))
        .isEqualTo(
            "# TYPE target info\n"
                + "# HELP target Target metadata\n"
                + "target_info{kr=\"vr\"} 1\n"
                + "# TYPE otel_scope_info info\n"
                + "# HELP otel_scope_info Scope metadata\n"
                + "otel_scope_info{otel_scope_name=\"full\",otel_scope_version=\"version\",ks=\"vs\"} 1\n"
                + "# TYPE monotonic_cumulative_double_sum counter\n"
                + "# HELP monotonic_cumulative_double_sum description\n"
                + "monotonic_cumulative_double_sum_total{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"mcds\"} 5.0 1633950672.000\n"
                + "# TYPE monotonic_cumulative_double_sum_suffix_total counter\n"
                + "# HELP monotonic_cumulative_double_sum_suffix_total description\n"
                + "monotonic_cumulative_double_sum_suffix_total{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"mcds\"} 5.0 1633950672.000\n"
                + "# TYPE non_monotonic_cumulative_double_sum gauge\n"
                + "# HELP non_monotonic_cumulative_double_sum description\n"
                + "non_monotonic_cumulative_double_sum_ratio{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"nmcds\"} 5.0 1633950672.000\n"
                + "# TYPE monotonic_cumulative_long_sum counter\n"
                + "# HELP monotonic_cumulative_long_sum unused\n"
                + "monotonic_cumulative_long_sum_total{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"mcls\"} 5.0 1633950672.000\n"
                + "# TYPE non_monotonic_cumulative_long_sum gauge\n"
                + "# HELP non_monotonic_cumulative_long_sum unused\n"
                + "non_monotonic_cumulative_long_sum_ratio{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"nmcls\"} 5.0 1633950672.000\n"
                + "# TYPE double_gauge gauge\n"
                + "# HELP double_gauge unused\n"
                + "double_gauge_ratio{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"dg\"} 5.0 1633950672.000\n"
                + "# TYPE long_gauge gauge\n"
                + "# HELP long_gauge unused\n"
                + "long_gauge_ratio{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"lg\"} 5.0 1633950672.000\n"
                + "# TYPE summary summary\n"
                + "# HELP summary unused\n"
                + "summary_count{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"s\"} 5.0 1633950672.000\n"
                + "summary_sum{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"s\"} 7.0 1633950672.000\n"
                + "summary{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"s\",quantile=\"0.9\"} 0.1 1633950672.000\n"
                + "summary{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"s\",quantile=\"0.99\"} 0.3 1633950672.000\n"
                + "# TYPE cumulative_histogram_no_attributes histogram\n"
                + "# HELP cumulative_histogram_no_attributes unused\n"
                + "cumulative_histogram_no_attributes_count{otel_scope_name=\"full\",otel_scope_version=\"version\"} 2.0 1633950672.000\n"
                + "cumulative_histogram_no_attributes_sum{otel_scope_name=\"full\",otel_scope_version=\"version\"} 1.0 1633950672.000\n"
                + "cumulative_histogram_no_attributes_bucket{otel_scope_name=\"full\",otel_scope_version=\"version\",le=\"+Inf\"} 2.0 1633950672.000 # {span_id=\"0000000000000002\",trace_id=\"00000000000000000000000000000001\"} 4.0 0.001\n"
                + "# TYPE cumulative_histogram_single_attribute histogram\n"
                + "# HELP cumulative_histogram_single_attribute unused\n"
                + "cumulative_histogram_single_attribute_count{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"hs\"} 2.0 1633950672.000\n"
                + "cumulative_histogram_single_attribute_sum{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"hs\"} 1.0 1633950672.000\n"
                + "cumulative_histogram_single_attribute_bucket{otel_scope_name=\"full\",otel_scope_version=\"version\",type=\"hs\",le=\"+Inf\"} 2.0 1633950672.000 # {span_id=\"0000000000000002\",trace_id=\"00000000000000000000000000000001\"} 4.0 0.001\n"
                + "# TYPE double_gauge_no_attributes gauge\n"
                + "# HELP double_gauge_no_attributes unused\n"
                + "double_gauge_no_attributes_ratio{otel_scope_name=\"full\",otel_scope_version=\"version\"} 7.0 1633950672.000\n"
                + "# TYPE double_gauge_multiple_attributes gauge\n"
                + "# HELP double_gauge_multiple_attributes unused\n"
                + "double_gauge_multiple_attributes_ratio{otel_scope_name=\"full\",otel_scope_version=\"version\",animal=\"bear\",type=\"dgma\"} 8.0 1633950672.000\n"
                + "# EOF\n");
  }

  @ParameterizedTest
  @MethodSource("provideRawMetricDataForTest")
  void metricNameSerializationTest(MetricData metricData, String expectedSerializedName) {
    assertEquals(
        expectedSerializedName,
        Serializer.metricName(metricData, PrometheusType.forMetric(metricData)));
  }

  private static Stream<Arguments> provideRawMetricDataForTest() {
    return Stream.of(
        // special case for gauge
        Arguments.of(
            sampleMetricDataGenerator("sample", "1", PrometheusType.GAUGE), "sample_ratio"),
        // special case for gauge with drop - metric unit should match "1"
        Arguments.of(
            sampleMetricDataGenerator("sample", "1{dropped}", PrometheusType.GAUGE), "sample"),
        // Gauge without "1" as unit
        Arguments.of(
            sampleMetricDataGenerator("sample", "unit", PrometheusType.GAUGE), "sample_unit"),
        // special case with counter
        Arguments.of(
            sampleMetricDataGenerator("sample", "unit", PrometheusType.COUNTER),
            "sample_unit_total"),
        // special case unit "1", but no gauge - "1" is dropped
        Arguments.of(
            sampleMetricDataGenerator("sample", "1", PrometheusType.COUNTER), "sample_total"),
        // units expressed as numbers other than 1 are retained
        Arguments.of(
            sampleMetricDataGenerator("sample", "2", PrometheusType.COUNTER), "sample_2_total"),
        // metric name with unsupported characters
        Arguments.of(
            sampleMetricDataGenerator("s%%ple", "%/m", PrometheusType.SUMMARY),
            "s_ple_percent_per_minute"),
        // metric name with dropped portions
        Arguments.of(
            sampleMetricDataGenerator("s%%ple", "%/m", PrometheusType.SUMMARY),
            "s_ple_percent_per_minute"),
        // metric unit as a number other than 1 is not treated specially
        Arguments.of(
            sampleMetricDataGenerator("metric_name", "2", PrometheusType.SUMMARY), "metric_name_2"),
        // metric unit is not appended if the name already contains the unit
        Arguments.of(
            sampleMetricDataGenerator("metric_name_total", "total", PrometheusType.COUNTER),
            "metric_name_total"),
        // metric unit is not appended if the name already contains the unit - special case for
        // total with non-counter type
        Arguments.of(
            sampleMetricDataGenerator("metric_name_total", "total", PrometheusType.SUMMARY),
            "metric_name_total"),
        // metric unit not appended if present in metric name - special case for ratio
        Arguments.of(
            sampleMetricDataGenerator("metric_name_ratio", "1", PrometheusType.GAUGE),
            "metric_name_ratio"),
        // metric unit not appended if present in metric name - special case for ratio - unit not
        // gauge
        Arguments.of(
            sampleMetricDataGenerator("metric_name_ratio", "1", PrometheusType.SUMMARY),
            "metric_name_ratio"),
        // metric unit is not appended if the name already contains the unit - unit can be anywhere
        Arguments.of(
            sampleMetricDataGenerator("metric_hertz", "hertz", PrometheusType.GAUGE),
            "metric_hertz"),
        // metric unit is not appended if the name already contains the unit - applies to every unit
        Arguments.of(
            sampleMetricDataGenerator("metric_hertz_total", "hertz_total", PrometheusType.COUNTER),
            "metric_hertz_total"),
        // metric unit is not appended if the name already contains the unit - order matters
        Arguments.of(
            sampleMetricDataGenerator("metric_total_hertz", "hertz_total", PrometheusType.COUNTER),
            "metric_total_hertz_hertz_total"),
        // metric name cannot start with a number
        Arguments.of(
            sampleMetricDataGenerator("2_metric_name", "By", PrometheusType.SUMMARY),
            "_metric_name_bytes"));
  }

  private static String serialize004(MetricData... metrics) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      new Serializer.Prometheus004Serializer(unused -> true).write(Arrays.asList(metrics), bos);
      return bos.toString("UTF-8");
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static String serializeOpenMetrics(MetricData... metrics) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      new Serializer.OpenMetrics100Serializer(unused -> true).write(Arrays.asList(metrics), bos);
      return bos.toString("UTF-8");
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static MetricData sampleMetricDataGenerator(
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
