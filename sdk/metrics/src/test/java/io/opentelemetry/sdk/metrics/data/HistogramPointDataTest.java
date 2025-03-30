/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import io.opentelemetry.api.common.Attributes;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

public class HistogramPointDataTest {

  @Test
  void create() {
    HistogramPointData pointData =
        HistogramPointData.create(
            0,
            0,
            Attributes.empty(),
            0.0,
            /* hasMin= */ false,
            0.0,
            /* hasMax= */ false,
            0.0,
            new ArrayList<>(),
            ImmutableList.of(0L));
    assertThat(pointData.getStartEpochNanos()).isEqualTo(0);
    assertThat(pointData.getEpochNanos()).isEqualTo(0);
    assertThat(pointData.getAttributes()).isEqualTo(Attributes.empty());
    assertThat(pointData.getSum()).isEqualTo(0.0);
    assertThat(pointData.getCount()).isEqualTo(0);
    assertThat(pointData.hasMin()).isFalse();
    assertThat(pointData.getMin()).isEqualTo(0.0);
    assertThat(pointData.hasMax()).isFalse();
    assertThat(pointData.getMax()).isEqualTo(0.0);
  }
}
