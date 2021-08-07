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

final class LongGaugeBuilderSdk extends AbstractInstrumentBuilder<LongGaugeBuilderSdk>
    implements LongGaugeBuilder {

  LongGaugeBuilderSdk(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      String name) {
    this(meterProviderSharedState, meterSharedState, name, "", "1");
  }

  LongGaugeBuilderSdk(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState sharedState,
      String name,
      String description,
      String unit) {
    super(meterProviderSharedState, sharedState, name, description, unit);
  }

  @Override
  protected LongGaugeBuilderSdk getThis() {
    return this;
  }

  @Override
  public DoubleGaugeBuilder ofDoubles() {
    return swapBuilder(DoubleGaugeBuilderSdk::new);
  }

  @Override
  public void buildWithCallback(Consumer<ObservableLongMeasurement> callback) {
    registerLongAsynchronousInstrument(InstrumentType.VALUE_OBSERVER, callback);
  }
}
