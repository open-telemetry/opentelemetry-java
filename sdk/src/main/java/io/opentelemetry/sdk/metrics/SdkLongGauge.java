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
import io.opentelemetry.metrics.LongGauge;
import io.opentelemetry.metrics.LongGauge.BoundLongGauge;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

class SdkLongGauge extends BaseInstrument<BoundLongGauge> implements LongGauge {

  private final boolean monotonic;

  private SdkLongGauge(
      String name,
      String description,
      Map<String, String> constantLabels,
      List<String> labelKeys,
      boolean monotonic) {
    super(name, description, constantLabels, labelKeys);
    this.monotonic = monotonic;
  }

  @Override
  public void set(long val, LabelSet labelSet) {
    createBoundInstrument(labelSet).set(val);
  }

  @Override
  BoundLongGauge createBoundInstrument(LabelSet labelSet) {
    return new SdkBoundLongGauge(labelSet, monotonic);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SdkLongGauge)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    SdkLongGauge that = (SdkLongGauge) o;

    return monotonic == that.monotonic;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (monotonic ? 1 : 0);
    return result;
  }

  private static class SdkBoundLongGauge extends BaseBoundInstrument implements BoundLongGauge {

    private final boolean monotonic;
    @Nullable private Long prev = null;

    SdkBoundLongGauge(LabelSet labels, boolean monotonic) {
      super(labels);
      this.monotonic = monotonic;
    }

    @Override
    public void set(long val) {
      if (monotonic && (prev != null && val < prev)) {
        throw new IllegalArgumentException("monotonic gauge can only increase");
      }
      prev = val;
      // todo: pass through to an aggregator/accumulator
    }
  }

  static class Builder extends AbstractGaugeBuilder<LongGauge.Builder, LongGauge>
      implements LongGauge.Builder {

    private Builder(String name) {
      super(name);
    }

    static LongGauge.Builder builder(String name) {
      return new Builder(name);
    }

    @Override
    Builder getThis() {
      return this;
    }

    @Override
    public LongGauge build() {
      return new SdkLongGauge(
          getName(), getDescription(), getConstantLabels(), getLabelKeys(), getMonotonic());
    }
  }
}
