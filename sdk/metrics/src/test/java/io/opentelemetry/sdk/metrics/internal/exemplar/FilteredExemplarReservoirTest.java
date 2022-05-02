/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FilteredExemplarReservoirTest {
  @Mock ExemplarReservoir<DoubleExemplarData> reservoir;
  @Mock ExemplarFilter filter;

  @Test
  void testFilter_preventsSampling() {
    when(filter.shouldSampleMeasurement(anyDouble(), any(), any())).thenReturn(false);
    ExemplarReservoir<DoubleExemplarData> filtered =
        new FilteredExemplarReservoir<>(filter, reservoir);
    filtered.offerDoubleMeasurement(1.0, Attributes.empty(), Context.root());
  }

  @Test
  void testFilter_allowsSampling() {
    when(filter.shouldSampleMeasurement(anyDouble(), any(), any())).thenReturn(true);
    ExemplarReservoir<DoubleExemplarData> filtered =
        new FilteredExemplarReservoir<>(filter, reservoir);
    filtered.offerDoubleMeasurement(1.0, Attributes.empty(), Context.root());
    verify(reservoir).offerDoubleMeasurement(1.0, Attributes.empty(), Context.root());
  }

  @Test
  void reservoir_collectsUnderlying() {
    when(reservoir.collectAndReset(Attributes.empty())).thenReturn(Collections.emptyList());
    ExemplarReservoir<DoubleExemplarData> filtered =
        new FilteredExemplarReservoir<>(filter, reservoir);
    assertThat(filtered.collectAndReset(Attributes.empty())).isEmpty();
  }
}
