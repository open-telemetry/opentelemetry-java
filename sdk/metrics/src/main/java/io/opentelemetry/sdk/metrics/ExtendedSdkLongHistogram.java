/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.metrics.BoundLongHistogram;
import io.opentelemetry.api.incubator.metrics.ExtendedLongHistogram;
import io.opentelemetry.api.incubator.metrics.ExtendedLongHistogramBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.internal.descriptor.Advice;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.BoundStorageHandle;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.List;
import javax.annotation.Nullable;

final class ExtendedSdkLongHistogram extends SdkLongHistogram
    implements ExtendedLongHistogram, BoundLongHistogram {

  // Non-null only when this is a bound instance returned from bind(); null for the instrument
  // itself. When set, the record() methods record straight to this handle instead of resolving the
  // series from the storage on each call.
  @Nullable private final BoundStorageHandle boundHandle;

  private ExtendedSdkLongHistogram(
      InstrumentDescriptor descriptor, SdkMeter sdkMeter, WriteableMetricStorage storage) {
    this(descriptor, sdkMeter, storage, null);
  }

  private ExtendedSdkLongHistogram(
      InstrumentDescriptor descriptor,
      SdkMeter sdkMeter,
      WriteableMetricStorage storage,
      @Nullable BoundStorageHandle boundHandle) {
    super(descriptor, sdkMeter, storage);
    this.boundHandle = boundHandle;
  }

  @Override
  public BoundLongHistogram bind(Attributes attributes) {
    return new ExtendedSdkLongHistogram(
        getDescriptor(), sdkMeter, storage, storage.bind(attributes));
  }

  @Override
  public void record(long value) {
    record(value, Context.current());
  }

  @Override
  public void record(long value, Context context) {
    if (!validateNonNegative(value)) {
      return;
    }
    if (boundHandle != null) {
      boundHandle.recordLong(value, context);
    } else {
      storage.recordLong(value, Attributes.empty(), context);
    }
  }

  static final class ExtendedSdkLongHistogramBuilder extends SdkLongHistogramBuilder
      implements ExtendedLongHistogramBuilder {

    ExtendedSdkLongHistogramBuilder(
        SdkMeter sdkMeter,
        String name,
        String description,
        String unit,
        Advice.AdviceBuilder adviceBuilder) {
      super(sdkMeter, name, description, unit, adviceBuilder);
    }

    @Override
    public ExtendedSdkLongHistogram build() {
      return builder.buildSynchronousInstrument(ExtendedSdkLongHistogram::new);
    }

    @Override
    public ExtendedLongHistogramBuilder setAttributesAdvice(List<AttributeKey<?>> attributes) {
      builder.setAdviceAttributes(attributes);
      return this;
    }
  }
}
