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

package openconsensus.stats;

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;

/**
 * Immutable representation of a measurement.
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
public abstract class Measurement {

  /**
   * Extracts the measured {@link Measure}.
   *
   * @return the {@link Measure} if this measurement.
   * @since 0.1.0
   */
  public abstract Measure getMeasure();

  /**
   * Returns the value for the {@link Measurement}.
   *
   * @return the value.
   * @since 0.1.0
   */
  public abstract double getValue();

  // Prevents this class from being subclassed anywhere else.
  Measurement() {}

  /**
   * Constructs a new {@link Measurement}.
   *
   * @since 0.1.0
   */
  static Measurement create(Measure measure, double value) {
    return new AutoValue_Measurement(measure, value);
  }
}
