/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import javax.annotation.Nullable;

/** Base for fixed-size reservoir sampling of Exemplars. */
class FixedSizeExemplarReservoir implements DoubleExemplarReservoir, LongExemplarReservoir {

  @Nullable private ReservoirCell[] storage;
  private final ReservoirCellSelector reservoirCellSelector;
  private final int size;
  private final Clock clock;
  private volatile boolean hasMeasurements = false;

  /** Instantiates an exemplar reservoir of fixed size. */
  FixedSizeExemplarReservoir(Clock clock, int size, ReservoirCellSelector reservoirCellSelector) {
    this.storage = null; // lazily initialize to avoid allocations
    this.size = size;
    this.clock = clock;
    this.reservoirCellSelector = reservoirCellSelector;
  }

  @Override
  public void offerLongMeasurement(long value, Attributes attributes, Context context) {
    if (storage == null) {
      storage = initStorage();
    }
    int bucket = reservoirCellSelector.reservoirCellIndexFor(storage, value, attributes, context);
    if (bucket != -1) {
      this.storage[bucket].recordLongMeasurement(value, attributes, context);
      this.hasMeasurements = true;
    }
  }

  @Override
  public List<DoubleExemplarData> collectAndResetDoubles(Attributes pointAttributes) {
    return doCollectAndReset(pointAttributes, ReservoirCell::getAndResetDouble);
  }

  @Override
  public List<LongExemplarData> collectAndResetLongs(Attributes pointAttributes) {
    return doCollectAndReset(pointAttributes, ReservoirCell::getAndResetLong);
  }

  @Override
  public void offerDoubleMeasurement(double value, Attributes attributes, Context context) {
    if (storage == null) {
      storage = initStorage();
    }
    int bucket = reservoirCellSelector.reservoirCellIndexFor(storage, value, attributes, context);
    if (bucket != -1) {
      this.storage[bucket].recordDoubleMeasurement(value, attributes, context);
      this.hasMeasurements = true;
    }
  }

  private ReservoirCell[] initStorage() {
    ReservoirCell[] storage = new ReservoirCell[this.size];
    for (int i = 0; i < size; ++i) {
      storage[i] = new ReservoirCell(this.clock);
    }
    return storage;
  }

  public <T extends ExemplarData> List<T> doCollectAndReset(
      Attributes pointAttributes, BiFunction<ReservoirCell, Attributes, T> mapAndResetCell) {
    if (!hasMeasurements || storage == null) {
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
