/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/** A helper class encapsulating immutable static data that can be shared across all the tests. */
class TestConstants {

  private TestConstants() {
    // Private constructor to prevent instantiation
  }

  private static final AttributeKey<String> TYPE = stringKey("type");

  static final MetricData MONOTONIC_CUMULATIVE_DOUBLE_SUM =
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

  static final MetricData MONOTONIC_CUMULATIVE_DOUBLE_SUM_WITH_SUFFIX_TOTAL =
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

  static final MetricData NON_MONOTONIC_CUMULATIVE_DOUBLE_SUM =
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

  static final MetricData DELTA_DOUBLE_SUM =
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

  static final MetricData MONOTONIC_CUMULATIVE_LONG_SUM =
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

  static final MetricData NON_MONOTONIC_CUMULATIVE_LONG_SUM =
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
  static final MetricData DELTA_LONG_SUM =
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

  static final MetricData DOUBLE_GAUGE =
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
  static final MetricData LONG_GAUGE =
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
  static final MetricData SUMMARY =
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

  static final MetricData DELTA_HISTOGRAM =
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

  static final MetricData CUMULATIVE_HISTOGRAM_NO_ATTRIBUTES =
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

  static final MetricData CUMULATIVE_HISTOGRAM_SINGLE_ATTRIBUTE =
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

  static final MetricData DOUBLE_GAUGE_NO_ATTRIBUTES =
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

  static final MetricData DOUBLE_GAUGE_MULTIPLE_ATTRIBUTES =
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
}
