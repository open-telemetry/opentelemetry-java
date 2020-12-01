/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.view.AggregationConfiguration;
import io.opentelemetry.sdk.metrics.view.Aggregations;
import io.opentelemetry.sdk.metrics.view.InstrumentSelector;
import org.junit.jupiter.api.Test;

class AggregationChooserTest {

  @Test
  void selection_onType() {
    AggregationConfiguration configuration =
        AggregationConfiguration.create(
            Aggregations.sum(), AggregationConfiguration.Temporality.DELTA);

    AggregationChooser aggregationChooser = new AggregationChooser();
    aggregationChooser.addView(
        InstrumentSelector.newBuilder().instrumentType(InstrumentType.COUNTER).build(),
        configuration);
    assertThat(
            aggregationChooser.chooseAggregation(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(configuration);
    // this one hasn't been configured, so it gets the default still..
    assertThat(
            aggregationChooser.chooseAggregation(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.UP_DOWN_COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(
            AggregationConfiguration.create(
                Aggregations.sum(), AggregationConfiguration.Temporality.CUMULATIVE));
  }

  @Test
  void selection_onName() {
    AggregationConfiguration configuration =
        AggregationConfiguration.create(
            Aggregations.sum(), AggregationConfiguration.Temporality.DELTA);

    AggregationChooser aggregationChooser = new AggregationChooser();
    aggregationChooser.addView(
        InstrumentSelector.newBuilder().instrumentNameRegex("overridden").build(), configuration);
    assertThat(
            aggregationChooser.chooseAggregation(
                InstrumentDescriptor.create(
                    "overridden", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(configuration);
    // this one hasn't been configured, so it gets the default still..
    assertThat(
            aggregationChooser.chooseAggregation(
                InstrumentDescriptor.create(
                    "default", "", "", InstrumentType.UP_DOWN_COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(
            AggregationConfiguration.create(
                Aggregations.sum(), AggregationConfiguration.Temporality.CUMULATIVE));
  }

  @Test
  void selection_moreSpecificWins() {
    AggregationConfiguration configuration1 =
        AggregationConfiguration.create(
            Aggregations.sum(), AggregationConfiguration.Temporality.DELTA);
    AggregationConfiguration configuration2 =
        AggregationConfiguration.create(
            Aggregations.count(), AggregationConfiguration.Temporality.DELTA);

    AggregationChooser aggregationChooser = new AggregationChooser();
    aggregationChooser.addView(
        InstrumentSelector.newBuilder()
            .instrumentNameRegex("overridden")
            .instrumentType(InstrumentType.COUNTER)
            .build(),
        configuration2);
    aggregationChooser.addView(
        InstrumentSelector.newBuilder().instrumentType(InstrumentType.COUNTER).build(),
        configuration1);

    assertThat(
            aggregationChooser.chooseAggregation(
                InstrumentDescriptor.create(
                    "overridden", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(configuration2);
    assertThat(
            aggregationChooser.chooseAggregation(
                InstrumentDescriptor.create(
                    "default", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(configuration1);
  }

  @Test
  void selection_regex() {
    AggregationConfiguration configuration1 =
        AggregationConfiguration.create(
            Aggregations.sum(), AggregationConfiguration.Temporality.DELTA);

    AggregationChooser aggregationChooser = new AggregationChooser();
    aggregationChooser.addView(
        InstrumentSelector.newBuilder()
            .instrumentNameRegex("overrid(es|den)")
            .instrumentType(InstrumentType.COUNTER)
            .build(),
        configuration1);

    assertThat(
            aggregationChooser.chooseAggregation(
                InstrumentDescriptor.create(
                    "overridden", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(configuration1);
    assertThat(
            aggregationChooser.chooseAggregation(
                InstrumentDescriptor.create(
                    "overrides", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(configuration1);
    // this one hasn't been configured, so it gets the default still..
    assertThat(
            aggregationChooser.chooseAggregation(
                InstrumentDescriptor.create(
                    "default", "", "", InstrumentType.UP_DOWN_COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(
            AggregationConfiguration.create(
                Aggregations.sum(), AggregationConfiguration.Temporality.CUMULATIVE));
  }

  @Test
  void defaults() {
    AggregationChooser aggregationChooser = new AggregationChooser();
    assertThat(
            aggregationChooser.chooseAggregation(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(
            AggregationConfiguration.create(
                Aggregations.sum(), AggregationConfiguration.Temporality.CUMULATIVE));
    assertThat(
            aggregationChooser.chooseAggregation(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.UP_DOWN_COUNTER, InstrumentValueType.LONG)))
        .isEqualTo(
            AggregationConfiguration.create(
                Aggregations.sum(), AggregationConfiguration.Temporality.CUMULATIVE));
    assertThat(
            aggregationChooser.chooseAggregation(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.VALUE_RECORDER, InstrumentValueType.LONG)))
        .isEqualTo(
            AggregationConfiguration.create(
                Aggregations.minMaxSumCount(), AggregationConfiguration.Temporality.DELTA));
    assertThat(
            aggregationChooser.chooseAggregation(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.SUM_OBSERVER, InstrumentValueType.LONG)))
        .isEqualTo(
            AggregationConfiguration.create(
                Aggregations.lastValue(), AggregationConfiguration.Temporality.CUMULATIVE));
    assertThat(
            aggregationChooser.chooseAggregation(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.VALUE_OBSERVER, InstrumentValueType.LONG)))
        .isEqualTo(
            AggregationConfiguration.create(
                Aggregations.lastValue(), AggregationConfiguration.Temporality.DELTA));
    assertThat(
            aggregationChooser.chooseAggregation(
                InstrumentDescriptor.create(
                    "", "", "", InstrumentType.UP_DOWN_SUM_OBSERVER, InstrumentValueType.LONG)))
        .isEqualTo(
            AggregationConfiguration.create(
                Aggregations.lastValue(), AggregationConfiguration.Temporality.CUMULATIVE));
  }
}
