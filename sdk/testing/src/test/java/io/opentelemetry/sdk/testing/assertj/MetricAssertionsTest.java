/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.equalTo;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.satisfies;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.offset;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData;
import io.opentelemetry.sdk.metrics.data.HistogramPointData;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.SummaryPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoubleExemplarData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoublePointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableExponentialHistogramData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableExponentialHistogramPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongExemplarData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSummaryData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSummaryPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableValueAtQuantile;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

// We always assert the contents of lists out of order to verify that assertions behave that way.
class MetricAssertionsTest {
  private static final String TRACE_ID = "00000000000000010000000000000002";
  private static final String SPAN_ID1 = "0000000000000003";
  private static final String SPAN_ID2 = "0000000000000004";

  private static final Resource RESOURCE =
      Resource.create(Attributes.builder().put("dog", "bark").put("dog is cute", true).build());
  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.builder("opentelemetry").setVersion("1.0").build();

  private static final AttributeKey<String> DOG = AttributeKey.stringKey("dog");
  private static final AttributeKey<String> BEAR = AttributeKey.stringKey("bear");
  private static final AttributeKey<String> CAT = AttributeKey.stringKey("cat");
  private static final AttributeKey<Boolean> WARM = AttributeKey.booleanKey("warm");
  private static final AttributeKey<Long> TEMPERATURE = AttributeKey.longKey("temperature");
  private static final AttributeKey<Double> LENGTH = AttributeKey.doubleKey("length");
  private static final AttributeKey<List<String>> COLORS = AttributeKey.stringArrayKey("colors");
  private static final AttributeKey<List<Boolean>> CONDITIONS =
      AttributeKey.booleanArrayKey("conditions");
  private static final AttributeKey<List<Long>> SCORES = AttributeKey.longArrayKey("scores");
  private static final AttributeKey<List<Double>> COINS = AttributeKey.doubleArrayKey("coins");

  private static final Attributes ATTRIBUTES =
      Attributes.builder()
          .put(BEAR, "mya")
          .put(WARM, true)
          .put(TEMPERATURE, 30)
          .put(LENGTH, 1.2)
          .put(COLORS, Arrays.asList("red", "blue"))
          .put(CONDITIONS, Arrays.asList(false, true))
          .put(SCORES, Arrays.asList(0L, 1L))
          .put(COINS, Arrays.asList(0.01, 0.05, 0.1))
          .build();

  private static final DoubleExemplarData DOUBLE_EXEMPLAR1 =
      ImmutableDoubleExemplarData.create(
          ATTRIBUTES,
          0,
          SpanContext.create(TRACE_ID, SPAN_ID1, TraceFlags.getDefault(), TraceState.getDefault()),
          1.0);

  private static final DoubleExemplarData DOUBLE_EXEMPLAR2 =
      ImmutableDoubleExemplarData.create(
          Attributes.empty(),
          2,
          SpanContext.create(TRACE_ID, SPAN_ID2, TraceFlags.getDefault(), TraceState.getDefault()),
          2.0);

  private static final DoublePointData DOUBLE_POINT_DATA =
      ImmutableDoublePointData.create(1, 2, ATTRIBUTES, 3.0, Collections.emptyList());

  private static final DoublePointData DOUBLE_POINT_DATA_WITH_EXEMPLAR =
      ImmutableDoublePointData.create(
          3, 4, Attributes.empty(), 3.0, Arrays.asList(DOUBLE_EXEMPLAR1, DOUBLE_EXEMPLAR2));

  private static final LongExemplarData LONG_EXEMPLAR1 =
      ImmutableLongExemplarData.create(
          ATTRIBUTES,
          0,
          SpanContext.create(TRACE_ID, SPAN_ID1, TraceFlags.getDefault(), TraceState.getDefault()),
          1);

  private static final LongExemplarData LONG_EXEMPLAR2 =
      ImmutableLongExemplarData.create(
          Attributes.empty(),
          2,
          SpanContext.create(TRACE_ID, SPAN_ID2, TraceFlags.getDefault(), TraceState.getDefault()),
          // TODO(anuraaga): Currently there is no good way of asserting on actual long exemplar
          // values.
          2);

  private static final LongPointData LONG_POINT_DATA =
      ImmutableLongPointData.create(1, 2, ATTRIBUTES, 1, Collections.emptyList());

  private static final LongPointData LONG_POINT_DATA_WITH_EXEMPLAR =
      ImmutableLongPointData.create(
          3, 4, Attributes.empty(), Long.MAX_VALUE, Arrays.asList(LONG_EXEMPLAR1, LONG_EXEMPLAR2));

  private static final MetricData DOUBLE_GAUGE_METRIC =
      ImmutableMetricData.createDoubleGauge(
          RESOURCE,
          INSTRUMENTATION_SCOPE_INFO,
          /* name= */ "gauge",
          /* description= */ "a gauge",
          /* unit= */ "1",
          ImmutableGaugeData.create(
              // Points
              Arrays.asList(DOUBLE_POINT_DATA, DOUBLE_POINT_DATA_WITH_EXEMPLAR)));

