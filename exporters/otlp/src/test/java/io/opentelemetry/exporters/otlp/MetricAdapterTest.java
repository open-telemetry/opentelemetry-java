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

package io.opentelemetry.exporters.otlp;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.proto.common.v1.AttributeKeyValue;
import io.opentelemetry.proto.common.v1.AttributeKeyValue.ValueType;
import io.opentelemetry.proto.common.v1.InstrumentationLibrary;
import io.opentelemetry.proto.common.v1.StringKeyValue;
import io.opentelemetry.proto.metrics.v1.DoubleDataPoint;
import io.opentelemetry.proto.metrics.v1.InstrumentationLibraryMetrics;
import io.opentelemetry.proto.metrics.v1.Int64DataPoint;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.MetricDescriptor;
import io.opentelemetry.proto.metrics.v1.MetricDescriptor.Type;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.proto.metrics.v1.SummaryDataPoint;
import io.opentelemetry.proto.metrics.v1.SummaryDataPoint.ValueAtPercentile;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link MetricAdapter}. */
@RunWith(JUnit4.class)
public class MetricAdapterTest {
  @Test
  public void toProtoLabels() {
    assertThat(MetricAdapter.toProtoLabels(Collections.<String, String>emptyMap())).isEmpty();
    assertThat(MetricAdapter.toProtoLabels(Collections.singletonMap("k", "v")))
        .containsExactly(StringKeyValue.newBuilder().setKey("k").setValue("v").build());
    assertThat(MetricAdapter.toProtoLabels(ImmutableMap.of("k1", "v1", "k2", "v2")))
        .containsExactly(
            StringKeyValue.newBuilder().setKey("k1").setValue("v1").build(),
            StringKeyValue.newBuilder().setKey("k2").setValue("v2").build());
  }

  @Test
  public void toProtoMetricDescriptorType() {
    assertThat(MetricAdapter.toProtoMetricDescriptorType(Descriptor.Type.NON_MONOTONIC_DOUBLE))
        .isEqualTo(Type.GAUGE_DOUBLE);
    assertThat(MetricAdapter.toProtoMetricDescriptorType(Descriptor.Type.NON_MONOTONIC_LONG))
        .isEqualTo(Type.GAUGE_INT64);
    assertThat(MetricAdapter.toProtoMetricDescriptorType(Descriptor.Type.MONOTONIC_DOUBLE))
        .isEqualTo(Type.COUNTER_DOUBLE);
    assertThat(MetricAdapter.toProtoMetricDescriptorType(Descriptor.Type.MONOTONIC_LONG))
        .isEqualTo(Type.COUNTER_INT64);
    assertThat(MetricAdapter.toProtoMetricDescriptorType(Descriptor.Type.SUMMARY))
        .isEqualTo(Type.SUMMARY);
  }

  @Test
  public void toProtoValueAtPercentiles() {
    assertThat(
            MetricAdapter.toProtoValueAtPercentiles(
                Collections.<MetricData.ValueAtPercentile>emptyList()))
        .isEmpty();
    assertThat(
            MetricAdapter.toProtoValueAtPercentiles(
                Collections.singletonList(MetricData.ValueAtPercentile.create(0.9, 1.1))))
        .containsExactly(ValueAtPercentile.newBuilder().setPercentile(0.9).setValue(1.1).build());
    assertThat(
            MetricAdapter.toProtoValueAtPercentiles(
                ImmutableList.of(
                    MetricData.ValueAtPercentile.create(0.9, 1.1),
                    MetricData.ValueAtPercentile.create(0.99, 20.3))))
        .containsExactly(
            ValueAtPercentile.newBuilder().setPercentile(0.9).setValue(1.1).build(),
            ValueAtPercentile.newBuilder().setPercentile(0.99).setValue(20.3).build());
  }

