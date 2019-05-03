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

package io.opentelemetry.stats;

import javax.annotation.concurrent.ThreadSafe;

/**
 * The definition of a {@link Measurement} that is taken by OpenTelemetry library.
 *
 * @since 0.1.0
 */
@ThreadSafe
public interface Measure {
  /**
   * An enum that represents all the possible value types for a {@code Measure} or a {@code
   * Measurement}.
   *
   * @since 0.1.0
   */
  enum Type {
    LONG,
    DOUBLE
  }

  /**
   * Returns a new {@link Measurement} for this {@code Measure}.
   *
   * @param value the corresponding {@code double} value for the {@code Measurement}.
   * @return a new {@link Measurement} for this {@code Measure}.
   * @throws UnsupportedOperationException if the type is not {@link Measure.Type#DOUBLE}.
   */
  Measurement createDoubleMeasurement(double value);

  /**
   * Returns a new {@link Measurement} for this {@code Measure}.
   *
   * @param value the corresponding {@code long} value for the {@code Measurement}.
   * @return a new {@link Measurement} for this {@code Measure}.
   * @throws UnsupportedOperationException if the type is not {@link Measure.Type#LONG}.
   */
  Measurement createLongMeasurement(long value);

  /** Builder class for the {@link Measure}. */
  interface Builder {
    /**
     * Sets the detailed description of the measure, used in documentation.
     *
     * <p>Default description is {@code ""}.
     *
     * @param description the detailed description of the {@code Measure}.
     * @return this.
     * @since 0.1.0
     */
    Builder setDescription(String description);

    /**
     * Sets the units in which {@code Measure} values are measured.
     *
     * <p>The suggested grammar for a unit is as follows:
     *
     * <ul>
     *   <li>Expression = Component { "." Component } {"/" Component };
     *   <li>Component = [ PREFIX ] UNIT [ Annotation ] | Annotation | "1";
     *   <li>Annotation = "{" NAME "}" ;
     * </ul>
     *
     * <p>For example, string “MBy{transmitted}/ms” stands for megabytes per milliseconds, and the
     * annotation transmitted inside {} is just a comment of the unit.
     *
     * <p>Default unit is {@code "1"}.
     *
     * @param unit the units in which {@code Measure} values are measured.
     * @return this.
     * @since 0.1.0
     */
    Builder setUnit(String unit);

    /**
     * Sets the {@code Type} corresponding to the underlying value of this {@code Measure}.
     *
     * <p>Default {@code Type} is {@link Type#DOUBLE}.
     *
     * @param type the
     * @return this.
     * @since 0.1.0
     */
    Builder setType(Type type);

    /**
     * Builds and returns a {@code Measure} with the desired options.
     *
     * @return a {@code Measure} with the desired options.
     */
    Measure build();
  }
}
