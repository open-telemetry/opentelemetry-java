/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import java.util.function.Consumer;
import javax.annotation.concurrent.ThreadSafe;

/**
 * {@code ValueObserver} is the asynchronous instrument corresponding to ValueRecorder, used to
 * capture values that are treated as individual observations, recorded with the observe(value)
 * method.
 *
 * <p>A {@code ValueObserver} is a good choice in situations where a measurement is expensive to
 * compute, such that it would be wasteful to compute on every request.
 *
 * <p>Example:
 *
 * <pre>{@code
 * class YourClass {
 *
 *   private static final Meter meter = OpenTelemetry.getMeterProvider().get("my_library_name");
 *   private static final LongValueObserver cpuObserver =
 *       meter.
 *           .longValueObserverBuilder("cpu_fan_speed")
 *           .setDescription("System CPU fan speed")
 *           .setUnit("ms")
 *           .build();
 *
 *   void init() {
 *     cpuObserver.setUpdater(
 *         new LongValueObserver.Callback<LongResult>() {
 *          {@literal @}Override
 *           public void update(LongResult result) {
 *             // Get system cpu fan speed
 *             result.observe(cpuFanSpeed);
 *           }
 *         });
 *   }
 * }
 * }</pre>
 */
@ThreadSafe
public interface LongValueObserver extends AsynchronousInstrument {

  /** Builder class for {@link LongValueObserver}. */
  interface Builder extends AsynchronousInstrument.Builder<LongResult> {
    @Override
    Builder setDescription(String description);

    @Override
    Builder setUnit(String unit);

    @Override
    Builder setUpdater(Consumer<LongResult> updater);

    @Override
    LongValueObserver build();
  }
}