  @Test
  public void toInt64DataPoints() {
    assertThat(MetricAdapter.toInt64DataPoints(Collections.<MetricData.Point>emptyList()))
        .isEmpty();
    assertThat(
            MetricAdapter.toInt64DataPoints(
                Collections.<Point>singletonList(
                    MetricData.LongPoint.create(123, 456, Collections.singletonMap("k", "v"), 5))))
        .containsExactly(
            Int64DataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .addAllLabels(
                    Collections.singletonList(
                        StringKeyValue.newBuilder().setKey("k").setValue("v").build()))
                .setValue(5)
                .build());
    assertThat(
            MetricAdapter.toInt64DataPoints(
                ImmutableList.<Point>of(
                    MetricData.LongPoint.create(
                        123, 456, Collections.<String, String>emptyMap(), 5),
                    MetricData.LongPoint.create(321, 654, Collections.singletonMap("k", "v"), 7))))
        .containsExactly(
            Int64DataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .setValue(5)
                .build(),
            Int64DataPoint.newBuilder()
                .setStartTimeUnixNano(321)
                .setTimeUnixNano(654)
                .addAllLabels(
                    Collections.singletonList(
                        StringKeyValue.newBuilder().setKey("k").setValue("v").build()))
                .setValue(7)
                .build());
  }

  @Test
  public void toDoubleDataPoints() {
    assertThat(MetricAdapter.toDoubleDataPoints(Collections.<MetricData.Point>emptyList()))
        .isEmpty();
    assertThat(
            MetricAdapter.toDoubleDataPoints(
                Collections.<Point>singletonList(
                    MetricData.DoublePoint.create(
                        123, 456, Collections.singletonMap("k", "v"), 5.1))))
        .containsExactly(
            DoubleDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .addAllLabels(
                    Collections.singletonList(
                        StringKeyValue.newBuilder().setKey("k").setValue("v").build()))
                .setValue(5.1)
                .build());
    assertThat(
            MetricAdapter.toDoubleDataPoints(
                ImmutableList.<Point>of(
                    MetricData.DoublePoint.create(
                        123, 456, Collections.<String, String>emptyMap(), 5.1),
                    MetricData.DoublePoint.create(
                        321, 654, Collections.singletonMap("k", "v"), 7.1))))
        .containsExactly(
            DoubleDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .setValue(5.1)
                .build(),
            DoubleDataPoint.newBuilder()
                .setStartTimeUnixNano(321)
                .setTimeUnixNano(654)
                .addAllLabels(
                    Collections.singletonList(
                        StringKeyValue.newBuilder().setKey("k").setValue("v").build()))
                .setValue(7.1)
                .build());
  }

