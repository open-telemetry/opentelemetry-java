/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import org.junit.jupiter.api.Test;

class AggregationTemporalitySelectorTest {

  @Test
  void alwaysCumulative() {
    AggregationTemporalitySelector selector = AggregationTemporalitySelector.alwaysCumulative();
    assertThat(selector.getAggregationTemporality(InstrumentType.COUNTER))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(selector.getAggregationTemporality(InstrumentType.OBSERVABLE_COUNTER))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(selector.getAggregationTemporality(InstrumentType.HISTOGRAM))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(selector.getAggregationTemporality(InstrumentType.OBSERVABLE_GAUGE))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(selector.getAggregationTemporality(InstrumentType.UP_DOWN_COUNTER))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(selector.getAggregationTemporality(InstrumentType.OBSERVABLE_UP_DOWN_COUNTER))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(selector.getAggregationTemporality(InstrumentType.GAUGE))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
  }

  @Test
  void deltaPreferred() {
    AggregationTemporalitySelector selector = AggregationTemporalitySelector.deltaPreferred();
    assertThat(selector.getAggregationTemporality(InstrumentType.COUNTER))
        .isEqualTo(AggregationTemporality.DELTA);
    assertThat(selector.getAggregationTemporality(InstrumentType.OBSERVABLE_COUNTER))
        .isEqualTo(AggregationTemporality.DELTA);
    assertThat(selector.getAggregationTemporality(InstrumentType.HISTOGRAM))
        .isEqualTo(AggregationTemporality.DELTA);
    assertThat(selector.getAggregationTemporality(InstrumentType.OBSERVABLE_GAUGE))
        .isEqualTo(AggregationTemporality.DELTA);
    assertThat(selector.getAggregationTemporality(InstrumentType.UP_DOWN_COUNTER))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(selector.getAggregationTemporality(InstrumentType.OBSERVABLE_UP_DOWN_COUNTER))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(selector.getAggregationTemporality(InstrumentType.GAUGE))
        .isEqualTo(AggregationTemporality.DELTA);
  }

  @Test
  void lowMemory() {
    AggregationTemporalitySelector selector = AggregationTemporalitySelector.lowMemory();
    assertThat(selector.getAggregationTemporality(InstrumentType.COUNTER))
        .isEqualTo(AggregationTemporality.DELTA);
    assertThat(selector.getAggregationTemporality(InstrumentType.OBSERVABLE_COUNTER))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(selector.getAggregationTemporality(InstrumentType.HISTOGRAM))
        .isEqualTo(AggregationTemporality.DELTA);
    assertThat(selector.getAggregationTemporality(InstrumentType.OBSERVABLE_GAUGE))
        .isEqualTo(AggregationTemporality.DELTA);
    assertThat(selector.getAggregationTemporality(InstrumentType.UP_DOWN_COUNTER))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(selector.getAggregationTemporality(InstrumentType.OBSERVABLE_UP_DOWN_COUNTER))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(selector.getAggregationTemporality(InstrumentType.GAUGE))
        .isEqualTo(AggregationTemporality.DELTA);
  }

  @Test
  void stringRepresentation() {
    assertThat(
            AggregationTemporalitySelector.asString(
                AggregationTemporalitySelector.alwaysCumulative()))
        .isEqualTo(
            "AggregationTemporalitySelector{"
                + "COUNTER=CUMULATIVE, "
                + "UP_DOWN_COUNTER=CUMULATIVE, "
                + "HISTOGRAM=CUMULATIVE, "
                + "OBSERVABLE_COUNTER=CUMULATIVE, "
                + "OBSERVABLE_UP_DOWN_COUNTER=CUMULATIVE, "
                + "OBSERVABLE_GAUGE=CUMULATIVE, "
                + "GAUGE=CUMULATIVE"
                + "}");
    assertThat(
            AggregationTemporalitySelector.asString(
                AggregationTemporalitySelector.deltaPreferred()))
        .isEqualTo(
            "AggregationTemporalitySelector{"
                + "COUNTER=DELTA, "
                + "UP_DOWN_COUNTER=CUMULATIVE, "
                + "HISTOGRAM=DELTA, "
                + "OBSERVABLE_COUNTER=DELTA, "
                + "OBSERVABLE_UP_DOWN_COUNTER=CUMULATIVE, "
                + "OBSERVABLE_GAUGE=DELTA, "
                + "GAUGE=DELTA"
                + "}");
  }
}
