/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.ExplicitBucketHistogramOptions;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Tests configuration errors in explicit bucket histograms. */
class ExplicitBucketHistogramAggregationTest {

  @Test
  void goodConfig() {
    assertThat(Aggregation.explicitBucketHistogram()).isNotNull();
  }

  @Test
  void badBuckets_throwArgumentException() {
    assertThatThrownBy(
            () ->
                Aggregation.explicitBucketHistogram(
                    ExplicitBucketHistogramOptions.builder()
                        .setBucketBoundaries(Collections.singletonList(Double.NEGATIVE_INFINITY))
                        .build()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invalid bucket boundary: -Inf");
    assertThatThrownBy(
            () ->
                Aggregation.explicitBucketHistogram(
                    ExplicitBucketHistogramOptions.builder()
                        .setBucketBoundaries(Arrays.asList(1.0, Double.POSITIVE_INFINITY))
                        .build()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invalid bucket boundary: +Inf");
    assertThatThrownBy(
            () ->
                Aggregation.explicitBucketHistogram(
                    ExplicitBucketHistogramOptions.builder()
                        .setBucketBoundaries(Arrays.asList(1.0, Double.NaN))
                        .build()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invalid bucket boundary: NaN");
    assertThatThrownBy(
            () ->
                Aggregation.explicitBucketHistogram(
                    ExplicitBucketHistogramOptions.builder()
                        .setBucketBoundaries(Arrays.asList(2.0, 1.0, 3.0))
                        .build()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Bucket boundaries must be in increasing order: 2.0 >= 1.0");
  }
}
