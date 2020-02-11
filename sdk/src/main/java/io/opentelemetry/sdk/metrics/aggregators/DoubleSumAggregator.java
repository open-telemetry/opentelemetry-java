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

package io.opentelemetry.sdk.metrics.aggregators;

import com.google.common.util.concurrent.AtomicDouble;

public final class DoubleSumAggregator implements Aggregator {
  // TODO: Change to use DoubleAdder when changed to java8.
  private final AtomicDouble current;

  public DoubleSumAggregator() {
    current = new AtomicDouble(0.0);
  }

  @Override
  public void mergeAndReset(Aggregator aggregator) {
    if (!(aggregator instanceof DoubleSumAggregator)) {
      return;
    }

    DoubleSumAggregator other = (DoubleSumAggregator) aggregator;
    other.current.getAndAdd(this.current.getAndSet(0));
  }

  @Override
  public void recordLong(long value) {
    throw new UnsupportedOperationException("This is a DoubleSumAggregator");
  }

  @Override
  public void recordDouble(double value) {
    current.getAndAdd(value);
  }
}
