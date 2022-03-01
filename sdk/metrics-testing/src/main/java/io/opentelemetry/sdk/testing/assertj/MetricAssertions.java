/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import io.opentelemetry.sdk.metrics.data.DoubleHistogramData;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramPointData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.DoubleSumData;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData;
import io.opentelemetry.sdk.metrics.data.GaugeData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.data.SummaryData;
import io.opentelemetry.sdk.metrics.data.SummaryPointData;
import org.assertj.core.api.Assertions;

/** Test assertions for data heading to exporters within the Metrics SDK. */
public final class MetricAssertions extends Assertions {
  /** Returns an assertion for {@link MetricData}. */
  public static MetricDataAssert assertThat(MetricData metric) {
    return new MetricDataAssert(metric);
  }

  /** Returns an assertion for {@link GaugeData}. */
  // There is no real use case for passing in a GaugeData that is a lambda, if for some reason it is
  // desired a cast will still work.
  @SuppressWarnings("FunctionalInterfaceClash")
  public static <T extends PointData> GaugeAssert<T> assertThat(GaugeData<T> metric) {
    return new GaugeAssert<>(metric);
  }

  /** Returns an assertion for {@link DoubleHistogramData}. */
  public static DoubleHistogramAssert assertThat(DoubleHistogramData metric) {
    return new DoubleHistogramAssert(metric);
  }

  /** Returns an assertion for {@link SummaryData}. */
  public static SummaryDataAssert assertThat(SummaryData metric) {
    return new SummaryDataAssert(metric);
  }

  /** Returns an assertion for {@link DoubleHistogramPointData}. */
  public static DoubleHistogramPointDataAssert assertThat(DoubleHistogramPointData point) {
    return new DoubleHistogramPointDataAssert(point);
  }

  /** Returns an assertion for {@link SummaryPointData}. */
  public static SummaryPointDataAssert assertThat(SummaryPointData point) {
    return new SummaryPointDataAssert(point);
  }

  /** Returns an assertion for {@link ExponentialHistogramPointData}. */
  public static ExponentialHistogramPointDataAssert assertThat(
      ExponentialHistogramPointData point) {
    return new ExponentialHistogramPointDataAssert(point);
  }

  /** Returns an assertion for {@link ExponentialHistogramBuckets}. */
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

  /** Returns an assertion for {@link LongSumData}. */
  public static LongSumDataAssert assertThat(LongSumData metric) {
    return new LongSumDataAssert(metric);
  }

  public static ExemplarDataAssert assertThat(ExemplarData exemplar) {
    return new ExemplarDataAssert(exemplar);
  }

  private MetricAssertions() {}
}
