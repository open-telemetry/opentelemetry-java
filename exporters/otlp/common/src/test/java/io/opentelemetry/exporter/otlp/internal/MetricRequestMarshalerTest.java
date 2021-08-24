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
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.InstrumentationLibrary;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.metrics.v1.Exemplar;
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
import io.opentelemetry.sdk.metrics.data.DoubleExemplar;
import io.opentelemetry.sdk.metrics.data.DoubleGaugeData;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramData;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramPointData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.DoubleSumData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryPointData;
import io.opentelemetry.sdk.metrics.data.LongExemplar;
import io.opentelemetry.sdk.metrics.data.LongGaugeData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.data.ValueAtPercentile;
import io.opentelemetry.sdk.resources.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

// Fill deprecated APIs before removing them after users get a chance to migrate.
class MetricRequestMarshalerTest {

  private static final Attributes KV_ATTR = Attributes.of(stringKey("k"), "v");

  private static AnyValue stringValue(String v) {
    return AnyValue.newBuilder().setStringValue(v).build();
  }

  @Test
  void longDataPoints() {
    assertThat(toNumberDataPoints(Collections.emptyList())).isEmpty();
    assertThat(
            toNumberDataPoints(
                singletonList(
                    LongPointData.create(
                        123,
                        456,
                        KV_ATTR,
                        5,
                        singletonList(
                            LongExemplar.create(
                                Attributes.of(stringKey("test"), "value"),
                                2,
                                /*spanId=*/ "0000000000000002",
                                /*traceId=*/ "00000000000000000000000000000001",
                                1))))))
        .containsExactly(
            NumberDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .addAllAttributes(
                    singletonList(
                        KeyValue.newBuilder().setKey("k").setValue(stringValue("v")).build()))
                .setAsInt(5)
                .addExemplars(
                    Exemplar.newBuilder()
                        .setTimeUnixNano(2)
                        .addFilteredAttributes(
                            KeyValue.newBuilder()
                                .setKey("test")
                                .setValue(stringValue("value"))
                                .build())
                        .setSpanId(ByteString.copyFrom(new byte[] {0, 0, 0, 0, 0, 0, 0, 2}))
                        .setTraceId(
                            ByteString.copyFrom(
                                new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}))
                        .setAsInt(1)
                        .build())
                .build());
    assertThat(
            toNumberDataPoints(
                ImmutableList.of(
                    LongPointData.create(123, 456, Attributes.empty(), 5),
                    LongPointData.create(321, 654, KV_ATTR, 7))))
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
  void doubleDataPoints() {
    assertThat(toNumberDataPoints(Collections.emptyList())).isEmpty();
    assertThat(toNumberDataPoints(singletonList(DoublePointData.create(123, 456, KV_ATTR, 5.1))))
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
            toNumberDataPoints(
                ImmutableList.of(
                    DoublePointData.create(123, 456, Attributes.empty(), 5.1),
                    DoublePointData.create(321, 654, KV_ATTR, 7.1))))
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
  void summaryDataPoints() {
    assertThat(
            toSummaryDataPoints(
                singletonList(
                    DoubleSummaryPointData.create(
                        123,
                        456,
                        KV_ATTR,
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
            toSummaryDataPoints(
                ImmutableList.of(
                    DoubleSummaryPointData.create(
                        123, 456, Attributes.empty(), 7, 15.3, Collections.emptyList()),
                    DoubleSummaryPointData.create(
                        321,
                        654,
                        KV_ATTR,
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
  void histogramDataPoints() {
    assertThat(
            toHistogramDataPoints(
                ImmutableList.of(
                    DoubleHistogramPointData.create(
                        123, 456, KV_ATTR, 14.2, ImmutableList.of(1.0), ImmutableList.of(1L, 5L)),
                    DoubleHistogramPointData.create(
                        123,
                        456,
                        Attributes.empty(),
                        15.3,
                        ImmutableList.of(),
                        ImmutableList.of(7L),
                        ImmutableList.of(
                            DoubleExemplar.create(
                                Attributes.of(stringKey("test"), "value"),
                                2,
                                /*spanId=*/ "0000000000000002",
                                /*traceId=*/ "00000000000000000000000000000001",
                                1.5))))))
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
                .addExemplars(
                    Exemplar.newBuilder()
                        .setTimeUnixNano(2)
                        .addFilteredAttributes(
                            KeyValue.newBuilder()
                                .setKey("test")
                                .setValue(stringValue("value"))
                                .build())
                        .setSpanId(ByteString.copyFrom(new byte[] {0, 0, 0, 0, 0, 0, 0, 2}))
                        .setTraceId(
                            ByteString.copyFrom(
                                new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}))
                        .setAsDouble(1.5)
                        .build())
                .build());
  }

  @Test
  void toProtoMetric_monotonic() {
    assertThat(
            toProtoMetric(
                MetricData.createLongSum(
                    Resource.empty(),
                    InstrumentationLibraryInfo.empty(),
                    "name",
                    "description",
                    "1",
                    LongSumData.create(
                        /* isMonotonic= */ true,
                        AggregationTemporality.CUMULATIVE,
                        singletonList(LongPointData.create(123, 456, KV_ATTR, 5))))))
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
            toProtoMetric(
                MetricData.createDoubleSum(
                    Resource.empty(),
                    InstrumentationLibraryInfo.empty(),
                    "name",
                    "description",
                    "1",
                    DoubleSumData.create(
                        /* isMonotonic= */ true,
                        AggregationTemporality.CUMULATIVE,
                        singletonList(DoublePointData.create(123, 456, KV_ATTR, 5.1))))))
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
            toProtoMetric(
                MetricData.createLongSum(
                    Resource.empty(),
                    InstrumentationLibraryInfo.empty(),
                    "name",
                    "description",
                    "1",
                    LongSumData.create(
                        /* isMonotonic= */ false,
                        AggregationTemporality.CUMULATIVE,
                        singletonList(LongPointData.create(123, 456, KV_ATTR, 5))))))
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
            toProtoMetric(
                MetricData.createDoubleSum(
                    Resource.empty(),
                    InstrumentationLibraryInfo.empty(),
                    "name",
                    "description",
                    "1",
                    DoubleSumData.create(
                        /* isMonotonic= */ false,
                        AggregationTemporality.CUMULATIVE,
                        singletonList(DoublePointData.create(123, 456, KV_ATTR, 5.1))))))
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
            toProtoMetric(
                MetricData.createLongGauge(
                    Resource.empty(),
                    InstrumentationLibraryInfo.empty(),
                    "name",
                    "description",
                    "1",
                    LongGaugeData.create(
                        singletonList(LongPointData.create(123, 456, KV_ATTR, 5))))))
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
            toProtoMetric(
                MetricData.createDoubleGauge(
                    Resource.empty(),
                    InstrumentationLibraryInfo.empty(),
                    "name",
                    "description",
                    "1",
                    DoubleGaugeData.create(
                        singletonList(DoublePointData.create(123, 456, KV_ATTR, 5.1))))))
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
            toProtoMetric(
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
                                KV_ATTR,
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
            toProtoMetric(
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
                                KV_ATTR,
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
  void protoResourceMetrics() {
    Resource resource =
        Resource.create(Attributes.of(stringKey("ka"), "va"), "http://resource.url");
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
            toProtoResourceMetrics(
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
                                DoublePointData.create(123, 456, KV_ATTR, 5.0)))),
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
                                DoublePointData.create(123, 456, KV_ATTR, 5.0)))),
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
                                DoublePointData.create(123, 456, KV_ATTR, 5.0)))),
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
                                DoublePointData.create(123, 456, KV_ATTR, 5.0)))))))
        .satisfiesExactlyInAnyOrder(
            resourceMetrics -> {
              assertThat(resourceMetrics.getResource()).isEqualTo(resourceProto);
              assertThat(resourceMetrics.getSchemaUrl()).isEqualTo("http://resource.url");
              assertThat(resourceMetrics.getInstrumentationLibraryMetricsList())
                  .containsExactlyInAnyOrder(
                      InstrumentationLibraryMetrics.newBuilder()
                          .setInstrumentationLibrary(instrumentationLibraryProto)
                          .addAllMetrics(ImmutableList.of(metricDoubleSum, metricDoubleSum))
                          .setSchemaUrl("http://url")
                          .build());
            },
            resourceMetrics -> {
              assertThat(resourceMetrics.getResource()).isEqualTo(emptyResourceProto);
              assertThat(resourceMetrics.getInstrumentationLibraryMetricsList())
                  .containsExactlyInAnyOrder(
                      InstrumentationLibraryMetrics.newBuilder()
                          .setInstrumentationLibrary(emptyInstrumentationLibraryProto)
                          .addAllMetrics(singletonList(metricDoubleSum))
                          .build(),
                      InstrumentationLibraryMetrics.newBuilder()
                          .setInstrumentationLibrary(instrumentationLibraryProto)
                          .addAllMetrics(singletonList(metricDoubleSum))
                          .setSchemaUrl("http://url")
                          .build());
            });
  }

  private static List<NumberDataPoint> toNumberDataPoints(Collection<? extends PointData> points) {
    return points.stream()
        .map(
            point ->
                parse(
                    NumberDataPoint.parser(),
                    toByteArray(MetricRequestMarshaler.NumberDataPointMarshaler.create(point))))
        .collect(Collectors.toList());
  }

  private static List<SummaryDataPoint> toSummaryDataPoints(
      Collection<DoubleSummaryPointData> points) {
    return points.stream()
        .map(
            point ->
                parse(
                    SummaryDataPoint.parser(),
                    toByteArray(MetricRequestMarshaler.SummaryDataPointMarshaler.create(point))))
        .collect(Collectors.toList());
  }

  private static List<HistogramDataPoint> toHistogramDataPoints(
      Collection<DoubleHistogramPointData> points) {
    return points.stream()
        .map(
            point ->
                parse(
                    HistogramDataPoint.parser(),
                    toByteArray(MetricRequestMarshaler.HistogramDataPointMarshaler.create(point))))
        .collect(Collectors.toList());
  }

  private static Metric toProtoMetric(MetricData metricData) {
    return parse(
        Metric.parser(), toByteArray(MetricRequestMarshaler.MetricMarshaler.create(metricData)));
  }

  private static List<ResourceMetrics> toProtoResourceMetrics(
      Collection<MetricData> metricDataList) {
    ExportMetricsServiceRequest exportRequest =
        parse(
            ExportMetricsServiceRequest.parser(),
            toByteArray(MetricRequestMarshaler.create(metricDataList)));
    return exportRequest.getResourceMetricsList();
  }

  private static <T extends Message> T parse(Parser<T> parser, byte[] serialized) {
    final T result;
    try {
      result = parser.parseFrom(serialized);
    } catch (InvalidProtocolBufferException e) {
      throw new UncheckedIOException(e);
    }
    // Our marshaler should produce the exact same length of serialized output (for example, field
    // default values are not outputted), so we check that here. The output itself may have slightly
    // different ordering, mostly due to the way we don't output oneof values in field order all the
    // tieme. If the lengths are equal and the resulting protos are equal, the marshaling is
    // guaranteed to be valid.
    assertThat(result.getSerializedSize()).isEqualTo(serialized.length);
    return result;
  }

  private static byte[] toByteArray(Marshaler marshaler) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    CodedOutputStream cos = CodedOutputStream.newInstance(bos);
    try {
      marshaler.writeTo(cos);
      cos.flush();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return bos.toByteArray();
  }
}
