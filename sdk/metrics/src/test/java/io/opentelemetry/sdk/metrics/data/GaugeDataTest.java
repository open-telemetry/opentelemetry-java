/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class GaugeDataTest {

  @Test
  void createLongGaugeData() {
    LongPointData point = LongPointData.create(0, 0, Attributes.empty(), 1);
    GaugeData<LongPointData> gaugeData =
        GaugeData.createLongGaugeData(Collections.singleton(point));
    assertThat(gaugeData.getPoints()).containsExactly(point);
  }

  @Test
  void createDoubleGaugeData() {
    DoublePointData point =
        DoublePointData.create(0, 0, Attributes.empty(), 1.0, Collections.emptyList());
    GaugeData<DoublePointData> gaugeData =
        GaugeData.createDoubleGaugeData(Collections.singleton(point));
    assertThat(gaugeData.getPoints()).containsExactly(point);
  }
}