  private static final MetricData LONG_GAUGE_METRIC =
      ImmutableMetricData.createLongGauge(
          RESOURCE,
          INSTRUMENTATION_SCOPE_INFO,
          /* name= */ "gauge",
          /* description= */ "a gauge",
          /* unit= */ "1",
          ImmutableGaugeData.create(
              // Points
              Arrays.asList(LONG_POINT_DATA, LONG_POINT_DATA_WITH_EXEMPLAR)));

  private static final MetricData DOUBLE_SUM_METRIC =
      ImmutableMetricData.createDoubleSum(
          RESOURCE,
          INSTRUMENTATION_SCOPE_INFO,
          /* name= */ "sum",
          /* description= */ "a sum",
          /* unit= */ "1",
          ImmutableSumData.create(
              true,
              AggregationTemporality.CUMULATIVE,
              // Points
              Arrays.asList(DOUBLE_POINT_DATA, DOUBLE_POINT_DATA_WITH_EXEMPLAR)));

  private static final MetricData DOUBLE_SUM_METRIC_DELTA_NONMONOTONIC =
      ImmutableMetricData.createDoubleSum(
          RESOURCE,
          INSTRUMENTATION_SCOPE_INFO,
          /* name= */ "sum",
          /* description= */ "a sum",
          /* unit= */ "1",
          ImmutableSumData.create(
              false,
              AggregationTemporality.DELTA,
              // Points
              Arrays.asList(DOUBLE_POINT_DATA, DOUBLE_POINT_DATA_WITH_EXEMPLAR)));

  private static final MetricData LONG_SUM_METRIC =
      ImmutableMetricData.createLongSum(
          RESOURCE,
          INSTRUMENTATION_SCOPE_INFO,
          /* name= */ "sum",
          /* description= */ "a sum",
          /* unit= */ "1",
          ImmutableSumData.create(
              true,
              AggregationTemporality.CUMULATIVE,
              // Points
              Arrays.asList(LONG_POINT_DATA, LONG_POINT_DATA_WITH_EXEMPLAR)));

  private static final MetricData LONG_SUM_METRIC_DELTA_NONMONOTONIC =
      ImmutableMetricData.createLongSum(
          RESOURCE,
          INSTRUMENTATION_SCOPE_INFO,
          /* name= */ "sum",
          /* description= */ "a sum",
          /* unit= */ "1",
          ImmutableSumData.create(
              false,
              AggregationTemporality.DELTA,
              // Points
              Arrays.asList(LONG_POINT_DATA, LONG_POINT_DATA_WITH_EXEMPLAR)));

  private static final HistogramPointData HISTOGRAM_POINT_DATA =
      ImmutableHistogramPointData.create(
          1,
          2,
          Attributes.empty(),
          15,
          /* hasMin= */ true,
          4.0,
          /* hasMax= */ true,
          7.0,
          Collections.singletonList(10.0),
          Arrays.asList(1L, 2L));

  private static final MetricData HISTOGRAM_METRIC =
      ImmutableMetricData.createDoubleHistogram(
          RESOURCE,
          INSTRUMENTATION_SCOPE_INFO,
          /* name= */ "histogram",
          /* description= */ "a histogram",
          /* unit= */ "1",
          ImmutableHistogramData.create(
              AggregationTemporality.CUMULATIVE,
              // Points
              Collections.singletonList(HISTOGRAM_POINT_DATA)));

  private static final MetricData HISTOGRAM_METRIC_DELTA =
      ImmutableMetricData.createDoubleHistogram(
          RESOURCE,
          INSTRUMENTATION_SCOPE_INFO,
          /* name= */ "histogram",
          /* description= */ "a histogram",
          /* unit= */ "1",
          ImmutableHistogramData.create(
              AggregationTemporality.DELTA,
              // Points
              Collections.singletonList(HISTOGRAM_POINT_DATA)));

  private static final ExponentialHistogramPointData EXPONENTIAL_HISTOGRAM_POINT_DATA =
      ImmutableExponentialHistogramPointData.create(
          1,
          10.0,
          1,
          /* hasMin= */ true,
          2.0,
          /* hasMax= */ true,
          4.0,
          ImmutableExponentialHistogramBuckets.create(1, 10, Arrays.asList(1L, 2L)),
          ImmutableExponentialHistogramBuckets.create(1, 0, Collections.emptyList()),
          1,
          2,
          Attributes.empty(),
          Arrays.asList(DOUBLE_EXEMPLAR1, DOUBLE_EXEMPLAR2));

  private static final MetricData EXPONENTIAL_HISTOGRAM_METRIC =
      ImmutableMetricData.createExponentialHistogram(
          RESOURCE,
          INSTRUMENTATION_SCOPE_INFO,
          /* name= */ "exponential_histogram",
          /* description= */ "description",
          /* unit= */ "unit",
          ImmutableExponentialHistogramData.create(
              AggregationTemporality.CUMULATIVE,
              // Points
              Collections.singletonList(EXPONENTIAL_HISTOGRAM_POINT_DATA)));

