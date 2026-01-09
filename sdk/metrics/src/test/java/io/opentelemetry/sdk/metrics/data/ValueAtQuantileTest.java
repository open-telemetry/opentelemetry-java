/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ValueAtQuantileTest {

  @Test
  void create() {
    ValueAtQuantile valueAtQuantile = ValueAtQuantile.create(0.0, 1.1);
    assertThat(valueAtQuantile.getQuantile()).isEqualTo(0.0);
    assertThat(valueAtQuantile.getValue()).isEqualTo(1.1);
  }
}
