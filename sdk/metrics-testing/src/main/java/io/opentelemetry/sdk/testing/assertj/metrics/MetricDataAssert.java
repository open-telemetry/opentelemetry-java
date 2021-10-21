/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj.metrics;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.resources.Resource;
import org.assertj.core.api.AbstractAssert;

/** Test assertions for {@link MetricData}. */
public class MetricDataAssert extends AbstractAssert<MetricDataAssert, MetricData> {
  protected MetricDataAssert(MetricData actual) {
    super(actual, MetricDataAssert.class);
  }

  /** Ensures the {@link Resource} associated with a metric matches the expected value. */
  public MetricDataAssert hasResource(Resource resource) {
    isNotNull();
    if (!actual.getResource().equals(resource)) {
      failWithActualExpectedAndMessage(
          actual,
          "resource: " + resource,
          "Exepcted MetricData to have resource <%s> but found <%s>",
          resource,
          actual.getResource());
    }
    return this;
  }

  /**
   * Ensures the {@link InstrumentationLibraryInfo} associated with a metric matches the expected
   * value.
   */
  public MetricDataAssert hasInstrumentationLibrary(
      InstrumentationLibraryInfo instrumentationLibrary) {
    isNotNull();
    if (!actual.getInstrumentationLibraryInfo().equals(instrumentationLibrary)) {
      failWithActualExpectedAndMessage(
          actual,
          "instrumentation library: " + instrumentationLibrary,
          "Exepcted MetricData to have resource <%s> but found <%s>",
          instrumentationLibrary,
          actual.getInstrumentationLibraryInfo());
    }
    return this;
  }

  /** Ensures the {@code name} field matches the expected value. */
  public MetricDataAssert hasName(String name) {
    isNotNull();
    if (!actual.getName().equals(name)) {
      failWithActualExpectedAndMessage(
          actual,
          "name: " + name,
          "Exepcted MetricData to have name <%s> but found <%s>",
          name,
          actual.getName());
    }
    return this;
  }

  /** Ensures the {@code description} field matches the expected value. */
  public MetricDataAssert hasDescription(String description) {
    isNotNull();
    if (!actual.getDescription().equals(description)) {
      failWithActualExpectedAndMessage(
          actual,
          "description: " + description,
          "Exepcted MetricData to have description <%s> but found <%s>",
          description,
          actual.getDescription());
    }
    return this;
  }

  /** Ensures the {@code unit} field matches the expected value. */
  public MetricDataAssert hasUnit(String unit) {
    isNotNull();
    if (!actual.getUnit().equals(unit)) {
      failWithActualExpectedAndMessage(
          actual,
          "unit: " + unit,
          "Exepcted MetricData to have unit <%s> but found <%s>",
          unit,
          actual.getUnit());
    }
    return this;
  }

  /**
   * Ensures this {@link MetricData} is a {@code DoubleHistogram}.
   *
   * @return convenience API to assert against the {@code DoubleHistogram}.
   */
  public DoubleHistogramAssert hasDoubleHistogram() {
    isNotNull();
    if (actual.getType() != MetricDataType.HISTOGRAM) {
      failWithActualExpectedAndMessage(
          actual,
          "type: HISTOGRAM",
          "Exepcted MetricData to have type <%s> but found <%s>",
          MetricDataType.HISTOGRAM,
          actual.getType());
    }
    return new DoubleHistogramAssert(actual.getDoubleHistogramData());
  }

  /**
   * Ensures this {@link MetricData} is a {@code ExponentialHistogram}
   *
   * @return convenience API to assert agains the {@code ExponentialHistogram}
   */
  public ExponentialHistogramAssert hasExponentialHistogram() {
    isNotNull();
    if (actual.getType() != MetricDataType.EXPONENTIAL_HISTOGRAM) {
      failWithActualExpectedAndMessage(
          actual,
          "type: EXPONENTIAL_HISTOGRAM",
          "Expected MetricData to have type <%s> but found <%s>",
          MetricDataType.EXPONENTIAL_HISTOGRAM,
          actual.getType());
    }
    return new ExponentialHistogramAssert(actual.getExponentialHistogramData());
  }

  /**
   * Ensures this {@link MetricData} is a {@code DoubleGauge}.
   *
   * @return convenience API to assert against the {@code DoubleGauge}.
   */
  public DoubleGaugeAssert hasDoubleGauge() {
    isNotNull();
    if (actual.getType() != MetricDataType.DOUBLE_GAUGE) {
      failWithActualExpectedAndMessage(
          actual,
          "type: DOUBLE_GAUGE",
          "Exepcted MetricData to have type <%s> but found <%s>",
          MetricDataType.DOUBLE_GAUGE,
          actual.getType());
    }
    return new DoubleGaugeAssert(actual.getDoubleGaugeData());
  }

  /**
   * Ensures this {@link MetricData} is a {@code DoubleSum}.
   *
   * @return convenience API to assert against the {@code DoubleSum}.
   */
  public DoubleSumDataAssert hasDoubleSum() {
    isNotNull();
    if (actual.getType() != MetricDataType.DOUBLE_SUM) {
      failWithActualExpectedAndMessage(
          actual,
          "type: DOUBLE_SUM",
          "Exepcted MetricData to have type <%s> but found <%s>",
          MetricDataType.DOUBLE_SUM,
          actual.getType());
    }
    return new DoubleSumDataAssert(actual.getDoubleSumData());
  }

  /**
   * Ensures this {@link MetricData} is a {@code LongGauge}.
   *
   * @return convenience API to assert against the {@code LongGauge}.
   */
  public LongGaugeDataAssert hasLongGauge() {
    isNotNull();
    if (actual.getType() != MetricDataType.LONG_GAUGE) {
      failWithActualExpectedAndMessage(
          actual,
          "type: LONG_GAUGE",
          "Exepcted MetricData to have type <%s> but found <%s>",
          MetricDataType.LONG_GAUGE,
          actual.getType());
    }
    return new LongGaugeDataAssert(actual.getLongGaugeData());
  }

  /**
   * Ensures this {@link MetricData} is a {@code LongSum}.
   *
   * @return convenience API to assert against the {@code LongSum}.
   */
  public LongSumDataAssert hasLongSum() {
    isNotNull();
    if (actual.getType() != MetricDataType.LONG_SUM) {
      failWithActualExpectedAndMessage(
          actual,
          "type: LONG_SUM",
          "Exepcted MetricData to have type <%s> but found <%s>",
          MetricDataType.LONG_SUM,
          actual.getType());
    }
    return new LongSumDataAssert(actual.getLongSumData());
  }

  /**
   * Ensures this {@link MetricData} is a {@code DoubleSummaryData}.
   *
   * @return convenience API to assert against the {@code DoubleSummaryData}.
   */
  public DoubleSummaryDataAssert hasDoubleSummary() {
    isNotNull();
    if (actual.getType() != MetricDataType.SUMMARY) {
      failWithActualExpectedAndMessage(
          actual,
          "type: SUMMARY",
          "Exepcted MetricData to have type <%s> but found <%s>",
          MetricDataType.SUMMARY,
          actual.getType());
    }
    return new DoubleSummaryDataAssert(actual.getDoubleSummaryData());
  }
}
