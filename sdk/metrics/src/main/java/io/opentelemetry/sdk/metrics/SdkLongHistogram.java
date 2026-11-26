/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.LongHistogramBuilder;
import io.opentelemetry.common.impl.ApiUsageLogger;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.internal.aggregator.ExplicitBucketHistogramUtils;
import io.opentelemetry.sdk.metrics.internal.descriptor.Advice;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

class SdkLongHistogram extends AbstractInstrument implements LongHistogram {
  private static final Logger logger = Logger.getLogger(SdkLongHistogram.class.getName());

  private final ThrottlingLogger throttlingLogger = new ThrottlingLogger(logger);
  final SdkMeter sdkMeter;
  final WriteableMetricStorage storage;

  SdkLongHistogram(
      InstrumentDescriptor descriptor, SdkMeter sdkMeter, WriteableMetricStorage storage) {
    super(descriptor, sdkMeter);
    this.sdkMeter = sdkMeter;
    this.storage = storage;
  }

  @Override
  public boolean isEnabled() {
    return sdkMeter.isMeterEnabled() && storage.isEnabled();
  }

  @Override
  public void record(long value, Attributes attributes, Context context) {
    if (!validateNonNegative(value)) {
      return;
    }
    storage.recordLong(value, attributes, context);
  }

  @Override
  public void record(long value, Attributes attributes) {
    record(value, attributes, currentOrRootContext());
  }

  @Override
  public void record(long value) {
    record(value, Attributes.empty());
  }

  /**
   * Returns true if {@code value} is non-negative, otherwise logs a warning and returns false.
   * Shared by the unbound and bound ({@link ExtendedSdkLongHistogram}) record paths.
   */
  boolean validateNonNegative(long value) {
    if (value < 0) {
      throttlingLogger.log(
          Level.WARNING,
          "Histograms can only record non-negative values. Instrument "
              + getDescriptor().getName()
              + " has recorded a negative value.");
      return false;
    }
    return true;
  }

  static class SdkLongHistogramBuilder implements LongHistogramBuilder {

    final InstrumentBuilder builder;

    SdkLongHistogramBuilder(
        SdkMeter sdkMeter,
        String name,
        String description,
        String unit,
        Advice.AdviceBuilder adviceBuilder) {
      builder =
          new InstrumentBuilder(name, InstrumentType.HISTOGRAM, InstrumentValueType.LONG, sdkMeter)
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
    public LongHistogramBuilder setExplicitBucketBoundariesAdvice(List<Long> bucketBoundaries) {
      if (bucketBoundaries == null) {
        ApiUsageLogger.logNullParam(
            LongHistogramBuilder.class, "setExplicitBucketBoundariesAdvice", "bucketBoundaries");
        return this;
      }
      List<Double> boundaries = new ArrayList<>(bucketBoundaries.size());
      String error = null;
      for (Long boundary : bucketBoundaries) {
        if (boundary == null) {
          error = "bucketBoundaries must not contain null";
          break;
        }
        boundaries.add(boundary.doubleValue());
      }
      if (error == null) {
        error = ExplicitBucketHistogramUtils.getBucketBoundariesValidationError(boundaries);
      }
      if (error != null) {
        ApiUsageLogger.logUsageIssue(
            LongHistogramBuilder.class, "setExplicitBucketBoundariesAdvice", error);
        return this;
      }
      builder.setExplicitBucketBoundaries(boundaries);
      return this;
    }

    @Override
    public String toString() {
      return builder.toStringHelper(getClass().getSimpleName());
    }
  }
}
