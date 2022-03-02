/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramData;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramPointData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.ValueAtPercentile;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSummaryData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSummaryPointData;
import io.opentelemetry.sdk.resources.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class SerializerTest {

  private static final Attributes KP_VP_ATTR = Attributes.of(stringKey("kp"), "vp");

  private static final MetricData MONOTONIC_CUMULATIVE_DOUBLE_SUM =
      MetricData.createDoubleSum(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationLibraryInfo.create("full", "version"),
          "instrument.name",
          "description",
          "1",
          ImmutableSumData.create(
              /* isMonotonic= */ true,
              AggregationTemporality.CUMULATIVE,
              Collections.singletonList(
                  DoublePointData.create(
                      1633947011000000000L, 1633950672000000000L, KP_VP_ATTR, 5))));
  private static final MetricData NON_MONOTONIC_CUMULATIVE_DOUBLE_SUM =
      MetricData.createDoubleSum(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationLibraryInfo.create("full", "version"),
          "instrument.name",
          "description",
          "1",
          ImmutableSumData.create(
              /* isMonotonic= */ false,
              AggregationTemporality.CUMULATIVE,
              Collections.singletonList(
                  DoublePointData.create(
                      1633947011000000000L, 1633950672000000000L, KP_VP_ATTR, 5))));
  private static final MetricData MONOTONIC_DELTA_DOUBLE_SUM =
      MetricData.createDoubleSum(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationLibraryInfo.create("full", "version"),
          "instrument.name",
          "description",
          "1",
          ImmutableSumData.create(
              /* isMonotonic= */ true,
              AggregationTemporality.DELTA,
              Collections.singletonList(
                  DoublePointData.create(
                      1633947011000000000L, 1633950672000000000L, KP_VP_ATTR, 5))));
  private static final MetricData NON_MONOTONIC_DELTA_DOUBLE_SUM =
      MetricData.createDoubleSum(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationLibraryInfo.create("full", "version"),
          "instrument.name",
          "description",
          "1",
          ImmutableSumData.create(
              /* isMonotonic= */ false,
              AggregationTemporality.DELTA,
              Collections.singletonList(
                  DoublePointData.create(
                      1633947011000000000L, 1633950672000000000L, KP_VP_ATTR, 5))));
  private static final MetricData MONOTONIC_CUMULATIVE_LONG_SUM =
      MetricData.createLongSum(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationLibraryInfo.create("full", "version"),
          "instrument.name",
          "description",
          "1",
          ImmutableSumData.create(
              /* isMonotonic= */ true,
              AggregationTemporality.CUMULATIVE,
              Collections.singletonList(
                  LongPointData.create(
                      1633947011000000000L, 1633950672000000000L, KP_VP_ATTR, 5))));
  private static final MetricData NON_MONOTONIC_CUMULATIVE_LONG_SUM =
      MetricData.createLongSum(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationLibraryInfo.create("full", "version"),
          "instrument.name",
          "description",
          "1",
          ImmutableSumData.create(
              /* isMonotonic= */ false,
              AggregationTemporality.CUMULATIVE,
              Collections.singletonList(
                  LongPointData.create(
                      1633947011000000000L, 1633950672000000000L, KP_VP_ATTR, 5))));
  private static final MetricData MONOTONIC_DELTA_LONG_SUM =
      MetricData.createLongSum(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationLibraryInfo.create("full", "version"),
          "instrument.name",
          "description",
          "1",
          ImmutableSumData.create(
              /* isMonotonic= */ true,
              AggregationTemporality.DELTA,
              Collections.singletonList(
                  LongPointData.create(
                      1633947011000000000L, 1633950672000000000L, KP_VP_ATTR, 5))));
  private static final MetricData NON_MONOTONIC_DELTA_LONG_SUM =
      MetricData.createLongSum(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationLibraryInfo.create("full", "version"),
          "instrument.name",
          "description",
          "1",
          ImmutableSumData.create(
              /* isMonotonic= */ false,
              AggregationTemporality.DELTA,
              Collections.singletonList(
                  LongPointData.create(
                      1633947011000000000L, 1633950672000000000L, KP_VP_ATTR, 5))));

  private static final MetricData DOUBLE_GAUGE =
      MetricData.createDoubleGauge(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationLibraryInfo.create("full", "version"),
          "instrument.name",
          "description",
          "1",
          ImmutableGaugeData.create(
              Collections.singletonList(
                  DoublePointData.create(
                      1633947011000000000L, 1633950672000000000L, KP_VP_ATTR, 5))));
  private static final MetricData LONG_GAUGE =
      MetricData.createLongGauge(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationLibraryInfo.create("full", "version"),
          "instrument.name",
          "description",
          "1",
          ImmutableGaugeData.create(
              Collections.singletonList(
                  LongPointData.create(
                      1633947011000000000L, 1633950672000000000L, KP_VP_ATTR, 5))));
  private static final MetricData SUMMARY =
      MetricData.createDoubleSummary(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationLibraryInfo.create("full", "version"),
          "instrument.name",
          "description",
          "1",
          ImmutableSummaryData.create(
              Collections.singletonList(
                  ImmutableSummaryPointData.create(
                      1633947011000000000L,
                      1633950672000000000L,
                      KP_VP_ATTR,
                      5,
                      7,
                      Arrays.asList(
                          ValueAtPercentile.create(0.9, 0.1),
                          ValueAtPercentile.create(0.99, 0.3))))));
  private static final MetricData HISTOGRAM =
      MetricData.createDoubleHistogram(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationLibraryInfo.create("full", "version"),
          "instrument.name",
          "description",
          "1",
          DoubleHistogramData.create(
              AggregationTemporality.DELTA,
              Collections.singletonList(
                  DoubleHistogramPointData.create(
                      1633947011000000000L,
                      1633950672000000000L,
                      KP_VP_ATTR,
                      1.0,
                      Collections.emptyList(),
                      Collections.singletonList(2L),
                      Collections.singletonList(
                          LongExemplarData.create(
                              Attributes.empty(),
                              TimeUnit.MILLISECONDS.toNanos(1L),
                              SpanContext.create(
                                  "00000000000000000000000000000001",
                                  "0000000000000002",
                                  TraceFlags.getDefault(),
                                  TraceState.getDefault()),
                              /* value= */ 4))))));
  private static final MetricData DOUBLE_GAUGE_NO_ATTRIBUTES =
      MetricData.createDoubleGauge(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationLibraryInfo.create("full", "version"),
          "instrument.name",
          "description",
          "1",
          ImmutableGaugeData.create(
              Collections.singletonList(
                  DoublePointData.create(
                      1633947011000000000L, 1633950672000000000L, Attributes.empty(), 7))));
  private static final MetricData DOUBLE_GAUGE_MULTIPLE_ATTRIBUTES =
      MetricData.createDoubleGauge(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationLibraryInfo.create("full", "version"),
          "instrument.name",
          "description",
          "1",
          ImmutableGaugeData.create(
              Collections.singletonList(
                  DoublePointData.create(
                      1633947011000000000L,
                      1633950672000000000L,
                      KP_VP_ATTR.toBuilder().put("animal", "bear").build(),
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
                NON_MONOTONIC_CUMULATIVE_DOUBLE_SUM,
                MONOTONIC_DELTA_DOUBLE_SUM,
                NON_MONOTONIC_DELTA_DOUBLE_SUM,
                MONOTONIC_CUMULATIVE_LONG_SUM,
                NON_MONOTONIC_CUMULATIVE_LONG_SUM,
                MONOTONIC_DELTA_LONG_SUM,
                NON_MONOTONIC_DELTA_LONG_SUM,
                DOUBLE_GAUGE,
                LONG_GAUGE,
                SUMMARY,
                HISTOGRAM,
                DOUBLE_GAUGE_NO_ATTRIBUTES,
                DOUBLE_GAUGE_MULTIPLE_ATTRIBUTES))
        .isEqualTo(
            "# TYPE instrument_name_total counter\n"
                + "# HELP instrument_name_total description\n"
                + "instrument_name_total{kp=\"vp\"} 5.0 1633950672000\n"
                + "# TYPE instrument_name gauge\n"
                + "# HELP instrument_name description\n"
                + "instrument_name{kp=\"vp\"} 5.0 1633950672000\n"
                + "# TYPE instrument_name gauge\n"
                + "# HELP instrument_name description\n"
                + "instrument_name{kp=\"vp\"} 5.0 1633950672000\n"
                + "# TYPE instrument_name gauge\n"
                + "# HELP instrument_name description\n"
                + "instrument_name{kp=\"vp\"} 5.0 1633950672000\n"
                + "# TYPE instrument_name_total counter\n"
                + "# HELP instrument_name_total description\n"
                + "instrument_name_total{kp=\"vp\"} 5.0 1633950672000\n"
                + "# TYPE instrument_name gauge\n"
                + "# HELP instrument_name description\n"
                + "instrument_name{kp=\"vp\"} 5.0 1633950672000\n"
                + "# TYPE instrument_name gauge\n"
                + "# HELP instrument_name description\n"
                + "instrument_name{kp=\"vp\"} 5.0 1633950672000\n"
                + "# TYPE instrument_name gauge\n"
                + "# HELP instrument_name description\n"
                + "instrument_name{kp=\"vp\"} 5.0 1633950672000\n"
                + "# TYPE instrument_name gauge\n"
                + "# HELP instrument_name description\n"
                + "instrument_name{kp=\"vp\"} 5.0 1633950672000\n"
                + "# TYPE instrument_name gauge\n"
                + "# HELP instrument_name description\n"
                + "instrument_name{kp=\"vp\"} 5.0 1633950672000\n"
                + "# TYPE instrument_name summary\n"
                + "# HELP instrument_name description\n"
                + "instrument_name_count{kp=\"vp\"} 5.0 1633950672000\n"
                + "instrument_name_sum{kp=\"vp\"} 7.0 1633950672000\n"
                + "instrument_name{kp=\"vp\",quantile=\"0.9\"} 0.1 1633950672000\n"
                + "instrument_name{kp=\"vp\",quantile=\"0.99\"} 0.3 1633950672000\n"
                + "# TYPE instrument_name histogram\n"
                + "# HELP instrument_name description\n"
                + "instrument_name_count{kp=\"vp\"} 2.0 1633950672000\n"
                + "instrument_name_sum{kp=\"vp\"} 1.0 1633950672000\n"
                + "instrument_name_bucket{kp=\"vp\",le=\"+Inf\"} 2.0 1633950672000\n"
                + "# TYPE instrument_name gauge\n"
                + "# HELP instrument_name description\n"
                + "instrument_name 7.0 1633950672000\n"
                + "# TYPE instrument_name gauge\n"
                + "# HELP instrument_name description\n"
                + "instrument_name{animal=\"bear\",kp=\"vp\"} 8.0 1633950672000\n");
  }

  @Test
  void openMetrics() {
    assertThat(
            serializeOpenMetrics(
                MONOTONIC_CUMULATIVE_DOUBLE_SUM,
                NON_MONOTONIC_CUMULATIVE_DOUBLE_SUM,
                MONOTONIC_DELTA_DOUBLE_SUM,
                NON_MONOTONIC_DELTA_DOUBLE_SUM,
                MONOTONIC_CUMULATIVE_LONG_SUM,
                NON_MONOTONIC_CUMULATIVE_LONG_SUM,
                MONOTONIC_DELTA_LONG_SUM,
                NON_MONOTONIC_DELTA_LONG_SUM,
                DOUBLE_GAUGE,
                LONG_GAUGE,
                SUMMARY,
                HISTOGRAM,
                DOUBLE_GAUGE_NO_ATTRIBUTES,
                DOUBLE_GAUGE_MULTIPLE_ATTRIBUTES))
        .isEqualTo(
            "# TYPE instrument_name counter\n"
                + "# HELP instrument_name description\n"
                + "instrument_name_total{kp=\"vp\"} 5.0 1633950672.000\n"
                + "# TYPE instrument_name gauge\n"
                + "# HELP instrument_name description\n"
                + "instrument_name{kp=\"vp\"} 5.0 1633950672.000\n"
                + "# TYPE instrument_name gauge\n"
                + "# HELP instrument_name description\n"
                + "instrument_name{kp=\"vp\"} 5.0 1633950672.000\n"
                + "# TYPE instrument_name gauge\n"
                + "# HELP instrument_name description\n"
                + "instrument_name{kp=\"vp\"} 5.0 1633950672.000\n"
                + "# TYPE instrument_name counter\n"
                + "# HELP instrument_name description\n"
                + "instrument_name_total{kp=\"vp\"} 5.0 1633950672.000\n"
                + "# TYPE instrument_name gauge\n"
                + "# HELP instrument_name description\n"
                + "instrument_name{kp=\"vp\"} 5.0 1633950672.000\n"
                + "# TYPE instrument_name gauge\n"
                + "# HELP instrument_name description\n"
                + "instrument_name{kp=\"vp\"} 5.0 1633950672.000\n"
                + "# TYPE instrument_name gauge\n"
                + "# HELP instrument_name description\n"
                + "instrument_name{kp=\"vp\"} 5.0 1633950672.000\n"
                + "# TYPE instrument_name gauge\n"
                + "# HELP instrument_name description\n"
                + "instrument_name{kp=\"vp\"} 5.0 1633950672.000\n"
                + "# TYPE instrument_name gauge\n"
                + "# HELP instrument_name description\n"
                + "instrument_name{kp=\"vp\"} 5.0 1633950672.000\n"
                + "# TYPE instrument_name summary\n"
                + "# HELP instrument_name description\n"
                + "instrument_name_count{kp=\"vp\"} 5.0 1633950672.000\n"
                + "instrument_name_sum{kp=\"vp\"} 7.0 1633950672.000\n"
                + "instrument_name{kp=\"vp\",quantile=\"0.9\"} 0.1 1633950672.000\n"
                + "instrument_name{kp=\"vp\",quantile=\"0.99\"} 0.3 1633950672.000\n"
                + "# TYPE instrument_name histogram\n"
                + "# HELP instrument_name description\n"
                + "instrument_name_count{kp=\"vp\"} 2.0 1633950672.000\n"
                + "instrument_name_sum{kp=\"vp\"} 1.0 1633950672.000\n"
                + "instrument_name_bucket{kp=\"vp\",le=\"+Inf\"} 2.0 1633950672.000 # {span_id=\"0000000000000002\",trace_id=\"00000000000000000000000000000001\"} 4.0 0.001\n"
                + "# TYPE instrument_name gauge\n"
                + "# HELP instrument_name description\n"
                + "instrument_name 7.0 1633950672.000\n"
                + "# TYPE instrument_name gauge\n"
                + "# HELP instrument_name description\n"
                + "instrument_name{animal=\"bear\",kp=\"vp\"} 8.0 1633950672.000\n"
                + "# EOF\n");
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
}
