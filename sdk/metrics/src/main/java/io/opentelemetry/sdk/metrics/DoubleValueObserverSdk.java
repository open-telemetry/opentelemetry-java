/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.DoubleValueObserver;
import io.opentelemetry.sdk.metrics.AbstractAsynchronousInstrument.AbstractDoubleAsynchronousInstrument;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import javax.annotation.Nullable;

final class DoubleValueObserverSdk extends AbstractDoubleAsynchronousInstrument
    implements DoubleValueObserver {

  DoubleValueObserverSdk(
      InstrumentDescriptor descriptor,
      InstrumentProcessor instrumentProcessor,
      @Nullable Callback<DoubleResult> metricUpdater) {
    super(descriptor, instrumentProcessor, metricUpdater);
  }

  static final class Builder
      extends AbstractAsynchronousInstrument.Builder<DoubleValueObserverSdk.Builder>
      implements DoubleValueObserver.Builder {

    @Nullable private Callback<DoubleResult> callback;

    Builder(
        String name,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState,
        MeterSdk meterSdk) {
      super(name, meterProviderSharedState, meterSharedState, meterSdk);
    }

    @Override
    Builder getThis() {
      return this;
    }

    @Override
    public Builder setCallback(Callback<DoubleResult> callback) {
      this.callback = callback;
      return this;
    }

    @Override
    public DoubleValueObserverSdk build() {
      InstrumentDescriptor instrumentDescriptor =
          getInstrumentDescriptor(InstrumentType.VALUE_OBSERVER, InstrumentValueType.DOUBLE);
      DoubleValueObserverSdk instrument =
          new DoubleValueObserverSdk(
              instrumentDescriptor, getBatcher(instrumentDescriptor), callback);
      return register(instrument);
    }
  }
}
