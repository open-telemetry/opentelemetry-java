/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.resources.Resource;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.assertj.core.api.AbstractAssert;

/**
 * Assertions for an exported {@link MetricData}.
 *
 * @since 1.14.0
 */
public final class MetricAssert extends AbstractAssert<MetricAssert, MetricData> {
  MetricAssert(@Nullable MetricData actual) {
    super(actual, MetricAssert.class);
  }

  /** Asserts the metric has the given {@link Resource}. */
  public MetricAssert hasResource(Resource resource) {
    isNotNull();
    if (!actual.getResource().equals(resource)) {
      failWithActualExpectedAndMessage(
          actual,
          "resource: " + resource,
          "Expected MetricData to have resource <%s> but found <%s>",
          resource,
          actual.getResource());
    }
    return this;
  }

  /**
   * Asserts the metric has a resource satisfying the given condition.
   *
   * @since 1.23.0
   */
  public MetricAssert hasResourceSatisfying(Consumer<ResourceAssert> resource) {
    isNotNull();
    resource.accept(
        new ResourceAssert(actual.getResource(), String.format("metric [%s]", actual.getName())));
    return this;
  }

  /** Asserts the metric has the given the {@link InstrumentationScopeInfo}. */
  public MetricAssert hasInstrumentationScope(InstrumentationScopeInfo instrumentationScopeInfo) {
    isNotNull();
    if (!actual.getInstrumentationScopeInfo().equals(instrumentationScopeInfo)) {
      failWithActualExpectedAndMessage(
          actual,
          "instrumentation scope: " + instrumentationScopeInfo,
          "Expected MetricData to have resource <%s> but found <%s>",
          instrumentationScopeInfo,
          actual.getInstrumentationScopeInfo());
    }
    return this;
  }

  /** Asserts the metric has the given name. */
  public MetricAssert hasName(String name) {
    isNotNull();
    if (!actual.getName().equals(name)) {
      failWithActualExpectedAndMessage(
          actual,
          "name: " + name,
          "Expected MetricData to have name <%s> but found <%s>",
          name,
          actual.getName());
    }
    return this;
  }

  /** Asserts the metric has the given description. */
  public MetricAssert hasDescription(String description) {
    isNotNull();
    if (!actual.getDescription().equals(description)) {
      failWithActualExpectedAndMessage(
          actual,
          "description: " + description,
          "Expected MetricData to have description <%s> but found <%s>",
          description,
          actual.getDescription());
    }
    return this;
  }

  /** Asserts the metric has the given unit. */
  public MetricAssert hasUnit(String unit) {
    isNotNull();
    if (!actual.getUnit().equals(unit)) {
      failWithActualExpectedAndMessage(
          actual,
          "unit: " + unit,
          "Expected MetricData to have unit <%s> but found <%s>",
          unit,
          actual.getUnit());
    }
    return this;
  }

  /**
   * Asserts this {@link MetricData} is a {@code DoubleGauge} that satisfies the provided assertion.
   */
  public MetricAssert hasDoubleGaugeSatisfying(Consumer<DoubleGaugeAssert> assertion) {
    isNotNull();
    if (actual.getType() != MetricDataType.DOUBLE_GAUGE) {
      failWithActualExpectedAndMessage(
          actual,
          "type: DOUBLE_GAUGE",
          "Expected MetricData to have type <%s> but found <%s>",
          MetricDataType.DOUBLE_GAUGE,
          actual.getType());
    }
    assertion.accept(new DoubleGaugeAssert(actual.getDoubleGaugeData()));
    return this;
  }

  /**
   * Asserts this {@link MetricData} is a {@code LongGauge} that satisfies the provided assertion.
   */
  public MetricAssert hasLongGaugeSatisfying(Consumer<LongGaugeAssert> assertion) {
    isNotNull();
    if (actual.getType() != MetricDataType.LONG_GAUGE) {
      failWithActualExpectedAndMessage(
          actual,
          "type: LONG_GAUGE",
          "Expected MetricData to have type <%s> but found <%s>",
          MetricDataType.LONG_GAUGE,
          actual.getType());
    }
    assertion.accept(new LongGaugeAssert(actual.getLongGaugeData()));
    return this;
  }

  /** Asserts this {@link MetricData} is a double sum that satisfies the provided assertion. */
  public MetricAssert hasDoubleSumSatisfying(Consumer<DoubleSumAssert> assertion) {
    isNotNull();
    if (actual.getType() != MetricDataType.DOUBLE_SUM) {
      failWithActualExpectedAndMessage(
          actual,
          "type: DOULE_SUM",
          "Expected MetricData to have type <%s> but found <%s>",
          MetricDataType.DOUBLE_SUM,
          actual.getType());
    }
    assertion.accept(new DoubleSumAssert(actual.getDoubleSumData()));
    return this;
  }

  /** Asserts this {@link MetricData} is a long sum that satisfies the provided assertion. */
  public MetricAssert hasLongSumSatisfying(Consumer<LongSumAssert> assertion) {
    isNotNull();
    if (actual.getType() != MetricDataType.LONG_SUM) {
      failWithActualExpectedAndMessage(
          actual,
          "type: LONG_SUM",
          "Expected MetricData to have type <%s> but found <%s>",
          MetricDataType.LONG_SUM,
          actual.getType());
    }
    assertion.accept(new LongSumAssert(actual.getLongSumData()));
    return this;
  }

  /** Asserts this {@link MetricData} is a histogram that satisfies the provided assertion. */
  public MetricAssert hasHistogramSatisfying(Consumer<HistogramAssert> assertion) {
    isNotNull();
    if (actual.getType() != MetricDataType.HISTOGRAM) {
      failWithActualExpectedAndMessage(
          actual,
          "type: HISTOGRAM",
          "Expected MetricData to have type <%s> but found <%s>",
          MetricDataType.HISTOGRAM,
          actual.getType());
    }
    assertion.accept(new HistogramAssert(actual.getHistogramData()));
    return this;
  }

  /**
   * Asserts this {@link MetricData} is an exponential histogram that satisfies the provided
   * assertion.
   *
   * @since 1.23.0
   */
  public MetricAssert hasExponentialHistogramSatisfying(
      Consumer<ExponentialHistogramAssert> assertion) {
    isNotNull();
    if (actual.getType() != MetricDataType.EXPONENTIAL_HISTOGRAM) {
      failWithActualExpectedAndMessage(
          actual,
          "type: EXPONENTIAL_HISTOGRAM",
          "Expected MetricData to have type <%s> but found <%s>",
          MetricDataType.EXPONENTIAL_HISTOGRAM,
          actual.getType());
    }
    assertion.accept(new ExponentialHistogramAssert(actual.getExponentialHistogramData()));
    return this;
  }

  /** Asserts this {@link MetricData} is a summary that satisfies the provided assertion. */
  public MetricAssert hasSummarySatisfying(Consumer<SummaryAssert> assertion) {
    isNotNull();
    if (actual.getType() != MetricDataType.SUMMARY) {
      failWithActualExpectedAndMessage(
          actual,
          "type: SUMMARY",
          "Expected MetricData to have type <%s> but found <%s>",
          MetricDataType.SUMMARY,
          actual.getType());
    }
    assertion.accept(new SummaryAssert(actual.getSummaryData()));
    return this;
  }
}
