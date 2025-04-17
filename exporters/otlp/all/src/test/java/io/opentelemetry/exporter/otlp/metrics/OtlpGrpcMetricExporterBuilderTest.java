/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class OtlpGrpcMetricExporterBuilderTest {

  @Test
  void verifyToBuilderPreservesSettings() {
    AggregationTemporalitySelector temporalitySelector =
        Mockito.mock(AggregationTemporalitySelector.class);
    DefaultAggregationSelector defaultAggregationSelector =
        Mockito.mock(DefaultAggregationSelector.class);

    OtlpGrpcMetricExporter original =
        OtlpGrpcMetricExporter.builder()
            .setMemoryMode(MemoryMode.IMMUTABLE_DATA)
            .setAggregationTemporalitySelector(temporalitySelector)
            .setDefaultAggregationSelector(defaultAggregationSelector)
            .build();

    OtlpGrpcMetricExporter copy = original.toBuilder().build();

    assertThat(copy.getMemoryMode()).isEqualTo(MemoryMode.IMMUTABLE_DATA);
    assertThat(copy.aggregationTemporalitySelector).isSameAs(temporalitySelector);
    assertThat(copy.defaultAggregationSelector).isSameAs(defaultAggregationSelector);

    original.close();
    copy.close();
  }
}
