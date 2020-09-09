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

import static io.opentelemetry.common.AttributeKeyImpl.stringKey;
import static io.opentelemetry.proto.metrics.v1.AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.Labels;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.InstrumentationLibrary;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.common.v1.StringKeyValue;
import io.opentelemetry.proto.metrics.v1.DoubleDataPoint;
import io.opentelemetry.proto.metrics.v1.DoubleHistogramDataPoint;
import io.opentelemetry.proto.metrics.v1.DoubleSum;
import io.opentelemetry.proto.metrics.v1.InstrumentationLibraryMetrics;
import io.opentelemetry.proto.metrics.v1.IntDataPoint;
import io.opentelemetry.proto.metrics.v1.IntSum;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link MetricAdapter}. */
class MetricAdapterTest {
  @Test
  void toProtoLabels() {
    assertThat(MetricAdapter.toProtoLabels(Labels.empty())).isEmpty();
    assertThat(MetricAdapter.toProtoLabels(Labels.of("k", "v")))
        .containsExactly(StringKeyValue.newBuilder().setKey("k").setValue("v").build());
    assertThat(MetricAdapter.toProtoLabels(Labels.of("k1", "v1", "k2", "v2")))
        .containsExactly(
            StringKeyValue.newBuilder().setKey("k1").setValue("v1").build(),
            StringKeyValue.newBuilder().setKey("k2").setValue("v2").build());
  }

  @Test
  void toProtoValueAtPercentiles() {
    DoubleHistogramDataPoint.Builder builder = DoubleHistogramDataPoint.newBuilder();
    MetricAdapter.addBucketValues(Collections.emptyList(), builder);
    // 0 count in default bucket of [-infinity, infinity].
    assertThat(builder.getBucketCountsList()).containsExactly(0L);

    builder.clear();
    MetricAdapter.addBucketValues(
        singletonList(MetricData.ValueAtPercentile.create(0.9, 1.1)), builder);
    assertThat(builder.getBucketCountsList()).containsExactly(1L, 0L);
    assertThat(builder.getExplicitBoundsList()).containsExactly(0.9);

    builder.clear();
    MetricAdapter.addBucketValues(
        ImmutableList.of(
            MetricData.ValueAtPercentile.create(0.9, 1.1),
            MetricData.ValueAtPercentile.create(0.99, 20.3)),
        builder);
    assertThat(builder.getBucketCountsList()).containsExactly(1L, 20L, 0L);
    assertThat(builder.getExplicitBoundsList()).containsExactly(0.9, 0.99);
  }

  @Test
  void toInt64DataPoints() {
    Descriptor descriptor =
        Descriptor.create(
            "test",
            "testDescription",
            "unit",
            Descriptor.Type.MONOTONIC_LONG,
            Labels.of("ck", "cv"));
    assertThat(MetricAdapter.toIntDataPoints(Collections.emptyList(), descriptor)).isEmpty();
    assertThat(
            MetricAdapter.toIntDataPoints(
                singletonList(MetricData.LongPoint.create(123, 456, Labels.of("k", "v"), 5)),
                descriptor))
        .containsExactly(
            IntDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .addAllLabels(
                    Arrays.asList(
                        StringKeyValue.newBuilder().setKey("ck").setValue("cv").build(),
                        StringKeyValue.newBuilder().setKey("k").setValue("v").build()))
                .setValue(5)
                .build());
    assertThat(
            MetricAdapter.toIntDataPoints(
                ImmutableList.of(
                    MetricData.LongPoint.create(123, 456, Labels.empty(), 5),
                    MetricData.LongPoint.create(321, 654, Labels.of("k", "v"), 7)),
                descriptor))
        .containsExactly(
            IntDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .addAllLabels(
                    singletonList(StringKeyValue.newBuilder().setKey("ck").setValue("cv").build()))
                .setValue(5)
                .build(),
            IntDataPoint.newBuilder()
                .setStartTimeUnixNano(321)
                .setTimeUnixNano(654)
                .addAllLabels(
                    Arrays.asList(
                        StringKeyValue.newBuilder().setKey("ck").setValue("cv").build(),
                        StringKeyValue.newBuilder().setKey("k").setValue("v").build()))
                .setValue(7)
                .build());
  }

