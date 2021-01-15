/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.view.AggregationConfiguration;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import org.junit.jupiter.api.Test;

class ViewRegistryTest {
  @Test
  void selection_onType() {
    AggregationConfiguration configuration =
        AggregationConfiguration.create(AggregatorFactory.sum(), AggregationTemporality.DELTA);

    ViewRegistry viewRegistry = new ViewRegistry();
    viewRegistry.registerView(
        InstrumentSelector.builder()
            .setInstrumentType(InstrumentType.COUNTER)
            .setInstrumentNameRegex(".*")
            .build(),
        configuration);
    assertThat(
            viewRegistry.findView(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(configuration);
    // this one hasn't been configured, so it gets the default still..
    assertThat(
            viewRegistry.findView(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.UP_DOWN_COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(
            AggregationConfiguration.create(
                AggregatorFactory.sum(), AggregationTemporality.CUMULATIVE));
  }

  @Test
  void selection_onName() {
    AggregationConfiguration configuration =
        AggregationConfiguration.create(AggregatorFactory.sum(), AggregationTemporality.DELTA);

    ViewRegistry viewRegistry = new ViewRegistry();
    viewRegistry.registerView(
        InstrumentSelector.builder()
            .setInstrumentType(InstrumentType.COUNTER)
            .setInstrumentNameRegex("overridden")
            .build(),
        configuration);
    assertThat(
            viewRegistry.findView(
                InstrumentDescriptor.create(
                    "overridden", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(configuration);
    // this one hasn't been configured, so it gets the default still..
    assertThat(
            viewRegistry.findView(
                InstrumentDescriptor.create(
                    "default", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(
            AggregationConfiguration.create(
                AggregatorFactory.sum(), AggregationTemporality.CUMULATIVE));
  }

  @Test
  void selection_LastAddedViewWins() {
    AggregationConfiguration configuration1 =
        AggregationConfiguration.create(AggregatorFactory.sum(), AggregationTemporality.DELTA);
    AggregationConfiguration configuration2 =
        AggregationConfiguration.create(AggregatorFactory.count(), AggregationTemporality.DELTA);

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
            viewRegistry.findView(
                InstrumentDescriptor.create(
                    "overridden", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(configuration2);
    assertThat(
            viewRegistry.findView(
                InstrumentDescriptor.create(
                    "default", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(configuration1);
  }

  @Test
  void selection_regex() {
    AggregationConfiguration configuration1 =
        AggregationConfiguration.create(AggregatorFactory.sum(), AggregationTemporality.DELTA);

    ViewRegistry viewRegistry = new ViewRegistry();
    viewRegistry.registerView(
        InstrumentSelector.builder()
            .setInstrumentNameRegex("overrid(es|den)")
            .setInstrumentType(InstrumentType.COUNTER)
            .build(),
        configuration1);

    assertThat(
            viewRegistry.findView(
                InstrumentDescriptor.create(
                    "overridden", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(configuration1);
    assertThat(
            viewRegistry.findView(
                InstrumentDescriptor.create(
                    "overrides", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(configuration1);
    // this one hasn't been configured, so it gets the default still..
    assertThat(
            viewRegistry.findView(
                InstrumentDescriptor.create(
                    "default", "", "", InstrumentType.UP_DOWN_COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(
            AggregationConfiguration.create(
                AggregatorFactory.sum(), AggregationTemporality.CUMULATIVE));
  }

  @Test
  void defaults() {
    ViewRegistry viewRegistry = new ViewRegistry();
    assertThat(
            viewRegistry.findView(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(
            AggregationConfiguration.create(
                AggregatorFactory.sum(), AggregationTemporality.CUMULATIVE));
    assertThat(
            viewRegistry.findView(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.UP_DOWN_COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(
            AggregationConfiguration.create(
                AggregatorFactory.sum(), AggregationTemporality.CUMULATIVE));
    assertThat(
            viewRegistry.findView(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.VALUE_RECORDER, InstrumentValueType.LONG)))
        .isEqualTo(
            AggregationConfiguration.create(
                AggregatorFactory.minMaxSumCount(), AggregationTemporality.DELTA));
    assertThat(
            viewRegistry.findView(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.SUM_OBSERVER, InstrumentValueType.LONG)))
        .isEqualTo(
            AggregationConfiguration.create(
                AggregatorFactory.lastValue(), AggregationTemporality.CUMULATIVE));
    assertThat(
            viewRegistry.findView(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.VALUE_OBSERVER, InstrumentValueType.LONG)))
        .isEqualTo(
            AggregationConfiguration.create(
                AggregatorFactory.lastValue(), AggregationTemporality.DELTA));
    assertThat(
            viewRegistry.findView(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.UP_DOWN_SUM_OBSERVER, InstrumentValueType.LONG)))
        .isEqualTo(
            AggregationConfiguration.create(
                AggregatorFactory.lastValue(), AggregationTemporality.CUMULATIVE));
  }
}
