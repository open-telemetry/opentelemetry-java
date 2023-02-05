/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.DoubleHistogramBuilder;
import io.opentelemetry.api.metrics.LongHistogramBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
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

  static final class SdkDoubleHistogramBuilder
      extends AbstractInstrumentBuilder<SdkDoubleHistogramBuilder>
      implements DoubleHistogramBuilder {

    SdkDoubleHistogramBuilder(
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState,
        String name) {
      super(
          meterProviderSharedState,
          meterSharedState,
          InstrumentType.HISTOGRAM,
          InstrumentValueType.DOUBLE,
          name,
          "",
          DEFAULT_UNIT);
    }

    @Override
    protected SdkDoubleHistogramBuilder getThis() {
      return this;
    }

    @Override
    public SdkDoubleHistogram build() {
      return buildSynchronousInstrument(SdkDoubleHistogram::new);
    }

    @Override
    public LongHistogramBuilder ofLongs() {
      return swapBuilder(SdkLongHistogram.SdkLongHistogramBuilder::new);
    }
  }
}
