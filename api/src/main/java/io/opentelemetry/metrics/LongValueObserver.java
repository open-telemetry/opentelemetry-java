/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.metrics;

import io.opentelemetry.metrics.AsynchronousInstrument.LongResult;
import java.util.Map;
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
 *   private static final Meter meter = OpenTelemetry.getMeterRegistry().get("my_library_name");
 *   private static final LongValueObserver cpuObserver =
 *       meter.
 *           .longValueObserverBuilder("cpu_fan_speed")
 *           .setDescription("System CPU fan speed")
 *           .setUnit("ms")
 *           .build();
 *
 *   void init() {
 *     cpuObserver.setCallback(
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
 *
 * @since 0.5.0
 */
@ThreadSafe
public interface LongValueObserver extends AsynchronousInstrument<LongResult> {
  @Override
  void setCallback(Callback<LongResult> callback);

  /** Builder class for {@link LongValueObserver}. */
  interface Builder extends AsynchronousInstrument.Builder {
    @Override
    Builder setDescription(String description);

    @Override
    Builder setUnit(String unit);

    @Override
    Builder setConstantLabels(Map<String, String> constantLabels);

    @Override
    LongValueObserver build();
  }
}
