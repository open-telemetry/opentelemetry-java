/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.metrics.DoubleGaugeBuilder;
import io.opentelemetry.api.metrics.LongGaugeBuilder;
import io.opentelemetry.api.metrics.ObservableDoubleGauge;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.extension.incubator.metrics.DoubleGaugeAdviceConfigurer;
import io.opentelemetry.extension.incubator.metrics.ExtendedDoubleGaugeBuilder;
import io.opentelemetry.sdk.metrics.internal.descriptor.MutableInstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import java.util.List;
import java.util.function.Consumer;

final class SdkDoubleGaugeBuilder
    implements ExtendedDoubleGaugeBuilder, DoubleGaugeAdviceConfigurer {

  private final InstrumentBuilder builder;

  SdkDoubleGaugeBuilder(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      String name) {
    MutableInstrumentDescriptor descriptor =
        MutableInstrumentDescriptor.create(
            name, InstrumentType.OBSERVABLE_GAUGE, InstrumentValueType.DOUBLE);
    builder = new InstrumentBuilder(meterProviderSharedState, meterSharedState, descriptor);
  }

  @Override
  public DoubleGaugeBuilder setAdvice(Consumer<DoubleGaugeAdviceConfigurer> adviceConsumer) {
    adviceConsumer.accept(this);
    return this;
  }

  @Override
  public DoubleGaugeBuilder setDescription(String description) {
    builder.setDescription(description);
    return this;
  }

  @Override
  public DoubleGaugeBuilder setUnit(String unit) {
    builder.setUnit(unit);
    return this;
  }

  @Override
  public LongGaugeBuilder ofLongs() {
    return builder.swapBuilder(SdkLongGaugeBuilder::new);
  }

  @Override
  public ObservableDoubleGauge buildWithCallback(Consumer<ObservableDoubleMeasurement> callback) {
    return builder.buildDoubleAsynchronousInstrument(InstrumentType.OBSERVABLE_GAUGE, callback);
  }

  @Override
  public ObservableDoubleMeasurement buildObserver() {
    return builder.buildObservableMeasurement(InstrumentType.OBSERVABLE_GAUGE);
  }

  @Override
  public DoubleGaugeAdviceConfigurer setAttributes(List<AttributeKey<?>> attributes) {
    builder.setAdviceAttributes(attributes);
    return this;
  }

  @Override
  public String toString() {
    return builder.toStringHelper(getClass().getSimpleName());
  }
}
