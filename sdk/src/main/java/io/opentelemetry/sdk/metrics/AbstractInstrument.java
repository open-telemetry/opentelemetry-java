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
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class AbstractInstrument implements Instrument {

  private final InstrumentDescriptor descriptor;
  private final MeterProviderSharedState meterProviderSharedState;
  private final InstrumentationLibraryInfo instrumentationLibraryInfo;
  private final ActiveBatcher activeBatcher;

  // All arguments cannot be null because they are checked in the abstract builder classes.
  AbstractInstrument(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      ActiveBatcher activeBatcher) {
    this.descriptor = descriptor;
    this.meterProviderSharedState = meterProviderSharedState;
    this.instrumentationLibraryInfo = instrumentationLibraryInfo;
    this.activeBatcher = activeBatcher;
  }

  final InstrumentDescriptor getDescriptor() {
    return descriptor;
  }

  final MeterProviderSharedState getMeterProviderSharedState() {
    return meterProviderSharedState;
  }

  final InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
    return instrumentationLibraryInfo;
  }

  final ActiveBatcher getActiveBatcher() {
    return activeBatcher;
  }

  abstract List<MetricData> collect();

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AbstractInstrument)) {
      return false;
    }

    AbstractInstrument that = (AbstractInstrument) o;

    return descriptor.equals(that.descriptor);
  }

  @Override
  public int hashCode() {
    return descriptor.hashCode();
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

    final MeterProviderSharedState getMeterProviderSharedState() {
      return meterProviderSharedState;
    }

    final InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
      return instrumentationLibraryInfo;
    }

    final InstrumentDescriptor getInstrumentDescriptor() {
      return InstrumentDescriptor.create(name, description, unit, constantLabels, labelKeys);
    }

    abstract B getThis();
  }
}
