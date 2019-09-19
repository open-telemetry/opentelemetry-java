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

import javax.annotation.concurrent.ThreadSafe;

/**
 * Observer to report pre-aggregated metrics with double value.
 *
 * <p>Example:
 *
 * <pre><code>
 * class YourClass {
 *
 *   private static final Meter meter = OpenTelemetry.getMeter();
 *   private static final ObserverLong observer =
 *       meter.
 *           .observerLongBuilder("doWork_latency")
 *           .setDescription("gRPC Latency")
 *           .setUnit("ms")
 *           .build();
 *
 *   void init() {
 *     observer.setCallback(
 *         new ObserverLong.Callback<Result>() {
 *           final AtomicInteger count = new AtomicInteger(0);
 *          {@literal @}Override
 *           public void update(Result result) {
 *             result.put(observer.getDefaultHandle(), count.addAndGet(1));
 *           }
 *         });
 *   }
 * }
 * </code></pre>
 *
 * @since 0.1.0
 */
@ThreadSafe
public interface ObserverLong extends Observer<ObserverLong.Result> {
  interface Result {
    void put(Handle handle, long value);
  }

  @Override
  void setCallback(Callback<Result> metricUpdater);

  /** Builder class for {@link ObserverLong}. */
  interface Builder extends Observer.Builder<ObserverLong.Builder, ObserverLong> {}
}
