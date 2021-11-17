/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.sdk.metrics.view.Aggregation;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Tests configuration errors in explicit bucket histograms. */
class ExplicitBucketHistogramTest {

  @Test
  void goodConfig() {
    assertThat(Aggregation.explicitBucketHistogram()).isNotNull();
  }

  @Test
  void badBuckets_throwArgumentException() {
    assertThatThrownBy(
            () ->
                Aggregation.explicitBucketHistogram(
                    Collections.singletonList(Double.NEGATIVE_INFINITY)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invalid bucket boundary: -Inf");
    assertThatThrownBy(
            () -> Aggregation.explicitBucketHistogram(Arrays.asList(1.0, Double.POSITIVE_INFINITY)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invalid bucket boundary: +Inf");
    assertThatThrownBy(() -> Aggregation.explicitBucketHistogram(Arrays.asList(1.0, Double.NaN)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invalid bucket boundary: NaN");
    assertThatThrownBy(() -> Aggregation.explicitBucketHistogram(Arrays.asList(2.0, 1.0, 3.0)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Bucket boundaries must be in increasing order: 2.0 >= 1.0");
  }
}
