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
import openconsensus.stats.Measure.MeasureDouble;
import openconsensus.stats.Measure.MeasureLong;

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
   * @since 0.1.0
   */
  public double getDoubleValue() {
    throw new UnsupportedOperationException(
        String.format("This type can only return %s data", getType().name()));
  }

  /**
   * Returns the long value for the {@link Measurement}.
   *
   * <p>This method should only be called with {@link MeasurementLong}.
   *
   * @return the long value.
   * @since 0.1.0
   */
  public long getLongValue() {
    throw new UnsupportedOperationException(
        String.format("This type can only return %s data", getType().name()));
  }

  /**
   * Returns a {@code Measure.Type} corresponding to the underlying value of this {@code
   * Measurement}.
   *
   * @return the {@code Measure.Type} for the value of this {@code Measurement}.
   * @since 0.1.0
   */
  public abstract Measure.Type getType();

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
      return new AutoValue_Measurement_MeasurementDouble(measure, value, Measure.Type.DOUBLE);
    }

    @Override
    public abstract MeasureDouble getMeasure();

    @Override
    public abstract double getDoubleValue();

    @Override
    public abstract Measure.Type getType();
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
      return new AutoValue_Measurement_MeasurementLong(measure, value, Measure.Type.LONG);
    }

    @Override
    public abstract MeasureLong getMeasure();

    @Override
    public abstract long getLongValue();

    @Override
    public abstract Measure.Type getType();
  }
}
