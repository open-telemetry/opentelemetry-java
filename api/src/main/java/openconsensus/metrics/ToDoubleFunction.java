/*
 * Copyright 2019, OpenConsensus Authors
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

package openconsensus.metrics;

/**
 * Represents a function that produces a double-valued result. See {@link MetricRegistry} for an
 * example of its use.
 *
 * <p>Note: This class is based on the java.util.ToDoubleFunction class added in Java 1.8. We cannot
 * use the Function from Java 1.8 because this library is Java 1.6 compatible.
 *
 * @since 0.1.0
 */
public interface ToDoubleFunction<T> {

  /**
   * Applies this function to the given argument.
   *
   * @param value the function argument.
   * @return the function result.
   */
  double applyAsDouble(T value);
}
