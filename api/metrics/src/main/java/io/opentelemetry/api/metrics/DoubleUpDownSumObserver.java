/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import java.util.function.Consumer;
import javax.annotation.concurrent.ThreadSafe;

/**
 * UpDownSumObserver is the asynchronous instrument corresponding to UpDownCounter, used to capture
 * a non-monotonic count with Observe(sum).
 *
 * <p>"Sum" appears in the name to remind that it is used to capture sums directly. Use a
 * UpDownSumObserver to capture any value that starts at zero and rises or falls throughout the
 * process lifetime.
 *
 * <p>A {@code UpDownSumObserver} is a good choice in situations where a measurement is expensive to
 * compute, such that it would be wasteful to compute on every request.
 *
 * <p>Example:
 *
 * <pre>{@code
 * class YourClass {
 *
 *   private static final Meter meter = OpenTelemetry.getMeterProvider().get("my_library_name");
 *   private static final DoubleUpDownSumObserver memoryObserver =
 *       meter.
 *           .doubleUpDownSumObserverBuilder("memory_usage")
 *           .setDescription("System memory usage")
 *           .setUnit("by")
 *           .build();
 *
 *   void init() {
 *     memoryObserver.setUpdater(
 *         new DoubleUpDownSumObserver.Callback<DoubleResult>() {
 *          {@literal @}Override
 *           public void update(DoubleResult result) {
 *             // Get system memory usage
 *             result.observe(memoryUsed, "state", "used");
 *             result.observe(memoryFree, "state", "free");
 *           }
 *         });
 *   }
 * }
 * }</pre>
 */
@ThreadSafe
public interface DoubleUpDownSumObserver extends AsynchronousInstrument {

  /** Builder class for {@link DoubleUpDownSumObserver}. */
  interface Builder extends AsynchronousInstrument.Builder<DoubleResult> {
    @Override
    Builder setDescription(String description);

    @Override
    Builder setUnit(String unit);

    @Override
    Builder setUpdater(Consumer<DoubleResult> updater);

    @Override
    DoubleUpDownSumObserver build();
  }
}
