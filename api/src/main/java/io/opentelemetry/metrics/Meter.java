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
   * Returns a new builder for a {@link DoubleMeasure}.
   *
   * @param name Name of measure, as a {@code String}. Should be a ASCII string with a length no
   *     greater than 255 characters.
   * @return a new builder for a {@code DoubleMeasure}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  DoubleMeasure.Builder doubleMeasureBuilder(String name);

  /**
   * Returns a new builder for a {@link LongMeasure}.
   *
   * @param name Name of measure, as a {@code String}. Should be a ASCII string with a length no
   *     greater than 255 characters.
   * @return a new builder for a {@code LongMeasure}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  LongMeasure.Builder longMeasureBuilder(String name);

  /**
   * Returns a new builder for a {@link DoubleObserver}.
   *
   * @param name Name of observer, as a {@code String}. Should be a ASCII string with a length no
   *     greater than 255 characters.
   * @return a new builder for a {@code DoubleObserver}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  DoubleObserver.Builder doubleObserverBuilder(String name);

  /**
   * Returns a new builder for a {@link LongObserver}.
   *
   * @param name Name of observer, as a {@code String}. Should be a ASCII string with a length no
   *     greater than 255 characters.
   * @return a new builder for a {@code LongObserver}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  LongObserver.Builder longObserverBuilder(String name);

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
