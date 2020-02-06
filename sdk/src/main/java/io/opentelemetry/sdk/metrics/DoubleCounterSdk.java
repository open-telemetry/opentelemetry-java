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

final class DoubleCounterSdk extends BaseInstrument implements DoubleCounter {

  private final boolean monotonic;

  private DoubleCounterSdk(
      String name,
      String description,
      String unit,
      Map<String, String> constantLabels,
      List<String> labelKeys,
      boolean monotonic) {
    super(name, description, unit, constantLabels, labelKeys);
    this.monotonic = monotonic;
  }

  @Override
  public void add(double delta, LabelSet labelSet) {
    bind(labelSet).add(delta);
  }

  @Override
  public BoundDoubleCounter bind(LabelSet labelSet) {
    return new Bound(labelSet, monotonic);
  }

  @Override
  public void unbind(BoundDoubleCounter boundInstrument) {
    // TODO: Implement this.
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DoubleCounterSdk)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    DoubleCounterSdk that = (DoubleCounterSdk) o;

    return monotonic == that.monotonic;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (monotonic ? 1 : 0);
    return result;
  }

  private static final class Bound extends BaseBoundInstrument implements BoundDoubleCounter {

    private final boolean monotonic;

    Bound(LabelSet labels, boolean monotonic) {
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

  static DoubleCounter.Builder builder(String name) {
    return new Builder(name);
  }

  private static final class Builder
      extends AbstractCounterBuilder<DoubleCounter.Builder, DoubleCounter>
      implements DoubleCounter.Builder {

    private Builder(String name) {
      super(name);
    }

    @Override
    Builder getThis() {
      return this;
    }

    @Override
    public DoubleCounter build() {
      return new DoubleCounterSdk(
          getName(),
          getDescription(),
          getUnit(),
          getConstantLabels(),
          getLabelKeys(),
          isMonotonic());
    }
  }
}
