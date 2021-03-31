/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.processor.LabelsProcessor;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import io.opentelemetry.sdk.metrics.view.View;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class AsynchronousInstrumentAccumulatorTest {
  private final TestClock testClock = TestClock.create();
  private final MeterProviderSharedState meterProviderSharedState =
      MeterProviderSharedState.create(testClock, Resource.empty());
  private final MeterSharedState meterSharedState =
      MeterSharedState.create(InstrumentationLibraryInfo.empty());
  private LabelsProcessor spyLabelProcessor;

  @BeforeEach
  void setup() {
    spyLabelProcessor =
        Mockito.spy(
            new LabelsProcessor() {
              @Override
              public Labels onLabelsBound(Context ctx, Labels labels) {
                return labels.toBuilder().build();
              }
            });
    meterProviderSharedState
        .getViewRegistry()
        .registerView(
            InstrumentSelector.builder().setInstrumentType(InstrumentType.VALUE_OBSERVER).build(),
            View.builder()
                .setAggregatorFactory(AggregatorFactory.lastValue())
                .setLabelsProcessorFactory(
                    (resource, instrumentationLibraryInfo, descriptor) -> spyLabelProcessor)
                .build());
  }

  @Test
  void doubleAsynchronousAccumulator_LabelsProcessor_used() {
    AsynchronousInstrumentAccumulator.doubleAsynchronousAccumulator(
            meterProviderSharedState,
            meterSharedState,
            InstrumentDescriptor.create(
                "name",
                "description",
                "unit",
                InstrumentType.VALUE_OBSERVER,
                InstrumentValueType.DOUBLE),
            value -> value.observe(1.0, Labels.empty()))
        .collectAll(testClock.nanoTime());
    Mockito.verify(spyLabelProcessor).onLabelsBound(Context.current(), Labels.empty());
  }

  @Test
  void longAsynchronousAccumulator_LabelsProcessor_used() {
    AsynchronousInstrumentAccumulator.longAsynchronousAccumulator(
            meterProviderSharedState,
            meterSharedState,
            InstrumentDescriptor.create(
                "name",
                "description",
                "unit",
                InstrumentType.VALUE_OBSERVER,
                InstrumentValueType.LONG),
            value -> value.observe(1, Labels.empty()))
        .collectAll(testClock.nanoTime());
    Mockito.verify(spyLabelProcessor).onLabelsBound(Context.current(), Labels.empty());
  }
}
