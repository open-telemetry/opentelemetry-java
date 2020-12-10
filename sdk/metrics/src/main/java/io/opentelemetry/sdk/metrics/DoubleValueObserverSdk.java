/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.DoubleValueObserver;
import io.opentelemetry.sdk.metrics.AbstractAsynchronousInstrument.AbstractDoubleAsynchronousInstrument;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import java.util.function.Consumer;
import javax.annotation.Nullable;

final class DoubleValueObserverSdk extends AbstractDoubleAsynchronousInstrument
    implements DoubleValueObserver {

  DoubleValueObserverSdk(
      InstrumentDescriptor descriptor,
      InstrumentProcessor instrumentProcessor,
      @Nullable Consumer<DoubleResult> metricUpdater) {
    super(descriptor, instrumentProcessor, metricUpdater);
  }

  static final class Builder
      extends AbstractAsynchronousInstrument.Builder<DoubleValueObserverSdk.Builder>
      implements DoubleValueObserver.Builder {

    @Nullable private Consumer<DoubleResult> callback;

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
    public Builder setUpdater(Consumer<DoubleResult> updater) {
      this.callback = updater;
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
