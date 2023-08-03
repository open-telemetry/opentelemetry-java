/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.data.ScopeMetricData;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.assertj.core.api.AbstractAssert;

/**
 * Assertions for an exported {@link ScopeMetricData}.
 *
 * @since 1.14.0
 */
public final class ScopeMetricAssert extends AbstractAssert<ScopeMetricAssert, ScopeMetricData> {
  ScopeMetricAssert(@Nullable ScopeMetricData actual) {
    super(actual, ScopeMetricAssert.class);
  }

  /** Asserts the metric has the given the {@link InstrumentationScopeInfo}. */
  public ScopeMetricAssert hasInstrumentationScope(
      InstrumentationScopeInfo instrumentationScopeInfo) {
    isNotNull();
    if (!actual.getInstrumentationScopeInfo().equals(instrumentationScopeInfo)) {
      failWithActualExpectedAndMessage(
          actual,
          "instrumentation scope: " + instrumentationScopeInfo,
          "Expected ScopeMetricData to have resource <%s> but found <%s>",
          instrumentationScopeInfo,
          actual.getInstrumentationScopeInfo());
    }
    return this;
  }

  /** Asserts the metric has the given name. */
  public ScopeMetricAssert hasName(String name) {
    isNotNull();
    if (!actual.getName().equals(name)) {
      failWithActualExpectedAndMessage(
          actual,
          "name: " + name,
          "Expected ScopeMetricData to have name <%s> but found <%s>",
          name,
          actual.getName());
    }
    return this;
  }

  /** Asserts the metric has the given description. */
  public ScopeMetricAssert hasDescription(String description) {
    isNotNull();
    if (!actual.getDescription().equals(description)) {
      failWithActualExpectedAndMessage(
          actual,
          "description: " + description,
          "Expected ScopeMetricData to have description <%s> but found <%s>",
          description,
          actual.getDescription());
    }
    return this;
  }

  /** Asserts the metric has the given unit. */
  public ScopeMetricAssert hasUnit(String unit) {
    isNotNull();
    if (!actual.getUnit().equals(unit)) {
      failWithActualExpectedAndMessage(
          actual,
          "unit: " + unit,
          "Expected ScopeMetricData to have unit <%s> but found <%s>",
          unit,
          actual.getUnit());
    }
    return this;
  }

  /**
   * Asserts this {@link ScopeMetricData} is a {@code DoubleGauge} that satisfies the provided
   * assertion.
   */
  public ScopeMetricAssert hasDoubleGaugeSatisfying(Consumer<DoubleGaugeAssert> assertion) {
    isNotNull();
    if (actual.getType() != MetricDataType.DOUBLE_GAUGE) {
      failWithActualExpectedAndMessage(
          actual,
          "type: DOUBLE_GAUGE",
          "Expected ScopeMetricData to have type <%s> but found <%s>",
          MetricDataType.DOUBLE_GAUGE,
          actual.getType());
    }
    assertion.accept(new DoubleGaugeAssert(actual.getDoubleGaugeData()));
    return this;
  }

  /**
   * Asserts this {@link ScopeMetricData} is a {@code LongGauge} that satisfies the provided
   * assertion.
   */
  public ScopeMetricAssert hasLongGaugeSatisfying(Consumer<LongGaugeAssert> assertion) {
    isNotNull();
    if (actual.getType() != MetricDataType.LONG_GAUGE) {
      failWithActualExpectedAndMessage(
          actual,
          "type: LONG_GAUGE",
          "Expected ScopeMetricData to have type <%s> but found <%s>",
          MetricDataType.LONG_GAUGE,
          actual.getType());
    }
    assertion.accept(new LongGaugeAssert(actual.getLongGaugeData()));
    return this;
  }

  /** Asserts this {@link ScopeMetricData} is a double sum that satisfies the provided assertion. */
  public ScopeMetricAssert hasDoubleSumSatisfying(Consumer<DoubleSumAssert> assertion) {
    isNotNull();
    if (actual.getType() != MetricDataType.DOUBLE_SUM) {
      failWithActualExpectedAndMessage(
          actual,
          "type: DOULE_SUM",
          "Expected ScopeMetricData to have type <%s> but found <%s>",
          MetricDataType.DOUBLE_SUM,
          actual.getType());
    }
    assertion.accept(new DoubleSumAssert(actual.getDoubleSumData()));
    return this;
  }

  /** Asserts this {@link ScopeMetricData} is a long sum that satisfies the provided assertion. */
  public ScopeMetricAssert hasLongSumSatisfying(Consumer<LongSumAssert> assertion) {
    isNotNull();
    if (actual.getType() != MetricDataType.LONG_SUM) {
      failWithActualExpectedAndMessage(
          actual,
          "type: LONG_SUM",
          "Expected ScopeMetricData to have type <%s> but found <%s>",
          MetricDataType.LONG_SUM,
          actual.getType());
    }
    assertion.accept(new LongSumAssert(actual.getLongSumData()));
    return this;
  }

  /** Asserts this {@link ScopeMetricData} is a histogram that satisfies the provided assertion. */
  public ScopeMetricAssert hasHistogramSatisfying(Consumer<HistogramAssert> assertion) {
    isNotNull();
    if (actual.getType() != MetricDataType.HISTOGRAM) {
      failWithActualExpectedAndMessage(
          actual,
          "type: HISTOGRAM",
          "Expected ScopeMetricData to have type <%s> but found <%s>",
          MetricDataType.HISTOGRAM,
          actual.getType());
    }
    assertion.accept(new HistogramAssert(actual.getHistogramData()));
    return this;
  }

  /**
   * Asserts this {@link ScopeMetricData} is an exponential histogram that satisfies the provided
   * assertion.
   *
   * @since 1.23.0
   */
  public ScopeMetricAssert hasExponentialHistogramSatisfying(
      Consumer<ExponentialHistogramAssert> assertion) {
    isNotNull();
    if (actual.getType() != MetricDataType.EXPONENTIAL_HISTOGRAM) {
      failWithActualExpectedAndMessage(
          actual,
          "type: EXPONENTIAL_HISTOGRAM",
          "Expected ScopeMetricData to have type <%s> but found <%s>",
          MetricDataType.EXPONENTIAL_HISTOGRAM,
          actual.getType());
    }
    assertion.accept(new ExponentialHistogramAssert(actual.getExponentialHistogramData()));
    return this;
  }

  /** Asserts this {@link ScopeMetricData} is a summary that satisfies the provided assertion. */
  public ScopeMetricAssert hasSummarySatisfying(Consumer<SummaryAssert> assertion) {
    isNotNull();
    if (actual.getType() != MetricDataType.SUMMARY) {
      failWithActualExpectedAndMessage(
          actual,
          "type: SUMMARY",
          "Expected ScopeMetricData to have type <%s> but found <%s>",
          MetricDataType.SUMMARY,
          actual.getType());
    }
    assertion.accept(new SummaryAssert(actual.getSummaryData()));
    return this;
  }
}
