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

/** Assertions for an exported {@link MetricData}. */
public final class MetricDataAssert extends AbstractAssert<MetricDataAssert, MetricData> {
  MetricDataAssert(@Nullable MetricData actual) {
    super(actual, MetricDataAssert.class);
  }

  /** Asserts the metric has the given {@link Resource}. */
  public MetricDataAssert hasResource(Resource resource) {
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

  /** Asserts the metric has the given the {@link InstrumentationScopeInfo}. */
  public MetricDataAssert hasInstrumentationScope(
      InstrumentationScopeInfo instrumentationScopeInfo) {
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
  public MetricDataAssert hasName(String name) {
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
  public MetricDataAssert hasDescription(String description) {
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
  public MetricDataAssert hasUnit(String unit) {
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
  public MetricDataAssert hasDoubleGaugeSatisfying(Consumer<DoubleGaugeAssert> assertion) {
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
  public MetricDataAssert hasLongGaugeSatisfying(Consumer<LongGaugeAssert> assertion) {
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
}
