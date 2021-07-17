/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.DoubleGaugeBuilder;
import io.opentelemetry.api.metrics.LongGaugeBuilder;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.sdk.metrics.instrument.InstrumentType;
import io.opentelemetry.sdk.metrics.instrument.InstrumentValueType;
import io.opentelemetry.sdk.metrics.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.state.MeterSharedState;
import java.util.function.Consumer;

class DoubleSdkGaugeBuilder extends AbstractInstrumentBuilder<DoubleSdkGaugeBuilder>
    implements DoubleGaugeBuilder {

  DoubleSdkGaugeBuilder(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      String name,
      String description,
      String unit) {
    super(meterProviderSharedState, meterSharedState, name, description, unit);
  }

  @Override
  protected DoubleSdkGaugeBuilder getThis() {
    return this;
  }

  @Override
  public LongGaugeBuilder ofLongs() {
    return swapBuilder(LongSdkGaugeBuilder::new);
  }

  @Override
  public void buildWithCallback(Consumer<ObservableDoubleMeasurement> callback) {
    registerAsychronousStorage(
        InstrumentType.OBSERVABLE_GAUGE, InstrumentValueType.DOUBLE, callback);
  }
}
