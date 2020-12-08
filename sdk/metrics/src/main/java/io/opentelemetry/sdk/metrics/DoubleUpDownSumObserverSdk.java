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
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      Batcher batcher,
      @Nullable Callback<DoubleResult> metricUpdater) {
    super(
        descriptor,
        meterProviderSharedState,
        meterSharedState,
        new ActiveBatcher(batcher),
        metricUpdater);
  }

  static final class Builder
      extends AbstractAsynchronousInstrument.Builder<DoubleUpDownSumObserverSdk.Builder>
      implements DoubleUpDownSumObserver.Builder {

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
    public DoubleUpDownSumObserverSdk build() {
      InstrumentDescriptor instrumentDescriptor =
          getInstrumentDescriptor(InstrumentType.UP_DOWN_SUM_OBSERVER, InstrumentValueType.DOUBLE);
      DoubleUpDownSumObserverSdk instrument =
          new DoubleUpDownSumObserverSdk(
              instrumentDescriptor,
              getMeterProviderSharedState(),
              getMeterSharedState(),
              getBatcher(instrumentDescriptor),
              callback);
      return register(instrument);
    }
  }
}
