/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.DoubleUpDownCounterBuilder;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.LongUpDownCounterBuilder;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.sdk.metrics.instrument.InstrumentType;
import io.opentelemetry.sdk.metrics.instrument.InstrumentValueType;
import io.opentelemetry.sdk.metrics.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.state.MeterSharedState;
import java.util.function.Consumer;

class LongSdkUpDownCounterBuilder extends AbstractInstrumentBuilder<LongSdkUpDownCounterBuilder>
    implements LongUpDownCounterBuilder {
  LongSdkUpDownCounterBuilder(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      String name,
      String description,
      String unit) {
    super(meterProviderSharedState, meterSharedState, name, description, unit);
  }

  @Override
  protected LongSdkUpDownCounterBuilder getThis() {
    return this;
  }

  @Override
  public DoubleUpDownCounterBuilder ofDoubles() {
    return swapBuilder(DoubleSdkUpDownCounterBuilder::new);
  }

  @Override
  public LongUpDownCounter build() {
    return new LongUpDownCounterSdk(
        makeSynchronousStorage(InstrumentType.UP_DOWN_COUNTER, InstrumentValueType.LONG));
  }

  @Override
  public void buildWithCallback(Consumer<ObservableLongMeasurement> callback) {
    registerAsychronousStorage(
        InstrumentType.OBSERVBALE_UP_DOWN_SUM, InstrumentValueType.LONG, callback);
  }
}
