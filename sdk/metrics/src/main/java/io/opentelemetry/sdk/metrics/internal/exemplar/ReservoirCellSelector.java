/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;

/** Selects which {@link FixedSizeExemplarReservoir} {@link ReservoirCell} receives measurements. */
interface ReservoirCellSelector {

  /** Determine the index of the {@code cells} to record the measurement to. */
  int reservoirCellIndexFor(
      ReservoirCell[] cells, long value, Attributes attributes, Context context);

  /** Determine the index of the {@code cells} to record the measurement to. */
  int reservoirCellIndexFor(
      ReservoirCell[] cells, double value, Attributes attributes, Context context);

  /** Called when {@link FixedSizeExemplarReservoir#collectAndReset(Attributes)}. */
  void reset();
}
