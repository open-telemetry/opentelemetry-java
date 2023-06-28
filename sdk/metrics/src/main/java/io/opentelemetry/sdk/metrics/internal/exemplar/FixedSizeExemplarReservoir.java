/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

/** Base for fixed-size reservoir sampling of Exemplars. */
abstract class FixedSizeExemplarReservoir<T extends ExemplarData> implements ExemplarReservoir<T> {

  private final ReservoirCell[] storage;
  private final ReservoirCellSelector reservoirCellSelector;
  private final BiFunction<ReservoirCell, Attributes, T> mapAndResetCell;
  private volatile boolean hasMeasurements = false;

  /** Instantiates an exemplar reservoir of fixed size. */
  FixedSizeExemplarReservoir(
      Clock clock,
      int size,
      ReservoirCellSelector reservoirCellSelector,
      BiFunction<ReservoirCell, Attributes, T> mapAndResetCell) {
    this.storage = new ReservoirCell[size];
    for (int i = 0; i < size; ++i) {
      this.storage[i] = new ReservoirCell(clock);
    }
    this.reservoirCellSelector = reservoirCellSelector;
    this.mapAndResetCell = mapAndResetCell;
  }

  @Override
  public void offerLongMeasurement(long value, Attributes attributes, Context context) {
    int bucket = reservoirCellSelector.reservoirCellIndexFor(storage, value, attributes, context);
    if (bucket != -1) {
      this.storage[bucket].recordLongMeasurement(value, attributes, context);
      this.hasMeasurements = true;
    }
  }

  @Override
  public void offerDoubleMeasurement(double value, Attributes attributes, Context context) {
    int bucket = reservoirCellSelector.reservoirCellIndexFor(storage, value, attributes, context);
    if (bucket != -1) {
      this.storage[bucket].recordDoubleMeasurement(value, attributes, context);
      this.hasMeasurements = true;
    }
  }

  @Override
  public List<T> collectAndReset(Attributes pointAttributes) {
    if (!hasMeasurements) {
      return Collections.emptyList();
    }
    // Note: we are collecting exemplars from buckets piecemeal, but we
    // could still be sampling exemplars during this process.
    List<T> results = new ArrayList<>();
    for (ReservoirCell reservoirCell : this.storage) {
      T result = mapAndResetCell.apply(reservoirCell, pointAttributes);
      if (result != null) {
        results.add(result);
      }
    }
    reservoirCellSelector.reset();
    this.hasMeasurements = false;
    return Collections.unmodifiableList(results);
  }
}
