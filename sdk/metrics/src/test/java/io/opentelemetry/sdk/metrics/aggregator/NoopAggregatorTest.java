/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Labels;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link NoopAggregator}. */
class NoopAggregatorTest {
  @Test
  void factoryAggregation() {
    AggregatorFactory factory = NoopAggregator.getFactory();
    assertThat(factory.getAggregator()).isInstanceOf(NoopAggregator.class);
  }

  @Test
  void noopOperations() {
    Aggregator aggregator = NoopAggregator.getFactory().getAggregator();
    aggregator.recordLong(12);
    aggregator.recordDouble(12.1);
    aggregator.mergeToAndReset(aggregator);
    assertThat(aggregator.toPoint(1, 2, Labels.empty())).isNull();
    assertThat(aggregator.hasRecordings()).isFalse();
  }
}
