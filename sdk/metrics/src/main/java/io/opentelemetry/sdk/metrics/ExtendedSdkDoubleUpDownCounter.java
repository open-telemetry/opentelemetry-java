/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.metrics.BoundDoubleUpDownCounter;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleUpDownCounter;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleUpDownCounterBuilder;
import io.opentelemetry.sdk.metrics.internal.descriptor.Advice;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.List;

final class ExtendedSdkDoubleUpDownCounter extends SdkDoubleUpDownCounter
    implements ExtendedDoubleUpDownCounter {

  private ExtendedSdkDoubleUpDownCounter(
      InstrumentDescriptor descriptor, SdkMeter sdkMeter, WriteableMetricStorage storage) {
    super(descriptor, sdkMeter, storage);
  }

  @Override
  public BoundDoubleUpDownCounter bind(Attributes attributes) {
    // TODO(bound-instruments): implement against WriteableMetricStorage in the implementation phase
    // (includes fresh-eyes work on DeltaSynchronousMetricStorage).
    throw new UnsupportedOperationException("bind is not yet implemented");
  }

  static final class ExtendedSdkDoubleUpDownCounterBuilder extends SdkDoubleUpDownCounterBuilder
      implements ExtendedDoubleUpDownCounterBuilder {

    ExtendedSdkDoubleUpDownCounterBuilder(
        SdkMeter sdkMeter,
        String name,
        String description,
        String unit,
        Advice.AdviceBuilder adviceBuilder) {
      super(sdkMeter, name, description, unit, adviceBuilder);
    }

    @Override
    public ExtendedDoubleUpDownCounter build() {
      return builder.buildSynchronousInstrument(ExtendedSdkDoubleUpDownCounter::new);
    }

    @Override
    public ExtendedDoubleUpDownCounterBuilder setAttributesAdvice(
        List<AttributeKey<?>> attributes) {
      builder.setAdviceAttributes(attributes);
      return this;
    }
  }
}
