/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.LongSumObserver;
import io.opentelemetry.sdk.metrics.AbstractAsynchronousInstrument.AbstractLongAsynchronousInstrument;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import java.util.function.Consumer;
import javax.annotation.Nullable;

final class LongSumObserverSdk extends AbstractLongAsynchronousInstrument
    implements LongSumObserver {
  LongSumObserverSdk(
      InstrumentDescriptor descriptor,
      InstrumentProcessor instrumentProcessor,
      @Nullable Consumer<LongResult> metricUpdater) {
    super(descriptor, instrumentProcessor, metricUpdater);
  }

  static final class Builder
      extends AbstractAsynchronousInstrument.Builder<LongSumObserverSdk.Builder>
      implements LongSumObserver.Builder {

    @Nullable private Consumer<LongResult> callback;

    Builder(
        String name,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState) {
      super(
          name,
          InstrumentType.SUM_OBSERVER,
          InstrumentValueType.LONG,
          meterProviderSharedState,
          meterSharedState);
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
    public LongSumObserverSdk build() {
      return build(
          (instrumentDescriptor, instrumentProcessor) ->
              new LongSumObserverSdk(instrumentDescriptor, instrumentProcessor, callback));
    }
  }
}
