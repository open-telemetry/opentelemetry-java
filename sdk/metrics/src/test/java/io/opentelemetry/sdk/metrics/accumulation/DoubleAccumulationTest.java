/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.accumulation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.metrics.data.MetricData;
import org.junit.jupiter.api.Test;

class DoubleAccumulationTest {
  @Test
  void toPoint() {
    Accumulation accumulation = DoubleAccumulation.create(12.1);
    assertThat(getPoint(accumulation).getValue()).isCloseTo(12.1, offset(1e-6));
  }

  private static MetricData.DoublePoint getPoint(Accumulation aggregator) {
    MetricData.Point point = aggregator.toPoint(12345, 12358, Labels.of("key", "value"));
    assertThat(point).isNotNull();
    assertThat(point.getStartEpochNanos()).isEqualTo(12345);
    assertThat(point.getEpochNanos()).isEqualTo(12358);
    assertThat(point.getLabels().size()).isEqualTo(1);
    assertThat(point.getLabels().get("key")).isEqualTo("value");
    assertThat(point).isInstanceOf(MetricData.DoublePoint.class);
    return (MetricData.DoublePoint) point;
  }
}