  @Test
  public void toSummaryDataPoints() {
    assertThat(MetricAdapter.toSummaryDataPoints(Collections.<MetricData.Point>emptyList()))
        .isEmpty();
    assertThat(
            MetricAdapter.toSummaryDataPoints(
                Collections.<Point>singletonList(
                    MetricData.SummaryPoint.create(
                        123,
                        456,
                        Collections.singletonMap("k", "v"),
                        5,
                        14.2,
                        Collections.singletonList(MetricData.ValueAtPercentile.create(0.9, 1.1))))))
        .containsExactly(
            SummaryDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .addAllLabels(
                    Collections.singletonList(
                        StringKeyValue.newBuilder().setKey("k").setValue("v").build()))
                .setCount(5)
                .setSum(14.2)
                .addAllPercentileValues(
                    Collections.singletonList(
                        ValueAtPercentile.newBuilder().setPercentile(0.9).setValue(1.1).build()))
                .build());
    assertThat(
            MetricAdapter.toSummaryDataPoints(
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
                        Collections.singletonMap("k", "v"),
                        9,
                        18.3,
                        ImmutableList.of(
                            MetricData.ValueAtPercentile.create(0.9, 1.1),
                            MetricData.ValueAtPercentile.create(0.99, 20.3))))))
        .containsExactly(
            SummaryDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .setCount(7)
                .setSum(15.3)
                .build(),
            SummaryDataPoint.newBuilder()
                .setStartTimeUnixNano(321)
                .setTimeUnixNano(654)
                .addAllLabels(
                    Collections.singletonList(
                        StringKeyValue.newBuilder().setKey("k").setValue("v").build()))
                .setCount(9)
                .setSum(18.3)
                .addAllPercentileValues(
                    ImmutableList.of(
                        ValueAtPercentile.newBuilder().setPercentile(0.9).setValue(1.1).build(),
                        ValueAtPercentile.newBuilder().setPercentile(0.99).setValue(20.3).build()))
                .build());
  }

  @Test
  public void toProtoMetricDescriptor() {
    assertThat(
            MetricAdapter.toProtoMetricDescriptor(
                Descriptor.create(
                    "name",
                    "description",
                    "1",
                    Descriptor.Type.MONOTONIC_DOUBLE,
                    Collections.singletonMap("k", "v"))))
        .isEqualTo(
            MetricDescriptor.newBuilder()
                .setName("name")
                .setDescription("description")
                .setUnit("1")
                .setType(Type.COUNTER_DOUBLE)
                .addAllLabels(
                    Collections.singletonList(
                        StringKeyValue.newBuilder().setKey("k").setValue("v").build()))
                .build());
    assertThat(
            MetricAdapter.toProtoMetricDescriptor(
                Descriptor.create(
                    "name",
                    "description",
                    "1",
                    Descriptor.Type.MONOTONIC_DOUBLE,
                    Collections.<String, String>emptyMap())))
        .isEqualTo(
            MetricDescriptor.newBuilder()
                .setName("name")
                .setDescription("description")
                .setUnit("1")
                .setType(Type.COUNTER_DOUBLE)
                .build());
  }

  @Test
  public void toProtoMetric() {
    assertThat(
            MetricAdapter.toProtoMetric(
                MetricData.create(
                    Descriptor.create(
                        "name",
                        "description",
                        "1",
                        Descriptor.Type.MONOTONIC_LONG,
                        Collections.singletonMap("k", "v")),
                    Resource.getEmpty(),
                    InstrumentationLibraryInfo.getEmpty(),
                    Collections.<Point>singletonList(
                        MetricData.LongPoint.create(
                            123, 456, Collections.singletonMap("k", "v"), 5)))))
        .isEqualTo(
            Metric.newBuilder()
                .setMetricDescriptor(
                    MetricDescriptor.newBuilder()
                        .setName("name")
                        .setDescription("description")
                        .setUnit("1")
                        .setType(Type.COUNTER_INT64)
                        .addAllLabels(
                            Collections.singletonList(
                                StringKeyValue.newBuilder().setKey("k").setValue("v").build()))
                        .build())
                .addAllInt64DataPoints(
                    Collections.singletonList(
                        Int64DataPoint.newBuilder()
                            .setStartTimeUnixNano(123)
                            .setTimeUnixNano(456)
                            .addAllLabels(
                                Collections.singletonList(
                                    StringKeyValue.newBuilder().setKey("k").setValue("v").build()))
                            .setValue(5)
                            .build()))
                .build());
    assertThat(
            MetricAdapter.toProtoMetric(
                MetricData.create(
                    Descriptor.create(
                        "name",
                        "description",
                        "1",
                        Descriptor.Type.MONOTONIC_DOUBLE,
                        Collections.singletonMap("k", "v")),
                    Resource.getEmpty(),
                    InstrumentationLibraryInfo.getEmpty(),
                    Collections.<Point>singletonList(
                        MetricData.DoublePoint.create(
                            123, 456, Collections.singletonMap("k", "v"), 5.1)))))
        .isEqualTo(
            Metric.newBuilder()
                .setMetricDescriptor(
                    MetricDescriptor.newBuilder()
                        .setName("name")
                        .setDescription("description")
                        .setUnit("1")
                        .setType(Type.COUNTER_DOUBLE)
                        .addAllLabels(
                            Collections.singletonList(
                                StringKeyValue.newBuilder().setKey("k").setValue("v").build()))
                        .build())
                .addAllDoubleDataPoints(
                    Collections.singletonList(
                        DoubleDataPoint.newBuilder()
                            .setStartTimeUnixNano(123)
                            .setTimeUnixNano(456)
                            .addAllLabels(
                                Collections.singletonList(
                                    StringKeyValue.newBuilder().setKey("k").setValue("v").build()))
                            .setValue(5.1)
                            .build()))
                .build());
  }

  @Test
  public void toProtoResourceMetrics() {
    Descriptor descriptor =
        Descriptor.create(
            "name",
            "description",
            "1",
            Descriptor.Type.MONOTONIC_DOUBLE,
            Collections.singletonMap("k", "v"));
    Resource resource =
        Resource.create(Collections.singletonMap("ka", AttributeValue.stringAttributeValue("va")));
    io.opentelemetry.proto.resource.v1.Resource resourceProto =
        io.opentelemetry.proto.resource.v1.Resource.newBuilder()
            .addAllAttributes(
                Collections.singletonList(
                    AttributeKeyValue.newBuilder()
                        .setKey("ka")
                        .setStringValue("va")
                        .setType(ValueType.STRING)
                        .build()))
            .build();
    io.opentelemetry.proto.resource.v1.Resource emptyResourceProto =
        io.opentelemetry.proto.resource.v1.Resource.newBuilder().build();
    InstrumentationLibraryInfo instrumentationLibraryInfo =
        InstrumentationLibraryInfo.create("name", "version");
    InstrumentationLibrary instrumentationLibraryProto =
        InstrumentationLibrary.newBuilder().setName("name").setVersion("version").build();
    InstrumentationLibrary emptyInstrumentationLibraryProto =
        InstrumentationLibrary.newBuilder().setName("").setVersion("").build();
    Metric metricNoPoints =
        Metric.newBuilder()
            .setMetricDescriptor(
                MetricDescriptor.newBuilder()
                    .setName("name")
                    .setDescription("description")
                    .setUnit("1")
                    .setType(Type.COUNTER_DOUBLE)
                    .addAllLabels(
                        Collections.singletonList(
                            StringKeyValue.newBuilder().setKey("k").setValue("v").build()))
                    .build())
            .build();

    assertThat(
            MetricAdapter.toProtoResourceMetrics(
                ImmutableList.of(
                    MetricData.create(
                        descriptor,
                        resource,
                        instrumentationLibraryInfo,
                        Collections.<Point>emptyList()),
                    MetricData.create(
                        descriptor,
                        resource,
                        instrumentationLibraryInfo,
                        Collections.<Point>emptyList()),
                    MetricData.create(
                        descriptor,
                        Resource.getEmpty(),
                        instrumentationLibraryInfo,
                        Collections.<Point>emptyList()),
                    MetricData.create(
                        descriptor,
                        Resource.getEmpty(),
                        InstrumentationLibraryInfo.getEmpty(),
                        Collections.<Point>emptyList()))))
        .containsExactly(
            ResourceMetrics.newBuilder()
                .setResource(resourceProto)
                .addAllInstrumentationLibraryMetrics(
                    Collections.singletonList(
                        InstrumentationLibraryMetrics.newBuilder()
                            .setInstrumentationLibrary(instrumentationLibraryProto)
                            .addAllMetrics(ImmutableList.of(metricNoPoints, metricNoPoints))
                            .build()))
                .build(),
            ResourceMetrics.newBuilder()
                .setResource(emptyResourceProto)
                .addAllInstrumentationLibraryMetrics(
                    ImmutableList.of(
                        InstrumentationLibraryMetrics.newBuilder()
                            .setInstrumentationLibrary(emptyInstrumentationLibraryProto)
                            .addAllMetrics(Collections.singletonList(metricNoPoints))
                            .build(),
                        InstrumentationLibraryMetrics.newBuilder()
                            .setInstrumentationLibrary(instrumentationLibraryProto)
                            .addAllMetrics(Collections.singletonList(metricNoPoints))
                            .build()))
                .build());
  }
}
