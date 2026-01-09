/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import org.junit.jupiter.api.Test;

class ExponentialHistogramDataTest {

  @Test
  void create() {
    ExponentialHistogramData histogramData =
        ExponentialHistogramData.create(AggregationTemporality.CUMULATIVE, Collections.emptyList());
    assertThat(histogramData.getAggregationTemporality())
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(histogramData.getPoints()).isEmpty();
  }
}
