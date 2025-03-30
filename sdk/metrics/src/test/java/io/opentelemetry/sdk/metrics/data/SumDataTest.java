/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

public class SumDataTest {

  @Test
  void create() {
    long START_EPOCH_NANOS = TimeUnit.MILLISECONDS.toNanos(1000);
    long EPOCH_NANOS = TimeUnit.MILLISECONDS.toNanos(2000);
    long LONG_VALUE = 10;
    AttributeKey<String> KEY = AttributeKey.stringKey("key");
    LongPointData LONG_POINT =
        LongPointData.create(
            START_EPOCH_NANOS, EPOCH_NANOS, Attributes.of(KEY, "value"), LONG_VALUE);
    SumData<LongPointData> sumData =
        SumData.create(
            /* isMonotonic= */ false,
            AggregationTemporality.CUMULATIVE,
            Collections.singletonList(LONG_POINT));
    assertThat(sumData.isMonotonic()).isFalse();
    assertThat(sumData.getAggregationTemporality()).isEqualTo(AggregationTemporality.CUMULATIVE);
  }
}
