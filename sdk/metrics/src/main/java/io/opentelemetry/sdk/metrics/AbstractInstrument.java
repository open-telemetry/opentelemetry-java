/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.api.internal.Utils;
import io.opentelemetry.api.metrics.Instrument;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.List;
import java.util.Objects;

abstract class AbstractInstrument implements Instrument {

  private final InstrumentDescriptor descriptor;
  private final MeterProviderSharedState meterProviderSharedState;
  private final MeterSharedState meterSharedState;
  private final InstrumentAccumulator instrumentAccumulator;

  // All arguments cannot be null because they are checked in the abstract builder classes.
  AbstractInstrument(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      InstrumentAccumulator instrumentAccumulator) {
    this.descriptor = descriptor;
    this.meterProviderSharedState = meterProviderSharedState;
    this.meterSharedState = meterSharedState;
    this.instrumentAccumulator = instrumentAccumulator;
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

  final InstrumentAccumulator getInstrumentAccumulator() {
    return instrumentAccumulator;
  }

  /**
   * Collects records from all the entries (labelSet, Bound) that changed since the previous call.
   */
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
    /* VisibleForTesting */ static final String ERROR_MESSAGE_INVALID_NAME =
        "Name should be a ASCII string with a length no greater than "
            + StringUtils.METRIC_NAME_MAX_LENGTH
            + " characters.";

    private final String name;
    private final MeterProviderSharedState meterProviderSharedState;
    private final MeterSharedState meterSharedState;
    private final MeterSdk meterSdk;
    private String description = "";
    private String unit = "1";

    Builder(
        String name,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState,
        MeterSdk meterSdk) {
      this.meterSdk = meterSdk;
      Objects.requireNonNull(name, "name");
      Utils.checkArgument(StringUtils.isValidMetricName(name), ERROR_MESSAGE_INVALID_NAME);
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

    final MeterProviderSharedState getMeterProviderSharedState() {
      return meterProviderSharedState;
    }

    final MeterSharedState getMeterSharedState() {
      return meterSharedState;
    }

    final InstrumentDescriptor getInstrumentDescriptor(
        InstrumentType type, InstrumentValueType valueType) {
      return InstrumentDescriptor.create(name, description, unit, type, valueType);
    }

    abstract B getThis();

    final <I extends AbstractInstrument> I register(I instrument) {
      return getMeterSharedState().getInstrumentRegistry().register(instrument);
    }

    protected InstrumentAccumulator getBatcher(InstrumentDescriptor descriptor) {
      return meterSdk.createBatcher(descriptor, meterProviderSharedState, meterSharedState);
    }
  }
}
