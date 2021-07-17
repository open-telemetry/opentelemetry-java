/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.DoubleGaugeBuilder;
import io.opentelemetry.api.metrics.LongGaugeBuilder;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.sdk.metrics.instrument.InstrumentType;
import io.opentelemetry.sdk.metrics.instrument.InstrumentValueType;
import io.opentelemetry.sdk.metrics.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.state.MeterSharedState;
import java.util.function.Consumer;

class LongSdkGaugeBuilder extends AbstractInstrumentBuilder<LongSdkGaugeBuilder>
    implements LongGaugeBuilder {
  LongSdkGaugeBuilder(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      String name,
      String description,
      String unit) {
    super(meterProviderSharedState, meterSharedState, name, description, unit);
  }

  @Override
  protected LongSdkGaugeBuilder getThis() {
    return this;
  }

  @Override
  public DoubleGaugeBuilder ofDoubles() {
    return swapBuilder(DoubleSdkGaugeBuilder::new);
  }

  @Override
  public void buildWithCallback(Consumer<ObservableLongMeasurement> callback) {
    registerAsychronousStorage(InstrumentType.OBSERVABLE_GAUGE, InstrumentValueType.LONG, callback);
  }
}
