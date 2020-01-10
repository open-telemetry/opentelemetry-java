/*
 * Copyright 2019, OpenTelemetry Authors
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

package io.opentelemetry.sdk.metrics.aggregators;

import io.opentelemetry.sdk.metrics.aggregators.BaseAggregator.BaseLongAggregator;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.concurrent.ThreadSafe;

/** LongSumAggregator aggregates long values by computing a Sum. */
@ThreadSafe
public final class LongSumAggregator implements BaseLongAggregator {
  // TODO: Change to use LongAdder when changed to java8.
  private final AtomicLong value;

  public LongSumAggregator() {
    this.value = new AtomicLong();
  }

  @Override
  public void merge(BaseAggregator aggregator) {
    if (!(aggregator instanceof LongSumAggregator)) {
      return;
    }
    LongSumAggregator other = (LongSumAggregator) aggregator;
    this.value.addAndGet(other.value.get());
  }

  @Override
  public void update(long value) {
    this.value.addAndGet(value);
  }
}
