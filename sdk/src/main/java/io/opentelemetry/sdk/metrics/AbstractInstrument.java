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

import io.opentelemetry.internal.StringUtils;
import io.opentelemetry.internal.Utils;
import io.opentelemetry.metrics.Instrument;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class AbstractInstrument implements Instrument {

  private final String name;
  private final String description;
  private final String unit;
  private final Map<String, String> constantLabels;
  private final List<String> labelKeys;

  // All arguments cannot be null because they are checked in the abstract builder classes.
  AbstractInstrument(
      String name,
      String description,
      String unit,
      Map<String, String> constantLabels,
      List<String> labelKeys) {
    this.name = name;
    this.description = description;
    this.unit = unit;
    this.constantLabels = constantLabels;
    this.labelKeys = labelKeys;
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

  abstract static class Builder<B extends Instrument.Builder<B, V>, V>
      implements Instrument.Builder<B, V> {
    /* VisibleForTesting */ static final int NAME_MAX_LENGTH = 255;
    /* VisibleForTesting */ static final String ERROR_MESSAGE_INVALID_NAME =
        "Name should be a ASCII string with a length no greater than "
            + NAME_MAX_LENGTH
            + " characters.";

    private final String name;
    private final MeterProviderSharedState meterProviderSharedState;
    private final InstrumentationLibraryInfo instrumentationLibraryInfo;
    private String description = "";
    private String unit = "1";
    private List<String> labelKeys = Collections.emptyList();
    private Map<String, String> constantLabels = Collections.emptyMap();

    Builder(
        String name,
        MeterProviderSharedState meterProviderSharedState,
        InstrumentationLibraryInfo instrumentationLibraryInfo) {
      Utils.checkNotNull(name, "name");
      Utils.checkArgument(
          StringUtils.isValidMetricName(name) && name.length() <= NAME_MAX_LENGTH,
          ERROR_MESSAGE_INVALID_NAME);
      this.name = name;
      this.meterProviderSharedState = meterProviderSharedState;
      this.instrumentationLibraryInfo = instrumentationLibraryInfo;
    }

    @Override
    public final B setDescription(String description) {
      this.description = Utils.checkNotNull(description, "description");
      return getThis();
    }

    @Override
    public final B setUnit(String unit) {
      this.unit = Utils.checkNotNull(unit, "unit");
      return getThis();
    }

    @Override
    public final B setLabelKeys(List<String> labelKeys) {
      Utils.checkListElementNotNull(Utils.checkNotNull(labelKeys, "labelKeys"), "labelKey");
      this.labelKeys = Collections.unmodifiableList(new ArrayList<>(labelKeys));
      return getThis();
    }

    @Override
    public final B setConstantLabels(Map<String, String> constantLabels) {
      Utils.checkMapKeysNotNull(
          Utils.checkNotNull(constantLabels, "constantLabels"), "constantLabel");
      this.constantLabels = Collections.unmodifiableMap(new HashMap<>(constantLabels));
      return getThis();
    }

    final String getName() {
      return name;
    }

    final MeterProviderSharedState getMeterProviderSharedState() {
      return meterProviderSharedState;
    }

    final InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
      return instrumentationLibraryInfo;
    }

    final String getDescription() {
      return description;
    }

    final String getUnit() {
      return unit;
    }

    final List<String> getLabelKeys() {
      return labelKeys;
    }

    final Map<String, String> getConstantLabels() {
      return constantLabels;
    }

    abstract B getThis();
  }
}
