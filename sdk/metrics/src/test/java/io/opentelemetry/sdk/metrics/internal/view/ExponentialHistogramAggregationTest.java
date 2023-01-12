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
    assertThat(ExponentialHistogramAggregation.create(10, 20)).isNotNull();
  }

  @Test
  void invalidConfig_Throws() {
    assertThatThrownBy(() -> ExponentialHistogramAggregation.create(0, 20))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("maxBuckets must be > 0");
    assertThatThrownBy(() -> ExponentialHistogramAggregation.create(1, 21))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("maxScale must be -10 <= x <= 20");
    assertThatThrownBy(() -> ExponentialHistogramAggregation.create(1, -11))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("maxScale must be -10 <= x <= 20");
  }
}