  private static final MetricData EXPONENTIAL_HISTOGRAM_DELTA_METRIC =
      ImmutableMetricData.createExponentialHistogram(
          RESOURCE,
          INSTRUMENTATION_SCOPE_INFO,
          /* name= */ "exponential_histogram_delta",
          /* description= */ "description",
          /* unit= */ "unit",
          ImmutableExponentialHistogramData.create(
              AggregationTemporality.DELTA,
              // Points
              Collections.singletonList(EXPONENTIAL_HISTOGRAM_POINT_DATA)));

  private static final SummaryPointData SUMMARY_POINT_DATA =
      ImmutableSummaryPointData.create(
          1,
          2,
          Attributes.empty(),
          1,
          2,
          Collections.singletonList(ImmutableValueAtQuantile.create(0, 1)));

  private static final MetricData SUMMARY_METRIC =
      ImmutableMetricData.createDoubleSummary(
          RESOURCE,
          INSTRUMENTATION_SCOPE_INFO,
          /* name= */ "summary",
          /* description= */ "a summary",
          /* unit= */ "1",
          ImmutableSummaryData.create(
              // Points
              Collections.singletonList(SUMMARY_POINT_DATA)));

  @Test
  @SuppressWarnings("Convert2MethodRef")
  void doubleGauge() {
    assertThat(DOUBLE_GAUGE_METRIC)
        .hasResource(RESOURCE)
        .hasResourceSatisfying(
            resource ->
                resource
                    .hasSchemaUrl(null)
                    .hasAttribute(DOG, "bark")
                    .hasAttributes(
                        Attributes.of(DOG, "bark", AttributeKey.booleanKey("dog is cute"), true))
                    .hasAttributes(
                        attributeEntry("dog", "bark"), attributeEntry("dog is cute", true))
                    .hasAttributesSatisfying(
                        attributes ->
                            assertThat(attributes)
                                .hasSize(2)
                                .containsEntry(AttributeKey.stringKey("dog"), "bark")
                                .hasEntrySatisfying(DOG, value -> assertThat(value).hasSize(4))
                                .hasEntrySatisfying(
                                    AttributeKey.booleanKey("dog is cute"),
                                    value -> assertThat(value).isTrue())))
        .hasResourceSatisfying(
            resource ->
                resource.hasAttributesSatisfying(satisfies(DOG, val -> val.isEqualTo("bark"))))
        .hasResourceSatisfying(
            resource ->
                resource.hasAttributesSatisfyingExactly(
                    equalTo(DOG, "bark"), equalTo(AttributeKey.booleanKey("dog is cute"), true)))
        .hasResourceSatisfying(
            resource ->
                resource.hasAttributesSatisfyingExactly(
                    satisfies(DOG, val -> val.startsWith("bar")),
                    satisfies(AttributeKey.booleanKey("dog is cute"), val -> val.isTrue())))
        .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
        .hasName("gauge")
        .hasDescription("a gauge")
        .hasUnit("1")
        .hasDoubleGaugeSatisfying(
            gauge ->
                gauge.hasPointsSatisfying(
                    point ->
                        point
                            .hasStartEpochNanos(3)
                            .hasEpochNanos(4)
                            .hasAttributes(Attributes.empty())
                            .hasValue(3.0)
                            .hasExemplars(DOUBLE_EXEMPLAR2, DOUBLE_EXEMPLAR1)
                            .hasExemplarsSatisfying(
                                exemplar ->
                                    exemplar
                                        .hasEpochNanos(2)
                                        .hasTraceId(TRACE_ID)
                                        .hasSpanId(SPAN_ID2)
                                        .hasValue(2.0)
                                        .hasFilteredAttributes(Attributes.empty()),
                                exemplar ->
                                    exemplar
                                        .hasEpochNanos(0)
                                        .hasTraceId(TRACE_ID)
                                        .hasSpanId(SPAN_ID1)
                                        .hasValue(1.0)
                                        .hasFilteredAttribute(BEAR, "mya")
                                        .hasFilteredAttribute(equalTo(BEAR, "mya"))
                                        .hasFilteredAttributes(ATTRIBUTES)
                                        .hasFilteredAttributes(
                                            attributeEntry("bear", "mya"),
                                            attributeEntry("warm", true),
                                            attributeEntry("temperature", 30),
                                            attributeEntry("length", 1.2),
                                            attributeEntry("colors", "red", "blue"),
                                            attributeEntry("conditions", false, true),
                                            attributeEntry("scores", 0L, 1L),
                                            attributeEntry("coins", 0.01, 0.05, 0.1))
                                        .hasFilteredAttributesSatisfying(
                                            equalTo(BEAR, "mya"),
                                            equalTo(WARM, true),
                                            equalTo(TEMPERATURE, 30),
                                            equalTo(LENGTH, 1.2))
                                        .hasFilteredAttributesSatisfying(
                                            satisfies(BEAR, val -> val.startsWith("mya")),
                                            satisfies(WARM, val -> val.isTrue()),
                                            satisfies(
                                                TEMPERATURE, val -> val.isGreaterThanOrEqualTo(30)),
                                            satisfies(LENGTH, val -> val.isCloseTo(1, offset(0.3))),
                                            satisfies(
                                                COLORS, val -> val.containsExactly("red", "blue")),
                                            satisfies(
                                                CONDITIONS,
                                                val -> val.containsExactly(false, true)),
                                            satisfies(SCORES, val -> val.containsExactly(0L, 1L)),
                                            satisfies(
                                                COINS, val -> val.containsExactly(0.01, 0.05, 0.1)))
                                        // Demonstrates common usage of many exact matches and one
                                        // needing a loose one.
                                        .hasFilteredAttributesSatisfying(
                                            equalTo(BEAR, "mya"),
                                            equalTo(COLORS, Arrays.asList("red", "blue")),
                                            satisfies(LENGTH, val -> val.isCloseTo(1, offset(0.3))))
                                        .hasFilteredAttributesSatisfyingExactly(
                                            equalTo(BEAR, "mya"),
                                            equalTo(WARM, true),
                                            equalTo(TEMPERATURE, 30L),
                                            equalTo(COLORS, Arrays.asList("red", "blue")),
                                            equalTo(CONDITIONS, Arrays.asList(false, true)),
                                            equalTo(SCORES, Arrays.asList(0L, 1L)),
                                            equalTo(COINS, Arrays.asList(0.01, 0.05, 0.1)),
                                            satisfies(
                                                LENGTH, val -> val.isCloseTo(1, offset(0.3))))),
                    point ->
                        point
                            .hasStartEpochNanos(1)
                            .hasEpochNanos(2)
                            .hasValue(3.0)
                            .hasAttribute(BEAR, "mya")
                            .hasAttribute(equalTo(BEAR, "mya"))
                            .hasAttributes(ATTRIBUTES)
                            .hasAttributes(
                                attributeEntry("bear", "mya"),
                                attributeEntry("warm", true),
                                attributeEntry("temperature", 30),
                                attributeEntry("length", 1.2),
                                attributeEntry("colors", "red", "blue"),
                                attributeEntry("conditions", false, true),
                                attributeEntry("scores", 0L, 1L),
                                attributeEntry("coins", 0.01, 0.05, 0.1))
                            .hasAttributesSatisfying(
                                equalTo(BEAR, "mya"),
                                equalTo(WARM, true),
                                equalTo(TEMPERATURE, 30),
                                equalTo(LENGTH, 1.2),
                                equalTo(COLORS, Arrays.asList("red", "blue")),
                                equalTo(CONDITIONS, Arrays.asList(false, true)),
                                equalTo(SCORES, Arrays.asList(0L, 1L)),
                                equalTo(COINS, Arrays.asList(0.01, 0.05, 0.1)))
                            .hasAttributesSatisfying(
                                satisfies(BEAR, val -> val.startsWith("mya")),
                                satisfies(WARM, val -> val.isTrue()),
                                satisfies(TEMPERATURE, val -> val.isGreaterThanOrEqualTo(30)),
                                satisfies(LENGTH, val -> val.isCloseTo(1, offset(0.3))),
                                satisfies(COLORS, val -> val.containsExactly("red", "blue")),
                                satisfies(CONDITIONS, val -> val.containsExactly(false, true)),
                                satisfies(SCORES, val -> val.containsExactly(0L, 1L)),
                                satisfies(COINS, val -> val.containsExactly(0.01, 0.05, 0.1)))
                            // Demonstrates common usage of many exact matches and one needing a
                            // loose one.
                            .hasAttributesSatisfying(
                                equalTo(BEAR, "mya"),
                                equalTo(WARM, true),
                                equalTo(TEMPERATURE, 30L),
                                equalTo(COLORS, Arrays.asList("red", "blue")),
                                equalTo(CONDITIONS, Arrays.asList(false, true)),
                                equalTo(SCORES, Arrays.asList(0L, 1L)),
                                equalTo(COINS, Arrays.asList(0.01, 0.05, 0.1)),
                                satisfies(LENGTH, val -> val.isCloseTo(1, offset(0.3))))));
  }

