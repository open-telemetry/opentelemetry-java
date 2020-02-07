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

import io.opentelemetry.metrics.DoubleObserver;
import java.util.List;
import java.util.Map;

final class DoubleObserverSdk extends AbstractInstrument implements DoubleObserver {
  private final boolean monotonic;

  DoubleObserverSdk(
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
  public void setCallback(Callback<DoubleObserver.ResultDoubleObserver> metricUpdater) {
    throw new UnsupportedOperationException("to be implemented");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DoubleObserverSdk)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    DoubleObserverSdk that = (DoubleObserverSdk) o;

    return monotonic == that.monotonic;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (monotonic ? 1 : 0);
    return result;
  }

  static DoubleObserver.Builder builder(String name) {
    return new Builder(name);
  }

  private static final class Builder
      extends AbstractObserverBuilder<DoubleObserver.Builder, DoubleObserver>
      implements DoubleObserver.Builder {

    private Builder(String name) {
      super(name);
    }

    @Override
    Builder getThis() {
      return this;
    }

    @Override
    public DoubleObserver build() {
      return new DoubleObserverSdk(
          getName(),
          getDescription(),
          getUnit(),
          getConstantLabels(),
          getLabelKeys(),
          isMonotonic());
    }
  }
}
