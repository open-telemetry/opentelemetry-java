/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.DoubleGaugeBuilder;
import io.opentelemetry.api.metrics.LongGaugeBuilder;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import java.util.function.Consumer;

final class SdkLongGaugeBuilder extends AbstractInstrumentBuilder<SdkLongGaugeBuilder>
    implements LongGaugeBuilder {

  SdkLongGaugeBuilder(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      String name) {
    this(meterProviderSharedState, meterSharedState, name, "", "1");
  }

  SdkLongGaugeBuilder(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState sharedState,
      String name,
      String description,
      String unit) {
    super(meterProviderSharedState, sharedState, name, description, unit);
  }

  @Override
  protected SdkLongGaugeBuilder getThis() {
    return this;
  }

  @Override
  public DoubleGaugeBuilder ofDoubles() {
    return swapBuilder(SdkDoubleGaugeBuilder::new);
  }

  @Override
  public void buildWithCallback(Consumer<ObservableLongMeasurement> callback) {
    registerLongAsynchronousInstrument(InstrumentType.OBSERVABLE_GAUGE, callback);
  }
}
