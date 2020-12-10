/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.DoubleUpDownSumObserver;
import io.opentelemetry.sdk.metrics.AbstractAsynchronousInstrument.AbstractDoubleAsynchronousInstrument;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import javax.annotation.Nullable;

final class DoubleUpDownSumObserverSdk extends AbstractDoubleAsynchronousInstrument
    implements DoubleUpDownSumObserver {

  DoubleUpDownSumObserverSdk(
      InstrumentDescriptor descriptor,
      InstrumentProcessor instrumentProcessor,
      @Nullable Callback<DoubleResult> metricUpdater) {
    super(descriptor, instrumentProcessor, metricUpdater);
  }

  static final class Builder
      extends AbstractAsynchronousInstrument.Builder<DoubleUpDownSumObserverSdk.Builder>
      implements DoubleUpDownSumObserver.Builder {

    @Nullable private Callback<DoubleResult> callback;

    Builder(
        String name,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState) {
      super(name, meterProviderSharedState, meterSharedState);
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
    public DoubleUpDownSumObserverSdk build() {
      InstrumentDescriptor instrumentDescriptor =
          getInstrumentDescriptor(InstrumentType.UP_DOWN_SUM_OBSERVER, InstrumentValueType.DOUBLE);
      DoubleUpDownSumObserverSdk instrument =
          new DoubleUpDownSumObserverSdk(
              instrumentDescriptor, getBatcher(instrumentDescriptor), callback);
      return register(instrument);
    }
  }
}
