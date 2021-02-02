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
 *   private static final DoubleUpDownCounter upDownCounter =
 *       meter.
 *           .doubleUpDownCounterBuilder("resource_usage")
 *           .setDescription("Current resource usage")
 *           .setUnit("1")
 *           .build();
 *
 *   // It is recommended that the API user keep references to a Bound Counters.
 *   private static final BoundDoubleUpDownCounter someWorkBound =
 *       upDownCounter.bind("work_name", "some_work");
 *
 *   void doSomeWork() {
 *      someWorkBound.add(10.2);  // Resources needed for this task.
 *      // Your code here.
 *      someWorkBound.add(-10.0);
 *   }
 * }
 * }</pre>
 */
@ThreadSafe
public interface DoubleUpDownCounter extends SynchronousInstrument<BoundDoubleUpDownCounter> {

  /**
   * Adds the given {@code increment} to the current value.
   *
   * <p>The value added is associated with the current {@code Context} and provided set of labels.
   *
   * @param increment the value to add.
   * @param labels the labels to be associated to this recording.
   */
  void add(double increment, Labels labels);

  /**
   * Adds the given {@code increment} to the current value.
   *
   * <p>The value added is associated with the current {@code Context} and empty labels.
   *
   * @param increment the value to add.
   */
  void add(double increment);

  @Override
  BoundDoubleUpDownCounter bind(Labels labels);
}
