/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class SumDataTest {

  @Test
  void createLongSumData() {
    LongPointData point = LongPointData.create(0, 0, Attributes.empty(), 1);
    SumData<LongPointData> sumData =
        SumData.createLongSumData(
            /* isMonotonic= */ false,
            AggregationTemporality.CUMULATIVE,
            Collections.singletonList(point));
    assertThat(sumData.getPoints()).containsExactly(point);
  }

  @Test
  void createDoubleSumData() {
    DoublePointData point =
        DoublePointData.create(0, 0, Attributes.empty(), 1.0, Collections.emptyList());
    SumData<DoublePointData> sumData =
        SumData.createDoubleSumData(
            /* isMonotonic= */ false,
            AggregationTemporality.CUMULATIVE,
            Collections.singletonList(point));
    assertThat(sumData.getPoints()).containsExactly(point);
  }
}
