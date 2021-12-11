/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static io.opentelemetry.sdk.metrics.internal.state.TemporalityUtils.resolveTemporality;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import org.junit.jupiter.api.Test;

class TemporalityUtilsTest {

  @Test
  void resolveTemporality_Preferred() {
    assertThat(resolveTemporality(AggregationTemporality.CUMULATIVE))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(resolveTemporality(AggregationTemporality.DELTA))
        .isEqualTo(AggregationTemporality.DELTA);
  }

  @Test
  void resolveTemporality_Default() {
    assertThat(resolveTemporality(null)).isEqualTo(AggregationTemporality.CUMULATIVE);
  }
}
