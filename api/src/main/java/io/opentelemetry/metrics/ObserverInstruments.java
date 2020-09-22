/*
 * Copyright 2020, OpenTelemetry Authors
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

public interface ObserverInstruments {
  /**
   * Returns a new builder for a {@link DoubleSumObserver}.
   *
   * @param name the name of the instrument. Should be a ASCII string with a length no greater than
   *     255 characters.
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
   * @param name the name of the instrument. Should be a ASCII string with a length no greater than
   *     255 characters.
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
   * @param name the name of the instrument. Should be a ASCII string with a length no greater than
   *     255 characters.
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
   * @param name the name of the instrument. Should be a ASCII string with a length no greater than
   *     255 characters.
   * @return a new builder for a {@code LongUpDownSumObserver}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  LongUpDownSumObserver.Builder longUpDownSumObserverBuilder(String name);

  /**
   * Returns a new builder for a {@link DoubleValueObserver}.
   *
   * @param name the name of the instrument. Should be a ASCII string with a length no greater than
   *     255 characters.
   * @return a new builder for a {@code DoubleValueObserver}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  DoubleValueObserver.Builder doubleValueObserverBuilder(String name);

  /**
   * Returns a new builder for a {@link LongValueObserver}.
   *
   * @param name the name of the instrument. Should be a ASCII string with a length no greater than
   *     255 characters.
   * @return a new builder for a {@code LongValueObserver}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  LongValueObserver.Builder longValueObserverBuilder(String name);
}
