/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.accumulation;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.metrics.data.MetricData;
import org.junit.jupiter.api.Test;

class LongAccumulationTest {
  @Test
  void toPoint() {
    Accumulation accumulation = LongAccumulation.create(12);
    assertThat(getPoint(accumulation).getValue()).isEqualTo(12);
  }

  private static MetricData.LongPoint getPoint(Accumulation accumulation) {
    MetricData.Point point = accumulation.toPoint(12345, 12358, Labels.of("key", "value"));
    assertThat(point).isNotNull();
    assertThat(point.getStartEpochNanos()).isEqualTo(12345);
    assertThat(point.getEpochNanos()).isEqualTo(12358);
    assertThat(point.getLabels().size()).isEqualTo(1);
    assertThat(point.getLabels().get("key")).isEqualTo("value");
    assertThat(point).isInstanceOf(MetricData.LongPoint.class);
    return (MetricData.LongPoint) point;
  }
}
