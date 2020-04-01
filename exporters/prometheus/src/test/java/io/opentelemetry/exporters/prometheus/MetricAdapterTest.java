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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import io.opentelemetry.sdk.resources.Resource;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Collector.Type;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link MetricAdapter}. */
@RunWith(JUnit4.class)
public class MetricAdapterTest {

  @Test
  public void toProtoMetricDescriptorType() {
    assertThat(MetricAdapter.toMetricFamilyType(Descriptor.Type.NON_MONOTONIC_DOUBLE))
        .isEqualTo(Type.GAUGE);
    assertThat(MetricAdapter.toMetricFamilyType(Descriptor.Type.NON_MONOTONIC_LONG))
        .isEqualTo(Type.GAUGE);
    assertThat(MetricAdapter.toMetricFamilyType(Descriptor.Type.MONOTONIC_DOUBLE))
        .isEqualTo(Type.COUNTER);
    assertThat(MetricAdapter.toMetricFamilyType(Descriptor.Type.MONOTONIC_LONG))
        .isEqualTo(Type.COUNTER);
    assertThat(MetricAdapter.toMetricFamilyType(Descriptor.Type.SUMMARY)).isEqualTo(Type.SUMMARY);
  }

  @Test
  public void toSamples_LongPoints() {
    assertThat(
            MetricAdapter.toSamples(
                "full_name",
                Descriptor.create(
                    "name",
                    "description",
                    "1",
                    Descriptor.Type.MONOTONIC_LONG,
                    Collections.<String, String>emptyMap()),
                Collections.<MetricData.Point>emptyList()))
        .isEmpty();

    assertThat(
            MetricAdapter.toSamples(
                "full_name",
                Descriptor.create(
                    "name",
                    "description",
                    "1",
                    Descriptor.Type.NON_MONOTONIC_LONG,
                    Collections.<String, String>emptyMap()),
                Collections.<Point>singletonList(
                    MetricData.LongPoint.create(
                        123, 456, Collections.singletonMap("kp", "vp"), 5))))
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
                    Collections.singletonMap("kc", "vc")),
                Collections.<Point>singletonList(
                    MetricData.LongPoint.create(
                        123, 456, Collections.singletonMap("kp", "vp"), 5))))
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
                    Collections.singletonMap("kc", "vc")),
                ImmutableList.<Point>of(
                    MetricData.LongPoint.create(
                        123, 456, Collections.<String, String>emptyMap(), 5),
                    MetricData.LongPoint.create(
                        321, 654, Collections.singletonMap("kp", "vp"), 7))))
        .containsExactly(
            new Sample("full_name", ImmutableList.of("kc"), ImmutableList.of("vc"), 5),
            new Sample("full_name", ImmutableList.of("kc", "kp"), ImmutableList.of("vc", "vp"), 7));
  }

  @Test
  public void toSamples_DoublePoints() {
    assertThat(
            MetricAdapter.toSamples(
                "full_name",
                Descriptor.create(
                    "name",
                    "description",
                    "1",
                    Descriptor.Type.NON_MONOTONIC_DOUBLE,
                    Collections.<String, String>emptyMap()),
                Collections.<MetricData.Point>emptyList()))
        .isEmpty();

    assertThat(
            MetricAdapter.toSamples(
                "full_name",
                Descriptor.create(
                    "name",
                    "description",
                    "1",
                    Descriptor.Type.MONOTONIC_DOUBLE,
                    Collections.<String, String>emptyMap()),
                Collections.<Point>singletonList(
                    MetricData.DoublePoint.create(
                        123, 456, Collections.singletonMap("kp", "vp"), 5))))
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
                    Collections.singletonMap("kc", "vc")),
                ImmutableList.<Point>of(
                    MetricData.DoublePoint.create(
                        123, 456, Collections.<String, String>emptyMap(), 5),
                    MetricData.DoublePoint.create(
                        321, 654, Collections.singletonMap("kp", "vp"), 7))))
        .containsExactly(
            new Sample("full_name", ImmutableList.of("kc"), ImmutableList.of("vc"), 5),
            new Sample("full_name", ImmutableList.of("kc", "kp"), ImmutableList.of("vc", "vp"), 7));
  }

  @Test
  public void toSamples_SummaryPoints() {
    assertThat(
            MetricAdapter.toSamples(
                "full_name",
                Descriptor.create(
                    "name",
                    "description",
                    "1",
                    Descriptor.Type.SUMMARY,
                    Collections.<String, String>emptyMap()),
                Collections.<MetricData.Point>emptyList()))
        .isEmpty();

    assertThat(
            MetricAdapter.toSamples(
                "full_name",
                Descriptor.create(
                    "name",
                    "description",
                    "1",
                    Descriptor.Type.SUMMARY,
                    Collections.<String, String>emptyMap()),
                ImmutableList.<Point>of(
                    MetricData.SummaryPoint.create(
                        321,
                        654,
                        Collections.singletonMap("kp", "vp"),
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
                    "name",
                    "description",
                    "1",
                    Descriptor.Type.SUMMARY,
                    Collections.singletonMap("kc", "vc")),
                ImmutableList.<Point>of(
                    MetricData.SummaryPoint.create(
                        123,
                        456,
                        Collections.<String, String>emptyMap(),
                        7,
                        15.3,
                        Collections.<MetricData.ValueAtPercentile>emptyList()),
                    MetricData.SummaryPoint.create(
                        321,
                        654,
                        Collections.singletonMap("kp", "vp"),
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
  public void toMetricFamilySamples() {
    Descriptor descriptor =
        Descriptor.create(
            "name",
            "description",
            "1",
            Descriptor.Type.MONOTONIC_DOUBLE,
            Collections.singletonMap("kc", "vc"));

    MetricData metricData =
        MetricData.create(
            descriptor,
            Resource.create(
                Collections.singletonMap("kr", AttributeValue.stringAttributeValue("vr"))),
            InstrumentationLibraryInfo.create("full", "version"),
            Collections.<Point>singletonList(
                MetricData.DoublePoint.create(123, 456, Collections.singletonMap("kp", "vp"), 5)));

    assertThat(MetricAdapter.toMetricFamilySamples(metricData))
        .isEqualTo(
            new MetricFamilySamples(
                "full_name",
                Type.COUNTER,
                descriptor.getDescription(),
                ImmutableList.of(
                    new Sample(
                        "full_name",
                        ImmutableList.of("kc", "kp"),
                        ImmutableList.of("vc", "vp"),
                        5))));
  }
}
