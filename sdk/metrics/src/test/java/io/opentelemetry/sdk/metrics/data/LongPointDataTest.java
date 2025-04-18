/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import org.junit.jupiter.api.Test;

class LongPointDataTest {

  @Test
  void create() {
    LongPointData pointData = LongPointData.create(0, 0, Attributes.empty(), 0);
    assertThat(pointData.getStartEpochNanos()).isEqualTo(0);
    assertThat(pointData.getEpochNanos()).isEqualTo(0);
    assertThat(pointData.getAttributes()).isEqualTo(Attributes.empty());
    assertThat(pointData.getValue()).isEqualTo(0);
  }
}
