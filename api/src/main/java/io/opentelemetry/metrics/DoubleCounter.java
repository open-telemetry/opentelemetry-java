/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.metrics;

import io.opentelemetry.common.Labels;
import io.opentelemetry.metrics.DoubleCounter.BoundDoubleCounter;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Counter is the most common synchronous instrument. This instrument supports an {@link
 * #add(double, Labels)}` function for reporting an increment, and is restricted to non-negative
 * increments. The default aggregation is `Sum`.
 *
 * <p>Example:
 *
 * <pre>{@code
 * class YourClass {
 *   private static final Meter meter = OpenTelemetry.getMeterProvider().get("my_library_name");
 *   private static final DoubleCounter counter =
 *       meter.
 *           .doubleCounterBuilder("allocated_resources")
 *           .setDescription("Total allocated resources")
 *           .setUnit("1")
 *           .build();
 *
 *   // It is recommended that the API user keep references to a Bound Counters.
 *   private static final BoundDoubleCounter someWorkBound =
 *       counter.bind("work_name", "some_work");
 *
 *   void doSomeWork() {
 *      someWorkBound.add(10.2);  // Resources needed for this task.
 *      // Your code here.
 *   }
 * }
 * }</pre>
 *
 * @since 0.1.0
 */
@ThreadSafe
public interface DoubleCounter extends SynchronousInstrument<BoundDoubleCounter> {

  /**
   * Adds the given {@code increment} to the current value. The values cannot be negative.
   *
   * <p>The value added is associated with the current {@code Context} and provided set of labels.
   *
   * @param increment the value to add.
   * @param labels the labels to be associated to this recording.
   * @since 0.1.0
   */
  void add(double increment, Labels labels);

  /**
   * Adds the given {@code increment} to the current value. The values cannot be negative.
   *
   * <p>The value added is associated with the current {@code Context} and with empty labels.
   *
   * @param increment the value to add.
   * @since 0.8.0
   */
  void add(double increment);

  @Override
  BoundDoubleCounter bind(Labels labels);

  /**
   * A {@code Bound Instrument} for a {@link DoubleCounter}.
   *
   * @since 0.1.0
   */
  @ThreadSafe
  interface BoundDoubleCounter extends SynchronousInstrument.BoundInstrument {
    /**
     * Adds the given {@code increment} to the current value. The values cannot be negative.
     *
     * <p>The value added is associated with the current {@code Context}.
     *
     * @param increment the value to add.
     * @since 0.1.0
     */
    void add(double increment);

    @Override
    void unbind();
  }

  /** Builder class for {@link DoubleCounter}. */
  interface Builder extends SynchronousInstrument.Builder {
    @Override
    Builder setDescription(String description);

    @Override
    Builder setUnit(String unit);

    @Override
    DoubleCounter build();
  }
}
