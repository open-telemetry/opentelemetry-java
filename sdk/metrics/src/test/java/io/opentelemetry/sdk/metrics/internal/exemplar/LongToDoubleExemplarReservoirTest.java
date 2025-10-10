/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LongToDoubleExemplarReservoirTest {
  @Mock ExemplarReservoir delegate;

  @Test
  void offerDoubleMeasurement() {
    ExemplarReservoir filtered = new LongToDoubleExemplarReservoir(delegate);
    filtered.offerDoubleMeasurement(1.0, Attributes.empty(), Context.root());
    verify(delegate).offerDoubleMeasurement(1.0, Attributes.empty(), Context.root());
    verify(delegate, never()).offerLongMeasurement(anyLong(), any(), any());
  }

  @Test
  void offerLongMeasurement() {
    ExemplarReservoir filtered = new LongToDoubleExemplarReservoir(delegate);
    filtered.offerLongMeasurement(1L, Attributes.empty(), Context.root());
    verify(delegate).offerDoubleMeasurement(1.0, Attributes.empty(), Context.root());
    verify(delegate, never()).offerLongMeasurement(anyLong(), any(), any());
  }

  @Test
  void collectAndReset() {
    ExemplarReservoir filtered = new LongToDoubleExemplarReservoir(delegate);

    assertThatThrownBy(() -> filtered.collectAndResetLongs(Attributes.empty()))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatCode(() -> filtered.collectAndResetDoubles(Attributes.empty()))
        .doesNotThrowAnyException();
  }
}
