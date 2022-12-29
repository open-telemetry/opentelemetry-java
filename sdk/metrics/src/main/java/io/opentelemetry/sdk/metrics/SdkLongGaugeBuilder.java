/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.LongGaugeBuilder;
import io.opentelemetry.api.metrics.ObservableLongGauge;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import java.util.function.Consumer;

final class SdkLongGaugeBuilder extends AbstractInstrumentBuilder<SdkLongGaugeBuilder>
    implements LongGaugeBuilder {

  SdkLongGaugeBuilder(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState sharedState,
      String name,
      String description,
      String unit) {
    super(
        meterProviderSharedState,
        sharedState,
        InstrumentType.OBSERVABLE_GAUGE,
        InstrumentValueType.LONG,
        name,
        description,
        unit);
  }

  @Override
  protected SdkLongGaugeBuilder getThis() {
    return this;
  }

  @Override
  public ObservableLongGauge buildWithCallback(Consumer<ObservableLongMeasurement> callback) {
    return registerLongAsynchronousInstrument(InstrumentType.OBSERVABLE_GAUGE, callback);
  }

  @Override
  public ObservableLongMeasurement buildObserver() {
    return buildObservableMeasurement(InstrumentType.OBSERVABLE_GAUGE);
  }
}
