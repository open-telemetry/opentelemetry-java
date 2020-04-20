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

import io.opentelemetry.metrics.DoubleObserver.ResultDoubleObserver;
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
 *   private static final DoubleObserver observer =
 *       meter.
 *           .observerDoubleBuilder("doWork_latency")
 *           .setDescription("gRPC Latency")
 *           .setUnit("ms")
 *           .build();
 *
 *   void init() {
 *     observer.setCallback(
 *         new DoubleObserver.Callback<DoubleObserver.ResultDoubleObserver>() {
 *           final AtomicInteger count = new AtomicInteger(0);
 *          {@literal @}Override
 *           public void update(Result result) {
 *             result.observe(0.8 * count.addAndGet(1), "labelKey", "labelValue");
 *           }
 *         });
 *   }
 * }
 * }</pre>
 *
 * @since 0.1.0
 */
@ThreadSafe
public interface DoubleObserver extends Observer<ResultDoubleObserver> {
  @Override
  void setCallback(Callback<ResultDoubleObserver> metricUpdater);

  /** Builder class for {@link DoubleObserver}. */
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
    DoubleObserver build();
  }

  /** The result for the {@link io.opentelemetry.metrics.Observer.Callback}. */
  interface ResultDoubleObserver {
    void observe(double value, String... keyValueLabelPairs);
  }
}