  @Test
  void toDoubleDataPoints() {
    Descriptor descriptor =
        Descriptor.create(
            "test",
            "testDescription",
            "unit",
            Descriptor.Type.MONOTONIC_DOUBLE,
            Labels.of("ck", "cv"));
    assertThat(MetricAdapter.toDoubleDataPoints(Collections.emptyList(), descriptor)).isEmpty();
    assertThat(
            MetricAdapter.toDoubleDataPoints(
                singletonList(MetricData.DoublePoint.create(123, 456, Labels.of("k", "v"), 5.1)),
                descriptor))
        .containsExactly(
            DoubleDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .addAllLabels(
                    Arrays.asList(
                        StringKeyValue.newBuilder().setKey("ck").setValue("cv").build(),
                        StringKeyValue.newBuilder().setKey("k").setValue("v").build()))
                .setValue(5.1)
                .build());
    assertThat(
            MetricAdapter.toDoubleDataPoints(
                ImmutableList.of(
                    MetricData.DoublePoint.create(123, 456, Labels.empty(), 5.1),
                    MetricData.DoublePoint.create(321, 654, Labels.of("k", "v"), 7.1)),
                descriptor))
        .containsExactly(
            DoubleDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .addAllLabels(
                    singletonList(StringKeyValue.newBuilder().setKey("ck").setValue("cv").build()))
                .setValue(5.1)
                .build(),
            DoubleDataPoint.newBuilder()
                .setStartTimeUnixNano(321)
                .setTimeUnixNano(654)
                .addAllLabels(
                    Arrays.asList(
                        StringKeyValue.newBuilder().setKey("ck").setValue("cv").build(),
                        StringKeyValue.newBuilder().setKey("k").setValue("v").build()))
                .setValue(7.1)
                .build());
  }

  @Test
  void toSummaryDataPoints() {
    Descriptor descriptor =
        Descriptor.create(
            "test", "testDescription", "unit", Descriptor.Type.SUMMARY, Labels.of("ck", "cv"));
    assertThat(
            MetricAdapter.toSummaryDataPoints(
                singletonList(
                    MetricData.SummaryPoint.create(
                        123,
                        456,
                        Labels.of("k", "v"),
                        5,
                        14.2,
                        singletonList(MetricData.ValueAtPercentile.create(0.9, 1.1)))),
                descriptor))
        .containsExactly(
            DoubleHistogramDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .addAllLabels(
                    Arrays.asList(
                        StringKeyValue.newBuilder().setKey("ck").setValue("cv").build(),
                        StringKeyValue.newBuilder().setKey("k").setValue("v").build()))
                .setCount(5)
                .setSum(14.2)
                .addBucketCounts(1)
                .addBucketCounts(0)
                .addExplicitBounds(0.9)
                .build());
    assertThat(
            MetricAdapter.toSummaryDataPoints(
                ImmutableList.of(
                    MetricData.SummaryPoint.create(
                        123, 456, Labels.empty(), 7, 15.3, Collections.emptyList()),
                    MetricData.SummaryPoint.create(
                        321,
                        654,
                        Labels.of("k", "v"),
                        9,
                        18.3,
                        ImmutableList.of(
                            MetricData.ValueAtPercentile.create(0.9, 1.1),
                            MetricData.ValueAtPercentile.create(0.99, 20.3)))),
                descriptor))
        .containsExactly(
            DoubleHistogramDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .addAllLabels(
                    singletonList(StringKeyValue.newBuilder().setKey("ck").setValue("cv").build()))
                .setCount(7)
                .setSum(15.3)
                .build(),
            DoubleHistogramDataPoint.newBuilder()
                .setStartTimeUnixNano(321)
                .setTimeUnixNano(654)
                .addAllLabels(
                    Arrays.asList(
                        StringKeyValue.newBuilder().setKey("ck").setValue("cv").build(),
                        StringKeyValue.newBuilder().setKey("k").setValue("v").build()))
                .setCount(9)
                .setSum(18.3)
                .addBucketCounts(1)
                .addBucketCounts(20)
                .addBucketCounts(0)
                .addExplicitBounds(0.9)
                .addExplicitBounds(0.99)
                .build());
  }

