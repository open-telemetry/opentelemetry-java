/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.exporters.prometheus;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
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
    assertThat(MetricAdapter.toMetricFamilyType(Descriptor.Type.NON_MONOTONIC_DOUBLE))
        .isEqualTo(Collector.Type.GAUGE);
    assertThat(MetricAdapter.toMetricFamilyType(Descriptor.Type.NON_MONOTONIC_LONG))
        .isEqualTo(Collector.Type.GAUGE);
    assertThat(MetricAdapter.toMetricFamilyType(Descriptor.Type.MONOTONIC_DOUBLE))
        .isEqualTo(Collector.Type.COUNTER);
    assertThat(MetricAdapter.toMetricFamilyType(Descriptor.Type.MONOTONIC_LONG))
        .isEqualTo(Collector.Type.COUNTER);
    assertThat(MetricAdapter.toMetricFamilyType(Descriptor.Type.SUMMARY))
        .isEqualTo(Collector.Type.SUMMARY);
  }

  @Test
  void toSamples_LongPoints() {
    assertThat(
            MetricAdapter.toSamples(
                "full_name",
                Descriptor.create(
                    "name", "description", "1", Descriptor.Type.MONOTONIC_LONG, Labels.empty()),
                Collections.emptyList()))
        .isEmpty();

    assertThat(
            MetricAdapter.toSamples(
                "full_name",
                Descriptor.create(
                    "name", "description", "1", Descriptor.Type.NON_MONOTONIC_LONG, Labels.empty()),
                Collections.singletonList(
                    MetricData.LongPoint.create(123, 456, Labels.of("kp", "vp"), 5))))
        .containsExactly(
            new Sample("full_name", ImmutableList.of("kp"), ImmutableList.of("vp"), 5));

    assertThat(
            MetricAdapter.toSamples(
                "full_name",
                Descriptor.create(
                    "name",
                    "description",
                    "1",
                    Descriptor.Type.NON_MONOTONIC_LONG,
                    Labels.of("kc", "vc")),
                Collections.singletonList(
                    MetricData.LongPoint.create(123, 456, Labels.of("kp", "vp"), 5))))
        .containsExactly(
            new Sample("full_name", ImmutableList.of("kc", "kp"), ImmutableList.of("vc", "vp"), 5));

    assertThat(
            MetricAdapter.toSamples(
                "full_name",
                Descriptor.create(
                    "name",
                    "description",
                    "1",
                    Descriptor.Type.MONOTONIC_LONG,
                    Labels.of("kc", "vc")),
                ImmutableList.of(
                    MetricData.LongPoint.create(123, 456, Labels.empty(), 5),
                    MetricData.LongPoint.create(321, 654, Labels.of("kp", "vp"), 7))))
        .containsExactly(
            new Sample("full_name", ImmutableList.of("kc"), ImmutableList.of("vc"), 5),
            new Sample("full_name", ImmutableList.of("kc", "kp"), ImmutableList.of("vc", "vp"), 7));
  }

  @Test
  void toSamples_DoublePoints() {
    assertThat(
            MetricAdapter.toSamples(
                "full_name",
                Descriptor.create(
                    "name",
                    "description",
                    "1",
                    Descriptor.Type.NON_MONOTONIC_DOUBLE,
                    Labels.empty()),
                Collections.emptyList()))
        .isEmpty();

    assertThat(
            MetricAdapter.toSamples(
                "full_name",
                Descriptor.create(
                    "name", "description", "1", Descriptor.Type.MONOTONIC_DOUBLE, Labels.empty()),
                Collections.singletonList(
                    MetricData.DoublePoint.create(123, 456, Labels.of("kp", "vp"), 5))))
        .containsExactly(
            new Sample("full_name", ImmutableList.of("kp"), ImmutableList.of("vp"), 5));

    assertThat(
            MetricAdapter.toSamples(
                "full_name",
                Descriptor.create(
                    "name",
                    "description",
                    "1",
                    Descriptor.Type.NON_MONOTONIC_DOUBLE,
                    Labels.of("kc", "vc")),
                ImmutableList.of(
                    MetricData.DoublePoint.create(123, 456, Labels.empty(), 5),
                    MetricData.DoublePoint.create(321, 654, Labels.of("kp", "vp"), 7))))
        .containsExactly(
            new Sample("full_name", ImmutableList.of("kc"), ImmutableList.of("vc"), 5),
            new Sample("full_name", ImmutableList.of("kc", "kp"), ImmutableList.of("vc", "vp"), 7));
  }

  @Test
  void toSamples_SummaryPoints() {
    assertThat(
            MetricAdapter.toSamples(
                "full_name",
                Descriptor.create(
                    "name", "description", "1", Descriptor.Type.SUMMARY, Labels.empty()),
                Collections.emptyList()))
        .isEmpty();

    assertThat(
            MetricAdapter.toSamples(
                "full_name",
                Descriptor.create(
                    "name", "description", "1", Descriptor.Type.SUMMARY, Labels.empty()),
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
                Descriptor.create(
                    "name", "description", "1", Descriptor.Type.SUMMARY, Labels.of("kc", "vc")),
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
            new Sample("full_name_count", ImmutableList.of("kc"), ImmutableList.of("vc"), 7),
            new Sample("full_name_sum", ImmutableList.of("kc"), ImmutableList.of("vc"), 15.3),
            new Sample(
                "full_name_count", ImmutableList.of("kc", "kp"), ImmutableList.of("vc", "vp"), 9),
            new Sample(
                "full_name_sum", ImmutableList.of("kc", "kp"), ImmutableList.of("vc", "vp"), 18.3),
            new Sample(
                "full_name",
                ImmutableList.of("kc", "kp", "quantile"),
                ImmutableList.of("vc", "vp", "0.9"),
                1.1),
            new Sample(
                "full_name",
                ImmutableList.of("kc", "kp", "quantile"),
                ImmutableList.of("vc", "vp", "0.99"),
                12.3));
  }

  @Test
  void toMetricFamilySamples() {
    Descriptor descriptor =
        Descriptor.create(
            "name", "description", "1", Descriptor.Type.MONOTONIC_DOUBLE, Labels.of("kc", "vc"));

    MetricData metricData =
        MetricData.create(
            descriptor,
            Resource.create(Attributes.of("kr", AttributeValue.stringAttributeValue("vr"))),
            InstrumentationLibraryInfo.create("full", "version"),
            Collections.singletonList(
                MetricData.DoublePoint.create(123, 456, Labels.of("kp", "vp"), 5)));

    assertThat(MetricAdapter.toMetricFamilySamples(metricData))
        .isEqualTo(
            new MetricFamilySamples(
                "full_name",
                Collector.Type.COUNTER,
                descriptor.getDescription(),
                ImmutableList.of(
                    new Sample(
                        "full_name",
                        ImmutableList.of("kc", "kp"),
                        ImmutableList.of("vc", "vp"),
                        5))));
  }
}
