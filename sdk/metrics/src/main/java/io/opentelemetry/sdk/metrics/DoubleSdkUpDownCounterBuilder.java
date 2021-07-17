/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.api.metrics.DoubleUpDownCounterBuilder;
import io.opentelemetry.api.metrics.LongUpDownCounterBuilder;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.sdk.metrics.instrument.InstrumentType;
import io.opentelemetry.sdk.metrics.instrument.InstrumentValueType;
import io.opentelemetry.sdk.metrics.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.state.MeterSharedState;
import java.util.function.Consumer;

class DoubleSdkUpDownCounterBuilder extends AbstractInstrumentBuilder<DoubleSdkUpDownCounterBuilder>
    implements DoubleUpDownCounterBuilder {

  DoubleSdkUpDownCounterBuilder(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      String name,
      String description,
      String unit) {
    super(meterProviderSharedState, meterSharedState, name, description, unit);
  }

  @Override
  protected DoubleSdkUpDownCounterBuilder getThis() {
    return this;
  }

  @Override
  public LongUpDownCounterBuilder ofLongs() {
    return swapBuilder(LongSdkUpDownCounterBuilder::new);
  }

  @Override
  public DoubleUpDownCounter build() {
    return new DoubleUpDownCounterSdk(
        makeSynchronousStorage(InstrumentType.UP_DOWN_COUNTER, InstrumentValueType.DOUBLE));
  }

  @Override
  public void buildWithCallback(Consumer<ObservableDoubleMeasurement> callback) {
    registerAsychronousStorage(
        InstrumentType.OBSERVBALE_UP_DOWN_SUM, InstrumentValueType.DOUBLE, callback);
  }
}
