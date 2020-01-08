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
import io.opentelemetry.metrics.LongCounter;
import io.opentelemetry.metrics.LongCounter.BoundLongCounter;
import java.util.List;
import java.util.Map;

class SdkLongCounter extends BaseInstrument<BoundLongCounter> implements LongCounter {

  private final boolean monotonic;

  SdkLongCounter(
      String name,
      String description,
      Map<String, String> constantLabels,
      List<String> labelKeys,
      boolean monotonic) {
    super(name, description, constantLabels, labelKeys);
    this.monotonic = monotonic;
  }

  @Override
  public void add(long delta, LabelSet labelSet) {
    create(labelSet).add(delta);
  }

  @Override
  BoundLongCounter create(LabelSet labelSet) {
    return new SdkBoundLongCounter(labelSet, monotonic);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SdkLongCounter)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    SdkLongCounter that = (SdkLongCounter) o;

    return monotonic == that.monotonic;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (monotonic ? 1 : 0);
    return result;
  }

  static class SdkBoundLongCounter extends BaseBoundInstrument implements BoundLongCounter {

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
}
