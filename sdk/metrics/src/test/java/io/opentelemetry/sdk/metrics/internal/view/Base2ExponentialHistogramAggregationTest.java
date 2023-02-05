/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class Base2ExponentialHistogramAggregationTest {

  @Test
  void goodConfig() {
    assertThat(Base2ExponentialHistogramAggregation.getDefault()).isNotNull();
    assertThat(Base2ExponentialHistogramAggregation.create(10, 20)).isNotNull();
  }

  @Test
  void invalidConfig_Throws() {
    assertThatThrownBy(() -> Base2ExponentialHistogramAggregation.create(0, 20))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("maxBuckets must be > 0");
    assertThatThrownBy(() -> Base2ExponentialHistogramAggregation.create(1, 21))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("maxScale must be -10 <= x <= 20");
    assertThatThrownBy(() -> Base2ExponentialHistogramAggregation.create(1, -11))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("maxScale must be -10 <= x <= 20");
  }
}
