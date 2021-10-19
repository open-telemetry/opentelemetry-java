/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleGaugeData;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramData;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramPointData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.DoubleSumData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryPointData;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import io.opentelemetry.sdk.metrics.data.LongGaugeData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.data.ValueAtPercentile;
import io.opentelemetry.sdk.resources.Resource;
import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.exemplars.Exemplar;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
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
          DoubleGaugeData.create(
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
          LongGaugeData.create(
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
                      Collections.emptyList()))));
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
                              /* spanId= */ "span_id",
                              /* traceId= */ "trace_id",
                              /* value= */ 4))))));

  @Test
  void toProtoMetricDescriptorType() {
    MetricFamilySamples metricFamilySamples =
        MetricAdapter.toMetricFamilySamples(MONOTONIC_CUMULATIVE_DOUBLE_SUM);
    assertThat(metricFamilySamples.type).isEqualTo(Collector.Type.COUNTER);
    assertThat(metricFamilySamples.samples).hasSize(1);

    metricFamilySamples = MetricAdapter.toMetricFamilySamples(NON_MONOTONIC_CUMULATIVE_DOUBLE_SUM);
    assertThat(metricFamilySamples.type).isEqualTo(Collector.Type.GAUGE);
    assertThat(metricFamilySamples.samples).hasSize(1);

    metricFamilySamples = MetricAdapter.toMetricFamilySamples(MONOTONIC_DELTA_DOUBLE_SUM);
    assertThat(metricFamilySamples.type).isEqualTo(Collector.Type.GAUGE);
    assertThat(metricFamilySamples.samples).hasSize(1);

    metricFamilySamples = MetricAdapter.toMetricFamilySamples(NON_MONOTONIC_DELTA_DOUBLE_SUM);
    assertThat(metricFamilySamples.type).isEqualTo(Collector.Type.GAUGE);
    assertThat(metricFamilySamples.samples).hasSize(1);

    metricFamilySamples = MetricAdapter.toMetricFamilySamples(MONOTONIC_CUMULATIVE_LONG_SUM);
    assertThat(metricFamilySamples.type).isEqualTo(Collector.Type.COUNTER);
    assertThat(metricFamilySamples.samples).hasSize(1);

    metricFamilySamples = MetricAdapter.toMetricFamilySamples(NON_MONOTONIC_CUMULATIVE_LONG_SUM);
    assertThat(metricFamilySamples.type).isEqualTo(Collector.Type.GAUGE);
    assertThat(metricFamilySamples.samples).hasSize(1);

    metricFamilySamples = MetricAdapter.toMetricFamilySamples(MONOTONIC_DELTA_LONG_SUM);
    assertThat(metricFamilySamples.type).isEqualTo(Collector.Type.GAUGE);
    assertThat(metricFamilySamples.samples).hasSize(1);

    metricFamilySamples = MetricAdapter.toMetricFamilySamples(NON_MONOTONIC_DELTA_LONG_SUM);
    assertThat(metricFamilySamples.type).isEqualTo(Collector.Type.GAUGE);
    assertThat(metricFamilySamples.samples).hasSize(1);

    metricFamilySamples = MetricAdapter.toMetricFamilySamples(SUMMARY);
    assertThat(metricFamilySamples.type).isEqualTo(Collector.Type.SUMMARY);
    assertThat(metricFamilySamples.samples).hasSize(2);

    metricFamilySamples = MetricAdapter.toMetricFamilySamples(DOUBLE_GAUGE);
    assertThat(metricFamilySamples.type).isEqualTo(Collector.Type.GAUGE);
    assertThat(metricFamilySamples.samples).hasSize(1);

    metricFamilySamples = MetricAdapter.toMetricFamilySamples(LONG_GAUGE);
    assertThat(metricFamilySamples.type).isEqualTo(Collector.Type.GAUGE);
    assertThat(metricFamilySamples.samples).hasSize(1);

    metricFamilySamples = MetricAdapter.toMetricFamilySamples(HISTOGRAM);
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
                            "other_span_id",
                            "other_trace_id",
                            /*value=*/ 0),
                        LongExemplarData.create(
                            Attributes.empty(),
                            /*recordTime=*/ TimeUnit.MILLISECONDS.toNanos(2),
                            "my_span_id",
                            "my_trace_id",
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
                new Exemplar(0d, 0L, "trace_id", "other_trace_id", "span_id", "other_span_id"),
                1633943350000L),
            new Sample(
                "full_name_bucket",
                ImmutableList.of("kp", "le"),
                ImmutableList.of("vp", "+Inf"),
                13,
                new Exemplar(2d, 2L, "trace_id", "my_trace_id", "span_id", "my_span_id"),
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
