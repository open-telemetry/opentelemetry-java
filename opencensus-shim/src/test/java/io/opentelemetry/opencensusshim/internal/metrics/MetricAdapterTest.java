/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim.internal.metrics;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;

import io.opencensus.common.Timestamp;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.data.AttachmentValue;
import io.opencensus.metrics.data.Exemplar;
import io.opencensus.metrics.export.Distribution;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor;
import io.opencensus.metrics.export.Point;
import io.opencensus.metrics.export.Summary;
import io.opencensus.metrics.export.TimeSeries;
import io.opencensus.metrics.export.Value;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoubleExemplarData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MetricAdapterTest {

  private static final Resource RESOURCE =
      Resource.create(Attributes.of(AttributeKey.stringKey("test"), "resource"));

  @Test
  void convertsTimeStamps() {
    assertThat(MetricAdapter.mapTimestamp(Timestamp.create(1, 2))).isEqualTo(1000000002L);
  }

  @Test
  void convertsLongValue() {
    assertThat(MetricAdapter.longValue(Point.create(Value.longValue(5), Timestamp.fromMillis(2))))
        .isEqualTo(5);
  }

  @Test
  void convertsDoubleValue() {
    assertThat(
            MetricAdapter.doubleValue(Point.create(Value.doubleValue(5), Timestamp.fromMillis(2))))
        .isEqualTo(5);
  }

  @Test
  void convertsLongGauge() {
    Metric censusMetric =
        Metric.createWithOneTimeSeries(
            MetricDescriptor.create(
                "name",
                "description",
                "unit",
                MetricDescriptor.Type.GAUGE_INT64,
                Collections.singletonList(LabelKey.create("key1", "desc1"))),
            TimeSeries.create(
                Collections.singletonList(LabelValue.create("value1")),
                Collections.singletonList(
                    Point.create(Value.longValue(4), Timestamp.fromMillis(2000))),
                Timestamp.fromMillis(1000)));

    assertThat(MetricAdapter.convert(RESOURCE, censusMetric))
        .hasResource(RESOURCE)
        .hasInstrumentationScope(MetricAdapter.INSTRUMENTATION_SCOPE_INFO)
        .hasName("name")
        .hasDescription("description")
        .hasUnit("unit")
        .hasLongGaugeSatisfying(
            gauge ->
                gauge.hasPointsSatisfying(
                    point ->
                        point
                            .hasValue(4)
                            .hasStartEpochNanos(1000000000)
                            .hasEpochNanos(2000000000)
                            .hasAttributes(attributeEntry("key1", "value1"))));
  }

  @Test
  void convertsDoubleGauge() {
    Metric censusMetric =
        Metric.createWithOneTimeSeries(
            MetricDescriptor.create(
                "name",
                "description",
                "unit",
                MetricDescriptor.Type.GAUGE_DOUBLE,
                Collections.singletonList(LabelKey.create("key1", "desc1"))),
            TimeSeries.create(
                Collections.singletonList(LabelValue.create("value1")),
                Collections.singletonList(
                    Point.create(Value.doubleValue(4), Timestamp.fromMillis(2000))),
                Timestamp.fromMillis(1000)));

    assertThat(MetricAdapter.convert(RESOURCE, censusMetric))
        .hasResource(RESOURCE)
        .hasInstrumentationScope(MetricAdapter.INSTRUMENTATION_SCOPE_INFO)
        .hasName("name")
        .hasDescription("description")
        .hasUnit("unit")
        .hasDoubleGaugeSatisfying(
            gauge ->
                gauge.hasPointsSatisfying(
                    point ->
                        point
                            .hasStartEpochNanos(1000000000)
                            .hasEpochNanos(2000000000)
                            .hasAttributes(attributeEntry("key1", "value1"))
                            .hasValue(4)));
  }

  @Test
  void convertsLongSum() {
    Metric censusMetric =
        Metric.createWithOneTimeSeries(
            MetricDescriptor.create(
                "name",
                "description",
                "unit",
                MetricDescriptor.Type.CUMULATIVE_INT64,
                Collections.singletonList(LabelKey.create("key1", "desc1"))),
            TimeSeries.create(
                Collections.singletonList(LabelValue.create("value1")),
                Collections.singletonList(
                    Point.create(Value.longValue(4), Timestamp.fromMillis(2000))),
                Timestamp.fromMillis(1000)));

    assertThat(MetricAdapter.convert(RESOURCE, censusMetric))
        .hasResource(RESOURCE)
        .hasInstrumentationScope(MetricAdapter.INSTRUMENTATION_SCOPE_INFO)
        .hasName("name")
        .hasDescription("description")
        .hasUnit("unit")
        .hasLongSumSatisfying(
            sum ->
                sum.isCumulative()
                    .isMonotonic()
                    .hasPointsSatisfying(
                        point ->
                            point
                                .hasStartEpochNanos(1000000000)
                                .hasEpochNanos(2000000000)
                                .hasAttributes(attributeEntry("key1", "value1"))
                                .hasValue(4)));
  }

  @Test
  void convertsDoubleSum() {
    Metric censusMetric =
        Metric.createWithOneTimeSeries(
            MetricDescriptor.create(
                "name",
                "description",
                "unit",
                MetricDescriptor.Type.CUMULATIVE_DOUBLE,
                Collections.singletonList(LabelKey.create("key1", "desc1"))),
            TimeSeries.create(
                Collections.singletonList(LabelValue.create("value1")),
                Collections.singletonList(
                    Point.create(Value.doubleValue(4), Timestamp.fromMillis(2000))),
                Timestamp.fromMillis(1000)));

    assertThat(MetricAdapter.convert(RESOURCE, censusMetric))
        .hasResource(RESOURCE)
        .hasInstrumentationScope(MetricAdapter.INSTRUMENTATION_SCOPE_INFO)
        .hasName("name")
        .hasDescription("description")
        .hasUnit("unit")
        .hasDoubleSumSatisfying(
            sum ->
                sum.isCumulative()
                    .isMonotonic()
                    .hasPointsSatisfying(
                        point ->
                            point
                                .hasStartEpochNanos(1000000000)
                                .hasEpochNanos(2000000000)
                                .hasAttributes(attributeEntry("key1", "value1"))
                                .hasValue(4)));
  }

  @Test
  void convertHistogram() {
    Map<String, AttachmentValue> exemplarAttachments = new HashMap<>();
    // TODO - Import opencensus util for a code-dependent test on common exemplar-trace usage.
    exemplarAttachments.put(
        "SpanContext",
        AttachmentValue.AttachmentValueString.create(
            "SpanContext{traceId=TraceId{traceId=00000000000000000000000000000001}, spanId=SpanId{spanId=0000000000000002}, others=stuff}"));
    Metric censusMetric =
        Metric.createWithOneTimeSeries(
            MetricDescriptor.create(
                "name",
                "description",
                "unit",
                MetricDescriptor.Type.CUMULATIVE_DISTRIBUTION,
                Collections.singletonList(LabelKey.create("key1", "desc1"))),
            TimeSeries.create(
                Collections.singletonList(LabelValue.create("value1")),
                Collections.singletonList(
                    Point.create(
                        Value.distributionValue(
                            Distribution.create(
                                10,
                                5,
                                2, // Sum of squared deviations, ignored
                                Distribution.BucketOptions.explicitOptions(Arrays.asList(2.0, 5.0)),
                                Arrays.asList(
                                    Distribution.Bucket.create(
                                        2,
                                        Exemplar.create(
                                            1.0, Timestamp.fromMillis(2), Collections.emptyMap())),
                                    Distribution.Bucket.create(
                                        6,
                                        Exemplar.create(
                                            4.0, Timestamp.fromMillis(1), exemplarAttachments)),
                                    Distribution.Bucket.create(2)))),
                        Timestamp.fromMillis(2000))),
                Timestamp.fromMillis(1000)));

    assertThat(MetricAdapter.convert(RESOURCE, censusMetric))
        .hasResource(RESOURCE)
        .hasInstrumentationScope(MetricAdapter.INSTRUMENTATION_SCOPE_INFO)
        .hasName("name")
        .hasDescription("description")
        .hasUnit("unit")
        .hasHistogramSatisfying(
            histogram ->
                histogram
                    .isCumulative()
                    .hasPointsSatisfying(
                        point ->
                            point
                                .hasStartEpochNanos(1000000000)
                                .hasEpochNanos(2000000000)
                                .hasSum(5)
                                .hasCount(10)
                                .hasBucketBoundaries(2.0, 5.0)
                                .hasBucketCounts(2, 6, 2)
                                .hasExemplars(
                                    ImmutableDoubleExemplarData.create(
                                        Attributes.empty(), 2000000, SpanContext.getInvalid(), 1.0),
                                    ImmutableDoubleExemplarData.create(
                                        Attributes.empty(),
                                        1000000,
                                        SpanContext.create(
                                            "00000000000000000000000000000001",
                                            "0000000000000002",
                                            TraceFlags.getDefault(),
                                            TraceState.getDefault()),
                                        4.0))));
  }

  @Test
  void convertSummary() {
    Metric censusMetric =
        Metric.createWithOneTimeSeries(
            MetricDescriptor.create(
                "name",
                "description",
                "unit",
                MetricDescriptor.Type.SUMMARY,
                Collections.singletonList(LabelKey.create("key1", "desc1"))),
            TimeSeries.create(
                Collections.singletonList(LabelValue.create("value1")),
                Collections.singletonList(
                    Point.create(
                        Value.summaryValue(
                            Summary.create(
                                10L,
                                5d,
                                Summary.Snapshot.create(
                                    10L,
                                    5d,
                                    Collections.singletonList(
                                        Summary.Snapshot.ValueAtPercentile.create(100.0, 200))))),
                        Timestamp.fromMillis(2000))),
                Timestamp.fromMillis(1000)));

    assertThat(MetricAdapter.convert(RESOURCE, censusMetric))
        .hasResource(RESOURCE)
        .hasInstrumentationScope(MetricAdapter.INSTRUMENTATION_SCOPE_INFO)
        .hasName("name")
        .hasDescription("description")
        .hasUnit("unit")
        .hasSummarySatisfying(
            summary ->
                summary.hasPointsSatisfying(
                    point ->
                        point
                            .hasStartEpochNanos(1000000000)
                            .hasEpochNanos(2000000000)
                            .hasAttributes(attributeEntry("key1", "value1"))
                            .hasCount(10)
                            .hasSum(5)
                            .hasValuesSatisfying(value -> value.hasValue(200.0).hasQuantile(1.0))));
  }

  @Test
  void convertGaugeHistogram() {
    Map<String, AttachmentValue> exemplarAttachments = new HashMap<>();
    // TODO - Import opencensus util for a code-dependent test on common exemplar-trace usage.
    exemplarAttachments.put(
        "SpanContext",
        AttachmentValue.AttachmentValueString.create(
            "SpanContext{traceId=TraceId{traceId=00000000000000000000000000000001}, spanId=SpanId{spanId=0000000000000002}, others=stuff}"));
    Metric censusMetric =
        Metric.createWithOneTimeSeries(
            MetricDescriptor.create(
                "name",
                "description",
                "unit",
                MetricDescriptor.Type.GAUGE_DISTRIBUTION,
                Collections.singletonList(LabelKey.create("key1", "desc1"))),
            TimeSeries.create(
                Collections.singletonList(LabelValue.create("value1")),
                Collections.singletonList(
                    Point.create(
                        Value.distributionValue(
                            Distribution.create(
                                10,
                                5,
                                2, // Sum of squared deviations, ignored
                                Distribution.BucketOptions.explicitOptions(Arrays.asList(2.0, 5.0)),
                                Arrays.asList(
                                    Distribution.Bucket.create(
                                        2,
                                        Exemplar.create(
                                            1.0, Timestamp.fromMillis(2), Collections.emptyMap())),
                                    Distribution.Bucket.create(
                                        6,
                                        Exemplar.create(
                                            4.0, Timestamp.fromMillis(1), exemplarAttachments)),
                                    Distribution.Bucket.create(2)))),
                        Timestamp.fromMillis(2000))),
                Timestamp.fromMillis(1000)));
    assertThat(MetricAdapter.convert(RESOURCE, censusMetric))
        .hasResource(RESOURCE)
        .hasInstrumentationScope(MetricAdapter.INSTRUMENTATION_SCOPE_INFO)
        .hasName("name")
        .hasDescription("description")
        .hasUnit("unit")
        .hasHistogramSatisfying(
            histogram ->
                histogram
                    .isDelta()
                    .hasPointsSatisfying(
                        point ->
                            point
                                .hasStartEpochNanos(2000000000)
                                .hasEpochNanos(2000000000)
                                .hasSum(5)
                                .hasCount(10)
                                .hasBucketBoundaries(2.0, 5.0)
                                .hasBucketCounts(2, 6, 2)
                                .hasExemplars(
                                    ImmutableDoubleExemplarData.create(
                                        Attributes.empty(), 2000000, SpanContext.getInvalid(), 1.0),
                                    ImmutableDoubleExemplarData.create(
                                        Attributes.empty(),
                                        1000000,
                                        SpanContext.create(
                                            "00000000000000000000000000000001",
                                            "0000000000000002",
                                            TraceFlags.getDefault(),
                                            TraceState.getDefault()),
                                        4.0))));
  }
}
