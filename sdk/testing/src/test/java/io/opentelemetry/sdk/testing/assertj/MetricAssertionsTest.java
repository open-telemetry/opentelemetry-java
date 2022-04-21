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
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoubleExemplarData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoublePointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongExemplarData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
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
      Resource.create(Attributes.builder().put("dog", "bark").build());
  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.create("opentelemetry", "1.0", null);

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

  @Test
  void doubleGauge() {
    assertThat(DOUBLE_GAUGE_METRIC)
        .hasResource(RESOURCE)
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
                                            equalTo(LENGTH, 1.2),
                                            equalTo(COLORS, Arrays.asList("red", "blue")),
                                            equalTo(CONDITIONS, Arrays.asList(false, true)),
                                            equalTo(SCORES, Arrays.asList(0L, 1L)),
                                            equalTo(COINS, Arrays.asList(0.01, 0.05, 0.1)))
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
  void doubleGaugeFailure() {
    assertThatThrownBy(() -> assertThat(DOUBLE_GAUGE_METRIC).hasResource(Resource.empty()))
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
                                exemplar -> exemplar.hasValue(2), exemplar -> exemplar.hasValue(1)),
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
  }
}
