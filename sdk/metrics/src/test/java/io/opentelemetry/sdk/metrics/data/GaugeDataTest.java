/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import org.junit.jupiter.api.Test;

public class GaugeDataTest {

  @Test
  void create() {
    GaugeData<LongPointData> gaugeData = GaugeData.create(Collections.emptyList());
    assertThat(gaugeData.getPoints()).isEmpty();
  }
}
