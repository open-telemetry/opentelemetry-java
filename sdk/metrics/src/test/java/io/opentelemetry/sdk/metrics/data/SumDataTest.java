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
    long startEpochNanos = TimeUnit.MILLISECONDS.toNanos(1000);
    long epochNanos = TimeUnit.MILLISECONDS.toNanos(2000);
    long longValue = 10;
    AttributeKey<String> key = AttributeKey.stringKey("key");
    LongPointData longPoint =
        LongPointData.create(startEpochNanos, epochNanos, Attributes.of(key, "value"), longValue);
    SumData<LongPointData> sumData =
        SumData.create(
            /* isMonotonic= */ false,
            AggregationTemporality.CUMULATIVE,
            Collections.singletonList(longPoint));
    assertThat(sumData.isMonotonic()).isFalse();
    assertThat(sumData.getAggregationTemporality()).isEqualTo(AggregationTemporality.CUMULATIVE);
  }
}
