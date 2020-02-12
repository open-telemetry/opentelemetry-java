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

import io.opentelemetry.metrics.Instrument;
import java.util.List;
import java.util.Map;

abstract class AbstractInstrument implements Instrument {

  private final String name;
  private final String description;
  private final String unit;
  private final Map<String, String> constantLabels;
  private final List<String> labelKeys;
  private final MeterSharedState meterSharedState;

  // All arguments cannot be null because they are checked in the abstract builder classes.
  AbstractInstrument(
      String name,
      String description,
      String unit,
      Map<String, String> constantLabels,
      List<String> labelKeys,
      MeterSharedState meterSharedState) {
    this.name = name;
    this.description = description;
    this.unit = unit;
    this.constantLabels = constantLabels;
    this.labelKeys = labelKeys;
    this.meterSharedState = meterSharedState;
  }

  final String getName() {
    return name;
  }

  final String getDescription() {
    return description;
  }

  final String getUnit() {
    return unit;
  }

  final Map<String, String> getConstantLabels() {
    return constantLabels;
  }

  final List<String> getLabelKeys() {
    return labelKeys;
  }

  final MeterSharedState getMeterSharedState() {
    return meterSharedState;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AbstractInstrument)) {
      return false;
    }

    AbstractInstrument that = (AbstractInstrument) o;

    return name.equals(that.name)
        && description.equals(that.description)
        && unit.equals(that.unit)
        && constantLabels.equals(that.constantLabels)
        && labelKeys.equals(that.labelKeys);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + description.hashCode();
    result = 31 * result + unit.hashCode();
    result = 31 * result + constantLabels.hashCode();
    result = 31 * result + labelKeys.hashCode();
    return result;
  }
}
