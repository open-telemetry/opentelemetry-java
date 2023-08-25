/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.metrics.LongGaugeBuilder;
import io.opentelemetry.api.metrics.ObservableLongGauge;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.extension.incubator.metrics.ExtendedLongGaugeBuilder;
import io.opentelemetry.extension.incubator.metrics.LongGaugeAdviceConfigurer;
import io.opentelemetry.sdk.metrics.internal.descriptor.Advice;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import java.util.List;
import java.util.function.Consumer;

final class SdkLongGaugeBuilder implements ExtendedLongGaugeBuilder, LongGaugeAdviceConfigurer {

  private final InstrumentBuilder builder;

  SdkLongGaugeBuilder(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState sharedState,
      String name,
      String description,
      String unit,
      Advice.AdviceBuilder adviceBuilder) {
    builder =
        new InstrumentBuilder(
            meterProviderSharedState,
            sharedState,
            InstrumentType.OBSERVABLE_GAUGE,
            InstrumentValueType.LONG,
            name,
            description,
            unit,
            adviceBuilder);
  }

  @Override
  public LongGaugeBuilder setAdvice(Consumer<LongGaugeAdviceConfigurer> adviceConsumer) {
    adviceConsumer.accept(this);
    return this;
  }

  @Override
  public LongGaugeBuilder setDescription(String description) {
    builder.setDescription(description);
    return this;
  }

  @Override
  public LongGaugeBuilder setUnit(String unit) {
    builder.setUnit(unit);
    return this;
  }

  @Override
  public ObservableLongGauge buildWithCallback(Consumer<ObservableLongMeasurement> callback) {
    return builder.registerLongAsynchronousInstrument(InstrumentType.OBSERVABLE_GAUGE, callback);
  }

  @Override
  public ObservableLongMeasurement buildObserver() {
    return builder.buildObservableMeasurement(InstrumentType.OBSERVABLE_GAUGE);
  }

  @Override
  public LongGaugeAdviceConfigurer setAttributes(List<AttributeKey<?>> attributes) {
    builder.setAdviceAttributes(attributes);
    return this;
  }

  @Override
  public String toString() {
    return builder.toStringHelper(getClass().getSimpleName());
  }
}
