/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
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
class FilteredDoubleExemplarReservoirTest {
  @Mock DoubleExemplarReservoir reservoir;
  @Mock ExemplarFilter filter;

  @Test
  void testFilter_preventsSamplingDoubles() {
    when(filter.shouldSampleMeasurement(anyDouble(), any(), any())).thenReturn(false);
    DoubleExemplarReservoir filtered = new FilteredDoubleExemplarReservoir(filter, reservoir);
    filtered.offerMeasurement(1.0, Attributes.empty(), Context.root());
  }

  @Test
  void testFilter_allowsSamplingDoubles() {
    when(filter.shouldSampleMeasurement(anyDouble(), any(), any())).thenReturn(true);
    DoubleExemplarReservoir filtered = new FilteredDoubleExemplarReservoir(filter, reservoir);
    filtered.offerMeasurement(1.0, Attributes.empty(), Context.root());
    verify(reservoir).offerMeasurement(1.0, Attributes.empty(), Context.root());
  }

  @Test
  void testFilter_preventsSamplingLongs() {
    when(filter.shouldSampleMeasurement(anyLong(), any(), any())).thenReturn(false);
    DoubleExemplarReservoir filtered = new FilteredDoubleExemplarReservoir(filter, reservoir);
    filtered.offerMeasurement(1L, Attributes.empty(), Context.root());
  }

  @Test
  void testFilter_allowsSamplingLongs() {
    when(filter.shouldSampleMeasurement(anyLong(), any(), any())).thenReturn(true);
    DoubleExemplarReservoir filtered = new FilteredDoubleExemplarReservoir(filter, reservoir);
    filtered.offerMeasurement(1L, Attributes.empty(), Context.root());
    verify(reservoir).offerMeasurement(1L, Attributes.empty(), Context.root());
  }

  @Test
  void reservoir_collectsUnderlying() {
    when(reservoir.collectAndReset(Attributes.empty())).thenReturn(Collections.emptyList());
    DoubleExemplarReservoir filtered = new FilteredDoubleExemplarReservoir(filter, reservoir);
    assertThat(filtered.collectAndReset(Attributes.empty())).isEmpty();
  }
}
