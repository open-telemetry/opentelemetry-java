/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link MetricAdapter}. */
class MetricAdapterTest {

  private static final MetricData MONOTONIC_CUMULATIVE_DOUBLE_SUM =
      MetricData.createDoubleSum(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationLibraryInfo.create("full", "version"),
          "instrument.name",
          "description",
          "1",
          MetricData.DoubleSumData.create(
              /* isMonotonic= */ true,
              MetricData.AggregationTemporality.CUMULATIVE,
              Collections.singletonList(
                  MetricData.DoublePoint.create(123, 456, Labels.of("kp", "vp"), 5))));
  private static final MetricData NON_MONOTONIC_CUMULATIVE_DOUBLE_SUM =
      MetricData.createDoubleSum(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationLibraryInfo.create("full", "version"),
          "instrument.name",
          "description",
          "1",
          MetricData.DoubleSumData.create(
              /* isMonotonic= */ false,
              MetricData.AggregationTemporality.CUMULATIVE,
              Collections.singletonList(
                  MetricData.DoublePoint.create(123, 456, Labels.of("kp", "vp"), 5))));
  private static final MetricData MONOTONIC_DELTA_DOUBLE_SUM =
      MetricData.createDoubleSum(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationLibraryInfo.create("full", "version"),
          "instrument.name",
          "description",
          "1",
          MetricData.DoubleSumData.create(
              /* isMonotonic= */ true,
              MetricData.AggregationTemporality.DELTA,
              Collections.singletonList(
                  MetricData.DoublePoint.create(123, 456, Labels.of("kp", "vp"), 5))));
  private static final MetricData NON_MONOTONIC_DELTA_DOUBLE_SUM =
      MetricData.createDoubleSum(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationLibraryInfo.create("full", "version"),
          "instrument.name",
          "description",
          "1",
          MetricData.DoubleSumData.create(
              /* isMonotonic= */ false,
              MetricData.AggregationTemporality.DELTA,
              Collections.singletonList(
                  MetricData.DoublePoint.create(123, 456, Labels.of("kp", "vp"), 5))));
  private static final MetricData MONOTONIC_CUMULATIVE_LONG_SUM =
      MetricData.createLongSum(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationLibraryInfo.create("full", "version"),
          "instrument.name",
          "description",
          "1",
          MetricData.LongSumData.create(
              /* isMonotonic= */ true,
              MetricData.AggregationTemporality.CUMULATIVE,
              Collections.singletonList(
                  MetricData.LongPoint.create(123, 456, Labels.of("kp", "vp"), 5))));
  private static final MetricData NON_MONOTONIC_CUMULATIVE_LONG_SUM =
      MetricData.createLongSum(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationLibraryInfo.create("full", "version"),
          "instrument.name",
          "description",
          "1",
          MetricData.LongSumData.create(
              /* isMonotonic= */ false,
              MetricData.AggregationTemporality.CUMULATIVE,
              Collections.singletonList(
                  MetricData.LongPoint.create(123, 456, Labels.of("kp", "vp"), 5))));
  private static final MetricData MONOTONIC_DELTA_LONG_SUM =
      MetricData.createLongSum(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationLibraryInfo.create("full", "version"),
          "instrument.name",
          "description",
          "1",
          MetricData.LongSumData.create(
              /* isMonotonic= */ true,
              MetricData.AggregationTemporality.DELTA,
              Collections.singletonList(
                  MetricData.LongPoint.create(123, 456, Labels.of("kp", "vp"), 5))));
  private static final MetricData NON_MONOTONIC_DELTA_LONG_SUM =
      MetricData.createLongSum(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationLibraryInfo.create("full", "version"),
          "instrument.name",
          "description",
          "1",
          MetricData.LongSumData.create(
              /* isMonotonic= */ false,
              MetricData.AggregationTemporality.DELTA,
              Collections.singletonList(
                  MetricData.LongPoint.create(123, 456, Labels.of("kp", "vp"), 5))));

