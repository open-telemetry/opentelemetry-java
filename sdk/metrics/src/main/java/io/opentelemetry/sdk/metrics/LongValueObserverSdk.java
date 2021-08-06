/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.DoubleGaugeBuilder;
import io.opentelemetry.api.metrics.LongGaugeBuilder;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import java.util.function.Consumer;

final class LongValueObserverSdk extends AbstractAsynchronousInstrument {

  LongValueObserverSdk(
      InstrumentDescriptor descriptor, AsynchronousInstrumentAccumulator accumulator) {
    super(descriptor, accumulator);
  }

  static final class Builder extends AbstractInstrumentBuilder<LongValueObserverSdk.Builder>
      implements LongGaugeBuilder {

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
    public DoubleGaugeBuilder ofDoubles() {
      return swapBuilder(DoubleValueObserverSdk.Builder::new);
    }

    @Override
    public void buildWithCallback(Consumer<ObservableLongMeasurement> callback) {
      buildLongAsynchronousInstrument(
          InstrumentType.VALUE_OBSERVER, callback, LongValueObserverSdk::new);
    }
  }
}
