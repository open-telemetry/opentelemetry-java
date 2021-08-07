/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.DoubleGaugeBuilder;
import io.opentelemetry.api.metrics.LongGaugeBuilder;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import java.util.function.Consumer;

final class DoubleGaugeBuilderSdk extends AbstractInstrumentBuilder<DoubleGaugeBuilderSdk>
    implements DoubleGaugeBuilder {

  DoubleGaugeBuilderSdk(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      String name) {
    this(meterProviderSharedState, meterSharedState, name, "", "1");
  }

  DoubleGaugeBuilderSdk(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState sharedState,
      String name,
      String description,
      String unit) {
    super(meterProviderSharedState, sharedState, name, description, unit);
  }

  @Override
  protected DoubleGaugeBuilderSdk getThis() {
    return this;
  }

  @Override
  public LongGaugeBuilder ofLongs() {
    return swapBuilder(LongGaugeBuilderSdk::new);
  }

  @Override
  public void buildWithCallback(Consumer<ObservableDoubleMeasurement> callback) {
    registerDoubleAsynchronousInstrument(InstrumentType.VALUE_OBSERVER, callback);
  }
}
