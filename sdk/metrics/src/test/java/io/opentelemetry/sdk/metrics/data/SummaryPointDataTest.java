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

class SummaryPointDataTest {

  @Test
  void create() {
    AttributeKey<String> kvAttr = AttributeKey.stringKey("key");
    long startEpochNanos = TimeUnit.MILLISECONDS.toNanos(1000);
    long epochNanos = TimeUnit.MILLISECONDS.toNanos(2000);
    double sum = 10.0;
    long count = 5;
    SummaryPointData summaryPoint =
        SummaryPointData.create(
            startEpochNanos,
            epochNanos,
            Attributes.of(kvAttr, "value"),
            count,
            sum,
            Collections.emptyList());
    assertThat(summaryPoint.getStartEpochNanos()).isEqualTo(startEpochNanos);
    assertThat(summaryPoint.getEpochNanos()).isEqualTo(epochNanos);
    assertThat(summaryPoint.getSum()).isEqualTo(sum);
    assertThat(summaryPoint.getCount()).isEqualTo(count);
  }
}
