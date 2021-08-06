/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.DoubleGaugeBuilder;
import io.opentelemetry.api.metrics.LongGaugeBuilder;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import java.util.function.Consumer;

final class DoubleValueObserverSdk extends AbstractAsynchronousInstrument {

  DoubleValueObserverSdk(
      InstrumentDescriptor descriptor, AsynchronousInstrumentAccumulator accumulator) {
    super(descriptor, accumulator);
  }

  static final class Builder extends AbstractInstrumentBuilder<DoubleValueObserverSdk.Builder>
      implements DoubleGaugeBuilder {

    Builder(
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState,
        String name) {
      this(meterProviderSharedState, meterSharedState, name, "", "1");
    }

    Builder(
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState sharedState,
        String name,
        String description,
        String unit) {
      super(meterProviderSharedState, sharedState, name, description, unit);
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    @Override
    public LongGaugeBuilder ofLongs() {
      return swapBuilder(LongValueObserverSdk.Builder::new);
    }

    @Override
    public void buildWithCallback(Consumer<ObservableDoubleMeasurement> callback) {
      buildDoubleAsynchronousInstrument(
          InstrumentType.VALUE_OBSERVER, callback, DoubleValueObserverSdk::new);
    }
  }
}
