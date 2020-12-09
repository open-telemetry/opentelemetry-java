/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.view.AggregationConfiguration;
import io.opentelemetry.sdk.metrics.view.Aggregations;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Test;

class ViewRegistryTest {

  @Test
  void registerView() {
    AggregationChooser chooser = mock(AggregationChooser.class);

    ViewRegistry viewRegistry = new ViewRegistry(chooser);
    InstrumentSelector selector =
        InstrumentSelector.newBuilder().instrumentType(InstrumentType.COUNTER).build();
    AggregationConfiguration specification =
        AggregationConfiguration.create(
            Aggregations.count(), AggregationConfiguration.Temporality.CUMULATIVE);

    viewRegistry.registerView(selector, specification);

    verify(chooser).addView(selector, specification);
  }

  @Test
  void createBatcher_cumulative() {
    AggregationChooser chooser = mock(AggregationChooser.class);

    ViewRegistry viewRegistry = new ViewRegistry(chooser);

    InstrumentDescriptor descriptor =
        InstrumentDescriptor.create(
            "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.DOUBLE);
    MeterProviderSharedState providerSharedState =
        MeterProviderSharedState.create(TestClock.create(), Resource.getEmpty());
    MeterSharedState meterSharedState =
        MeterSharedState.create(InstrumentationLibraryInfo.create("test", "1.0"));

    AggregationConfiguration specification =
        AggregationConfiguration.create(
            Aggregations.count(), AggregationConfiguration.Temporality.CUMULATIVE);
    InstrumentAccumulator expectedInstrumentAccumulator =
        InstrumentAccumulator.getCumulativeAllLabels(
            descriptor, providerSharedState, meterSharedState, Aggregations.count());

    when(chooser.chooseAggregation(descriptor)).thenReturn(specification);

    InstrumentAccumulator result =
        viewRegistry.createBatcher(providerSharedState, meterSharedState, descriptor);

    assertThat(result.generatesDeltas()).isFalse();
    assertThat(result).isEqualTo(expectedInstrumentAccumulator);

    assertThat(result).isNotNull();
  }

  @Test
  void createBatcher_delta() {
    AggregationChooser chooser = mock(AggregationChooser.class);

    ViewRegistry viewRegistry = new ViewRegistry(chooser);

    InstrumentDescriptor descriptor =
        InstrumentDescriptor.create(
            "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.DOUBLE);
    MeterProviderSharedState providerSharedState =
        MeterProviderSharedState.create(TestClock.create(), Resource.getEmpty());
    MeterSharedState meterSharedState =
        MeterSharedState.create(InstrumentationLibraryInfo.create("test", "1.0"));

    AggregationConfiguration specification =
        AggregationConfiguration.create(
            Aggregations.count(), AggregationConfiguration.Temporality.DELTA);
    InstrumentAccumulator expectedInstrumentAccumulator =
        InstrumentAccumulator.getDeltaAllLabels(
            descriptor, providerSharedState, meterSharedState, Aggregations.count());

    when(chooser.chooseAggregation(descriptor)).thenReturn(specification);

    InstrumentAccumulator result =
        viewRegistry.createBatcher(providerSharedState, meterSharedState, descriptor);

    assertThat(result.generatesDeltas()).isTrue();
    assertThat(result).isEqualTo(expectedInstrumentAccumulator);

    assertThat(result).isNotNull();
  }
}
