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
  void testUseConfigured() {
    assertThat(
            resolveTemporality(
                EnumSet.allOf(AggregationTemporality.class),
                AggregationTemporality.CUMULATIVE,
                /* configured= */ AggregationTemporality.DELTA))
        .isEqualTo(AggregationTemporality.DELTA);
    assertThat(
            resolveTemporality(
                EnumSet.allOf(AggregationTemporality.class),
                null,
                /* configured= */ AggregationTemporality.DELTA))
        .isEqualTo(AggregationTemporality.DELTA);
    // If configured is not supported, we choose a different temporality.
    assertThat(
            resolveTemporality(
                EnumSet.of(AggregationTemporality.CUMULATIVE),
                null,
                /* configured= */ AggregationTemporality.DELTA))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
  }

  @Test
  void testUsePreferred() {
    assertThat(
            resolveTemporality(
                EnumSet.allOf(AggregationTemporality.class),
                AggregationTemporality.CUMULATIVE,
                /* configured= */ null))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(
            resolveTemporality(
                EnumSet.allOf(AggregationTemporality.class), AggregationTemporality.DELTA, null))
        .isEqualTo(AggregationTemporality.DELTA);
  }

  @Test
  void testDefaultToCumulativeIfAble() {
    assertThat(resolveTemporality(EnumSet.allOf(AggregationTemporality.class), null, null))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(resolveTemporality(EnumSet.of(AggregationTemporality.CUMULATIVE), null, null))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(resolveTemporality(EnumSet.of(AggregationTemporality.DELTA), null, null))
        .isEqualTo(AggregationTemporality.DELTA);
  }

  @Test
  void testHandleErrorScenarios() {
    // Default to cumulative if preferred is empty.
    assertThat(
            resolveTemporality(
                EnumSet.noneOf(AggregationTemporality.class), null, AggregationTemporality.DELTA))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
  }
}
