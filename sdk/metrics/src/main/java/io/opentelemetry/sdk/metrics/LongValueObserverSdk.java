/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.LongValueObserver;
import io.opentelemetry.sdk.metrics.AbstractAsynchronousInstrument.AbstractLongAsynchronousInstrument;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import java.util.function.Consumer;
import javax.annotation.Nullable;

final class LongValueObserverSdk extends AbstractLongAsynchronousInstrument
    implements LongValueObserver {

  LongValueObserverSdk(
      InstrumentDescriptor descriptor,
      InstrumentProcessor instrumentProcessor,
      @Nullable Consumer<LongResult> metricUpdater) {
    super(descriptor, instrumentProcessor, metricUpdater);
  }

  static final class Builder
      extends AbstractAsynchronousInstrument.Builder<LongValueObserverSdk.Builder>
      implements LongValueObserver.Builder {

    @Nullable private Consumer<LongResult> callback;

    Builder(
        String name,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState) {
      super(
          name,
          InstrumentType.VALUE_OBSERVER,
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
    public LongValueObserverSdk build() {
      return build(
          (instrumentDescriptor, instrumentProcessor) ->
              new LongValueObserverSdk(instrumentDescriptor, instrumentProcessor, callback));
    }
  }
}
