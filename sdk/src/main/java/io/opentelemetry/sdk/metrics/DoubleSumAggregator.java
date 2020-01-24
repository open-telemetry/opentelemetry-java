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

package io.opentelemetry.sdk.metrics;

import com.google.common.util.concurrent.AtomicDouble;

final class DoubleSumAggregator
    implements Aggregator.DoubleAggregator<DoubleSumAggregator> {
  // TODO: Change to use DoubleAdder when changed to java8.
  private final AtomicDouble value;

  DoubleSumAggregator() {
    this.value = new AtomicDouble();
  }

  @Override
  public void merge(DoubleSumAggregator other) {
    this.value.addAndGet(other.value.get());
  }

  @Override
  public void update(double value) {
    this.value.addAndGet(value);
  }
}
