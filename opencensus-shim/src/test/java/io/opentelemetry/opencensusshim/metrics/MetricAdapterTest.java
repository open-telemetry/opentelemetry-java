/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim.metrics;

import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

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
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.ValueAtPercentile;
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
                Arrays.asList(LabelKey.create("key1", "desc1"))),
            TimeSeries.create(
                Arrays.asList(LabelValue.create("value1")),
                Arrays.asList(Point.create(Value.longValue(4), Timestamp.fromMillis(2000))),
                Timestamp.fromMillis(1000)));

    assertThat(MetricAdapter.convert(RESOURCE, censusMetric))
        .hasResource(RESOURCE)
        .hasInstrumentationLibrary(MetricAdapter.INSTRUMENTATION_LIBRARY_INFO)
        .hasName("name")
        .hasDescription("description")
        .hasUnit("unit")
        .hasLongGauge()
        .points()
        .satisfiesExactly(
            point ->
                assertThat(point)
                    .hasStartEpochNanos(1000000000)
                    .hasEpochNanos(2000000000)
                    .hasAttributes(Attributes.of(AttributeKey.stringKey("key1"), "value1"))
                    .hasValue(4));
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
                Arrays.asList(LabelKey.create("key1", "desc1"))),
            TimeSeries.create(
                Arrays.asList(LabelValue.create("value1")),
                Arrays.asList(Point.create(Value.doubleValue(4), Timestamp.fromMillis(2000))),
                Timestamp.fromMillis(1000)));

    assertThat(MetricAdapter.convert(RESOURCE, censusMetric))
        .hasResource(RESOURCE)
        .hasInstrumentationLibrary(MetricAdapter.INSTRUMENTATION_LIBRARY_INFO)
        .hasName("name")
        .hasDescription("description")
        .hasUnit("unit")
        .hasDoubleGauge()
        .points()
        .satisfiesExactly(
            point ->
                assertThat(point)
                    .hasStartEpochNanos(1000000000)
                    .hasEpochNanos(2000000000)
                    .hasAttributes(Attributes.of(AttributeKey.stringKey("key1"), "value1"))
                    .hasValue(4));
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
                Arrays.asList(LabelKey.create("key1", "desc1"))),
            TimeSeries.create(
                Arrays.asList(LabelValue.create("value1")),
                Arrays.asList(Point.create(Value.longValue(4), Timestamp.fromMillis(2000))),
                Timestamp.fromMillis(1000)));

    assertThat(MetricAdapter.convert(RESOURCE, censusMetric))
        .hasResource(RESOURCE)
        .hasInstrumentationLibrary(MetricAdapter.INSTRUMENTATION_LIBRARY_INFO)
        .hasName("name")
        .hasDescription("description")
        .hasUnit("unit")
        .hasLongSum()
        .isCumulative()
        .isMonotonic()
        .points()
        .satisfiesExactly(
            point ->
                assertThat(point)
                    .hasStartEpochNanos(1000000000)
                    .hasEpochNanos(2000000000)
                    .hasAttributes(Attributes.of(AttributeKey.stringKey("key1"), "value1"))
                    .hasValue(4));
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
                Arrays.asList(LabelKey.create("key1", "desc1"))),
            TimeSeries.create(
                Arrays.asList(LabelValue.create("value1")),
                Arrays.asList(Point.create(Value.doubleValue(4), Timestamp.fromMillis(2000))),
                Timestamp.fromMillis(1000)));

    assertThat(MetricAdapter.convert(RESOURCE, censusMetric))
        .hasResource(RESOURCE)
        .hasInstrumentationLibrary(MetricAdapter.INSTRUMENTATION_LIBRARY_INFO)
        .hasName("name")
        .hasDescription("description")
        .hasUnit("unit")
        .hasDoubleSum()
        .isCumulative()
        .isMonotonic()
        .points()
        .satisfiesExactly(
            point ->
                assertThat(point)
                    .hasStartEpochNanos(1000000000)
                    .hasEpochNanos(2000000000)
                    .hasAttributes(Attributes.of(AttributeKey.stringKey("key1"), "value1"))
                    .hasValue(4));
  }

  @Test
  void convertHistogram() {
    Map<String, AttachmentValue> exemplarAttachements = new HashMap<>();
    // TODO - Import opencensus util for a code-dependent test on common exemplar-trace usage.
    exemplarAttachements.put(
        "SpanContext",
        AttachmentValue.AttachmentValueString.create(
            "SpanContext(traceId=1234, spanId=5678, others=stuff)"));
    Metric censusMetric =
        Metric.createWithOneTimeSeries(
            MetricDescriptor.create(
                "name",
                "description",
                "unit",
                MetricDescriptor.Type.CUMULATIVE_DISTRIBUTION,
                Arrays.asList(LabelKey.create("key1", "desc1"))),
            TimeSeries.create(
                Arrays.asList(LabelValue.create("value1")),
                Arrays.asList(
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
                                            4.0, Timestamp.fromMillis(1), exemplarAttachements)),
                                    Distribution.Bucket.create(2)))),
                        Timestamp.fromMillis(2000))),
                Timestamp.fromMillis(1000)));

    assertThat(MetricAdapter.convert(RESOURCE, censusMetric))
        .hasResource(RESOURCE)
        .hasInstrumentationLibrary(MetricAdapter.INSTRUMENTATION_LIBRARY_INFO)
        .hasName("name")
        .hasDescription("description")
        .hasUnit("unit")
        .hasDoubleHistogram()
        .isCumulative()
        .points()
        .satisfiesExactly(
            point ->
                assertThat(point)
                    .hasStartEpochNanos(1000000000)
                    .hasEpochNanos(2000000000)
                    .hasSum(5)
                    .hasCount(10)
                    .hasBucketBoundaries(2.0, 5.0)
                    .hasBucketCounts(2, 6, 2)
                    .hasExemplars(
                        DoubleExemplarData.create(
                            Attributes.empty(),
                            2000000,
                            /* spanId= */ null,
                            /* traceId= */ null,
                            1.0),
                        DoubleExemplarData.create(
                            Attributes.empty(), 1000000, "5678", "1234", 4.0)));
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
                Arrays.asList(LabelKey.create("key1", "desc1"))),
            TimeSeries.create(
                Arrays.asList(LabelValue.create("value1")),
                Arrays.asList(
                    Point.create(
                        Value.summaryValue(
                            Summary.create(
                                10L,
                                5d,
                                Summary.Snapshot.create(
                                    10L,
                                    5d,
                                    Arrays.asList(
                                        Summary.Snapshot.ValueAtPercentile.create(1.0, 200))))),
                        Timestamp.fromMillis(2000))),
                Timestamp.fromMillis(1000)));

    assertThat(MetricAdapter.convert(RESOURCE, censusMetric))
        .hasResource(RESOURCE)
        .hasInstrumentationLibrary(MetricAdapter.INSTRUMENTATION_LIBRARY_INFO)
        .hasName("name")
        .hasDescription("description")
        .hasUnit("unit")
        .hasDoubleSummary()
        .points()
        .satisfiesExactly(
            point ->
                assertThat(point)
                    .hasStartEpochNanos(1000000000)
                    .hasEpochNanos(2000000000)
                    .hasAttributes(Attributes.of(AttributeKey.stringKey("key1"), "value1"))
                    .hasCount(10)
                    .hasSum(5)
                    .hasPercentileValues(ValueAtPercentile.create(1.0, 200)));
  }

  @Test
  void convertGaugeHistogram() {
    Map<String, AttachmentValue> exemplarAttachements = new HashMap<>();
    // TODO - Import opencensus util for a code-dependent test on common exemplar-trace usage.
    exemplarAttachements.put(
        "SpanContext",
        AttachmentValue.AttachmentValueString.create(
            "SpanContext(traceId=1234, spanId=5678, others=stuff)"));
    Metric censusMetric =
        Metric.createWithOneTimeSeries(
            MetricDescriptor.create(
                "name",
                "description",
                "unit",
                MetricDescriptor.Type.GAUGE_DISTRIBUTION,
                Arrays.asList(LabelKey.create("key1", "desc1"))),
            TimeSeries.create(
                Arrays.asList(LabelValue.create("value1")),
                Arrays.asList(
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
                                            4.0, Timestamp.fromMillis(1), exemplarAttachements)),
                                    Distribution.Bucket.create(2)))),
                        Timestamp.fromMillis(2000))),
                Timestamp.fromMillis(1000)));
    assertThat(MetricAdapter.convert(RESOURCE, censusMetric))
        .hasResource(RESOURCE)
        .hasInstrumentationLibrary(MetricAdapter.INSTRUMENTATION_LIBRARY_INFO)
        .hasName("name")
        .hasDescription("description")
        .hasUnit("unit")
        .hasDoubleHistogram()
        .isDelta()
        .points()
        .satisfiesExactly(
            point ->
                assertThat(point)
                    .hasStartEpochNanos(2000000000)
                    .hasEpochNanos(2000000000)
                    .hasSum(5)
                    .hasCount(10)
                    .hasBucketBoundaries(2.0, 5.0)
                    .hasBucketCounts(2, 6, 2)
                    .hasExemplars(
                        DoubleExemplarData.create(
                            Attributes.empty(),
                            2000000,
                            /* spanId= */ null,
                            /* traceId= */ null,
                            1.0),
                        DoubleExemplarData.create(
                            Attributes.empty(), 1000000, "5678", "1234", 4.0)));
  }
}
