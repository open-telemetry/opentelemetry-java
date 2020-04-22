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
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import io.opentelemetry.sdk.metrics.view.Aggregation;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

abstract class AbstractInstrument implements Instrument {

  private final InstrumentDescriptor descriptor;
  private final MeterProviderSharedState meterProviderSharedState;
  private final MeterSharedState meterSharedState;
  private final ActiveBatcher activeBatcher;

  // All arguments cannot be null because they are checked in the abstract builder classes.
  AbstractInstrument(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      ActiveBatcher activeBatcher) {
    this.descriptor = descriptor;
    this.meterProviderSharedState = meterProviderSharedState;
    this.meterSharedState = meterSharedState;
    this.activeBatcher = activeBatcher;
  }

  final InstrumentDescriptor getDescriptor() {
    return descriptor;
  }

  final MeterProviderSharedState getMeterProviderSharedState() {
    return meterProviderSharedState;
  }

  final MeterSharedState getMeterSharedState() {
    return meterSharedState;
  }

  final ActiveBatcher getActiveBatcher() {
    return activeBatcher;
  }

  abstract List<MetricData> collectAll();

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

  abstract static class Builder<B extends AbstractInstrument.Builder<?>>
      implements Instrument.Builder {
    /* VisibleForTesting */ static final int NAME_MAX_LENGTH = 255;
    /* VisibleForTesting */ static final String ERROR_MESSAGE_INVALID_NAME =
        "Name should be a ASCII string with a length no greater than "
            + NAME_MAX_LENGTH
            + " characters.";

    private final String name;
    private final MeterProviderSharedState meterProviderSharedState;
    private final MeterSharedState meterSharedState;
    private String description = "";
    private String unit = "1";
    private Map<String, String> constantLabels = Collections.emptyMap();

    Builder(
        String name,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState) {
      Objects.requireNonNull(name, "name");
      Utils.checkArgument(
          StringUtils.isValidMetricName(name) && name.length() <= NAME_MAX_LENGTH,
          ERROR_MESSAGE_INVALID_NAME);
      this.name = name;
      this.meterProviderSharedState = meterProviderSharedState;
      this.meterSharedState = meterSharedState;
    }

    @Override
    public final B setDescription(String description) {
      this.description = Objects.requireNonNull(description, "description");
      return getThis();
    }

    @Override
    public final B setUnit(String unit) {
      this.unit = Objects.requireNonNull(unit, "unit");
      return getThis();
    }

    @Override
    public final B setConstantLabels(Map<String, String> constantLabels) {
      Utils.checkMapKeysNotNull(
          Objects.requireNonNull(constantLabels, "constantLabels"), "constantLabel");
      this.constantLabels = Collections.unmodifiableMap(new HashMap<>(constantLabels));
      return getThis();
    }

    final MeterProviderSharedState getMeterProviderSharedState() {
      return meterProviderSharedState;
    }

    final MeterSharedState getMeterSharedState() {
      return meterSharedState;
    }

    final InstrumentDescriptor getInstrumentDescriptor() {
      return InstrumentDescriptor.create(name, description, unit, constantLabels);
    }

    abstract B getThis();

    final <I extends AbstractInstrument> I register(I instrument) {
      return getMeterSharedState().getInstrumentRegistry().register(instrument);
    }
  }

  static Descriptor getDefaultMetricDescriptor(
      InstrumentDescriptor descriptor,
      InstrumentType instrumentType,
      InstrumentValueType instrumentValueType,
      Aggregation aggregation) {
    return Descriptor.create(
        descriptor.getName(),
        descriptor.getDescription(),
        aggregation.getUnit(descriptor.getUnit()),
        aggregation.getDescriptorType(instrumentType, instrumentValueType),
        descriptor.getConstantLabels());
  }

  static Batcher getDefaultBatcher(
      InstrumentDescriptor descriptor,
      InstrumentType instrumentType,
      InstrumentValueType instrumentValueType,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      Aggregation defaultAggregation) {
    return Batchers.getCumulativeAllLabels(
        getDefaultMetricDescriptor(
            descriptor, instrumentType, instrumentValueType, defaultAggregation),
        meterProviderSharedState.getResource(),
        meterSharedState.getInstrumentationLibraryInfo(),
        defaultAggregation.getAggregatorFactory(instrumentValueType),
        meterProviderSharedState.getClock());
  }
}
