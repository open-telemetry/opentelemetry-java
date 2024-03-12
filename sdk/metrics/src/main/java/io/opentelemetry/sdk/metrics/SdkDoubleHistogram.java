/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleHistogramBuilder;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.DoubleHistogramBuilder;
import io.opentelemetry.api.metrics.LongHistogramBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.internal.aggregator.ExplicitBucketHistogramUtils;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

final class SdkDoubleHistogram extends AbstractInstrument implements DoubleHistogram {
  private static final Logger logger = Logger.getLogger(SdkDoubleHistogram.class.getName());

  private final ThrottlingLogger throttlingLogger = new ThrottlingLogger(logger);
  private final WriteableMetricStorage storage;

  private SdkDoubleHistogram(InstrumentDescriptor descriptor, WriteableMetricStorage storage) {
    super(descriptor);
    this.storage = storage;
  }

  @Override
  public void record(double value, Attributes attributes, Context context) {
    if (value < 0) {
      throttlingLogger.log(
          Level.WARNING,
          "Histograms can only record non-negative values. Instrument "
              + getDescriptor().getName()
              + " has recorded a negative value.");
      return;
    }
    storage.recordDouble(value, attributes, context);
  }

  @Override
  public void record(double value, Attributes attributes) {
    record(value, attributes, Context.current());
  }

  @Override
  public void record(double value) {
    record(value, Attributes.empty());
  }

  static final class SdkDoubleHistogramBuilder implements ExtendedDoubleHistogramBuilder {

    private final InstrumentBuilder builder;

    SdkDoubleHistogramBuilder(
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState,
        String name) {
      builder =
          new InstrumentBuilder(
              name,
              InstrumentType.HISTOGRAM,
              InstrumentValueType.DOUBLE,
              meterProviderSharedState,
              meterSharedState);
    }

    @Override
    public DoubleHistogramBuilder setDescription(String description) {
      builder.setDescription(description);
      return this;
    }

    @Override
    public DoubleHistogramBuilder setUnit(String unit) {
      builder.setUnit(unit);
      return this;
    }

    @Override
    public SdkDoubleHistogram build() {
      return builder.buildSynchronousInstrument(SdkDoubleHistogram::new);
    }

    @Override
    public LongHistogramBuilder ofLongs() {
      return builder.swapBuilder(SdkLongHistogram.SdkLongHistogramBuilder::new);
    }

    @Override
    public ExtendedDoubleHistogramBuilder setExplicitBucketBoundariesAdvice(
        List<Double> bucketBoundaries) {
      try {
        Objects.requireNonNull(bucketBoundaries, "bucketBoundaries must not be null");
        ExplicitBucketHistogramUtils.validateBucketBoundaries(bucketBoundaries);
      } catch (IllegalArgumentException | NullPointerException e) {
        logger.warning("Error setting explicit bucket boundaries advice: " + e.getMessage());
        return this;
      }
      builder.setExplicitBucketBoundaries(bucketBoundaries);
      return this;
    }

    @Override
    public ExtendedDoubleHistogramBuilder setAttributesAdvice(List<AttributeKey<?>> attributes) {
      builder.setAdviceAttributes(attributes);
      return this;
    }

    @Override
    public String toString() {
      return builder.toStringHelper(getClass().getSimpleName());
    }
  }
}
