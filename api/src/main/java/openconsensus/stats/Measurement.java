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
 * Immutable representation of a Measurement.
 *
 * @since 0.1.0
 */
@Immutable
public abstract class Measurement {

  /**
   * Extracts the measured {@link Measure}.
   *
   * @return the {@link Measure} if this measurement.
   * @since 0.1.0
   */
  public abstract Measure getMeasure();

  /**
   * Returns the double value for the {@link Measurement}.
   *
   * <p>This method should only be called with {@link MeasurementDouble}.
   *
   * @return the double value.
   * @throws UnsupportedOperationException if the {@code Measure} type is not {@link
   *     Measure.Type#DOUBLE}.
   * @since 0.1.0
   */
  public double getDoubleValue() {
    throw new UnsupportedOperationException("This type can only return double data");
  }

  /**
   * Returns the long value for the {@link Measurement}.
   *
   * <p>This method should only be called with {@link MeasurementLong}.
   *
   * @return the long value.
   * @throws UnsupportedOperationException if the {@code Measure} type is not {@link
   *     Measure.Type#LONG}.
   * @since 0.1.0
   */
  public long getLongValue() {
    throw new UnsupportedOperationException("This type can only return long data");
  }

  // Prevents this class from being subclassed anywhere else.
  private Measurement() {}

  /**
   * {@code double} typed {@link Measurement}.
   *
   * @since 0.1.0
   */
  @Immutable
  @AutoValue
  public abstract static class MeasurementDouble extends Measurement {
    MeasurementDouble() {}

    /**
     * Constructs a new {@link MeasurementDouble}.
     *
     * @since 0.1.0
     */
    public static MeasurementDouble create(Measure measure, double value) {
      return new AutoValue_Measurement_MeasurementDouble(measure, value);
    }

    @Override
    public abstract Measure getMeasure();

    @Override
    public abstract double getDoubleValue();
  }

  /**
   * {@code long} typed {@link Measurement}.
   *
   * @since 0.1.0
   */
  @Immutable
  @AutoValue
  public abstract static class MeasurementLong extends Measurement {
    MeasurementLong() {}

    /**
     * Constructs a new {@link MeasurementLong}.
     *
     * @since 0.1.0
     */
    public static MeasurementLong create(Measure measure, long value) {
      return new AutoValue_Measurement_MeasurementLong(measure, value);
    }

    @Override
    public abstract Measure getMeasure();

    @Override
    public abstract long getLongValue();
  }
}
