/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FilteredExemplarReservoirTest {
  @Mock DoubleExemplarReservoir doubleReservoir;
  @Mock LongExemplarReservoir longReservoir;
  @Mock ExemplarFilterInternal filter;

  @Test
  void testFilterDouble_preventsSampling() {
    when(filter.shouldSampleMeasurement(anyDouble(), any(), any())).thenReturn(false);
    DoubleExemplarReservoir filtered = new DoubleFilteredExemplarReservoir(filter, doubleReservoir);
    filtered.offerDoubleMeasurement(1.0, Attributes.empty(), Context.root());
    verify(doubleReservoir, never()).offerDoubleMeasurement(anyDouble(), any(), any());
  }

  @Test
  void testFilterLong_preventsSampling() {
    when(filter.shouldSampleMeasurement(anyLong(), any(), any())).thenReturn(false);
    LongExemplarReservoir filtered = new LongFilteredExemplarReservoir(filter, longReservoir);
    filtered.offerLongMeasurement(1L, Attributes.empty(), Context.root());
    verify(longReservoir, never()).offerLongMeasurement(anyLong(), any(), any());
  }

  @Test
  void testFilterDouble_allowsSampling() {
    when(filter.shouldSampleMeasurement(anyDouble(), any(), any())).thenReturn(true);
    DoubleExemplarReservoir filtered = new DoubleFilteredExemplarReservoir(filter, doubleReservoir);
    filtered.offerDoubleMeasurement(1.0, Attributes.empty(), Context.root());
    verify(doubleReservoir).offerDoubleMeasurement(1.0, Attributes.empty(), Context.root());
  }

  @Test
  void testFilterLong_allowsSampling() {
    when(filter.shouldSampleMeasurement(anyLong(), any(), any())).thenReturn(true);
    LongExemplarReservoir filtered = new LongFilteredExemplarReservoir(filter, longReservoir);
    filtered.offerLongMeasurement(1L, Attributes.empty(), Context.root());
    verify(longReservoir).offerLongMeasurement(1L, Attributes.empty(), Context.root());
  }

  @Test
  void reservoirDouble_collectsUnderlying() {
    when(doubleReservoir.collectAndResetDoubles(Attributes.empty()))
        .thenReturn(Collections.emptyList());
    DoubleExemplarReservoir filtered =
        new DoubleFilteredExemplarReservoir(filter, doubleReservoir) {};
    assertThat(filtered.collectAndResetDoubles(Attributes.empty())).isEmpty();
  }

  @Test
  void reservoirLong_collectsUnderlying() {
    when(longReservoir.collectAndResetLongs(Attributes.empty()))
        .thenReturn(Collections.emptyList());
    LongExemplarReservoir filtered = new LongFilteredExemplarReservoir(filter, longReservoir) {};
    assertThat(filtered.collectAndResetLongs(Attributes.empty())).isEmpty();
  }
}
