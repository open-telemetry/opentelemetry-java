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

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.metrics.DoubleGauge;
import io.opentelemetry.metrics.DoubleGauge.BoundDoubleGauge;
import io.opentelemetry.metrics.LabelSet;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

class SdkDoubleGauge extends BaseInstrument<BoundDoubleGauge> implements DoubleGauge {

  private final boolean monotonic;

  private SdkDoubleGauge(
      String name,
      String description,
      Map<String, String> constantLabels,
      List<String> labelKeys,
      boolean monotonic) {
    super(name, description, constantLabels, labelKeys);
    this.monotonic = monotonic;
  }

  @Override
  public void set(double val, LabelSet labelSet) {
    createBoundInstrument(labelSet).set(val);
  }

  @Override
  BoundDoubleGauge createBoundInstrument(LabelSet labelSet) {
    return new SdkBoundDoubleGauge(labelSet, monotonic);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SdkDoubleGauge)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    SdkDoubleGauge that = (SdkDoubleGauge) o;

    return monotonic == that.monotonic;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (monotonic ? 1 : 0);
    return result;
  }

  private static class SdkBoundDoubleGauge extends BaseBoundInstrument implements BoundDoubleGauge {

    private final boolean monotonic;
    @Nullable private Double currentValue = null;

    SdkBoundDoubleGauge(LabelSet labels, boolean monotonic) {
      super(labels);
      this.monotonic = monotonic;
    }

    @Override
    public void set(double value) {
      if (monotonic && (currentValue != null && value < currentValue)) {
        throw new IllegalArgumentException("monotonic gauge can only increase");
      }
      currentValue = value;
      // todo: pass through to an aggregator/accumulator
    }
  }

  static class Builder extends AbstractGaugeBuilder<DoubleGauge.Builder, DoubleGauge>
      implements DoubleGauge.Builder {

    private Builder(String name) {
      super(name);
    }

    static DoubleGauge.Builder builder(String name) {
      return new Builder(name);
    }

    @Override
    Builder getThis() {
      return this;
    }

    @Override
    public DoubleGauge build() {
      return new SdkDoubleGauge(
          getName(), getDescription(), getConstantLabels(), getLabelKeys(), getMonotonic());
    }
  }
}
