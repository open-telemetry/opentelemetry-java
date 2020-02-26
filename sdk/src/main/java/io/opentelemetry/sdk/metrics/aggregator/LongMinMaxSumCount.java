/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.metrics.aggregator;

import com.google.errorprone.annotations.concurrent.GuardedBy;
import io.opentelemetry.sdk.metrics.data.MetricData.DoubleSummaryPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.DoubleValueAtPercentile;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class LongMinMaxSumCount extends AbstractAggregator {

  private static final AggregatorFactory AGGREGATOR_FACTORY =
      new AggregatorFactory() {
        @Override
        public Aggregator getAggregator() {
          return new LongMinMaxSumCount();
        }
      };

  // The current value. This controls its own internal thread-safety via method access. Don't
  // try to use its fields directly.
  private final LongSummary current = new LongSummary();

  public static AggregatorFactory getFactory() {
    return AGGREGATOR_FACTORY;
  }

  @Override
  void doMergeAndReset(Aggregator target) {
    LongMinMaxSumCount other = (LongMinMaxSumCount) target;

    LongSummary copy = current.copyAndReset();
    other.current.update(copy);
  }

  @Nullable
  @Override
  public Point toPoint(long startEpochNanos, long epochNanos, Map<String, String> labels) {
    return current.toPoint(startEpochNanos, epochNanos, labels);
  }

  @Override
  public void recordLong(long value) {
    current.record(value);
  }

  private static final class LongSummary {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @GuardedBy("lock")
    private long sum = 0;

    @GuardedBy("lock")
    private long count = 0;

    @Nullable
    @GuardedBy("lock")
    private Long min = null;

    @Nullable
    @GuardedBy("lock")
    private Long max = null;

    private void update(LongSummary summary) {
      lock.writeLock().lock();
      try {
        this.count += summary.count;
        this.sum += summary.sum;
        if (this.min == null) {
          this.min = summary.min;
        } else if (summary.min != null) {
          this.min = Math.min(summary.min, this.min);
        }
        if (this.max == null) {
          this.max = summary.max;
        } else if (summary.max != null) {
          this.max = Math.max(summary.max, this.max);
        }
      } finally {
        lock.writeLock().unlock();
      }
    }

    private void record(long value) {
      lock.writeLock().lock();
      try {
        count++;
        sum += value;
        min = min == null ? value : Math.min(value, min);
        max = max == null ? value : Math.max(value, max);
      } finally {
        lock.writeLock().unlock();
      }
    }

    private LongSummary copyAndReset() {
      LongSummary copy = new LongSummary();
      lock.writeLock().lock();
      try {
        copy.count = count;
        copy.sum = sum;
        copy.min = min;
        copy.max = max;
        count = 0;
        sum = 0;
        min = null;
        max = null;
      } finally {
        lock.writeLock().unlock();
      }
      return copy;
    }

    private DoubleSummaryPoint toPoint(
        long startEpochNanos, long epochNanos, Map<String, String> labels) {
      lock.readLock().lock();
      try {
        return DoubleSummaryPoint.create(
            startEpochNanos,
            epochNanos,
            labels,
            count,
            sum,
            (min == null || max == null)
                ? Collections.<DoubleValueAtPercentile>emptyList()
                : Arrays.asList(
                    DoubleValueAtPercentile.create(0.0, min.doubleValue()),
                    DoubleValueAtPercentile.create(100.0, max.doubleValue())));
      } finally {
        lock.readLock().unlock();
      }
    }
  }
}
