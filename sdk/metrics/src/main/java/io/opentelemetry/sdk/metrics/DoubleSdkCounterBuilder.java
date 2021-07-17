/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleCounterBuilder;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.sdk.metrics.instrument.InstrumentType;
import io.opentelemetry.sdk.metrics.instrument.InstrumentValueType;
import io.opentelemetry.sdk.metrics.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.state.MeterSharedState;
import java.util.function.Consumer;

class DoubleSdkCounterBuilder extends AbstractInstrumentBuilder<DoubleSdkCounterBuilder>
    implements DoubleCounterBuilder {

  DoubleSdkCounterBuilder(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      String name,
      String description,
      String unit) {
    super(meterProviderSharedState, meterSharedState, name, description, unit);
  }

  @Override
  protected DoubleSdkCounterBuilder getThis() {
    return this;
  }

  @Override
  public LongCounterBuilder ofLongs() {
    return swapBuilder(LongSdkCounterBuilder::new);
  }

  @Override
  public DoubleCounter build() {
    return new DoubleCounterSdk(
        makeSynchronousStorage(InstrumentType.COUNTER, InstrumentValueType.DOUBLE));
  }

  @Override
  public void buildWithCallback(Consumer<ObservableDoubleMeasurement> callback) {
    registerAsychronousStorage(InstrumentType.OBSERVABLE_SUM, InstrumentValueType.DOUBLE, callback);
  }
}
