/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleHistogram;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleHistogramBuilder;
import io.opentelemetry.api.incubator.metrics.ExtendedLongHistogramBuilder;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.List;

final class ExtendedSdkDoubleHistogram extends SdkDoubleHistogram
    implements ExtendedDoubleHistogram {

  ExtendedSdkDoubleHistogram(
      InstrumentDescriptor descriptor, SdkMeter sdkMeter, WriteableMetricStorage storage) {
    super(descriptor, sdkMeter, storage);
  }

  @Override
  public boolean isEnabled() {
    return sdkMeter.isMeterEnabled() && storage.isEnabled();
  }

  static final class ExtendedSdkDoubleHistogramBuilder extends SdkDoubleHistogramBuilder
      implements ExtendedDoubleHistogramBuilder {

    ExtendedSdkDoubleHistogramBuilder(SdkMeter sdkMeter, String name) {
      super(sdkMeter, name);
    }

    @Override
    public ExtendedSdkDoubleHistogram build() {
      return builder.buildSynchronousInstrument(ExtendedSdkDoubleHistogram::new);
    }

    @Override
    public ExtendedLongHistogramBuilder ofLongs() {
      return builder.swapBuilder(ExtendedSdkLongHistogram.ExtendedSdkLongHistogramBuilder::new);
    }

    @Override
    public ExtendedDoubleHistogramBuilder setAttributesAdvice(List<AttributeKey<?>> attributes) {
      builder.setAdviceAttributes(attributes);
      return this;
    }
  }
}
