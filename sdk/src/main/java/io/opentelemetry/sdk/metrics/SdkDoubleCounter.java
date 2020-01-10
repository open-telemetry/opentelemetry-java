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

import io.opentelemetry.metrics.DoubleCounter;
import io.opentelemetry.metrics.DoubleCounter.BoundDoubleCounter;
import io.opentelemetry.metrics.LabelSet;
import java.util.List;
import java.util.Map;

class SdkDoubleCounter extends BaseInstrument<BoundDoubleCounter> implements DoubleCounter {

  private final boolean monotonic;

  private SdkDoubleCounter(
      String name,
      String description,
      Map<String, String> constantLabels,
      List<String> labelKeys,
      boolean monotonic) {
    super(name, description, constantLabels, labelKeys);
    this.monotonic = monotonic;
  }

  @Override
  public void add(double delta, LabelSet labelSet) {
    createBoundInstrument(labelSet).add(delta);
  }

  @Override
  BoundDoubleCounter createBoundInstrument(LabelSet labelSet) {
    return new SdkBoundDoubleCounter(labelSet, monotonic);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SdkDoubleCounter)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    SdkDoubleCounter that = (SdkDoubleCounter) o;

    return monotonic == that.monotonic;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (monotonic ? 1 : 0);
    return result;
  }

  private static class SdkBoundDoubleCounter extends BaseBoundInstrument implements BoundDoubleCounter {

    private final boolean monotonic;

    SdkBoundDoubleCounter(LabelSet labels, boolean monotonic) {
      super(labels);
      this.monotonic = monotonic;
    }

    @Override
    public void add(double delta) {
      if (monotonic && delta < 0) {
        throw new IllegalArgumentException("monotonic counters can only increase");
      }
      // todo: pass through to an aggregator/accumulator
    }
  }

  static class SdkDoubleCounterBuilder
      extends AbstractCounterBuilder<DoubleCounter.Builder, DoubleCounter>
      implements DoubleCounter.Builder {

    private SdkDoubleCounterBuilder(String name) {
      super(name);
    }

    static DoubleCounter.Builder builder(String name) {
      return new SdkDoubleCounterBuilder(name);
    }

    @Override
    SdkDoubleCounterBuilder getThis() {
      return this;
    }

    @Override
    public DoubleCounter build() {
      return new SdkDoubleCounter(
          getName(), getDescription(), getConstantLabels(), getLabelKeys(), getMonotonic());
    }
  }
}
