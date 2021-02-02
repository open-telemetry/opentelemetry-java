/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import io.opentelemetry.api.metrics.common.Labels;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Counter is the most common synchronous instrument. This instrument supports an {@link #add(long,
 * Labels)}` function for reporting an increment, and is restricted to non-negative increments. The
 * default aggregation is `Sum`.
 *
 * <p>Example:
 *
 * <pre>{@code
 * class YourClass {
 *   private static final Meter meter = OpenTelemetry.getMeterProvider().get("my_library_name");
 *   private static final LongCounter counter =
 *       meter.
 *           .longCounterBuilder("processed_jobs")
 *           .setDescription("Processed jobs")
 *           .setUnit("1")
 *           .build();
 *
 *   // It is recommended that the API user keep a reference to a Bound Counter.
 *   private static final BoundLongCounter someWorkBound =
 *       counter.bind("work_name", "some_work");
 *
 *   void doSomeWork() {
 *      // Your code here.
 *      someWorkBound.add(10);
 *   }
 * }
 * }</pre>
 */
@ThreadSafe
public interface LongCounter extends SynchronousInstrument<BoundLongCounter> {

  /**
   * Adds the given {@code increment} to the current value. The values cannot be negative.
   *
   * <p>The value added is associated with the current {@code Context} and provided set of labels.
   *
   * @param increment the value to add.
   * @param labels the set of labels to be associated to this recording.
   */
  void add(long increment, Labels labels);

  /**
   * Adds the given {@code increment} to the current value. The values cannot be negative.
   *
   * <p>The value added is associated with the current {@code Context} and empty labels.
   *
   * @param increment the value to add.
   */
  void add(long increment);

  @Override
  BoundLongCounter bind(Labels labels);
}
