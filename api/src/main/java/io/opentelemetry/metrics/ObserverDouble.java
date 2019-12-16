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

import io.opentelemetry.metrics.ObserverDouble.BoundDoubleObserver;
import io.opentelemetry.metrics.ObserverDouble.ResultDoubleObserver;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Observer to report pre-aggregated metrics with double value.
 *
 * <p>Example:
 *
 * <pre>{@code
 * class YourClass {
 *
 *   private static final Meter meter = OpenTelemetry.getMeterFactory().get("my_library_name");
 *   private static final ObserverDouble observer =
 *       meter.
 *           .observerDoubleBuilder("doWork_latency")
 *           .setDescription("gRPC Latency")
 *           .setUnit("ms")
 *           .build();
 *
 *   void init() {
 *     observer.setCallback(
 *         new ObserverDouble.Callback<ObserverDouble.Result>() {
 *           final AtomicInteger count = new AtomicInteger(0);
 *          {@literal @}Override
 *           public void update(Result result) {
 *             result.put(observer.getDefaultBound(), 0.8 * count.addAndGet(1));
 *           }
 *         });
 *   }
 * }
 * }</pre>
 *
 * @since 0.1.0
 */
@ThreadSafe
public interface ObserverDouble extends Observer<ResultDoubleObserver, BoundDoubleObserver> {
  @Override
  BoundDoubleObserver bind(LabelSet labelSet);

  @Override
  BoundDoubleObserver getDefaultBound();

  @Override
  void unbind(BoundDoubleObserver bound);

  @Override
  void setCallback(Callback<ResultDoubleObserver> metricUpdater);

  /** Builder class for {@link ObserverDouble}. */
  interface Builder extends Observer.Builder<ObserverDouble.Builder, ObserverDouble> {}

  /**
   * A {@code Bound} for a {@code Observer}.
   *
   * @since 0.1.0
   */
  @ThreadSafe
  interface BoundDoubleObserver {}

  /** The result for the {@link io.opentelemetry.metrics.Observer.Callback}. */
  interface ResultDoubleObserver {
    void put(BoundDoubleObserver bound, double value);
  }
}
