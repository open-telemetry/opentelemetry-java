/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class DoublePointDataTest {

  @Test
  void create() {
    DoublePointData pointData =
        DoublePointData.create(
            1, 2, Attributes.builder().put("key", "value1").build(), 3, Collections.emptyList());
    assertThat(pointData.getStartEpochNanos()).isEqualTo(1);
    assertThat(pointData.getEpochNanos()).isEqualTo(2);
    assertThat(pointData.getAttributes())
        .isEqualTo(Attributes.builder().put("key", "value1").build());
    assertThat(pointData.getValue()).isEqualTo(3);
  }
}
