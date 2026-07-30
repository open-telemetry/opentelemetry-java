/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.metrics.BoundDoubleHistogram;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleHistogram;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleHistogramBuilder;
import io.opentelemetry.api.incubator.metrics.ExtendedLongHistogramBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.BoundStorageHandle;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.List;
import javax.annotation.Nullable;

final class ExtendedSdkDoubleHistogram extends SdkDoubleHistogram
    implements ExtendedDoubleHistogram, BoundDoubleHistogram {

  // Non-null only when this is a bound instance returned from bind(); null for the instrument
  // itself. When set, the record() methods record straight to this handle instead of resolving the
  // series from the storage on each call.
  @Nullable private final BoundStorageHandle boundHandle;

  ExtendedSdkDoubleHistogram(
      InstrumentDescriptor descriptor, SdkMeter sdkMeter, WriteableMetricStorage storage) {
    this(descriptor, sdkMeter, storage, null);
  }

  private ExtendedSdkDoubleHistogram(
      InstrumentDescriptor descriptor,
      SdkMeter sdkMeter,
      WriteableMetricStorage storage,
      @Nullable BoundStorageHandle boundHandle) {
    super(descriptor, sdkMeter, storage);
    this.boundHandle = boundHandle;
  }

  @Override
  public BoundDoubleHistogram bind(Attributes attributes) {
    return new ExtendedSdkDoubleHistogram(
        getDescriptor(), sdkMeter, storage, storage.bind(attributes));
  }

  @Override
  public void record(double value) {
    record(value, Context.current());
  }

  @Override
  public void record(double value, Context context) {
    if (!validateNonNegative(value)) {
      return;
    }
    if (boundHandle != null) {
      boundHandle.recordDouble(value, context);
    } else {
      storage.recordDouble(value, Attributes.empty(), context);
    }
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
