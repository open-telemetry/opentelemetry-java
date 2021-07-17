/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.DoubleHistogramBuilder;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.LongHistogramBuilder;
import io.opentelemetry.sdk.metrics.instrument.InstrumentType;
import io.opentelemetry.sdk.metrics.instrument.InstrumentValueType;
import io.opentelemetry.sdk.metrics.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.state.MeterSharedState;

class LongSdkHistogramBuilder extends AbstractInstrumentBuilder<LongSdkHistogramBuilder>
    implements LongHistogramBuilder {

  LongSdkHistogramBuilder(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      String name,
      String description,
      String unit) {
    super(meterProviderSharedState, meterSharedState, name, description, unit);
  }

  @Override
  protected LongSdkHistogramBuilder getThis() {
    return this;
  }

  @Override
  public DoubleHistogramBuilder ofDoubles() {
    return swapBuilder(DoubleSdkHistogramBuilder::new);
  }

  @Override
  public LongHistogram build() {
    return new LongHistogramSdk(
        makeSynchronousStorage(InstrumentType.HISTOGRAM, InstrumentValueType.LONG));
  }
}