  @Test
  void toProtoMetric() {
    assertThat(
            MetricAdapter.toProtoMetric(
                MetricData.create(
                    Descriptor.create(
                        "name",
                        "description",
                        "1",
                        Descriptor.Type.MONOTONIC_LONG,
                        Labels.of("ck", "cv")),
                    Resource.getEmpty(),
                    InstrumentationLibraryInfo.getEmpty(),
                    singletonList(MetricData.LongPoint.create(123, 456, Labels.of("k", "v"), 5)))))
        .isEqualTo(
            Metric.newBuilder()
                .setName("name")
                .setDescription("description")
                .setUnit("1")
                .setIntSum(
                    IntSum.newBuilder()
                        .setIsMonotonic(true)
                        .setAggregationTemporality(AGGREGATION_TEMPORALITY_CUMULATIVE)
                        .addDataPoints(
                            IntDataPoint.newBuilder()
                                .setStartTimeUnixNano(123)
                                .setTimeUnixNano(456)
                                .addAllLabels(
                                    Arrays.asList(
                                        StringKeyValue.newBuilder()
                                            .setKey("ck")
                                            .setValue("cv")
                                            .build(),
                                        StringKeyValue.newBuilder()
                                            .setKey("k")
                                            .setValue("v")
                                            .build()))
                                .setValue(5)
                                .build())
                        .build())
                .build());
    assertThat(
            MetricAdapter.toProtoMetric(
                MetricData.create(
                    Descriptor.create(
                        "name",
                        "description",
                        "1",
                        Descriptor.Type.MONOTONIC_DOUBLE,
                        Labels.of("ck", "cv")),
                    Resource.getEmpty(),
                    InstrumentationLibraryInfo.getEmpty(),
                    singletonList(
                        MetricData.DoublePoint.create(123, 456, Labels.of("k", "v"), 5.1)))))
        .isEqualTo(
            Metric.newBuilder()
                .setName("name")
                .setDescription("description")
                .setUnit("1")
                .setDoubleSum(
                    DoubleSum.newBuilder()
                        .setIsMonotonic(true)
                        .setAggregationTemporality(AGGREGATION_TEMPORALITY_CUMULATIVE)
                        .addDataPoints(
                            DoubleDataPoint.newBuilder()
                                .setStartTimeUnixNano(123)
                                .setTimeUnixNano(456)
                                .addAllLabels(
                                    Arrays.asList(
                                        StringKeyValue.newBuilder()
                                            .setKey("ck")
                                            .setValue("cv")
                                            .build(),
                                        StringKeyValue.newBuilder()
                                            .setKey("k")
                                            .setValue("v")
                                            .build()))
                                .setValue(5.1)
                                .build())
                        .build())
                .build());
  }

  @Test
  void toProtoResourceMetrics() {
    Descriptor descriptor =
        Descriptor.create(
            "name", "description", "1", Descriptor.Type.MONOTONIC_DOUBLE, Labels.of("k", "v"));
    Resource resource = Resource.create(Attributes.of(stringKey("ka"), "va"));
    io.opentelemetry.proto.resource.v1.Resource resourceProto =
        io.opentelemetry.proto.resource.v1.Resource.newBuilder()
            .addAllAttributes(
                singletonList(
                    KeyValue.newBuilder()
                        .setKey("ka")
                        .setValue(AnyValue.newBuilder().setStringValue("va").build())
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
        Metric.newBuilder().setName("name").setDescription("description").setUnit("1").build();

    assertThat(
            MetricAdapter.toProtoResourceMetrics(
                ImmutableList.of(
                    MetricData.create(
                        descriptor, resource, instrumentationLibraryInfo, Collections.emptyList()),
                    MetricData.create(
                        descriptor, resource, instrumentationLibraryInfo, Collections.emptyList()),
                    MetricData.create(
                        descriptor,
                        Resource.getEmpty(),
                        instrumentationLibraryInfo,
                        Collections.emptyList()),
                    MetricData.create(
                        descriptor,
                        Resource.getEmpty(),
                        InstrumentationLibraryInfo.getEmpty(),
                        Collections.emptyList()))))
        .containsExactlyInAnyOrder(
            ResourceMetrics.newBuilder()
                .setResource(resourceProto)
                .addAllInstrumentationLibraryMetrics(
                    singletonList(
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
                            .addAllMetrics(singletonList(metricNoPoints))
                            .build(),
                        InstrumentationLibraryMetrics.newBuilder()
                            .setInstrumentationLibrary(instrumentationLibraryProto)
                            .addAllMetrics(singletonList(metricNoPoints))
                            .build()))
                .build());
  }
}
