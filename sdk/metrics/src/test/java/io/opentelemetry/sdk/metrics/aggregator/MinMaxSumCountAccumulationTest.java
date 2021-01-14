/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryPointData;
import io.opentelemetry.sdk.metrics.data.ValueAtPercentile;
import org.junit.jupiter.api.Test;

class MinMaxSumCountAccumulationTest {
  @Test
  void toPoint() {
    MinMaxSumCountAccumulation accumulation = MinMaxSumCountAccumulation.create(12, 25, 1, 3);
    DoubleSummaryPointData point = getPoint(accumulation);
    assertThat(point.getCount()).isEqualTo(12);
    assertThat(point.getSum()).isEqualTo(25);
    assertThat(point.getPercentileValues()).hasSize(2);
    assertThat(point.getPercentileValues().get(0)).isEqualTo(ValueAtPercentile.create(0.0, 1));
    assertThat(point.getPercentileValues().get(1)).isEqualTo(ValueAtPercentile.create(100.0, 3));
  }

  private static DoubleSummaryPointData getPoint(MinMaxSumCountAccumulation accumulation) {
    DoubleSummaryPointData point = accumulation.toPoint(12345, 12358, Labels.of("key", "value"));
    assertThat(point).isNotNull();
    assertThat(point.getStartEpochNanos()).isEqualTo(12345);
    assertThat(point.getEpochNanos()).isEqualTo(12358);
    assertThat(point.getLabels().size()).isEqualTo(1);
    assertThat(point.getLabels().get("key")).isEqualTo("value");
    assertThat(point).isInstanceOf(DoubleSummaryPointData.class);
    return point;
  }
}