  private static final MetricData DOUBLE_GAUGE =
      MetricData.createDoubleGauge(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationLibraryInfo.create("full", "version"),
          "instrument.name",
          "description",
          "1",
          MetricData.DoubleGaugeData.create(
              Collections.singletonList(
                  MetricData.DoublePoint.create(123, 456, Labels.of("kp", "vp"), 5))));
  private static final MetricData LONG_GAUGE =
      MetricData.createLongGauge(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationLibraryInfo.create("full", "version"),
          "instrument.name",
          "description",
          "1",
          MetricData.LongGaugeData.create(
              Collections.singletonList(
                  MetricData.LongPoint.create(123, 456, Labels.of("kp", "vp"), 5))));
  private static final MetricData SUMMARY =
      MetricData.createDoubleSummary(
          Resource.create(Attributes.of(stringKey("kr"), "vr")),
          InstrumentationLibraryInfo.create("full", "version"),
          "instrument.name",
          "description",
          "1",
          MetricData.DoubleSummaryData.create(
              Collections.singletonList(
                  MetricData.DoubleSummaryPoint.create(
                      123, 456, Labels.of("kp", "vp"), 5, 7, Collections.emptyList()))));

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
  }

  @Test
  void toSamples_LongPoints() {
    assertThat(
            MetricAdapter.toSamples("full_name", MetricData.Type.LONG_SUM, Collections.emptyList()))
        .isEmpty();

    assertThat(
            MetricAdapter.toSamples(
                "full_name",
                MetricData.Type.LONG_SUM,
                ImmutableList.of(
                    MetricData.LongPoint.create(123, 456, Labels.empty(), 5),
                    MetricData.LongPoint.create(321, 654, Labels.of("kp", "vp"), 7))))
        .containsExactly(
            new Sample("full_name", Collections.emptyList(), Collections.emptyList(), 5),
            new Sample("full_name", ImmutableList.of("kp"), ImmutableList.of("vp"), 7));

    assertThat(
            MetricAdapter.toSamples(
                "full_name",
                MetricData.Type.LONG_GAUGE,
                ImmutableList.of(
                    MetricData.LongPoint.create(123, 456, Labels.empty(), 5),
                    MetricData.LongPoint.create(321, 654, Labels.of("kp", "vp"), 7))))
        .containsExactly(
            new Sample("full_name", Collections.emptyList(), Collections.emptyList(), 5),
            new Sample("full_name", ImmutableList.of("kp"), ImmutableList.of("vp"), 7));
  }

  @Test
  void toSamples_DoublePoints() {
    assertThat(
            MetricAdapter.toSamples(
                "full_name", MetricData.Type.DOUBLE_SUM, Collections.emptyList()))
        .isEmpty();

    assertThat(
            MetricAdapter.toSamples(
                "full_name",
                MetricData.Type.DOUBLE_SUM,
                Collections.singletonList(
                    MetricData.DoublePoint.create(123, 456, Labels.of("kp", "vp"), 5))))
        .containsExactly(
            new Sample("full_name", ImmutableList.of("kp"), ImmutableList.of("vp"), 5));

    assertThat(
            MetricAdapter.toSamples(
                "full_name",
                MetricData.Type.DOUBLE_GAUGE,
                ImmutableList.of(
                    MetricData.DoublePoint.create(123, 456, Labels.empty(), 5),
                    MetricData.DoublePoint.create(321, 654, Labels.of("kp", "vp"), 7))))
        .containsExactly(
            new Sample("full_name", Collections.emptyList(), Collections.emptyList(), 5),
            new Sample("full_name", ImmutableList.of("kp"), ImmutableList.of("vp"), 7));
  }

  @Test
  void toSamples_SummaryPoints() {
    assertThat(
            MetricAdapter.toSamples("full_name", MetricData.Type.SUMMARY, Collections.emptyList()))
        .isEmpty();

    assertThat(
            MetricAdapter.toSamples(
                "full_name",
                MetricData.Type.SUMMARY,
                ImmutableList.of(
                    MetricData.DoubleSummaryPoint.create(
                        321,
                        654,
                        Labels.of("kp", "vp"),
                        9,
                        18.3,
                        ImmutableList.of(MetricData.ValueAtPercentile.create(0.9, 1.1))))))
        .containsExactly(
            new Sample("full_name_count", ImmutableList.of("kp"), ImmutableList.of("vp"), 9),
            new Sample("full_name_sum", ImmutableList.of("kp"), ImmutableList.of("vp"), 18.3),
            new Sample(
                "full_name",
                ImmutableList.of("kp", "quantile"),
                ImmutableList.of("vp", "0.9"),
                1.1));

    assertThat(
            MetricAdapter.toSamples(
                "full_name",
                MetricData.Type.SUMMARY,
                ImmutableList.of(
                    MetricData.DoubleSummaryPoint.create(
                        123, 456, Labels.empty(), 7, 15.3, Collections.emptyList()),
                    MetricData.DoubleSummaryPoint.create(
                        321,
                        654,
                        Labels.of("kp", "vp"),
                        9,
                        18.3,
                        ImmutableList.of(
                            MetricData.ValueAtPercentile.create(0.9, 1.1),
                            MetricData.ValueAtPercentile.create(0.99, 12.3))))))
        .containsExactly(
            new Sample("full_name_count", Collections.emptyList(), Collections.emptyList(), 7),
            new Sample("full_name_sum", Collections.emptyList(), Collections.emptyList(), 15.3),
            new Sample("full_name_count", ImmutableList.of("kp"), ImmutableList.of("vp"), 9),
            new Sample("full_name_sum", ImmutableList.of("kp"), ImmutableList.of("vp"), 18.3),
            new Sample(
                "full_name",
                ImmutableList.of("kp", "quantile"),
                ImmutableList.of("vp", "0.9"),
                1.1),
            new Sample(
                "full_name",
                ImmutableList.of("kp", "quantile"),
                ImmutableList.of("vp", "0.99"),
                12.3));
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
                        "instrument_name", ImmutableList.of("kp"), ImmutableList.of("vp"), 5))));
  }
}
