/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.exemplar;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.RandomHolder;
import java.util.concurrent.atomic.LongAdder;

/**
 * A Reservoir sampler with fixed size that stores the given number of exemplars.
 *
 * <p>This implementation uses a un-unweighted/naive algorithm for sampler where the probability of
 * sampling decrease as the number of observations continue. The collectAndReset method resets the
 * count of observations, making the probability of sampling effectively 1.0.
 *
 * <p>Additionally this implementation ONLY exports double valued exemplars.
 */
public class FixedSizeExemplarReservoir extends AbstractFixedSizeExemplarReservoir {
  private final RandomHolder randomHolder;
  private final LongAdder numMeasurements = new LongAdder();

  /**
   * Instantiates an exemplar reservoir of fixed size.
   *
   * @param clock The clock to use when annotating measurements with time.
   * @param size The number of exemplars to preserve.
   * @param randomHolder The random number generated to use for sampling.
   */
  public FixedSizeExemplarReservoir(Clock clock, int size, RandomHolder randomHolder) {
    super(clock, size);
    this.randomHolder = randomHolder;
  }

  @Override
  protected int bucketFor(double value, Attributes attributes, Context context) {
    // Purposefuly truncate here.
    int count = (int) numMeasurements.sum() + 1;
    int index = this.randomHolder.getRandom().nextInt(count > 0 ? count : 1);
    numMeasurements.increment();
    if (index < maxSize()) {
      return index;
    }
    return -1;
  }

  @Override
  protected void reset() {
    // Reset the count so exemplars are likely to be filled.
    numMeasurements.reset();
  }
}
