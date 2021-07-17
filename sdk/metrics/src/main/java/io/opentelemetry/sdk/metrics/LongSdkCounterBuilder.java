/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.DoubleCounterBuilder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.sdk.metrics.instrument.InstrumentType;
import io.opentelemetry.sdk.metrics.instrument.InstrumentValueType;
import io.opentelemetry.sdk.metrics.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.state.MeterSharedState;
import java.util.function.Consumer;

class LongSdkCounterBuilder extends AbstractInstrumentBuilder<LongSdkCounterBuilder>
    implements LongCounterBuilder {
  LongSdkCounterBuilder(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      String name,
      String description,
      String unit) {
    super(meterProviderSharedState, meterSharedState, name, description, unit);
  }

  @Override
  protected LongSdkCounterBuilder getThis() {
    return this;
  }

  @Override
  public DoubleCounterBuilder ofDoubles() {
    return swapBuilder(DoubleSdkCounterBuilder::new);
  }

  @Override
  public LongCounter build() {
    return new LongCounterSdk(
        makeSynchronousStorage(InstrumentType.COUNTER, InstrumentValueType.LONG));
  }

  @Override
  public void buildWithCallback(Consumer<ObservableLongMeasurement> callback) {
    registerAsychronousStorage(InstrumentType.OBSERVABLE_SUM, InstrumentValueType.LONG, callback);
  }
}
