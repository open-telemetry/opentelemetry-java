/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class ExplicitBucketHistogramUtilsTest {

  /**
   * {@link ExplicitBucketHistogramUtils#getDefaultBucketBoundaries()} returns different values when
   * {@code otel.java.histogram.legacy.buckets.enabled=true}, which is set for this test in
   * {@code build.gradle.kts}.
   */
  @Test
  void defaultBucketBoundaries() {
    assertThat(ExplicitBucketHistogramUtils.getDefaultBucketBoundaries())
        .isEqualTo(
            Arrays.asList(
                5d, 10d, 25d, 50d, 75d, 100d, 250d, 500d, 750d, 1_000d, 2_500d, 5_000d, 7_500d,
                10_000d));
  }
}
