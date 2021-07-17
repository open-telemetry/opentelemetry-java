/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.DoubleHistogramBuilder;
import io.opentelemetry.api.metrics.LongHistogramBuilder;
import io.opentelemetry.sdk.metrics.instrument.InstrumentType;
import io.opentelemetry.sdk.metrics.instrument.InstrumentValueType;
import io.opentelemetry.sdk.metrics.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.state.MeterSharedState;

class DoubleSdkHistogramBuilder extends AbstractInstrumentBuilder<DoubleSdkHistogramBuilder>
    implements DoubleHistogramBuilder {

  DoubleSdkHistogramBuilder(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      String name,
      String description,
      String unit) {
    super(meterProviderSharedState, meterSharedState, name, description, unit);
  }

  @Override
  protected DoubleSdkHistogramBuilder getThis() {
    return this;
  }

  @Override
  public LongHistogramBuilder ofLongs() {
    return swapBuilder(LongSdkHistogramBuilder::new);
  }

  @Override
  public DoubleHistogram build() {
    return new DoubleHistogramSdk(
        makeSynchronousStorage(InstrumentType.HISTOGRAM, InstrumentValueType.DOUBLE));
  }
}
