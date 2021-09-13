/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarSampler;
import io.opentelemetry.sdk.metrics.internal.view.AttributesProcessor;
import io.opentelemetry.sdk.metrics.internal.view.ViewRegistry;
import io.opentelemetry.sdk.metrics.view.Aggregation;
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
  private AttributesProcessor spyAttributesProcessor;
  private View view;

  @BeforeEach
  void setup() {
    spyAttributesProcessor = Mockito.spy(AttributesProcessor.noop());
    view =
        View.builder()
            .setAggregation(Aggregation.lastValue())
            .setAttributesProcessor(spyAttributesProcessor)
            .build();
    ViewRegistry viewRegistry =
        ViewRegistry.builder()
            .addView(
                InstrumentSelector.builder()
                    .setInstrumentType(InstrumentType.OBSERVABLE_GAUGE)
                    .build(),
                view)
            .build();

    meterProviderSharedState =
        MeterProviderSharedState.create(
            testClock, Resource.empty(), viewRegistry, ExemplarSampler.builder().build());
  }

  @Test
  void doubleAsynchronousAccumulator_AttributesProcessor_used() {
    AsynchronousMetricStorage.doubleAsynchronousAccumulator(
            view,
            InstrumentDescriptor.create(
                "name",
                "description",
                "unit",
                InstrumentType.OBSERVABLE_GAUGE,
                InstrumentValueType.DOUBLE),
            meterProviderSharedState.getResource(),
            meterSharedState.getInstrumentationLibraryInfo(),
            meterProviderSharedState.getStartEpochNanos(),
            value -> value.observe(1.0, Attributes.empty()))
        .collectAndReset(0, testClock.now());
    Mockito.verify(spyAttributesProcessor).process(Attributes.empty(), Context.current());
  }

  @Test
  void longAsynchronousAccumulator_AttributesProcessor_used() {
    AsynchronousMetricStorage.longAsynchronousAccumulator(
            view,
            InstrumentDescriptor.create(
                "name",
                "description",
                "unit",
                InstrumentType.OBSERVABLE_GAUGE,
                InstrumentValueType.LONG),
            meterProviderSharedState.getResource(),
            meterSharedState.getInstrumentationLibraryInfo(),
            meterProviderSharedState.getStartEpochNanos(),
            value -> value.observe(1, Attributes.empty()))
        .collectAndReset(0, testClock.nanoTime());
    Mockito.verify(spyAttributesProcessor).process(Attributes.empty(), Context.current());
  }
}
