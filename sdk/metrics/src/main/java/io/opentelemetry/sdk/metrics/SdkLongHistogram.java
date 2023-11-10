/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.LongHistogramBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.extension.incubator.metrics.ExtendedLongHistogramBuilder;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.internal.aggregator.ExplicitBucketHistogramUtils;
import io.opentelemetry.sdk.metrics.internal.descriptor.Advice;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

final class SdkLongHistogram extends AbstractInstrument implements LongHistogram {
  private static final Logger logger = Logger.getLogger(SdkLongHistogram.class.getName());

  private final ThrottlingLogger throttlingLogger = new ThrottlingLogger(logger);
  private final WriteableMetricStorage storage;

  private SdkLongHistogram(InstrumentDescriptor descriptor, WriteableMetricStorage storage) {
    super(descriptor);
    this.storage = storage;
  }

  @Override
  public void record(long value, Attributes attributes, Context context) {
    if (value < 0) {
      throttlingLogger.log(
          Level.WARNING,
          "Histograms can only record non-negative values. Instrument "
              + getDescriptor().getName()
              + " has recorded a negative value.");
      return;
    }
    storage.recordLong(value, attributes, context);
  }

  @Override
  public void record(long value, Attributes attributes) {
    record(value, attributes, Context.current());
  }

  @Override
  public void record(long value) {
    record(value, Attributes.empty());
  }

  static final class SdkLongHistogramBuilder implements ExtendedLongHistogramBuilder {

    private final InstrumentBuilder builder;

    SdkLongHistogramBuilder(
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState sharedState,
        String name,
        String description,
        String unit,
        Advice.AdviceBuilder adviceBuilder) {
      builder =
          new InstrumentBuilder(
                  name,
                  InstrumentType.HISTOGRAM,
                  InstrumentValueType.LONG,
                  meterProviderSharedState,
                  sharedState)
              .setDescription(description)
              .setUnit(unit)
              .setAdviceBuilder(adviceBuilder);
    }

    @Override
    public LongHistogramBuilder setDescription(String description) {
      builder.setDescription(description);
      return this;
    }

    @Override
    public LongHistogramBuilder setUnit(String unit) {
      builder.setUnit(unit);
      return this;
    }

    @Override
    public SdkLongHistogram build() {
      return builder.buildSynchronousInstrument(SdkLongHistogram::new);
    }

    @Override
    public ExtendedLongHistogramBuilder setExplicitBucketBoundariesAdvice(
        List<Long> bucketBoundaries) {
      List<Double> boundaries;
      try {
        Objects.requireNonNull(bucketBoundaries, "bucketBoundaries must not be null");
        boundaries = bucketBoundaries.stream().map(Long::doubleValue).collect(Collectors.toList());
        ExplicitBucketHistogramUtils.validateBucketBoundaries(boundaries);
      } catch (IllegalArgumentException | NullPointerException e) {
        logger.warning("Error setting explicit bucket boundaries advice: " + e.getMessage());
        return this;
      }
      builder.setExplicitBucketBoundaries(boundaries);
      return this;
    }

    @Override
    public ExtendedLongHistogramBuilder setAttributesAdvice(List<AttributeKey<?>> attributes) {
      builder.setAdviceAttributes(attributes);
      return this;
    }

    @Override
    public String toString() {
      return builder.toStringHelper(getClass().getSimpleName());
    }
  }
}
