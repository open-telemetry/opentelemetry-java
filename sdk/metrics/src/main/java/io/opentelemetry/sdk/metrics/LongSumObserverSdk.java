/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.LongSumObserver;
import io.opentelemetry.sdk.metrics.AbstractAsynchronousInstrument.AbstractLongAsynchronousInstrument;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;

final class LongSumObserverSdk extends AbstractLongAsynchronousInstrument
    implements LongSumObserver {
  LongSumObserverSdk(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      Batcher batcher) {
    super(descriptor, meterProviderSharedState, meterSharedState, new ActiveBatcher(batcher));
  }

  static final class Builder
      extends AbstractAsynchronousInstrument.Builder<LongSumObserverSdk.Builder>
      implements LongSumObserver.Builder {

    private Callback<LongResult> callback;

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
    public Builder setCallback(Callback<LongResult> callback) {
      this.callback = callback;
      return this;
    }

    @SuppressWarnings("deprecation") // need to call the deprecated method for now
    @Override
    public LongSumObserverSdk build() {
      InstrumentDescriptor instrumentDescriptor =
          getInstrumentDescriptor(InstrumentType.SUM_OBSERVER, InstrumentValueType.LONG);
      LongSumObserverSdk instrument =
          new LongSumObserverSdk(
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
