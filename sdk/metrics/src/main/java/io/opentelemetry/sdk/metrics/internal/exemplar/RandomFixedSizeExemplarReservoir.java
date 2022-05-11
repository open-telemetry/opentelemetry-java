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
import io.opentelemetry.sdk.metrics.internal.concurrent.AdderUtil;
import io.opentelemetry.sdk.metrics.internal.concurrent.LongAdder;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * A {@link FixedSizeExemplarReservoir} which uses an un-unweighted/naive algorithm for sampler
 * where the probability of sampling decrease as the number of observations continue.
 *
 * <p>When measurements are collected via {@link
 * FixedSizeExemplarReservoir#collectAndReset(Attributes)}, the observation count is reset, making
 * the probability of samplings effectively 1.0.
 */
class RandomFixedSizeExemplarReservoir<T extends ExemplarData>
    extends FixedSizeExemplarReservoir<T> {

  private RandomFixedSizeExemplarReservoir(
      Clock clock,
      int size,
      Supplier<Random> randomSupplier,
      BiFunction<ReservoirCell, Attributes, T> mapAndResetCell) {
    super(clock, size, new RandomCellSelector(randomSupplier), mapAndResetCell);
  }

  static RandomFixedSizeExemplarReservoir<LongExemplarData> createLong(
      Clock clock, int size, Supplier<Random> randomSupplier) {
    return new RandomFixedSizeExemplarReservoir<>(
        clock, size, randomSupplier, ReservoirCell::getAndResetLong);
  }

  static RandomFixedSizeExemplarReservoir<DoubleExemplarData> createDouble(
      Clock clock, int size, Supplier<Random> randomSupplier) {
    return new RandomFixedSizeExemplarReservoir<>(
        clock, size, randomSupplier, ReservoirCell::getAndResetDouble);
  }

  static class RandomCellSelector implements ReservoirCellSelector {
    private final LongAdder numMeasurements = AdderUtil.createLongAdder();

    private final Supplier<Random> randomSupplier;

    private RandomCellSelector(Supplier<Random> randomSupplier) {
      this.randomSupplier = randomSupplier;
    }

    @Override
    public int reservoirCellIndexFor(
        ReservoirCell[] cells, long value, Attributes attributes, Context context) {
      return reservoirCellIndex(cells);
    }

    @Override
    public int reservoirCellIndexFor(
        ReservoirCell[] cells, double value, Attributes attributes, Context context) {
      return reservoirCellIndex(cells);
    }

    private int reservoirCellIndex(ReservoirCell[] cells) {
      int count = numMeasurements.intValue() + 1;
      int index = this.randomSupplier.get().nextInt(count > 0 ? count : 1);
      numMeasurements.increment();
      if (index < cells.length) {
        return index;
      }
      return -1;
    }

    @Override
    public void reset() {
      numMeasurements.reset();
    }
  }
}
