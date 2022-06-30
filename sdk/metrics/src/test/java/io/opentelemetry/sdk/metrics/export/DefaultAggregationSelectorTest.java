/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import org.junit.jupiter.api.Test;

class DefaultAggregationSelectorTest {

  @Test
  void getDefault() {
    DefaultAggregationSelector selector = DefaultAggregationSelector.getDefault();
    for (InstrumentType instrumentType : InstrumentType.values()) {
      assertThat(selector.getDefaultAggregation(instrumentType))
          .isEqualTo(Aggregation.defaultAggregation());
    }
  }

  @Test
  void with() {
    assertThatThrownBy(() -> DefaultAggregationSelector.getDefault().with(null, Aggregation.drop()))
        .hasMessage("instrumentType");
    assertThatThrownBy(
            () -> DefaultAggregationSelector.getDefault().with(InstrumentType.HISTOGRAM, null))
        .hasMessage("aggregation");

    DefaultAggregationSelector selector1 =
        DefaultAggregationSelector.getDefault().with(InstrumentType.HISTOGRAM, Aggregation.drop());
    assertThat(selector1.getDefaultAggregation(InstrumentType.COUNTER))
        .isEqualTo(Aggregation.defaultAggregation());
    assertThat(selector1.getDefaultAggregation(InstrumentType.UP_DOWN_COUNTER))
        .isEqualTo(Aggregation.defaultAggregation());
    assertThat(selector1.getDefaultAggregation(InstrumentType.HISTOGRAM))
        .isEqualTo(Aggregation.drop());
    assertThat(selector1.getDefaultAggregation(InstrumentType.OBSERVABLE_COUNTER))
        .isEqualTo(Aggregation.defaultAggregation());
    assertThat(selector1.getDefaultAggregation(InstrumentType.OBSERVABLE_UP_DOWN_COUNTER))
        .isEqualTo(Aggregation.defaultAggregation());
    assertThat(selector1.getDefaultAggregation(InstrumentType.OBSERVABLE_GAUGE))
        .isEqualTo(Aggregation.defaultAggregation());

    DefaultAggregationSelector selector2 =
        selector1.with(InstrumentType.COUNTER, Aggregation.drop());
    assertThat(selector2.getDefaultAggregation(InstrumentType.COUNTER))
        .isEqualTo(Aggregation.drop());
    assertThat(selector2.getDefaultAggregation(InstrumentType.UP_DOWN_COUNTER))
        .isEqualTo(Aggregation.defaultAggregation());
    assertThat(selector2.getDefaultAggregation(InstrumentType.HISTOGRAM))
        .isEqualTo(Aggregation.drop());
    assertThat(selector2.getDefaultAggregation(InstrumentType.OBSERVABLE_COUNTER))
        .isEqualTo(Aggregation.defaultAggregation());
    assertThat(selector2.getDefaultAggregation(InstrumentType.OBSERVABLE_UP_DOWN_COUNTER))
        .isEqualTo(Aggregation.defaultAggregation());
    assertThat(selector2.getDefaultAggregation(InstrumentType.OBSERVABLE_GAUGE))
        .isEqualTo(Aggregation.defaultAggregation());
  }
}
