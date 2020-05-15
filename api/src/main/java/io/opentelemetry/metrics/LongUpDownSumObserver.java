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

import io.opentelemetry.metrics.LongUpDownSumObserver.ResultLongUpDownSumObserver;
import java.util.Map;
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
 *   private static final Meter meter = OpenTelemetry.getMeterRegistry().get("my_library_name");
 *   private static final LongUpDownSumObserver memoryObserver =
 *       meter.
 *           .longUpDownSumObserverBuilder("memory_usage")
 *           .setDescription("System memory usage")
 *           .setUnit("by")
 *           .build();
 *
 *   void init() {
 *     memoryObserver.setCallback(
 *         new LongSumObserver.Callback<LongObserver.ResultLongObserver>() {
 *          {@literal @}Override
 *           public void update(ResultLongObserver result) {
 *             // Get system memory usage
 *             result.observe(memoryUsed, "state", "used");
 *             result.observe(memoryFree, "state", "free");
 *           }
 *         });
 *   }
 * }
 * }</pre>
 *
 * @since 0.1.0
 */
@ThreadSafe
public interface LongUpDownSumObserver extends AsynchronousInstrument<ResultLongUpDownSumObserver> {
  @Override
  void setCallback(Callback<ResultLongUpDownSumObserver> metricUpdater);

  /** Builder class for {@link LongUpDownSumObserver}. */
  interface Builder extends AsynchronousInstrument.Builder {
    @Override
    Builder setDescription(String description);

    @Override
    Builder setUnit(String unit);

    @Override
    Builder setConstantLabels(Map<String, String> constantLabels);

    @Override
    LongUpDownSumObserver build();
  }

  /** The result for the {@link Callback}. */
  interface ResultLongUpDownSumObserver {
    void observe(long sum, String... keyValueLabelPairs);
  }
}
