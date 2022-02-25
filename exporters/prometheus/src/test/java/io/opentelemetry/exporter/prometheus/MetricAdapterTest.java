/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramData;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramPointData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.DoubleSumData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryPointData;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.data.ValueAtPercentile;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import io.opentelemetry.sdk.resources.Resource;
import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.exemplars.Exemplar;
import io.prometheus.client.exporter.common.TextFormat;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.assertj.core.presentation.StandardRepresentation;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link MetricAdapter}. */
class MetricAdapterTest {

  private static final Attributes KP_VP_ATTR = Attributes.of(stringKey("kp"), "vp");

  private static final MetricData MONOTONIC_CUMULATIVE_DOUBLE_SUM =
      MetricData.createDoubleSum(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationLibraryInfo.create("full", "version"),
          "instrument.name",
          "description",
          "1",
          DoubleSumData.create(
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
          DoubleSumData.create(
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
          DoubleSumData.create(
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
          DoubleSumData.create(
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
          LongSumData.create(
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
          LongSumData.create(
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
          LongSumData.create(
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
          LongSumData.create(
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
          DoubleSummaryData.create(
              Collections.singletonList(
                  DoubleSummaryPointData.create(
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

  @Test
  void monotonicCumulativeDoubleSum() {
    MetricFamilySamples metricFamilySamples =
        MetricAdapter.toMetricFamilySamples(MONOTONIC_CUMULATIVE_DOUBLE_SUM);
    assertThat(metricFamilySamples.type).isEqualTo(Collector.Type.COUNTER);
    assertThat(metricFamilySamples.samples).hasSize(1);
  }

  @Test
  void nonMonotonicCumulativeDoubleSum() {
    MetricFamilySamples metricFamilySamples =
        MetricAdapter.toMetricFamilySamples(NON_MONOTONIC_CUMULATIVE_DOUBLE_SUM);
    assertThat(metricFamilySamples.type).isEqualTo(Collector.Type.GAUGE);
    assertThat(metricFamilySamples.samples).hasSize(1);
  }

  @Test
  void monotonicDeltaDoubleSum() {
    MetricFamilySamples metricFamilySamples =
        MetricAdapter.toMetricFamilySamples(MONOTONIC_DELTA_DOUBLE_SUM);
    assertThat(metricFamilySamples.type).isEqualTo(Collector.Type.GAUGE);
    assertThat(metricFamilySamples.samples).hasSize(1);
  }

  @Test
  void nonMonotonicDeltaDoubleSum() {
    MetricFamilySamples metricFamilySamples =
        MetricAdapter.toMetricFamilySamples(NON_MONOTONIC_DELTA_DOUBLE_SUM);
    assertThat(metricFamilySamples.type).isEqualTo(Collector.Type.GAUGE);
    assertThat(metricFamilySamples.samples).hasSize(1);
  }

  @Test
  void monotonicCumulativeLongSum() {
    MetricFamilySamples metricFamilySamples =
        MetricAdapter.toMetricFamilySamples(MONOTONIC_CUMULATIVE_LONG_SUM);
    assertThat(metricFamilySamples.type).isEqualTo(Collector.Type.COUNTER);
    assertThat(metricFamilySamples.samples).hasSize(1);
  }

  @Test
  void nonMontonicCumulativeLongSum() {
    MetricFamilySamples metricFamilySamples =
        MetricAdapter.toMetricFamilySamples(NON_MONOTONIC_CUMULATIVE_LONG_SUM);
    assertThat(metricFamilySamples.type).isEqualTo(Collector.Type.GAUGE);
    assertThat(metricFamilySamples.samples).hasSize(1);
  }

  @Test
  void monotonicDeltaLongSum() {
    MetricFamilySamples metricFamilySamples =
        MetricAdapter.toMetricFamilySamples(MONOTONIC_DELTA_LONG_SUM);
    assertThat(metricFamilySamples.type).isEqualTo(Collector.Type.GAUGE);
    assertThat(metricFamilySamples.samples).hasSize(1);
  }

  @Test
  void nonMonotonicDeltaLongSum() {
    MetricFamilySamples metricFamilySamples =
        MetricAdapter.toMetricFamilySamples(NON_MONOTONIC_DELTA_LONG_SUM);
    assertThat(metricFamilySamples.type).isEqualTo(Collector.Type.GAUGE);
    assertThat(metricFamilySamples.samples).hasSize(1);
  }

  @Test
  void summary() {
    MetricFamilySamples metricFamilySamples = MetricAdapter.toMetricFamilySamples(SUMMARY);
    assertThat(metricFamilySamples.type).isEqualTo(Collector.Type.SUMMARY);
    assertThat(metricFamilySamples.samples).hasSize(4);
  }

  @Test
  void doubleGauge() {
    MetricFamilySamples metricFamilySamples = MetricAdapter.toMetricFamilySamples(DOUBLE_GAUGE);
    assertThat(metricFamilySamples.type).isEqualTo(Collector.Type.GAUGE);
    assertThat(metricFamilySamples.samples).hasSize(1);
  }

  @Test
  void longGauge() {
    MetricFamilySamples metricFamilySamples = MetricAdapter.toMetricFamilySamples(LONG_GAUGE);
    assertThat(metricFamilySamples.type).isEqualTo(Collector.Type.GAUGE);
    assertThat(metricFamilySamples.samples).hasSize(1);
  }

  @Test
  void histogram() {
    MetricFamilySamples metricFamilySamples = MetricAdapter.toMetricFamilySamples(HISTOGRAM);
    assertThat(metricFamilySamples.type).isEqualTo(Collector.Type.HISTOGRAM);
    assertThat(metricFamilySamples.samples).hasSize(3);
  }

  @Test
  void toSamples_LongPoints() {
    assertThat(
            MetricAdapter.toSamples("full_name", MetricDataType.LONG_SUM, Collections.emptyList()))
        .isEmpty();

    assertThat(
            MetricAdapter.toSamples(
                "full_name",
                MetricDataType.LONG_SUM,
                ImmutableList.of(
                    LongPointData.create(
                        1633947011000000000L, 1633950672000000000L, Attributes.empty(), 5),
                    LongPointData.create(
                        1633939689000000000L, 1633943350000000000L, KP_VP_ATTR, 7))))
        .containsExactly(
            new Sample(
                "full_name",
                Collections.emptyList(),
                Collections.emptyList(),
                5,
                null,
                1633950672000L),
            new Sample(
                "full_name",
                ImmutableList.of("kp"),
                ImmutableList.of("vp"),
                7,
                null,
                1633943350000L));

    assertThat(
            MetricAdapter.toSamples(
                "full_name",
                MetricDataType.LONG_GAUGE,
                ImmutableList.of(
                    LongPointData.create(
                        1633947011000000000L, 1633950672000000000L, Attributes.empty(), 5),
                    LongPointData.create(
                        1633939689000000000L, 1633943350000000000L, KP_VP_ATTR, 7))))
        .containsExactly(
            new Sample(
                "full_name",
                Collections.emptyList(),
                Collections.emptyList(),
                5,
                null,
                1633950672000L),
            new Sample(
                "full_name",
                ImmutableList.of("kp"),
                ImmutableList.of("vp"),
                7,
                null,
                1633943350000L));
  }

  @Test
  void toSamples_DoublePoints() {
    assertThat(
            MetricAdapter.toSamples(
                "full_name", MetricDataType.DOUBLE_SUM, Collections.emptyList()))
        .isEmpty();

    assertThat(
            MetricAdapter.toSamples(
                "full_name",
                MetricDataType.DOUBLE_SUM,
                Collections.singletonList(
                    DoublePointData.create(
                        1633947011000000000L, 1633950672000000000L, KP_VP_ATTR, 5))))
        .containsExactly(
            new Sample(
                "full_name",
                ImmutableList.of("kp"),
                ImmutableList.of("vp"),
                5,
                null,
                1633950672000L));

    assertThat(
            MetricAdapter.toSamples(
                "full_name",
                MetricDataType.DOUBLE_GAUGE,
                ImmutableList.of(
                    DoublePointData.create(
                        1633947011000000000L, 1633950672000000000L, Attributes.empty(), 5),
                    DoublePointData.create(
                        1633939689000000000L, 1633943350000000000L, KP_VP_ATTR, 7))))
        .containsExactly(
            new Sample(
                "full_name",
                Collections.emptyList(),
                Collections.emptyList(),
                5,
                null,
                1633950672000L),
            new Sample(
                "full_name",
                ImmutableList.of("kp"),
                ImmutableList.of("vp"),
                7,
                null,
                1633943350000L));
  }

  @Test
  void toSamples_SummaryPoints() {
    assertThat(
            MetricAdapter.toSamples("full_name", MetricDataType.SUMMARY, Collections.emptyList()))
        .isEmpty();

    assertThat(
            MetricAdapter.toSamples(
                "full_name",
                MetricDataType.SUMMARY,
                ImmutableList.of(
                    DoubleSummaryPointData.create(
                        1633939689000000000L,
                        1633943350000000000L,
                        KP_VP_ATTR,
                        9,
                        18.3,
                        ImmutableList.of(ValueAtPercentile.create(0.9, 1.1))))))
        .containsExactly(
            new Sample(
                "full_name_count",
                ImmutableList.of("kp"),
                ImmutableList.of("vp"),
                9,
                null,
                1633943350000L),
            new Sample(
                "full_name_sum",
                ImmutableList.of("kp"),
                ImmutableList.of("vp"),
                18.3,
                null,
                1633943350000L),
            new Sample(
                "full_name",
                ImmutableList.of("kp", "quantile"),
                ImmutableList.of("vp", "0.9"),
                1.1,
                null,
                1633943350000L));

    assertThat(
            MetricAdapter.toSamples(
                "full_name",
                MetricDataType.SUMMARY,
                ImmutableList.of(
                    DoubleSummaryPointData.create(
                        1633947011000000000L,
                        1633950672000000000L,
                        Attributes.empty(),
                        7,
                        15.3,
                        Collections.emptyList()),
                    DoubleSummaryPointData.create(
                        1633939689000000000L,
                        1633943350000000000L,
                        KP_VP_ATTR,
                        9,
                        18.3,
                        ImmutableList.of(
                            ValueAtPercentile.create(0.9, 1.1),
                            ValueAtPercentile.create(0.99, 12.3))))))
        .containsExactly(
            new Sample(
                "full_name_count",
                Collections.emptyList(),
                Collections.emptyList(),
                7,
                null,
                1633950672000L),
            new Sample(
                "full_name_sum",
                Collections.emptyList(),
                Collections.emptyList(),
                15.3,
                null,
                1633950672000L),
            new Sample(
                "full_name_count",
                ImmutableList.of("kp"),
                ImmutableList.of("vp"),
                9,
                null,
                1633943350000L),
            new Sample(
                "full_name_sum",
                ImmutableList.of("kp"),
                ImmutableList.of("vp"),
                18.3,
                null,
                1633943350000L),
            new Sample(
                "full_name",
                ImmutableList.of("kp", "quantile"),
                ImmutableList.of("vp", "0.9"),
                1.1,
                null,
                1633943350000L),
            new Sample(
                "full_name",
                ImmutableList.of("kp", "quantile"),
                ImmutableList.of("vp", "0.99"),
                12.3,
                null,
                1633943350000L));
  }

  @Test
  void toSamples_HistogramPoints() {
    assertThat(
            MetricAdapter.toSamples("full_name", MetricDataType.HISTOGRAM, Collections.emptyList()))
        .isEmpty();

    java.util.List<Sample> result =
        MetricAdapter.toSamples(
            "full_name",
            MetricDataType.HISTOGRAM,
            ImmutableList.of(
                DoubleHistogramPointData.create(
                    1633939689000000000L,
                    1633943350000000000L,
                    KP_VP_ATTR,
                    18.3,
                    ImmutableList.of(1.0),
                    ImmutableList.of(4L, 9L),
                    ImmutableList.of(
                        LongExemplarData.create(
                            Attributes.empty(),
                            /*recordTime=*/ 0,
                            SpanContext.create(
                                "00000000000000000000000000000004",
                                "0000000000000003",
                                TraceFlags.getDefault(),
                                TraceState.getDefault()),
                            /*value=*/ 0),
                        LongExemplarData.create(
                            Attributes.empty(),
                            /*recordTime=*/ TimeUnit.MILLISECONDS.toNanos(2),
                            SpanContext.create(
                                "00000000000000000000000000000001",
                                "0000000000000002",
                                TraceFlags.getDefault(),
                                TraceState.getDefault()),
                            /*value=*/ 2)))));
    assertThat(result)
        .withRepresentation(new ExemplarFriendlyRepresentation())
        .containsExactly(
            new Sample(
                "full_name_count",
                ImmutableList.of("kp"),
                ImmutableList.of("vp"),
                13,
                null,
                1633943350000L),
            new Sample(
                "full_name_sum",
                ImmutableList.of("kp"),
                ImmutableList.of("vp"),
                18.3,
                null,
                1633943350000L),
            new Sample(
                "full_name_bucket",
                ImmutableList.of("kp", "le"),
                ImmutableList.of("vp", "1.0"),
                4,
                new Exemplar(
                    0d,
                    0L,
                    "trace_id",
                    "00000000000000000000000000000004",
                    "span_id",
                    "0000000000000003"),
                1633943350000L),
            new Sample(
                "full_name_bucket",
                ImmutableList.of("kp", "le"),
                ImmutableList.of("vp", "+Inf"),
                13,
                new Exemplar(
                    2d,
                    2L,
                    "trace_id",
                    "00000000000000000000000000000001",
                    "span_id",
                    "0000000000000002"),
                1633943350000L));
  }

  @Test
  void toMetricFamilySamples() {
    MetricData metricData = MONOTONIC_CUMULATIVE_DOUBLE_SUM;
    assertThat(MetricAdapter.toMetricFamilySamples(metricData))
        .isEqualTo(
            new MetricFamilySamples(
                "instrument_name",
                Collector.Type.COUNTER,
                metricData.getDescription(),
                ImmutableList.of(
                    new Sample(
                        "instrument_name",
                        ImmutableList.of("kp"),
                        ImmutableList.of("vp"),
                        5,
                        null,
                        1633950672000L))));
  }

  @Test
  void serialize() {
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
                HISTOGRAM))
        .isEqualTo(
            "# HELP instrument_name_total description\n"
                + "# TYPE instrument_name_total counter\n"
                + "instrument_name_total{kp=\"vp\",} 5.0 1633950672000\n"
                + "# HELP instrument_name description\n"
                + "# TYPE instrument_name gauge\n"
                + "instrument_name{kp=\"vp\",} 5.0 1633950672000\n"
                + "# HELP instrument_name description\n"
                + "# TYPE instrument_name gauge\n"
                + "instrument_name{kp=\"vp\",} 5.0 1633950672000\n"
                + "# HELP instrument_name description\n"
                + "# TYPE instrument_name gauge\n"
                + "instrument_name{kp=\"vp\",} 5.0 1633950672000\n"
                + "# HELP instrument_name_total description\n"
                + "# TYPE instrument_name_total counter\n"
                + "instrument_name_total{kp=\"vp\",} 5.0 1633950672000\n"
                + "# HELP instrument_name description\n"
                + "# TYPE instrument_name gauge\n"
                + "instrument_name{kp=\"vp\",} 5.0 1633950672000\n"
                + "# HELP instrument_name description\n"
                + "# TYPE instrument_name gauge\n"
                + "instrument_name{kp=\"vp\",} 5.0 1633950672000\n"
                + "# HELP instrument_name description\n"
                + "# TYPE instrument_name gauge\n"
                + "instrument_name{kp=\"vp\",} 5.0 1633950672000\n"
                + "# HELP instrument_name description\n"
                + "# TYPE instrument_name gauge\n"
                + "instrument_name{kp=\"vp\",} 5.0 1633950672000\n"
                + "# HELP instrument_name description\n"
                + "# TYPE instrument_name gauge\n"
                + "instrument_name{kp=\"vp\",} 5.0 1633950672000\n"
                + "# HELP instrument_name description\n"
                + "# TYPE instrument_name summary\n"
                + "instrument_name_count{kp=\"vp\",} 5.0 1633950672000\n"
                + "instrument_name_sum{kp=\"vp\",} 7.0 1633950672000\n"
                + "instrument_name{kp=\"vp\",quantile=\"0.9\",} 0.1 1633950672000\n"
                + "instrument_name{kp=\"vp\",quantile=\"0.99\",} 0.3 1633950672000\n"
                + "# HELP instrument_name description\n"
                + "# TYPE instrument_name histogram\n"
                + "instrument_name_count{kp=\"vp\",} 2.0 1633950672000\n"
                + "instrument_name_sum{kp=\"vp\",} 1.0 1633950672000\n"
                + "instrument_name_bucket{kp=\"vp\",le=\"+Inf\",} 2.0 1633950672000\n");

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
                HISTOGRAM))
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
                + "# EOF\n");
  }

  private static String serialize004(MetricData... metrics) {
    StringWriter writer = new StringWriter();
    try {
      TextFormat.write004(
          writer,
          Collections.enumeration(
              Arrays.stream(metrics)
                  .map(MetricAdapter::toMetricFamilySamples)
                  .collect(Collectors.toList())));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return writer.toString();
  }

  private static String serializeOpenMetrics(MetricData... metrics) {
    StringWriter writer = new StringWriter();
    try {
      TextFormat.writeOpenMetrics100(
          writer,
          Collections.enumeration(
              Arrays.stream(metrics)
                  .map(MetricAdapter::toMetricFamilySamples)
                  .collect(Collectors.toList())));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return writer.toString();
  }

  /**
   * Make pretty-printing error messages nice, as prometheus doesn't output exemplars in toString.
   */
  private static class ExemplarFriendlyRepresentation extends StandardRepresentation {
    @Override
    public String fallbackToStringOf(Object object) {
      if (object instanceof Exemplar) {
        return exemplarToString((Exemplar) object);
      }
      if (object instanceof Sample) {
        Sample sample = (Sample) object;
        if (sample.exemplar != null) {
          StringBuilder sb = new StringBuilder(sample.toString());
          sb.append(" Exemplar=").append(exemplarToString(sample.exemplar));
          return sb.toString();
        }
      }
      if (object != null) {
        return super.fallbackToStringOf(object);
      }
      return "null";
    }
    /** Convert an exemplar into a human readable string. */
    private static String exemplarToString(Exemplar exemplar) {
      StringBuilder sb = new StringBuilder("Exemplar{ value=");
      sb.append(exemplar.getValue());
      sb.append(", ts=");
      sb.append(exemplar.getTimestampMs());
      sb.append(", labels=");
      for (int idx = 0; idx < exemplar.getNumberOfLabels(); ++idx) {
        sb.append(exemplar.getLabelName(idx));
        sb.append("=");
        sb.append(exemplar.getLabelValue(idx));
        sb.append(" ");
      }
      sb.append("}");
      return sb.toString();
    }
  }
}
