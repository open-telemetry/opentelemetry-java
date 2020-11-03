/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.util.concurrent.AtomicDouble;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

public class AbstractAggregatorTest {
  @Test
  void testRecordings() {
    TestAggregator testAggregator = new TestAggregator();

    assertThat(testAggregator.hasRecordings()).isFalse();

    testAggregator.recordLong(22);
    assertThat(testAggregator.hasRecordings()).isTrue();
    assertThat(testAggregator.recordedLong.get()).isEqualTo(22);
    assertThat(testAggregator.recordedDouble.get()).isEqualTo(0);

    testAggregator.mergeToAndReset(new TestAggregator());

    assertThat(testAggregator.hasRecordings()).isFalse();
    assertThat(testAggregator.recordedLong.get()).isEqualTo(0);
    assertThat(testAggregator.recordedDouble.get()).isEqualTo(0);

    testAggregator.recordDouble(33.55);
    assertThat(testAggregator.hasRecordings()).isTrue();
    assertThat(testAggregator.recordedLong.get()).isEqualTo(0);
    assertThat(testAggregator.recordedDouble.get()).isEqualTo(33.55);

    testAggregator.mergeToAndReset(new TestAggregator());

    assertThat(testAggregator.hasRecordings()).isFalse();
    assertThat(testAggregator.recordedLong.get()).isEqualTo(0);
    assertThat(testAggregator.recordedDouble.get()).isEqualTo(0);
  }

  private static class TestAggregator extends AbstractAggregator {
    final AtomicLong recordedLong = new AtomicLong();
    final AtomicDouble recordedDouble = new AtomicDouble();

    @Override
    public MetricData.Point toPoint(long startEpochNanos, long epochNanos, Labels labels) {
      return null;
    }

    @Override
    void doMergeAndReset(Aggregator aggregator) {
      recordedLong.set(0);
      recordedDouble.set(0);
    }

    @Override
    protected void doRecordLong(long value) {
      recordedLong.set(value);
    }

    @Override
    protected void doRecordDouble(double value) {
      recordedDouble.set(value);
    }
  }
}
