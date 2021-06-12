/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.proto.metrics.v1.AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE;
import static io.opentelemetry.proto.metrics.v1.AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.InstrumentationLibrary;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.metrics.v1.Gauge;
import io.opentelemetry.proto.metrics.v1.Histogram;
import io.opentelemetry.proto.metrics.v1.HistogramDataPoint;
import io.opentelemetry.proto.metrics.v1.InstrumentationLibraryMetrics;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.NumberDataPoint;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.proto.metrics.v1.Sum;
import io.opentelemetry.proto.metrics.v1.Summary;
import io.opentelemetry.proto.metrics.v1.SummaryDataPoint;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleGaugeData;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramData;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramPointData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.DoubleSumData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryPointData;
import io.opentelemetry.sdk.metrics.data.LongGaugeData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.ValueAtPercentile;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class MetricAdapterTest {
  @Test
  void toProtoLabels() {
    assertThat(MetricAdapter.toProtoLabels(Labels.empty())).isEmpty();
    assertThat(MetricAdapter.toProtoLabels(Labels.of("k", "v")))
        .containsExactly(KeyValue.newBuilder().setKey("k").setValue(stringValue("v")).build());
    assertThat(MetricAdapter.toProtoLabels(Labels.of("k1", "v1", "k2", "v2")))
        .containsExactly(
            KeyValue.newBuilder().setKey("k1").setValue(stringValue("v1")).build(),
            KeyValue.newBuilder().setKey("k2").setValue(stringValue("v2")).build());
  }

  private static AnyValue stringValue(String v) {
    return AnyValue.newBuilder().setStringValue(v).build();
  }

  @Test
  void toInt64DataPoints() {
    assertThat(MetricAdapter.toIntDataPoints(Collections.emptyList())).isEmpty();
    assertThat(
            MetricAdapter.toIntDataPoints(
                singletonList(LongPointData.create(123, 456, Labels.of("k", "v"), 5))))
        .containsExactly(
            NumberDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .addAllAttributes(
                    singletonList(
                        KeyValue.newBuilder().setKey("k").setValue(stringValue("v")).build()))
                .setAsInt(5)
                .build());
    assertThat(
            MetricAdapter.toIntDataPoints(
                ImmutableList.of(
                    LongPointData.create(123, 456, Labels.empty(), 5),
                    LongPointData.create(321, 654, Labels.of("k", "v"), 7))))
        .containsExactly(
            NumberDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .setAsInt(5)
                .build(),
            NumberDataPoint.newBuilder()
                .setStartTimeUnixNano(321)
                .setTimeUnixNano(654)
                .addAllAttributes(
                    singletonList(
                        KeyValue.newBuilder().setKey("k").setValue(stringValue("v")).build()))
                .setAsInt(7)
                .build());
  }

  @Test
  void toDoubleDataPoints() {
    assertThat(MetricAdapter.toDoubleDataPoints(Collections.emptyList())).isEmpty();
    assertThat(
            MetricAdapter.toDoubleDataPoints(
                singletonList(DoublePointData.create(123, 456, Labels.of("k", "v"), 5.1))))
        .containsExactly(
            NumberDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .addAllAttributes(
                    singletonList(
                        KeyValue.newBuilder().setKey("k").setValue(stringValue("v")).build()))
                .setAsDouble(5.1)
                .build());
    assertThat(
            MetricAdapter.toDoubleDataPoints(
                ImmutableList.of(
                    DoublePointData.create(123, 456, Labels.empty(), 5.1),
                    DoublePointData.create(321, 654, Labels.of("k", "v"), 7.1))))
        .containsExactly(
            NumberDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .setAsDouble(5.1)
                .build(),
            NumberDataPoint.newBuilder()
                .setStartTimeUnixNano(321)
                .setTimeUnixNano(654)
                .addAllAttributes(
                    singletonList(
                        KeyValue.newBuilder().setKey("k").setValue(stringValue("v")).build()))
                .setAsDouble(7.1)
                .build());
  }

  @Test
  void toSummaryDataPoints() {
    assertThat(
            MetricAdapter.toSummaryDataPoints(
                singletonList(
                    DoubleSummaryPointData.create(
                        123,
                        456,
                        Labels.of("k", "v"),
                        5,
                        14.2,
                        singletonList(ValueAtPercentile.create(0.0, 1.1))))))
        .containsExactly(
            SummaryDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .addAllAttributes(
                    singletonList(
                        KeyValue.newBuilder().setKey("k").setValue(stringValue("v")).build()))
                .setCount(5)
                .setSum(14.2)
                .addQuantileValues(
                    SummaryDataPoint.ValueAtQuantile.newBuilder()
                        .setQuantile(0.0)
                        .setValue(1.1)
                        .build())
                .build());
    assertThat(
            MetricAdapter.toSummaryDataPoints(
                ImmutableList.of(
                    DoubleSummaryPointData.create(
                        123, 456, Labels.empty(), 7, 15.3, Collections.emptyList()),
                    DoubleSummaryPointData.create(
                        321,
                        654,
                        Labels.of("k", "v"),
                        9,
                        18.3,
                        ImmutableList.of(
                            ValueAtPercentile.create(0.0, 1.1),
                            ValueAtPercentile.create(100.0, 20.3))))))
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
                .addAllAttributes(
                    singletonList(
                        KeyValue.newBuilder().setKey("k").setValue(stringValue("v")).build()))
                .setCount(9)
                .setSum(18.3)
                .addQuantileValues(
                    SummaryDataPoint.ValueAtQuantile.newBuilder()
                        .setQuantile(0.0)
                        .setValue(1.1)
                        .build())
                .addQuantileValues(
                    SummaryDataPoint.ValueAtQuantile.newBuilder()
                        .setQuantile(1.0)
                        .setValue(20.3)
                        .build())
                .build());
  }

  @Test
  void toHistogramDataPoints() {
    assertThat(
            MetricAdapter.toHistogramDataPoints(
                ImmutableList.of(
                    DoubleHistogramPointData.create(
                        123,
                        456,
                        Labels.of("k", "v"),
                        14.2,
                        ImmutableList.of(1.0),
                        ImmutableList.of(1L, 5L)),
                    DoubleHistogramPointData.create(
                        123, 456, Labels.empty(), 15.3, ImmutableList.of(), ImmutableList.of(7L)))))
        .containsExactly(
            HistogramDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .addAllAttributes(
                    singletonList(
                        KeyValue.newBuilder().setKey("k").setValue(stringValue("v")).build()))
                .setCount(6)
                .setSum(14.2)
                .addBucketCounts(1)
                .addBucketCounts(5)
                .addExplicitBounds(1.0)
                .build(),
            HistogramDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .setCount(7)
                .setSum(15.3)
                .addBucketCounts(7)
                .build());
  }

  @Test
  void toProtoMetric_monotonic() {
    assertThat(
            MetricAdapter.toProtoMetric(
                MetricData.createLongSum(
                    Resource.empty(),
                    InstrumentationLibraryInfo.empty(),
                    "name",
                    "description",
                    "1",
                    LongSumData.create(
                        /* isMonotonic= */ true,
                        AggregationTemporality.CUMULATIVE,
                        singletonList(LongPointData.create(123, 456, Labels.of("k", "v"), 5))))))
        .isEqualTo(
            Metric.newBuilder()
                .setName("name")
                .setDescription("description")
                .setUnit("1")
                .setSum(
                    Sum.newBuilder()
                        .setIsMonotonic(true)
                        .setAggregationTemporality(AGGREGATION_TEMPORALITY_CUMULATIVE)
                        .addDataPoints(
                            NumberDataPoint.newBuilder()
                                .setStartTimeUnixNano(123)
                                .setTimeUnixNano(456)
                                .addAllAttributes(
                                    singletonList(
                                        KeyValue.newBuilder()
                                            .setKey("k")
                                            .setValue(stringValue("v"))
                                            .build()))
                                .setAsInt(5)
                                .build())
                        .build())
                .build());
    assertThat(
            MetricAdapter.toProtoMetric(
                MetricData.createDoubleSum(
                    Resource.empty(),
                    InstrumentationLibraryInfo.empty(),
                    "name",
                    "description",
                    "1",
                    DoubleSumData.create(
                        /* isMonotonic= */ true,
                        AggregationTemporality.CUMULATIVE,
                        singletonList(
                            DoublePointData.create(123, 456, Labels.of("k", "v"), 5.1))))))
        .isEqualTo(
            Metric.newBuilder()
                .setName("name")
                .setDescription("description")
                .setUnit("1")
                .setSum(
                    Sum.newBuilder()
                        .setIsMonotonic(true)
                        .setAggregationTemporality(AGGREGATION_TEMPORALITY_CUMULATIVE)
                        .addDataPoints(
                            NumberDataPoint.newBuilder()
                                .setStartTimeUnixNano(123)
                                .setTimeUnixNano(456)
                                .addAllAttributes(
                                    singletonList(
                                        KeyValue.newBuilder()
                                            .setKey("k")
                                            .setValue(stringValue("v"))
                                            .build()))
                                .setAsDouble(5.1)
                                .build())
                        .build())
                .build());
  }

  @Test
  void toProtoMetric_nonMonotonic() {
    assertThat(
            MetricAdapter.toProtoMetric(
                MetricData.createLongSum(
                    Resource.empty(),
                    InstrumentationLibraryInfo.empty(),
                    "name",
                    "description",
                    "1",
                    LongSumData.create(
                        /* isMonotonic= */ false,
                        AggregationTemporality.CUMULATIVE,
                        singletonList(LongPointData.create(123, 456, Labels.of("k", "v"), 5))))))
        .isEqualTo(
            Metric.newBuilder()
                .setName("name")
                .setDescription("description")
                .setUnit("1")
                .setSum(
                    Sum.newBuilder()
                        .setIsMonotonic(false)
                        .setAggregationTemporality(AGGREGATION_TEMPORALITY_CUMULATIVE)
                        .addDataPoints(
                            NumberDataPoint.newBuilder()
                                .setStartTimeUnixNano(123)
                                .setTimeUnixNano(456)
                                .addAllAttributes(
                                    singletonList(
                                        KeyValue.newBuilder()
                                            .setKey("k")
                                            .setValue(stringValue("v"))
                                            .build()))
                                .setAsInt(5)
                                .build())
                        .build())
                .build());
    assertThat(
            MetricAdapter.toProtoMetric(
                MetricData.createDoubleSum(
                    Resource.empty(),
                    InstrumentationLibraryInfo.empty(),
                    "name",
                    "description",
                    "1",
                    DoubleSumData.create(
                        /* isMonotonic= */ false,
                        AggregationTemporality.CUMULATIVE,
                        singletonList(
                            DoublePointData.create(123, 456, Labels.of("k", "v"), 5.1))))))
        .isEqualTo(
            Metric.newBuilder()
                .setName("name")
                .setDescription("description")
                .setUnit("1")
                .setSum(
                    Sum.newBuilder()
                        .setIsMonotonic(false)
                        .setAggregationTemporality(AGGREGATION_TEMPORALITY_CUMULATIVE)
                        .addDataPoints(
                            NumberDataPoint.newBuilder()
                                .setStartTimeUnixNano(123)
                                .setTimeUnixNano(456)
                                .addAllAttributes(
                                    singletonList(
                                        KeyValue.newBuilder()
                                            .setKey("k")
                                            .setValue(stringValue("v"))
                                            .build()))
                                .setAsDouble(5.1)
                                .build())
                        .build())
                .build());
  }

  @Test
  void toProtoMetric_gauges() {
    assertThat(
            MetricAdapter.toProtoMetric(
                MetricData.createLongGauge(
                    Resource.empty(),
                    InstrumentationLibraryInfo.empty(),
                    "name",
                    "description",
                    "1",
                    LongGaugeData.create(
                        singletonList(LongPointData.create(123, 456, Labels.of("k", "v"), 5))))))
        .isEqualTo(
            Metric.newBuilder()
                .setName("name")
                .setDescription("description")
                .setUnit("1")
                .setGauge(
                    Gauge.newBuilder()
                        .addDataPoints(
                            NumberDataPoint.newBuilder()
                                .setStartTimeUnixNano(123)
                                .setTimeUnixNano(456)
                                .addAllAttributes(
                                    singletonList(
                                        KeyValue.newBuilder()
                                            .setKey("k")
                                            .setValue(stringValue("v"))
                                            .build()))
                                .setAsInt(5)
                                .build())
                        .build())
                .build());
    assertThat(
            MetricAdapter.toProtoMetric(
                MetricData.createDoubleGauge(
                    Resource.empty(),
                    InstrumentationLibraryInfo.empty(),
                    "name",
                    "description",
                    "1",
                    DoubleGaugeData.create(
                        singletonList(
                            DoublePointData.create(123, 456, Labels.of("k", "v"), 5.1))))))
        .isEqualTo(
            Metric.newBuilder()
                .setName("name")
                .setDescription("description")
                .setUnit("1")
                .setGauge(
                    Gauge.newBuilder()
                        .addDataPoints(
                            NumberDataPoint.newBuilder()
                                .setStartTimeUnixNano(123)
                                .setTimeUnixNano(456)
                                .addAllAttributes(
                                    singletonList(
                                        KeyValue.newBuilder()
                                            .setKey("k")
                                            .setValue(stringValue("v"))
                                            .build()))
                                .setAsDouble(5.1)
                                .build())
                        .build())
                .build());
  }

  @Test
  void toProtoMetric_summary() {
    assertThat(
            MetricAdapter.toProtoMetric(
                MetricData.createDoubleSummary(
                    Resource.empty(),
                    InstrumentationLibraryInfo.empty(),
                    "name",
                    "description",
                    "1",
                    DoubleSummaryData.create(
                        singletonList(
                            DoubleSummaryPointData.create(
                                123,
                                456,
                                Labels.of("k", "v"),
                                5,
                                33d,
                                ImmutableList.of(
                                    ValueAtPercentile.create(0, 1.1),
                                    ValueAtPercentile.create(100.0, 20.3))))))))
        .isEqualTo(
            Metric.newBuilder()
                .setName("name")
                .setDescription("description")
                .setUnit("1")
                .setSummary(
                    Summary.newBuilder()
                        .addDataPoints(
                            SummaryDataPoint.newBuilder()
                                .setStartTimeUnixNano(123)
                                .setTimeUnixNano(456)
                                .addAllAttributes(
                                    singletonList(
                                        KeyValue.newBuilder()
                                            .setKey("k")
                                            .setValue(stringValue("v"))
                                            .build()))
                                .setCount(5)
                                .setSum(33d)
                                .addQuantileValues(
                                    SummaryDataPoint.ValueAtQuantile.newBuilder()
                                        .setQuantile(0)
                                        .setValue(1.1)
                                        .build())
                                .addQuantileValues(
                                    SummaryDataPoint.ValueAtQuantile.newBuilder()
                                        .setQuantile(1.0)
                                        .setValue(20.3)
                                        .build())
                                .build())
                        .build())
                .build());
  }

  @Test
  void toProtoMetric_histogram() {
    assertThat(
            MetricAdapter.toProtoMetric(
                MetricData.createDoubleHistogram(
                    Resource.empty(),
                    InstrumentationLibraryInfo.empty(),
                    "name",
                    "description",
                    "1",
                    DoubleHistogramData.create(
                        AggregationTemporality.DELTA,
                        singletonList(
                            DoubleHistogramPointData.create(
                                123,
                                456,
                                Labels.of("k", "v"),
                                4.0,
                                ImmutableList.of(),
                                ImmutableList.of(33L)))))))
        .isEqualTo(
            Metric.newBuilder()
                .setName("name")
                .setDescription("description")
                .setUnit("1")
                .setHistogram(
                    Histogram.newBuilder()
                        .setAggregationTemporality(AGGREGATION_TEMPORALITY_DELTA)
                        .addDataPoints(
                            HistogramDataPoint.newBuilder()
                                .setStartTimeUnixNano(123)
                                .setTimeUnixNano(456)
                                .addAllAttributes(
                                    singletonList(
                                        KeyValue.newBuilder()
                                            .setKey("k")
                                            .setValue(stringValue("v"))
                                            .build()))
                                .setCount(33)
                                .setSum(4.0)
                                .addBucketCounts(33)
                                .build())
                        .build())
                .build());
  }

  @Test
  void toProtoResourceMetrics() {
    Resource resource = Resource.create(Attributes.of(stringKey("ka"), "va"));
    io.opentelemetry.proto.resource.v1.Resource resourceProto =
        io.opentelemetry.proto.resource.v1.Resource.newBuilder()
            .addAllAttributes(
                singletonList(
                    KeyValue.newBuilder().setKey("ka").setValue(stringValue("va")).build()))
            .build();
    io.opentelemetry.proto.resource.v1.Resource emptyResourceProto =
        io.opentelemetry.proto.resource.v1.Resource.newBuilder().build();
    InstrumentationLibraryInfo instrumentationLibraryInfo =
        InstrumentationLibraryInfo.create("name", "version", "http://url");
    InstrumentationLibrary instrumentationLibraryProto =
        InstrumentationLibrary.newBuilder().setName("name").setVersion("version").build();
    InstrumentationLibrary emptyInstrumentationLibraryProto =
        InstrumentationLibrary.newBuilder().setName("").setVersion("").build();
    Metric metricDoubleSum =
        Metric.newBuilder()
            .setName("name")
            .setDescription("description")
            .setUnit("1")
            .setSum(
                Sum.newBuilder()
                    .setIsMonotonic(true)
                    .setAggregationTemporality(AGGREGATION_TEMPORALITY_CUMULATIVE)
                    .addDataPoints(
                        NumberDataPoint.newBuilder()
                            .setStartTimeUnixNano(123)
                            .setTimeUnixNano(456)
                            .addAllAttributes(
                                singletonList(
                                    KeyValue.newBuilder()
                                        .setKey("k")
                                        .setValue(stringValue("v"))
                                        .build()))
                            .setAsDouble(5.0)
                            .build())
                    .build())
            .build();

    assertThat(
            MetricAdapter.toProtoResourceMetrics(
                ImmutableList.of(
                    MetricData.createDoubleSum(
                        resource,
                        instrumentationLibraryInfo,
                        "name",
                        "description",
                        "1",
                        DoubleSumData.create(
                            /* isMonotonic= */ true,
                            AggregationTemporality.CUMULATIVE,
                            Collections.singletonList(
                                DoublePointData.create(123, 456, Labels.of("k", "v"), 5.0)))),
                    MetricData.createDoubleSum(
                        resource,
                        instrumentationLibraryInfo,
                        "name",
                        "description",
                        "1",
                        DoubleSumData.create(
                            /* isMonotonic= */ true,
                            AggregationTemporality.CUMULATIVE,
                            Collections.singletonList(
                                DoublePointData.create(123, 456, Labels.of("k", "v"), 5.0)))),
                    MetricData.createDoubleSum(
                        Resource.empty(),
                        instrumentationLibraryInfo,
                        "name",
                        "description",
                        "1",
                        DoubleSumData.create(
                            /* isMonotonic= */ true,
                            AggregationTemporality.CUMULATIVE,
                            Collections.singletonList(
                                DoublePointData.create(123, 456, Labels.of("k", "v"), 5.0)))),
                    MetricData.createDoubleSum(
                        Resource.empty(),
                        InstrumentationLibraryInfo.empty(),
                        "name",
                        "description",
                        "1",
                        DoubleSumData.create(
                            /* isMonotonic= */ true,
                            AggregationTemporality.CUMULATIVE,
                            Collections.singletonList(
                                DoublePointData.create(123, 456, Labels.of("k", "v"), 5.0)))))))
        .containsExactlyInAnyOrder(
            ResourceMetrics.newBuilder()
                .setResource(resourceProto)
                .addAllInstrumentationLibraryMetrics(
                    singletonList(
                        InstrumentationLibraryMetrics.newBuilder()
                            .setInstrumentationLibrary(instrumentationLibraryProto)
                            .addAllMetrics(ImmutableList.of(metricDoubleSum, metricDoubleSum))
                            .setSchemaUrl("http://url")
                            .build()))
                .build(),
            ResourceMetrics.newBuilder()
                .setResource(emptyResourceProto)
                .addAllInstrumentationLibraryMetrics(
                    ImmutableList.of(
                        InstrumentationLibraryMetrics.newBuilder()
                            .setInstrumentationLibrary(instrumentationLibraryProto)
                            .addAllMetrics(singletonList(metricDoubleSum))
                            .setSchemaUrl("http://url")
                            .build(),
                        InstrumentationLibraryMetrics.newBuilder()
                            .setInstrumentationLibrary(emptyInstrumentationLibraryProto)
                            .addAllMetrics(singletonList(metricDoubleSum))
                            .build()))
                .build());
  }
}
