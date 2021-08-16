/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.internal.view.ViewRegistry;
import io.opentelemetry.sdk.metrics.processor.LabelsProcessor;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import io.opentelemetry.sdk.metrics.view.View;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.time.TestClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class AsynchronousMetricStorageTest {
  private final TestClock testClock = TestClock.create();
  private MeterProviderSharedState meterProviderSharedState;
  private final MeterSharedState meterSharedState =
      MeterSharedState.create(InstrumentationLibraryInfo.empty());
  private LabelsProcessor spyLabelProcessor;

  @BeforeEach
  void setup() {
    spyLabelProcessor =
        Mockito.spy(
            // note: can't convert to a lambda here because Mockito gets grumpy
            new LabelsProcessor() {
              @Override
              public Attributes onLabelsBound(Context ctx, Attributes labels) {
                return labels.toBuilder().build();
              }
            });
    ViewRegistry viewRegistry =
        ViewRegistry.builder()
            .addView(
                InstrumentSelector.builder()
                    .setInstrumentType(InstrumentType.OBSERVABLE_GAUGE)
                    .build(),
                View.builder()
                    .setAggregatorFactory(AggregatorFactory.lastValue())
                    .setLabelsProcessorFactory(
                        (resource, instrumentationLibraryInfo, descriptor) -> spyLabelProcessor)
                    .build())
            .build();

    meterProviderSharedState =
        MeterProviderSharedState.create(testClock, Resource.empty(), viewRegistry);
  }

  @Test
  void doubleAsynchronousAccumulator_LabelsProcessor_used() {
    AsynchronousMetricStorage.doubleAsynchronousAccumulator(
            meterProviderSharedState,
            meterSharedState,
            InstrumentDescriptor.create(
                "name",
                "description",
                "unit",
                InstrumentType.OBSERVABLE_GAUGE,
                InstrumentValueType.DOUBLE),
            value -> value.observe(1.0, Attributes.empty()))
        .collectAndReset(0, testClock.now());
    Mockito.verify(spyLabelProcessor).onLabelsBound(Context.current(), Attributes.empty());
  }

  @Test
  void longAsynchronousAccumulator_LabelsProcessor_used() {
    AsynchronousMetricStorage.longAsynchronousAccumulator(
            meterProviderSharedState,
            meterSharedState,
            InstrumentDescriptor.create(
                "name",
                "description",
                "unit",
                InstrumentType.OBSERVABLE_GAUGE,
                InstrumentValueType.LONG),
            value -> value.observe(1, Attributes.empty()))
        .collectAndReset(0, testClock.nanoTime());
    Mockito.verify(spyLabelProcessor).onLabelsBound(Context.current(), Attributes.empty());
  }
}
