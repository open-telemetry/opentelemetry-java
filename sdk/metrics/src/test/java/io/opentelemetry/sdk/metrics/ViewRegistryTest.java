/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.aggregation.Aggregations;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.view.AggregationConfiguration;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Test;

class ViewRegistryTest {

  @Test
  void createBatcher_cumulative() {
    ViewRegistry viewRegistry = new ViewRegistry();
    InstrumentDescriptor descriptor =
        InstrumentDescriptor.create(
            "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.DOUBLE);
    MeterProviderSharedState providerSharedState =
        MeterProviderSharedState.create(TestClock.create(), Resource.getEmpty());
    MeterSharedState meterSharedState =
        MeterSharedState.create(InstrumentationLibraryInfo.create("test", "1.0"));

    AggregationConfiguration specification =
        AggregationConfiguration.create(
            Aggregations.count(), MetricData.AggregationTemporality.CUMULATIVE);
    viewRegistry.registerView(
        InstrumentSelector.builder()
            .setInstrumentType(InstrumentType.COUNTER)
            .setInstrumentNameRegex("name")
            .build(),
        specification);

    InstrumentProcessor expectedInstrumentProcessor =
        InstrumentProcessor.getCumulativeAllLabels(
            descriptor, providerSharedState, meterSharedState, Aggregations.count());

    InstrumentProcessor result =
        viewRegistry.createBatcher(providerSharedState, meterSharedState, descriptor);

    assertThat(result.generatesDeltas()).isFalse();
    assertThat(result).isEqualTo(expectedInstrumentProcessor);

    assertThat(result).isNotNull();
  }

  @Test
  void createBatcher_delta() {
    ViewRegistry viewRegistry = new ViewRegistry();

    InstrumentDescriptor descriptor =
        InstrumentDescriptor.create(
            "name", "description", "unit", InstrumentType.COUNTER, InstrumentValueType.DOUBLE);
    MeterProviderSharedState providerSharedState =
        MeterProviderSharedState.create(TestClock.create(), Resource.getEmpty());
    MeterSharedState meterSharedState =
        MeterSharedState.create(InstrumentationLibraryInfo.create("test", "1.0"));

    AggregationConfiguration specification =
        AggregationConfiguration.create(
            Aggregations.count(), MetricData.AggregationTemporality.DELTA);
    viewRegistry.registerView(
        InstrumentSelector.builder()
            .setInstrumentType(InstrumentType.COUNTER)
            .setInstrumentNameRegex("name")
            .build(),
        specification);

    InstrumentProcessor expectedInstrumentProcessor =
        InstrumentProcessor.getDeltaAllLabels(
            descriptor, providerSharedState, meterSharedState, Aggregations.count());

    InstrumentProcessor result =
        viewRegistry.createBatcher(providerSharedState, meterSharedState, descriptor);

    assertThat(result.generatesDeltas()).isTrue();
    assertThat(result).isEqualTo(expectedInstrumentProcessor);

    assertThat(result).isNotNull();
  }

  @Test
  void selection_onType() {
    AggregationConfiguration configuration =
        AggregationConfiguration.create(
            Aggregations.sum(), MetricData.AggregationTemporality.DELTA);

    ViewRegistry viewRegistry = new ViewRegistry();
    viewRegistry.registerView(
        InstrumentSelector.builder()
            .setInstrumentType(InstrumentType.COUNTER)
            .setInstrumentNameRegex(".*")
            .build(),
        configuration);
    assertThat(
            viewRegistry.chooseAggregation(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(configuration);
    // this one hasn't been configured, so it gets the default still..
    assertThat(
            viewRegistry.chooseAggregation(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.UP_DOWN_COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(
            AggregationConfiguration.create(
                Aggregations.sum(), MetricData.AggregationTemporality.CUMULATIVE));
  }

  @Test
  void selection_onName() {
    AggregationConfiguration configuration =
        AggregationConfiguration.create(
            Aggregations.sum(), MetricData.AggregationTemporality.DELTA);

    ViewRegistry viewRegistry = new ViewRegistry();
    viewRegistry.registerView(
        InstrumentSelector.builder()
            .setInstrumentType(InstrumentType.COUNTER)
            .setInstrumentNameRegex("overridden")
            .build(),
        configuration);
    assertThat(
            viewRegistry.chooseAggregation(
                InstrumentDescriptor.create(
                    "overridden", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(configuration);
    // this one hasn't been configured, so it gets the default still..
    assertThat(
            viewRegistry.chooseAggregation(
                InstrumentDescriptor.create(
                    "default", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(
            AggregationConfiguration.create(
                Aggregations.sum(), MetricData.AggregationTemporality.CUMULATIVE));
  }

  @Test
  void selection_LastAddedViewWins() {
    AggregationConfiguration configuration1 =
        AggregationConfiguration.create(
            Aggregations.sum(), MetricData.AggregationTemporality.DELTA);
    AggregationConfiguration configuration2 =
        AggregationConfiguration.create(
            Aggregations.count(), MetricData.AggregationTemporality.DELTA);

    ViewRegistry viewRegistry = new ViewRegistry();
    viewRegistry.registerView(
        InstrumentSelector.builder()
            .setInstrumentType(InstrumentType.COUNTER)
            .setInstrumentNameRegex(".*")
            .build(),
        configuration1);
    viewRegistry.registerView(
        InstrumentSelector.builder()
            .setInstrumentType(InstrumentType.COUNTER)
            .setInstrumentNameRegex("overridden")
            .build(),
        configuration2);

    assertThat(
            viewRegistry.chooseAggregation(
                InstrumentDescriptor.create(
                    "overridden", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(configuration2);
    assertThat(
            viewRegistry.chooseAggregation(
                InstrumentDescriptor.create(
                    "default", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(configuration1);
  }

  @Test
  void selection_regex() {
    AggregationConfiguration configuration1 =
        AggregationConfiguration.create(
            Aggregations.sum(), MetricData.AggregationTemporality.DELTA);

    ViewRegistry viewRegistry = new ViewRegistry();
    viewRegistry.registerView(
        InstrumentSelector.builder()
            .setInstrumentNameRegex("overrid(es|den)")
            .setInstrumentType(InstrumentType.COUNTER)
            .build(),
        configuration1);

    assertThat(
            viewRegistry.chooseAggregation(
                InstrumentDescriptor.create(
                    "overridden", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(configuration1);
    assertThat(
            viewRegistry.chooseAggregation(
                InstrumentDescriptor.create(
                    "overrides", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(configuration1);
    // this one hasn't been configured, so it gets the default still..
    assertThat(
            viewRegistry.chooseAggregation(
                InstrumentDescriptor.create(
                    "default", "", "", InstrumentType.UP_DOWN_COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(
            AggregationConfiguration.create(
                Aggregations.sum(), MetricData.AggregationTemporality.CUMULATIVE));
  }

  @Test
  void defaults() {
    ViewRegistry viewRegistry = new ViewRegistry();
    assertThat(
            viewRegistry.chooseAggregation(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(
            AggregationConfiguration.create(
                Aggregations.sum(), MetricData.AggregationTemporality.CUMULATIVE));
    assertThat(
            viewRegistry.chooseAggregation(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.UP_DOWN_COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(
            AggregationConfiguration.create(
                Aggregations.sum(), MetricData.AggregationTemporality.CUMULATIVE));
    assertThat(
            viewRegistry.chooseAggregation(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.VALUE_RECORDER, InstrumentValueType.LONG)))
        .isEqualTo(
            AggregationConfiguration.create(
                Aggregations.minMaxSumCount(), MetricData.AggregationTemporality.DELTA));
    assertThat(
            viewRegistry.chooseAggregation(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.SUM_OBSERVER, InstrumentValueType.LONG)))
        .isEqualTo(
            AggregationConfiguration.create(
                Aggregations.lastValue(), MetricData.AggregationTemporality.CUMULATIVE));
    assertThat(
            viewRegistry.chooseAggregation(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.VALUE_OBSERVER, InstrumentValueType.LONG)))
        .isEqualTo(
            AggregationConfiguration.create(
                Aggregations.lastValue(), MetricData.AggregationTemporality.DELTA));
    assertThat(
            viewRegistry.chooseAggregation(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.UP_DOWN_SUM_OBSERVER, InstrumentValueType.LONG)))
        .isEqualTo(
            AggregationConfiguration.create(
                Aggregations.lastValue(), MetricData.AggregationTemporality.CUMULATIVE));
  }
}
