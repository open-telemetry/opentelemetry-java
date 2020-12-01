/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.DoubleValueObserver;
import io.opentelemetry.sdk.metrics.AbstractAsynchronousInstrument.AbstractDoubleAsynchronousInstrument;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;

final class DoubleValueObserverSdk extends AbstractDoubleAsynchronousInstrument
    implements DoubleValueObserver {

  DoubleValueObserverSdk(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      Batcher batcher) {
    super(descriptor, meterProviderSharedState, meterSharedState, new ActiveBatcher(batcher));
  }

  static final class Builder
      extends AbstractAsynchronousInstrument.Builder<DoubleValueObserverSdk.Builder>
      implements DoubleValueObserver.Builder {

    private Callback<DoubleResult> callback;

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

    @SuppressWarnings("deprecation") // need to call the deprecated method for now
    @Override
    public DoubleValueObserverSdk build() {
      InstrumentDescriptor instrumentDescriptor =
          getInstrumentDescriptor(InstrumentType.VALUE_OBSERVER, InstrumentValueType.DOUBLE);
      DoubleValueObserverSdk instrument =
          new DoubleValueObserverSdk(
              instrumentDescriptor,
              getMeterProviderSharedState(),
              getMeterSharedState(),
              getBatcher(instrumentDescriptor));
      if (callback != null) {
        instrument.setCallback(callback);
      }
      return register(instrument);
    }
  }
}
