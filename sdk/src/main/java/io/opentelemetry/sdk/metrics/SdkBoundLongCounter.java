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

import io.opentelemetry.metrics.LabelSet;
import io.opentelemetry.metrics.LongCounter.BoundLongCounter;

class SdkBoundLongCounter extends BaseBoundInstrument<SdkLongCounter> implements BoundLongCounter {

  private final boolean monotonic;

  SdkBoundLongCounter(LabelSet labels, boolean monotonic) {
    super(labels);
    this.monotonic = monotonic;
  }

  @Override
  public void add(long delta) {
    if (monotonic && delta < 0) {
      throw new IllegalArgumentException("monotonic counters can only increase");
    }
    // todo: pass through to an aggregator/accumulator
  }
}
