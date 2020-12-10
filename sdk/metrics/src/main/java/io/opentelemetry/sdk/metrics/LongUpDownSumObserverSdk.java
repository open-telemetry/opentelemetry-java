/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.LongUpDownSumObserver;
import io.opentelemetry.sdk.metrics.AbstractAsynchronousInstrument.AbstractLongAsynchronousInstrument;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import java.util.function.Consumer;
import javax.annotation.Nullable;

final class LongUpDownSumObserverSdk extends AbstractLongAsynchronousInstrument
    implements LongUpDownSumObserver {
  LongUpDownSumObserverSdk(
      InstrumentDescriptor descriptor,
      InstrumentProcessor instrumentProcessor,
      @Nullable Consumer<LongResult> metricUpdater) {
    super(descriptor, instrumentProcessor, metricUpdater);
  }

  static final class Builder
      extends AbstractAsynchronousInstrument.Builder<LongUpDownSumObserverSdk.Builder>
      implements LongUpDownSumObserver.Builder {

    @Nullable private Consumer<LongResult> callback;

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
    public Builder setUpdater(Consumer<LongResult> updater) {
      this.callback = updater;
      return this;
    }

    @Override
    public LongUpDownSumObserverSdk build() {
      InstrumentDescriptor instrumentDescriptor =
          getInstrumentDescriptor(InstrumentType.UP_DOWN_SUM_OBSERVER, InstrumentValueType.LONG);
      LongUpDownSumObserverSdk instrument =
          new LongUpDownSumObserverSdk(
              instrumentDescriptor, getBatcher(instrumentDescriptor), callback);
      return register(instrument);
    }
  }
}
