/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static io.opentelemetry.sdk.metrics.internal.state.TemporalityUtils.resolveTemporality;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;

class TemporalityUtilsTest {

  @Test
  void testUsePreferred() {
    assertThat(
            resolveTemporality(
                EnumSet.allOf(AggregationTemporality.class), AggregationTemporality.CUMULATIVE))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(
            resolveTemporality(
                EnumSet.allOf(AggregationTemporality.class), AggregationTemporality.DELTA))
        .isEqualTo(AggregationTemporality.DELTA);
  }

  @Test
  void testDefaultToCumulativeIfAble() {
    assertThat(resolveTemporality(EnumSet.allOf(AggregationTemporality.class), null))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(resolveTemporality(EnumSet.of(AggregationTemporality.CUMULATIVE), null))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(resolveTemporality(EnumSet.of(AggregationTemporality.DELTA), null))
        .isEqualTo(AggregationTemporality.DELTA);
  }

  @Test
  void testHandleErrorScenarios() {
    // Default to cumulative if preferred/supported is empty.
    assertThat(resolveTemporality(EnumSet.noneOf(AggregationTemporality.class), null))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
  }
}
