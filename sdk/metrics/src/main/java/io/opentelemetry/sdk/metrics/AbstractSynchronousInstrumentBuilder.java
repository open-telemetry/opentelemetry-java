/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import java.util.function.BiFunction;

abstract class AbstractSynchronousInstrumentBuilder<
        B extends AbstractSynchronousInstrumentBuilder<?>>
    extends AbstractInstrument.Builder<B> {
  private final MeterProviderSharedState meterProviderSharedState;
  private final MeterSharedState meterSharedState;

  AbstractSynchronousInstrumentBuilder(
      String name,
      InstrumentType instrumentType,
      InstrumentValueType instrumentValueType,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState) {
    super(name, instrumentType, instrumentValueType);
    this.meterProviderSharedState = meterProviderSharedState;
    this.meterSharedState = meterSharedState;
  }

  final <I extends AbstractInstrument> I buildInstrument(
      BiFunction<InstrumentDescriptor, SynchronousInstrumentAccumulator<?>, I> instrumentFactory) {
    InstrumentDescriptor descriptor = buildDescriptor();
    return meterSharedState
        .getInstrumentRegistry()
        .register(
            instrumentFactory.apply(
                descriptor,
                SynchronousInstrumentAccumulator.create(
                    meterProviderSharedState, meterSharedState, descriptor)));
  }
}
