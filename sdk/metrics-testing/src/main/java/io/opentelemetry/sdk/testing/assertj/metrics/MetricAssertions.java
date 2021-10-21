/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj.metrics;

import io.opentelemetry.sdk.metrics.data.DoubleGaugeData;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramData;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramPointData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.DoubleSumData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryPointData;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData;
import io.opentelemetry.sdk.metrics.data.LongGaugeData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import org.assertj.core.api.Assertions;

/** Test assertions for data heading to exporters within the Metrics SDK. */
public final class MetricAssertions extends Assertions {
  /** Returns an assertion for {@link MetricData}. */
  public static MetricDataAssert assertThat(MetricData metric) {
    return new MetricDataAssert(metric);
  }

  /** Returns an assertion for {@link DoubleGaugeData}. */
  public static DoubleGaugeAssert assertThat(DoubleGaugeData metric) {
    return new DoubleGaugeAssert(metric);
  }

  /** Returns an assertion for {@link DoubleHistogramData}. */
  public static DoubleHistogramAssert assertThat(DoubleHistogramData metric) {
    return new DoubleHistogramAssert(metric);
  }

  /** Returns an assertion for {@link DoubleSummaryData}. */
  public static DoubleSummaryDataAssert assertThat(DoubleSummaryData metric) {
    return new DoubleSummaryDataAssert(metric);
  }

  /** Returns an assertion for {@link DoubleHistogramPointData}. */
  public static DoubleHistogramPointDataAssert assertThat(DoubleHistogramPointData point) {
    return new DoubleHistogramPointDataAssert(point);
  }

  /** Returns an assertion for {@link DoubleSummaryPointData}. */
  public static DoubleSummaryPointDataAssert assertThat(DoubleSummaryPointData point) {
    return new DoubleSummaryPointDataAssert(point);
  }

  public static ExponentialHistogramPointDataAssert assertThat(
      ExponentialHistogramPointData point) {
    return new ExponentialHistogramPointDataAssert(point);
  }

  public static ExponentialHistogramBucketsAssert assertThat(ExponentialHistogramBuckets buckets) {
    return new ExponentialHistogramBucketsAssert(buckets);
  }

  /** Returns an assertion for {@link DoublePointData}. */
  public static DoublePointDataAssert assertThat(DoublePointData point) {
    return new DoublePointDataAssert(point);
  }

  /** Returns an assertion for {@link DoubleSumData}. */
  public static DoubleSumDataAssert assertThat(DoubleSumData point) {
    return new DoubleSumDataAssert(point);
  }

  /** Returns an assertion for {@link LongPointData}. */
  public static LongPointDataAssert assertThat(LongPointData point) {
    return new LongPointDataAssert(point);
  }

  /** Returns an assertion for {@link LongGaugeData}. */
  public static LongGaugeDataAssert assertThat(LongGaugeData metric) {
    return new LongGaugeDataAssert(metric);
  }

  /** Returns an assertion for {@link LongSumData}. */
  public static LongSumDataAssert assertThat(LongSumData metric) {
    return new LongSumDataAssert(metric);
  }

  public static ExemplarDataAssert assertThat(ExemplarData exemplar) {
    return new ExemplarDataAssert(exemplar);
  }

  private MetricAssertions() {}
}
