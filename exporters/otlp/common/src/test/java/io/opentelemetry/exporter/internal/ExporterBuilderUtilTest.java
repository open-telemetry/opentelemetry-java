/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import org.junit.jupiter.api.Test;

class ExporterBuilderUtilTest {

  @Test
  void deltaPreferred() {
    assertThat(ExporterBuilderUtil.deltaPreferred(InstrumentType.COUNTER))
        .isEqualTo(AggregationTemporality.DELTA);
    assertThat(ExporterBuilderUtil.deltaPreferred(InstrumentType.OBSERVABLE_COUNTER))
        .isEqualTo(AggregationTemporality.DELTA);
    assertThat(ExporterBuilderUtil.deltaPreferred(InstrumentType.HISTOGRAM))
        .isEqualTo(AggregationTemporality.DELTA);
    assertThat(ExporterBuilderUtil.deltaPreferred(InstrumentType.OBSERVABLE_GAUGE))
        .isEqualTo(AggregationTemporality.DELTA);
    assertThat(ExporterBuilderUtil.deltaPreferred(InstrumentType.UP_DOWN_COUNTER))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(ExporterBuilderUtil.deltaPreferred(InstrumentType.OBSERVABLE_UP_DOWN_COUNTER))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
  }

  @Test
  void cumulativePreferred() {
    assertThat(ExporterBuilderUtil.cumulativePreferred(InstrumentType.COUNTER))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(ExporterBuilderUtil.cumulativePreferred(InstrumentType.OBSERVABLE_COUNTER))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(ExporterBuilderUtil.cumulativePreferred(InstrumentType.HISTOGRAM))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(ExporterBuilderUtil.cumulativePreferred(InstrumentType.OBSERVABLE_GAUGE))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(ExporterBuilderUtil.cumulativePreferred(InstrumentType.UP_DOWN_COUNTER))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
    assertThat(ExporterBuilderUtil.cumulativePreferred(InstrumentType.OBSERVABLE_UP_DOWN_COUNTER))
        .isEqualTo(AggregationTemporality.CUMULATIVE);
  }
}
