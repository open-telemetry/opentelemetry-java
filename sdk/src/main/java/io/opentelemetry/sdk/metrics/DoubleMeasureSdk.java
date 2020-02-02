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

import io.opentelemetry.metrics.DoubleMeasure;
import io.opentelemetry.metrics.DoubleMeasure.BoundDoubleMeasure;
import io.opentelemetry.metrics.LabelSet;
import java.util.List;
import java.util.Map;

final class DoubleMeasureSdk extends BaseInstrument<BoundDoubleMeasure> implements DoubleMeasure {

  private final boolean absolute;

  private DoubleMeasureSdk(
      String name,
      String description,
      Map<String, String> constantLabels,
      List<String> labelKeys,
      boolean absolute) {
    super(name, description, constantLabels, labelKeys);
    this.absolute = absolute;
  }

  @Override
  public void record(double value, LabelSet labelSet) {
    createBoundInstrument(labelSet).record(value);
  }

  @Override
  BoundDoubleMeasure createBoundInstrument(LabelSet labelSet) {
    return new Bound(labelSet, this.absolute);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DoubleMeasureSdk)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    DoubleMeasureSdk that = (DoubleMeasureSdk) o;

    return absolute == that.absolute;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (absolute ? 1 : 0);
    return result;
  }

  private static final class Bound extends BaseBoundInstrument implements BoundDoubleMeasure {

    private final boolean absolute;

    Bound(LabelSet labels, boolean absolute) {
      super(labels);
      this.absolute = absolute;
    }

    @Override
    public void record(double value) {
      if (this.absolute && value < 0) {
        throw new IllegalArgumentException("absolute measure can only record positive values");
      }
      // todo: pass through to an aggregator/accumulator
    }
  }

  static final class Builder extends AbstractMeasureBuilder<DoubleMeasure.Builder, DoubleMeasure>
      implements DoubleMeasure.Builder {

    private Builder(String name) {
      super(name);
    }

    static DoubleMeasure.Builder builder(String name) {
      return new Builder(name);
    }

    @Override
    Builder getThis() {
      return this;
    }

    @Override
    public DoubleMeasure build() {
      return new DoubleMeasureSdk(
          getName(), getDescription(), getConstantLabels(), getLabelKeys(), isAbsolute());
    }
  }
}