  @Test
  @SuppressWarnings("Convert2MethodRef")
  void doubleGaugeFailure() {
    assertThatThrownBy(() -> assertThat(DOUBLE_GAUGE_METRIC).hasResource(Resource.empty()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_GAUGE_METRIC)
                    .hasResourceSatisfying(resource -> resource.hasSchemaUrl("http://example.com")))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_GAUGE_METRIC)
                    .hasResourceSatisfying(resource -> resource.hasAttribute(DOG, "meow")))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_GAUGE_METRIC)
                    .hasResourceSatisfying(
                        resource -> resource.hasAttributes(Attributes.of(DOG, "bark"))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_GAUGE_METRIC)
                    .hasResourceSatisfying(
                        resource -> resource.hasAttributes(attributeEntry("dog is cute", true))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_GAUGE_METRIC)
                    .hasResourceSatisfying(
                        resource ->
                            resource.hasAttributesSatisfying(
                                attributes -> assertThat(attributes).hasSize(1))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_GAUGE_METRIC)
                    .hasResourceSatisfying(
                        resource ->
                            resource.hasAttributesSatisfying(
                                attributes ->
                                    assertThat(attributes)
                                        .containsEntry(AttributeKey.stringKey("dog"), "meow"))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_GAUGE_METRIC)
                    .hasResourceSatisfying(
                        resource ->
                            resource.hasAttributesSatisfying(
                                attributes ->
                                    assertThat(attributes)
                                        .containsEntry(
                                            AttributeKey.booleanKey("dog is cute"), false))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_GAUGE_METRIC)
                    .hasResourceSatisfying(
                        resource ->
                            resource.hasAttributesSatisfying(
                                satisfies(DOG, val -> val.isEqualTo("meow")))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_GAUGE_METRIC)
                    .hasResourceSatisfying(
                        resource -> resource.hasAttributesSatisfyingExactly(equalTo(DOG, "bark"))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_GAUGE_METRIC)
                    .hasResourceSatisfying(
                        resource ->
                            resource.hasAttributesSatisfyingExactly(
                                satisfies(DOG, val -> val.isEqualTo("bark")))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_GAUGE_METRIC)
                    .hasInstrumentationScope(InstrumentationScopeInfo.empty()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(DOUBLE_GAUGE_METRIC).hasName("whoami"))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(DOUBLE_GAUGE_METRIC).hasDescription("whatami"))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(DOUBLE_GAUGE_METRIC).hasUnit("kelvin"))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(DOUBLE_GAUGE_METRIC).hasLongGaugeSatisfying(gauge -> {}))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_GAUGE_METRIC)
                    .hasDoubleGaugeSatisfying(
                        // Not enough points
                        gauge -> gauge.hasPointsSatisfying(point -> {})))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_GAUGE_METRIC)
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point -> point.hasStartEpochNanos(100), point -> {})))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_GAUGE_METRIC)
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point -> point.hasEpochNanos(100), point -> {})))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_GAUGE_METRIC)
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(point -> point.hasValue(100), point -> {})))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_GAUGE_METRIC)
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                // Not enough exemplars
                                point -> point.hasExemplarsSatisfying(exemplar -> {}),
                                point -> {})))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_GAUGE_METRIC)
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                // Not enough exemplars
                                point -> point.hasExemplars(DOUBLE_EXEMPLAR1),
                                point -> {})))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_GAUGE_METRIC)
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point.hasExemplarsSatisfying(
                                        exemplar -> exemplar.hasEpochNanos(100), exemplar -> {}),
                                point -> {})))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_GAUGE_METRIC)
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point.hasExemplarsSatisfying(
                                        exemplar -> exemplar.hasTraceId("trace"), exemplar -> {}),
                                point -> {})))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_GAUGE_METRIC)
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point.hasExemplarsSatisfying(
                                        exemplar -> exemplar.hasSpanId("span"), exemplar -> {}),
                                point -> {})))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_GAUGE_METRIC)
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point.hasExemplarsSatisfying(
                                        exemplar -> exemplar.hasValue(100.0), exemplar -> {}),
                                point -> {})))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_GAUGE_METRIC)
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point.hasExemplarsSatisfying(
                                        exemplar -> exemplar.hasFilteredAttribute(CAT, "garfield"),
                                        exemplar -> {}),
                                point -> {})))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_GAUGE_METRIC)
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point.hasExemplarsSatisfying(
                                        exemplar ->
                                            exemplar.hasFilteredAttributes(
                                                Attributes.of(CAT, "garfield")),
                                        exemplar -> {}),
                                point -> {})))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_GAUGE_METRIC)
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point.hasExemplarsSatisfying(
                                        exemplar ->
                                            // Extra CAT
                                            exemplar.hasFilteredAttributesSatisfying(
                                                satisfies(BEAR, val -> val.startsWith("mya")),
                                                satisfies(CAT, val -> val.startsWith("nya")),
                                                satisfies(WARM, val -> val.isTrue()),
                                                satisfies(
                                                    TEMPERATURE,
                                                    val -> val.isGreaterThanOrEqualTo(30)),
                                                satisfies(
                                                    LENGTH, val -> val.isCloseTo(1, offset(0.3))),
                                                satisfies(
                                                    COLORS,
                                                    val -> val.containsExactly("red", "blue")),
                                                satisfies(
                                                    CONDITIONS,
                                                    val -> val.containsExactly(false, true)),
                                                satisfies(
                                                    SCORES, val -> val.containsExactly(0L, 1L)),
                                                satisfies(
                                                    COINS,
                                                    val -> val.containsExactly(0.01, 0.05, 0.1))),
                                        exemplar -> {}),
                                point -> {})))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_GAUGE_METRIC)
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point.hasExemplarsSatisfying(
                                        exemplar ->
                                            // Extra CAT
                                            exemplar.hasFilteredAttributesSatisfyingExactly(
                                                satisfies(WARM, val -> val.isTrue()),
                                                satisfies(
                                                    TEMPERATURE,
                                                    val -> val.isGreaterThanOrEqualTo(30)),
                                                satisfies(
                                                    LENGTH, val -> val.isCloseTo(1, offset(0.3))),
                                                satisfies(
                                                    COLORS,
                                                    val -> val.containsExactly("red", "blue")),
                                                satisfies(
                                                    CONDITIONS,
                                                    val -> val.containsExactly(false, true)),
                                                satisfies(
                                                    SCORES, val -> val.containsExactly(0L, 1L)),
                                                satisfies(
                                                    COINS,
                                                    val -> val.containsExactly(0.01, 0.05, 0.1))),
                                        exemplar -> {}),
                                point -> {})))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_GAUGE_METRIC)
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point -> point.hasAttributes(Attributes.empty()),
                                point -> point.hasAttribute(CAT, "garfield"))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_GAUGE_METRIC)
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point -> point.hasAttributes(Attributes.empty()),
                                point -> point.hasAttributes(Attributes.of(CAT, "garfield")))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_GAUGE_METRIC)
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point -> point.hasAttributes(Attributes.empty()),
                                // Extra CAT
                                point ->
                                    point.hasAttributesSatisfying(
                                        satisfies(BEAR, val -> val.startsWith("mya")),
                                        satisfies(CAT, val -> val.startsWith("nya")),
                                        satisfies(WARM, val -> val.isTrue()),
                                        satisfies(
                                            TEMPERATURE, val -> val.isGreaterThanOrEqualTo(30)),
                                        satisfies(LENGTH, val -> val.isCloseTo(1, offset(0.3))),
                                        satisfies(
                                            COLORS, val -> val.containsExactly("red", "blue")),
                                        satisfies(
                                            CONDITIONS, val -> val.containsExactly(false, true)),
                                        satisfies(SCORES, val -> val.containsExactly(0L, 1L)),
                                        satisfies(
                                            COINS, val -> val.containsExactly(0.01, 0.05, 0.1))))))
        .isInstanceOf(AssertionError.class);
  }

  // The above tests verify shared behavior in AbstractPointDataAssert and MetricDataAssert so we
  // keep tests simpler
  // by only checking specific logic below.

  @Test
  void longGauge() {
    assertThat(LONG_GAUGE_METRIC)
        .hasLongGaugeSatisfying(
            gauge ->
                gauge.hasPointsSatisfying(
                    point ->
                        point
                            .hasValue(Long.MAX_VALUE)
                            .hasExemplarsSatisfying(
                                exemplar -> exemplar.hasValue(2),
                                exemplar ->
                                    exemplar
                                        .hasValue(1)
                                        .hasFilteredAttributesSatisfying(
                                            equalTo(BEAR, "mya"),
                                            equalTo(WARM, true),
                                            equalTo(TEMPERATURE, 30L),
                                            equalTo(COLORS, Arrays.asList("red", "blue")),
                                            satisfies(LENGTH, val -> val.isCloseTo(1, offset(0.3))))
                                        .hasFilteredAttributesSatisfyingExactly(
                                            equalTo(BEAR, "mya"),
                                            equalTo(WARM, true),
                                            equalTo(TEMPERATURE, 30L),
                                            equalTo(COLORS, Arrays.asList("red", "blue")),
                                            equalTo(CONDITIONS, Arrays.asList(false, true)),
                                            equalTo(SCORES, Arrays.asList(0L, 1L)),
                                            equalTo(COINS, Arrays.asList(0.01, 0.05, 0.1)),
                                            satisfies(
                                                LENGTH, val -> val.isCloseTo(1, offset(0.3))))),
                    point -> point.hasValue(1)));
  }

  @Test
  void longGaugeFailure() {
    assertThatThrownBy(() -> assertThat(LONG_GAUGE_METRIC).hasDoubleGaugeSatisfying(gauge -> {}))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(LONG_GAUGE_METRIC)
                    .hasLongGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                // Not enough points
                                point -> {})))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(LONG_GAUGE_METRIC)
                    .hasLongGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point -> point.hasValue(2), point -> point.hasValue(1))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(LONG_GAUGE_METRIC)
                    .hasLongGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point -> point.hasExemplars(LONG_EXEMPLAR2),
                                point -> point.hasValue(1))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(LONG_GAUGE_METRIC)
                    .hasLongGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point.hasExemplarsSatisfying(
                                        exemplar -> exemplar.hasValue(100), exemplar -> {}),
                                point -> point.hasValue(1))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(LONG_GAUGE_METRIC)
                    .hasLongGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point.hasExemplarsSatisfying(
                                        exemplar ->
                                            exemplar.hasFilteredAttributesSatisfying(
                                                equalTo(CAT, "mya"), equalTo(WARM, true))))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(LONG_GAUGE_METRIC)
                    .hasLongGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point.hasExemplarsSatisfying(
                                        exemplar ->
                                            exemplar.hasFilteredAttributesSatisfyingExactly(
                                                equalTo(BEAR, "mya"),
                                                equalTo(WARM, true),
                                                satisfies(
                                                    LENGTH,
                                                    val -> val.isCloseTo(1, offset(0.3))))))))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void doubleSum() {
    assertThat(DOUBLE_SUM_METRIC)
        .hasDoubleSumSatisfying(
            sum -> sum.isMonotonic().isCumulative().hasPointsSatisfying(point -> {}, point -> {}));
    assertThat(DOUBLE_SUM_METRIC_DELTA_NONMONOTONIC)
        .hasDoubleSumSatisfying(
            sum -> sum.isNotMonotonic().isDelta().hasPointsSatisfying(point -> {}, point -> {}));
  }

  @Test
  @SuppressWarnings("Convert2MethodRef")
  void doubleSumFailure() {
    assertThatThrownBy(() -> assertThat(DOUBLE_SUM_METRIC).hasLongSumSatisfying(sum -> {}))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_SUM_METRIC)
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                // Not enough points
                                point -> {})))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () -> assertThat(DOUBLE_SUM_METRIC).hasDoubleSumSatisfying(sum -> sum.isNotMonotonic()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () -> assertThat(DOUBLE_SUM_METRIC).hasDoubleSumSatisfying(sum -> sum.isDelta()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_SUM_METRIC_DELTA_NONMONOTONIC)
                    .hasDoubleSumSatisfying(sum -> sum.isMonotonic()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_SUM_METRIC_DELTA_NONMONOTONIC)
                    .hasDoubleSumSatisfying(sum -> sum.isCumulative()))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void longSum() {
    assertThat(LONG_SUM_METRIC)
        .hasLongSumSatisfying(
            sum -> sum.isMonotonic().isCumulative().hasPointsSatisfying(point -> {}, point -> {}));
    assertThat(LONG_SUM_METRIC_DELTA_NONMONOTONIC)
        .hasLongSumSatisfying(
            sum -> sum.isNotMonotonic().isDelta().hasPointsSatisfying(point -> {}, point -> {}));
  }

  @Test
  @SuppressWarnings("Convert2MethodRef")
  void longSumFailure() {
    assertThatThrownBy(() -> assertThat(LONG_SUM_METRIC).hasDoubleSumSatisfying(sum -> {}))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(LONG_SUM_METRIC)
                    .hasLongSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                // Not enough points
                                point -> {})))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () -> assertThat(LONG_SUM_METRIC).hasLongSumSatisfying(sum -> sum.isNotMonotonic()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(() -> assertThat(LONG_SUM_METRIC).hasLongSumSatisfying(sum -> sum.isDelta()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(LONG_SUM_METRIC_DELTA_NONMONOTONIC)
                    .hasLongSumSatisfying(sum -> sum.isMonotonic()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(LONG_SUM_METRIC_DELTA_NONMONOTONIC)
                    .hasLongSumSatisfying(sum -> sum.isCumulative()))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void histogram() {
    assertThat(HISTOGRAM_METRIC)
        .hasHistogramSatisfying(
            histogram ->
                histogram
                    .isCumulative()
                    .hasPointsSatisfying(
                        point ->
                            point
                                .hasSum(15.0)
                                .hasSumGreaterThan(10.1)
                                .hasMax(7.0)
                                .hasMin(4.0)
                                .hasCount(3)
                                .hasBucketBoundaries(10.0)));
    assertThat(HISTOGRAM_METRIC_DELTA).hasHistogramSatisfying(histogram -> histogram.isDelta());
  }

  @Test
  void histogram_failure() {
    assertThatThrownBy(() -> assertThat(HISTOGRAM_METRIC).hasDoubleGaugeSatisfying(gauge -> {}))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(HISTOGRAM_METRIC)
                    .hasHistogramSatisfying(histogram -> histogram.isDelta()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(HISTOGRAM_METRIC_DELTA)
                    .hasHistogramSatisfying(histogram -> histogram.isCumulative()))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(HISTOGRAM_METRIC)
                    .hasHistogramSatisfying(
                        histogram -> histogram.hasPointsSatisfying(point -> {}, point -> {})))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(HISTOGRAM_METRIC)
                    .hasHistogramSatisfying(
                        histogram -> histogram.hasPointsSatisfying(point -> point.hasSum(14.0))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(HISTOGRAM_METRIC)
                    .hasHistogramSatisfying(
                        histogram ->
                            histogram.hasPointsSatisfying(point -> point.hasSumGreaterThan(16.0))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(HISTOGRAM_METRIC)
                    .hasHistogramSatisfying(
                        histogram -> histogram.hasPointsSatisfying(point -> point.hasMax(8.0))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(HISTOGRAM_METRIC)
                    .hasHistogramSatisfying(
                        histogram -> histogram.hasPointsSatisfying(point -> point.hasMin(5.0))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(HISTOGRAM_METRIC)
                    .hasHistogramSatisfying(
                        histogram -> histogram.hasPointsSatisfying(point -> point.hasCount(4))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(HISTOGRAM_METRIC)
                    .hasHistogramSatisfying(
                        histogram ->
                            histogram.hasPointsSatisfying(
                                point -> point.hasBucketBoundaries(11.0))))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void exponentialHistogram() {
    assertThat(EXPONENTIAL_HISTOGRAM_METRIC)
        .hasExponentialHistogramSatisfying(
            histogram ->
                histogram
                    .isCumulative()
                    .hasPointsSatisfying(
                        point ->
                            point
                                .hasScale(1)
                                .hasSum(10.0)
                                .hasZeroCount(1)
                                .hasCount(4)
                                .hasMin(2.0)
                                .hasMax(4.0)
                                .hasPositiveBucketsSatisfying(
                                    buckets ->
                                        buckets
                                            .hasOffset(10)
                                            .hasCounts(Arrays.asList(1L, 2L))
                                            .hasTotalCount(3))
                                .hasNegativeBucketsSatisfying(
                                    buckets ->
                                        buckets
                                            .hasOffset(0)
                                            .hasCounts(Collections.emptyList())
                                            .hasTotalCount(0))
                                .hasStartEpochNanos(1)
                                .hasEpochNanos(2)
                                .hasAttributes(Attributes.empty())
                                .hasExemplars(DOUBLE_EXEMPLAR1, DOUBLE_EXEMPLAR2)
                                .hasExemplarsSatisfying(exemplar -> {}, exemplar -> {})));
    assertThat(EXPONENTIAL_HISTOGRAM_DELTA_METRIC)
        .hasExponentialHistogramSatisfying(ExponentialHistogramAssert::isDelta);
  }

  @Test
  void exponentialHistogram_failure() {
    assertThatThrownBy(
            () -> assertThat(EXPONENTIAL_HISTOGRAM_METRIC).hasDoubleGaugeSatisfying(gauge -> {}))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(EXPONENTIAL_HISTOGRAM_METRIC)
                    .hasExponentialHistogramSatisfying(ExponentialHistogramAssert::isDelta))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(EXPONENTIAL_HISTOGRAM_DELTA_METRIC)
                    .hasExponentialHistogramSatisfying(ExponentialHistogramAssert::isCumulative))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(EXPONENTIAL_HISTOGRAM_METRIC)
                    .hasExponentialHistogramSatisfying(
                        histogram -> histogram.hasPointsSatisfying(point -> {}, point -> {})))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(EXPONENTIAL_HISTOGRAM_METRIC)
                    .hasExponentialHistogramSatisfying(
                        histogram -> histogram.hasPointsSatisfying(point -> point.hasSum(14.0))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(HISTOGRAM_METRIC)
                    .hasExponentialHistogramSatisfying(
                        histogram -> histogram.hasPointsSatisfying(point -> point.hasMax(8.0))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(HISTOGRAM_METRIC)
                    .hasExponentialHistogramSatisfying(
                        histogram -> histogram.hasPointsSatisfying(point -> point.hasMin(5.0))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(HISTOGRAM_METRIC)
                    .hasExponentialHistogramSatisfying(
                        histogram -> histogram.hasPointsSatisfying(point -> point.hasCount(4))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(HISTOGRAM_METRIC)
                    .hasExponentialHistogramSatisfying(
                        histogram ->
                            histogram.hasPointsSatisfying(
                                point ->
                                    point.hasPositiveBucketsSatisfying(
                                        buckets ->
                                            buckets.hasCounts(Collections.singletonList(1L))))))
        .isInstanceOf(AssertionError.class);
  }

  @Test
  void summary() {
    assertThat(SUMMARY_METRIC)
        .hasSummarySatisfying(
            summary ->
                summary.hasPointsSatisfying(
                    point ->
                        point
                            .hasSum(2.0)
                            .hasCount(1)
                            .hasValuesSatisfying(value -> value.hasQuantile(0.0).hasValue(1.0))));
  }

  @Test
  void summary_failure() {
    assertThatThrownBy(() -> assertThat(SUMMARY_METRIC).hasHistogramSatisfying(histogram -> {}))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(DOUBLE_GAUGE_METRIC)
                    .hasSummarySatisfying(
                        summary -> summary.hasPointsSatisfying(point -> {}, point -> {})))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SUMMARY_METRIC)
                    .hasSummarySatisfying(
                        summary -> summary.hasPointsSatisfying(point -> {}, point -> {})))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SUMMARY_METRIC)
                    .hasSummarySatisfying(
                        summary -> summary.hasPointsSatisfying(point -> point.hasSum(1.0))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SUMMARY_METRIC)
                    .hasSummarySatisfying(
                        summary -> summary.hasPointsSatisfying(point -> point.hasCount(2))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SUMMARY_METRIC)
                    .hasSummarySatisfying(
                        summary ->
                            summary.hasPointsSatisfying(
                                point -> point.hasValuesSatisfying(value -> {}, value -> {}))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SUMMARY_METRIC)
                    .hasSummarySatisfying(
                        summary ->
                            summary.hasPointsSatisfying(
                                point ->
                                    point.hasValuesSatisfying(value -> value.hasQuantile(1.0)))))
        .isInstanceOf(AssertionError.class);
    assertThatThrownBy(
            () ->
                assertThat(SUMMARY_METRIC)
                    .hasSummarySatisfying(
                        summary ->
                            summary.hasPointsSatisfying(
                                point -> point.hasValuesSatisfying(value -> value.hasValue(3.0)))))
        .isInstanceOf(AssertionError.class);
  }
}
