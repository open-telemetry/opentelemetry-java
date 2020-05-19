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
 * Meter is a simple, interface that allows users to record measurements (metrics).
 *
 * <p>There are two ways to record measurements:
 *
 * <ul>
 *   <li>Record raw measurements, and defer defining the aggregation and the labels for the exported
 *       Instrument. This should be used in libraries like gRPC to record measurements like
 *       "server_latency" or "received_bytes".
 *   <li>Record pre-defined aggregation data (or already aggregated data). This should be used to
 *       report cpu/memory usage, or simple metrics like "queue_length".
 * </ul>
 *
 * <p>TODO: Update comment.
 */
@ThreadSafe
public interface Meter {

  /**
   * Returns a builder for a {@link DoubleCounter}.
   *
   * @param name the name of the metric. Should be a ASCII string with a length no greater than 255
   *     characters.
   * @return a {@code DoubleCounter.Builder}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  DoubleCounter.Builder doubleCounterBuilder(String name);

  /**
   * Returns a builder for a {@link LongCounter}.
   *
   * @param name the name of the metric. Should be a ASCII string with a length no greater than 255
   *     characters.
   * @return a {@code LongCounter.Builder}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  LongCounter.Builder longCounterBuilder(String name);

  /**
   * Returns a builder for a {@link DoubleUpDownCounter}.
   *
   * @param name the name of the metric. Should be a ASCII string with a length no greater than 255
   *     characters.
   * @return a {@code DoubleCounter.Builder}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  DoubleUpDownCounter.Builder doubleUpDownCounterBuilder(String name);

  /**
   * Returns a builder for a {@link LongUpDownCounter}.
   *
   * @param name the name of the metric. Should be a ASCII string with a length no greater than 255
   *     characters.
   * @return a {@code LongCounter.Builder}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  LongUpDownCounter.Builder longUpDownCounterBuilder(String name);

  /**
   * Returns a new builder for a {@link DoubleValueRecorder}.
   *
   * @param name the name of the metric. Should be a ASCII string with a length no greater than 255
   *     characters.
   * @return a new builder for a {@code DoubleValueRecorder}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  DoubleValueRecorder.Builder doubleValueRecorderBuilder(String name);

  /**
   * Returns a new builder for a {@link LongValueRecorder}.
   *
   * @param name the name of the metric. Should be a ASCII string with a length no greater than 255
   *     characters.
   * @return a new builder for a {@code LongValueRecorder}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  LongValueRecorder.Builder longValueRecorderBuilder(String name);

  /**
   * Returns a new builder for a {@link DoubleSumObserver}.
   *
   * @param name the name of the metric. Should be a ASCII string with a length no greater than 255
   *     characters.
   * @return a new builder for a {@code DoubleSumObserver}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  DoubleSumObserver.Builder doubleSumObserverBuilder(String name);

  /**
   * Returns a new builder for a {@link LongSumObserver}.
   *
   * @param name the name of the metric. Should be a ASCII string with a length no greater than 255
   *     characters.
   * @return a new builder for a {@code LongSumObserver}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  LongSumObserver.Builder longSumObserverBuilder(String name);

  /**
   * Returns a new builder for a {@link DoubleUpDownSumObserver}.
   *
   * @param name the name of the metric. Should be a ASCII string with a length no greater than 255
   *     characters.
   * @return a new builder for a {@code DoubleUpDownObserver}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  DoubleUpDownSumObserver.Builder doubleUpDownSumObserverBuilder(String name);

  /**
   * Returns a new builder for a {@link LongUpDownSumObserver}.
   *
   * @param name the name of the metric. Should be a ASCII string with a length no greater than 255
   *     characters.
   * @return a new builder for a {@code LongUpDownSumObserver}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  LongUpDownSumObserver.Builder longUpDownSumObserverBuilder(String name);

  /**
   * Utility method that allows users to atomically record measurements to a set of Measures with a
   * common set of labels.
   *
   * @param keyValuePairs The set of labels to associate with this recorder and all it's recordings.
   * @return a {@code MeasureBatchRecorder} that can be use to atomically record a set of
   *     measurements associated with different Measures.
   * @since 0.1.0
   */
  BatchRecorder newBatchRecorder(String... keyValuePairs);
}
