/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.metrics.BoundDoubleGauge;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleGauge;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleGaugeBuilder;
import io.opentelemetry.api.incubator.metrics.ExtendedLongGaugeBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.BoundStorageHandle;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.List;
import javax.annotation.Nullable;

final class ExtendedSdkDoubleGauge extends SdkDoubleGauge
    implements ExtendedDoubleGauge, BoundDoubleGauge {

  // Non-null only when this is a bound instance returned from bind(); null for the instrument
  // itself. When set, the set() methods record straight to this handle instead of resolving the
  // series from the storage on each call.
  @Nullable private final BoundStorageHandle boundHandle;

  private ExtendedSdkDoubleGauge(
      InstrumentDescriptor descriptor, SdkMeter sdkMeter, WriteableMetricStorage storage) {
    this(descriptor, sdkMeter, storage, null);
  }

  private ExtendedSdkDoubleGauge(
      InstrumentDescriptor descriptor,
      SdkMeter sdkMeter,
      WriteableMetricStorage storage,
      @Nullable BoundStorageHandle boundHandle) {
    super(descriptor, sdkMeter, storage);
    this.boundHandle = boundHandle;
  }

  @Override
  public BoundDoubleGauge bind(Attributes attributes) {
    return new ExtendedSdkDoubleGauge(getDescriptor(), sdkMeter, storage, storage.bind(attributes));
  }

  @Override
  public void set(double value) {
    set(value, Context.current());
  }

  @Override
  public void set(double value, Context context) {
    if (boundHandle != null) {
      boundHandle.recordDouble(value, context);
    } else {
      storage.recordDouble(value, Attributes.empty(), context);
    }
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
