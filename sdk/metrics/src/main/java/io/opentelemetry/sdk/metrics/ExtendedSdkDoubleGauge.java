/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleGauge;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleGaugeBuilder;
import io.opentelemetry.api.incubator.metrics.ExtendedLongGaugeBuilder;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.List;

final class ExtendedSdkDoubleGauge extends SdkDoubleGauge implements ExtendedDoubleGauge {

  private ExtendedSdkDoubleGauge(
      InstrumentDescriptor descriptor, SdkMeter sdkMeter, WriteableMetricStorage storage) {
    super(descriptor, sdkMeter, storage);
  }

  @Override
  public boolean isEnabled() {
    return sdkMeter.isMeterEnabled() && storage.isEnabled();
  }

  static final class ExtendedSdkDoubleGaugeBuilder extends SdkDoubleGaugeBuilder
      implements ExtendedDoubleGaugeBuilder {
    ExtendedSdkDoubleGaugeBuilder(SdkMeter sdkMeter, String name) {
      super(sdkMeter, name);
    }

    @Override
    public ExtendedSdkDoubleGauge build() {
      return builder.buildSynchronousInstrument(ExtendedSdkDoubleGauge::new);
    }

    @Override
    public ExtendedDoubleGaugeBuilder setAttributesAdvice(List<AttributeKey<?>> attributes) {
      builder.setAdviceAttributes(attributes);
      return this;
    }

    @Override
    public ExtendedLongGaugeBuilder ofLongs() {
      return builder.swapBuilder(ExtendedSdkLongGauge.ExtendedSdkLongGaugeBuilder::new);
    }
  }
}
