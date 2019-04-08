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

package openconsensus.stats.data;

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;
import openconsensus.stats.data.Measure.MeasureDouble;
import openconsensus.stats.data.Measure.MeasureLong;

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
   * @since 0.1.0
   */
  public abstract Measure getMeasure();

  /**
   * Returns a {@code MeasureType} corresponding to the underlying value of this {@code
   * Measurement}.
   *
   * @return the {@code MeasureType} for the value of this {@code Measurement}.
   * @since 0.1.0
   */
  public abstract MeasureType getType();

  // Prevents this class from being subclassed anywhere else.
  private Measurement() {}

  /**
   * {@code Double} typed {@link Measurement}.
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
    public static MeasurementDouble create(MeasureDouble measure, double value) {
      return new AutoValue_Measurement_MeasurementDouble(measure, value, MeasureType.DOUBLE);
    }

    @Override
    public abstract MeasureDouble getMeasure();

    /**
     * Returns the value for the measure.
     *
     * @return the value for the measure.
     * @since 0.1.0
     */
    public abstract double getValue();

    @Override
    public abstract MeasureType getType();
  }

  /**
   * {@code Long} typed {@link Measurement}.
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
    public static MeasurementLong create(MeasureLong measure, long value) {
      return new AutoValue_Measurement_MeasurementLong(measure, value, MeasureType.LONG);
    }

    @Override
    public abstract MeasureLong getMeasure();

    /**
     * Returns the value for the measure.
     *
     * @return the value for the measure.
     * @since 0.1.0
     */
    public abstract long getValue();

    @Override
    public abstract MeasureType getType();
  }
}
