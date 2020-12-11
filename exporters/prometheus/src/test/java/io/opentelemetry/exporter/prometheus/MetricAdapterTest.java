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

  @Test
  void toProtoMetricDescriptorType() {
    assertThat(MetricAdapter.toMetricFamilyType(MetricData.Type.NON_MONOTONIC_DOUBLE_SUM))
        .isEqualTo(Collector.Type.GAUGE);
    assertThat(MetricAdapter.toMetricFamilyType(MetricData.Type.NON_MONOTONIC_LONG_SUM))
        .isEqualTo(Collector.Type.GAUGE);
    assertThat(MetricAdapter.toMetricFamilyType(MetricData.Type.DOUBLE_SUM))
        .isEqualTo(Collector.Type.COUNTER);
    assertThat(MetricAdapter.toMetricFamilyType(MetricData.Type.LONG_SUM))
        .isEqualTo(Collector.Type.COUNTER);
    assertThat(MetricAdapter.toMetricFamilyType(MetricData.Type.SUMMARY))
        .isEqualTo(Collector.Type.SUMMARY);
    assertThat(MetricAdapter.toMetricFamilyType(MetricData.Type.DOUBLE_GAUGE))
        .isEqualTo(Collector.Type.GAUGE);
    assertThat(MetricAdapter.toMetricFamilyType(MetricData.Type.LONG_GAUGE))
        .isEqualTo(Collector.Type.GAUGE);
  }

  @Test
  void toSamples_LongPoints() {
    assertThat(
            MetricAdapter.toSamples("full_name", MetricData.Type.LONG_SUM, Collections.emptyList()))
        .isEmpty();

    assertThat(
            MetricAdapter.toSamples(
                "full_name",
                MetricData.Type.NON_MONOTONIC_LONG_SUM,
                Collections.singletonList(
                    MetricData.LongPoint.create(123, 456, Labels.of("kp", "vp"), 5))))
        .containsExactly(
            new Sample("full_name", ImmutableList.of("kp"), ImmutableList.of("vp"), 5));

    assertThat(
            MetricAdapter.toSamples(
                "full_name",
                MetricData.Type.NON_MONOTONIC_LONG_SUM,
                Collections.singletonList(
                    MetricData.LongPoint.create(123, 456, Labels.of("kp", "vp"), 5))))
        .containsExactly(
            new Sample("full_name", ImmutableList.of("kp"), ImmutableList.of("vp"), 5));

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
                "full_name", MetricData.Type.NON_MONOTONIC_DOUBLE_SUM, Collections.emptyList()))
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
                MetricData.Type.NON_MONOTONIC_DOUBLE_SUM,
                ImmutableList.of(
                    MetricData.DoublePoint.create(123, 456, Labels.empty(), 5),
                    MetricData.DoublePoint.create(321, 654, Labels.of("kp", "vp"), 7))))
        .containsExactly(
            new Sample("full_name", Collections.emptyList(), Collections.emptyList(), 5),
            new Sample("full_name", ImmutableList.of("kp"), ImmutableList.of("vp"), 7));

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
                    MetricData.SummaryPoint.create(
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
                    MetricData.SummaryPoint.create(
                        123, 456, Labels.empty(), 7, 15.3, Collections.emptyList()),
                    MetricData.SummaryPoint.create(
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
    MetricData metricData =
        MetricData.create(
            Resource.create(Attributes.of(stringKey("kr"), "vr")),
            InstrumentationLibraryInfo.create("full", "version"),
            "instrument.name",
            "description",
            "1",
            MetricData.Type.DOUBLE_SUM,
            Collections.singletonList(
                MetricData.DoublePoint.create(123, 456, Labels.of("kp", "vp"), 5)));

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
