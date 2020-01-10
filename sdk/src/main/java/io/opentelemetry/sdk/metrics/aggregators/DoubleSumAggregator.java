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

import com.google.common.util.concurrent.AtomicDouble;
import io.opentelemetry.sdk.metrics.aggregators.BaseAggregator.BaseDoubleAggregator;
import javax.annotation.concurrent.ThreadSafe;

/** DoubleSumAggregator aggregates double values by computing a Sum. */
@ThreadSafe
public class DoubleSumAggregator implements BaseDoubleAggregator {
  // TODO: Change to use DoubleAdder when changed to java8.
  private final AtomicDouble value;

  public DoubleSumAggregator() {
    this.value = new AtomicDouble();
  }

  @Override
  public void merge(BaseAggregator aggregator) {
    if (!(aggregator instanceof DoubleSumAggregator)) {
      return;
    }
    DoubleSumAggregator other = (DoubleSumAggregator) aggregator;
    this.value.addAndGet(other.value.get());
  }

  @Override
  public void update(double value) {
    this.value.addAndGet(value);
  }
}
