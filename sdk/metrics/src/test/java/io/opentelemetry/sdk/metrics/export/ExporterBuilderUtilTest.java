/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import org.junit.jupiter.api.Test;

class ExporterBuilderUtilTest {

  @Test
  void alwaysCumulative() {
    assertThat(MetricExporter.alwaysCumulative(InstrumentType.COUNTER))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(MetricExporter.alwaysCumulative(InstrumentType.OBSERVABLE_COUNTER))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(MetricExporter.alwaysCumulative(InstrumentType.HISTOGRAM))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(MetricExporter.alwaysCumulative(InstrumentType.OBSERVABLE_GAUGE))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(MetricExporter.alwaysCumulative(InstrumentType.UP_DOWN_COUNTER))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(
            MetricExporter.alwaysCumulative(InstrumentType.OBSERVABLE_UP_DOWN_COUNTER))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
  }

  @Test
  void deltaPreferred() {
    assertThat(MetricExporter.deltaPreferred(InstrumentType.COUNTER))
        .isEqualTo(AggregationTemporality.DELTA);
    assertThat(MetricExporter.deltaPreferred(InstrumentType.OBSERVABLE_COUNTER))
        .isEqualTo(AggregationTemporality.DELTA);
    assertThat(MetricExporter.deltaPreferred(InstrumentType.HISTOGRAM))
        .isEqualTo(AggregationTemporality.DELTA);
    assertThat(MetricExporter.deltaPreferred(InstrumentType.OBSERVABLE_GAUGE))
        .isEqualTo(AggregationTemporality.DELTA);
    assertThat(MetricExporter.deltaPreferred(InstrumentType.UP_DOWN_COUNTER))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(MetricExporter.deltaPreferred(InstrumentType.OBSERVABLE_UP_DOWN_COUNTER))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
  }
}
