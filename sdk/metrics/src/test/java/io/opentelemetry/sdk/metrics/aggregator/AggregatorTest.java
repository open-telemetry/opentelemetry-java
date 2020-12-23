/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.util.concurrent.AtomicDouble;
import io.opentelemetry.sdk.metrics.aggregation.Accumulation;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;

public class AggregatorTest {

  @Test
  void acquireMapped() {
    TestAggregator testAggregator = new TestAggregator();
    assertThat(testAggregator.acquire()).isTrue();
    testAggregator.release();
    assertThat(testAggregator.acquire()).isTrue();
    assertThat(testAggregator.acquire()).isTrue();
    testAggregator.release();
    assertThat(testAggregator.acquire()).isTrue();
    testAggregator.release();
    testAggregator.release();
  }

  @Test
  void tryUnmap_AcquiredHandler() {
    TestAggregator testAggregator = new TestAggregator();
    assertThat(testAggregator.acquire()).isTrue();
    assertThat(testAggregator.tryUnmap()).isFalse();
    testAggregator.release();
    // The aggregator is by default acquired, so need an extra release.
    assertThat(testAggregator.tryUnmap()).isFalse();
    testAggregator.release();
    assertThat(testAggregator.tryUnmap()).isTrue();
  }

  @Test
  void tryUnmap_AcquiredHandler_MultipleTimes() {
    TestAggregator testAggregator = new TestAggregator();
    assertThat(testAggregator.acquire()).isTrue();
    assertThat(testAggregator.acquire()).isTrue();
    assertThat(testAggregator.acquire()).isTrue();
    assertThat(testAggregator.tryUnmap()).isFalse();
    testAggregator.release();
    assertThat(testAggregator.acquire()).isTrue();
    assertThat(testAggregator.tryUnmap()).isFalse();
    testAggregator.release();
    assertThat(testAggregator.tryUnmap()).isFalse();
    testAggregator.release();
    assertThat(testAggregator.tryUnmap()).isFalse();
    testAggregator.release();
    // The aggregator is by default acquired, so need an extra release.
    assertThat(testAggregator.tryUnmap()).isFalse();
    testAggregator.release();
    assertThat(testAggregator.tryUnmap()).isTrue();
  }

  @Test
  void bind_ThenUnmap_ThenTryToBind() {
    TestAggregator testAggregator = new TestAggregator();
    testAggregator.release();
    assertThat(testAggregator.tryUnmap()).isTrue();
    assertThat(testAggregator.acquire()).isFalse();
    testAggregator.release();
  }

  @Test
  void testRecordings() {
    TestAggregator testAggregator = new TestAggregator();

    testAggregator.recordLong(22);
    assertThat(testAggregator.recordedLong.get()).isEqualTo(22);
    assertThat(testAggregator.recordedDouble.get()).isEqualTo(0);

    testAggregator.accumulateThenReset();
    assertThat(testAggregator.recordedLong.get()).isEqualTo(0);
    assertThat(testAggregator.recordedDouble.get()).isEqualTo(0);

    testAggregator.recordDouble(33.55);
    assertThat(testAggregator.recordedLong.get()).isEqualTo(0);
    assertThat(testAggregator.recordedDouble.get()).isEqualTo(33.55);

    testAggregator.accumulateThenReset();
    assertThat(testAggregator.recordedLong.get()).isEqualTo(0);
    assertThat(testAggregator.recordedDouble.get()).isEqualTo(0);
  }

  private static class TestAggregator extends Aggregator<Accumulation> {
    final AtomicLong recordedLong = new AtomicLong();
    final AtomicDouble recordedDouble = new AtomicDouble();

    @Nullable
    @Override
    protected Accumulation doAccumulateThenReset() {
      recordedLong.set(0);
      recordedDouble.set(0);
      return null;
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
