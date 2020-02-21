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
import io.opentelemetry.sdk.metrics.data.MetricData.LongSummaryPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class LongSummaryAggregator extends AbstractAggregator {
  private static final AggregatorFactory AGGREGATOR_FACTORY =
      new AggregatorFactory() {
        @Override
        public Aggregator getAggregator() {
          return new LongSummaryAggregator();
        }
      };

  // The current value. This controls its own internal thread-safety via method access. Don't
  // try to use its fields directly.
  private final Summary current = new Summary();

  public static AggregatorFactory getFactory() {
    return AGGREGATOR_FACTORY;
  }

  @Override
  void doMergeAndReset(Aggregator target) {
    LongSummaryAggregator other = (LongSummaryAggregator) target;

    Summary copy = current.copyAndReset();
    other.current.update(copy.count, copy.sum, copy.min, copy.max);
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

  private static class Summary {
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

    private void update(long count, long sum, Long min, Long max) {
      lock.writeLock().lock();
      try {
        this.count += count;
        this.sum += sum;
        this.min = this.min == null ? min : Math.min(min, this.min);
        this.max = this.max == null ? max : Math.max(max, this.max);
      } finally {
        lock.writeLock().unlock();
      }
    }

    private void record(long value) {
      lock.writeLock().lock();
      try {
        count++;
        sum += value;
        min = this.min == null ? value : Math.min(value, min);
        max = this.max == null ? value : Math.max(value, max);
      } finally {
        lock.writeLock().unlock();
      }
    }

    private Summary copyAndReset() {
      Summary copy = new Summary();
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

    private Point toPoint(long startEpochNanos, long epochNanos, Map<String, String> labels) {
      lock.readLock().lock();
      try {
        return LongSummaryPoint.create(startEpochNanos, epochNanos, labels, count, sum, min, max);
      } finally {
        lock.readLock().unlock();
      }
    }
  }
}
