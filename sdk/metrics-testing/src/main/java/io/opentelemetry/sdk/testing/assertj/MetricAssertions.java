/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import org.assertj.core.api.Assertions;

/** Test assertions for data heading to exporters within the Metrics SDK. */
public final class MetricAssertions extends Assertions {
  /** Returns an assertion for {@link MetricData}. */
  public static MetricDataAssert assertThat(MetricData metric) {
    return new MetricDataAssert(metric);
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

  public static ExemplarDataAssert assertThat(ExemplarData exemplar) {
    return new ExemplarDataAssert(exemplar);
  }

  private MetricAssertions() {}
}
