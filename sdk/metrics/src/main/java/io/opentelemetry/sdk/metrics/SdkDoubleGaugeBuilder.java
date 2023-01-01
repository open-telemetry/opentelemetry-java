/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.DoubleGaugeBuilder;
import io.opentelemetry.api.metrics.LongGaugeBuilder;
import io.opentelemetry.api.metrics.ObservableDoubleGauge;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import java.util.function.Consumer;

final class SdkDoubleGaugeBuilder extends AbstractInstrumentBuilder<SdkDoubleGaugeBuilder>
    implements DoubleGaugeBuilder {

  SdkDoubleGaugeBuilder(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      String name) {
    super(
        meterProviderSharedState,
        meterSharedState,
        InstrumentType.OBSERVABLE_GAUGE,
        InstrumentValueType.DOUBLE,
        name,
        "",
        DEFAULT_UNIT);
  }

  @Override
  protected SdkDoubleGaugeBuilder getThis() {
    return this;
  }

  @Override
  public LongGaugeBuilder ofLongs() {
    return swapBuilder(SdkLongGaugeBuilder::new);
  }

  @Override
  public ObservableDoubleGauge buildWithCallback(Consumer<ObservableDoubleMeasurement> callback) {
    return registerDoubleAsynchronousInstrument(InstrumentType.OBSERVABLE_GAUGE, callback);
  }

  @Override
  public ObservableDoubleMeasurement buildObserver() {
    return buildObservableMeasurement(InstrumentType.OBSERVABLE_GAUGE);
  }
}
