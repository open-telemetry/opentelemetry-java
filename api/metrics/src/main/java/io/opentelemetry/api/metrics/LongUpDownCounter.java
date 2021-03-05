/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import io.opentelemetry.api.metrics.common.Labels;
import javax.annotation.concurrent.ThreadSafe;

/**
 * UpDownCounter is a synchronous instrument and very similar to Counter except that Add(increment)
 * supports negative increments. This makes UpDownCounter not useful for computing a rate
 * aggregation. The default aggregation is `Sum`, only the sum is non-monotonic. It is generally
 * useful for capturing changes in an amount of resources used, or any quantity that rises and falls
 * during a request.
 *
 * <p>Example:
 *
 * <pre>{@code
 * class YourClass {
 *   private static final Meter meter = OpenTelemetry.getMeterProvider().get("my_library_name");
 *   private static final LongUpDownCounter upDownCounter =
 *       meter.
 *           .longUpDownCounterBuilder("active_tasks")
 *           .setDescription("Number of active tasks")
 *           .setUnit("1")
 *           .build();
 *
 *   // It is recommended that the API user keep a reference to a Bound Counter.
 *   private static final BoundLongUpDownCounter someWorkBound =
 *       upDownCounter.bind("work_name", "some_work");
 *
 *   void doSomeWork() {
 *      someWorkBound.add(1);
 *      // Your code here.
 *      someWorkBound.add(-1);
 *   }
 * }
 * }</pre>
 */
@ThreadSafe
public interface LongUpDownCounter extends SynchronousInstrument<BoundLongUpDownCounter> {

  /**
   * Adds the given {@code increment} to the current value.
   *
   * <p>The value added is associated with the current {@code Context} and provided set of labels.
   *
   * @param increment the value to add.
   * @param labels the set of labels to be associated to this recording.
   */
  void add(long increment, Labels labels);

  /**
   * Adds the given {@code increment} to the current value.
   *
   * <p>The value added is associated with the current {@code Context} and empty labels.
   *
   * @param increment the value to add.
   */
  void add(long increment);

  @Override
  BoundLongUpDownCounter bind(Labels labels);
}
