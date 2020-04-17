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

import io.opentelemetry.metrics.LongObserver.ResultLongObserver;
import java.util.Map;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Observer to report pre-aggregated metrics with double value.
 *
 * <p>Example:
 *
 * <pre>{@code
 * class YourClass {
 *
 *   private static final Meter meter = OpenTelemetry.getMeterRegistry().get("my_library_name");
 *   private static final LongObserver observer =
 *       meter.
 *           .observerLongBuilder("doWork_latency")
 *           .setDescription("gRPC Latency")
 *           .setUnit("ms")
 *           .build();
 *
 *   void init() {
 *     observer.setCallback(
 *         new LongObserver.Callback<LongObserver.ResultLongObserver>() {
 *           final AtomicInteger count = new AtomicInteger(0);
 *          {@literal @}Override
 *           public void update(ResultLongObserver result) {
 *             result.observe(count.addAndGet(1), "my_label_key", "my_label_value");
 *           }
 *         });
 *   }
 * }
 * }</pre>
 *
 * @since 0.1.0
 */
@ThreadSafe
public interface LongObserver extends Observer<ResultLongObserver> {
  @Override
  void setCallback(Callback<ResultLongObserver> metricUpdater);

  /** Builder class for {@link LongObserver}. */
  interface Builder extends Observer.Builder {
    @Override
    Builder setDescription(String description);

    @Override
    Builder setUnit(String unit);

    @Override
    Builder setConstantLabels(Map<String, String> constantLabels);

    @Override
    Builder setMonotonic(boolean monotonic);

    @Override
    LongObserver build();
  }

  /** The result for the {@link io.opentelemetry.metrics.Observer.Callback}. */
  interface ResultLongObserver {
    void observe(long value, String... keyValueLabelPairs);
  }
}
