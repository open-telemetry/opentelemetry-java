/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ExponentialHistogramAggregationTest {

  @Test
  void goodConfig() {
    assertThat(ExponentialHistogramAggregation.getDefault()).isNotNull();
    assertThat(ExponentialHistogramAggregation.create(10, 10)).isNotNull();
  }

  @Test
  void badBuckets_throwArgumentException() {
    assertThatThrownBy(() -> ExponentialHistogramAggregation.create(1, -1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("maxBuckets must be >= 0");
  }
}
