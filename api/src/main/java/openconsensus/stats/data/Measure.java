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
import openconsensus.internal.StringUtils;
import openconsensus.internal.Utils;

/**
 * The definition of the {@link Measurement} that is taken by OpenCensus library.
 *
 * @since 0.1.0
 */
@Immutable
public abstract class Measure {
  static final int NAME_MAX_LENGTH = 255;
  private static final String ERROR_MESSAGE_INVALID_NAME =
      "Name should be a ASCII string with a length no greater than "
          + NAME_MAX_LENGTH
          + " characters.";

  /**
   * Name of measure, as a {@code String}. Should be a ASCII string with a length no greater than
   * 255 characters.
   *
   * <p>Suggested format for name: {@code <web_host>/<path>}.
   *
   * @since 0.1.0
   */
  public abstract String getName();

  /**
   * Detailed description of the measure, used in documentation.
   *
   * @since 0.1.0
   */
  public abstract String getDescription();

  /**
   * The units in which {@link Measure} values are measured.
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
   * @since 0.1.0
   */
  public abstract String getUnit();

  /**
   * Returns a {@code Type} corresponding to the underlying value of this {@code Measure}.
   *
   * @return the {@code Type} for the value of this {@code Measure}.
   * @since 0.1.0
   */
  public abstract Type getType();

  // Prevents this class from being subclassed anywhere else.
  private Measure() {}

  /**
   * {@link Measure} with {@code Double} typed values.
   *
   * @since 0.1.0
   */
  @Immutable
  @AutoValue
  public abstract static class MeasureDouble extends Measure {

    MeasureDouble() {}

    /**
     * Constructs a new {@link MeasureDouble}.
     *
     * @param name name of {@code Measure}. Suggested format: {@code <web_host>/<path>}.
     * @param description description of {@code Measure}.
     * @param unit unit of {@code Measure}.
     * @return a {@code MeasureDouble}.
     * @since 0.1.0
     */
    public static MeasureDouble create(String name, String description, String unit) {
      Utils.checkArgument(
          StringUtils.isPrintableString(name) && name.length() <= NAME_MAX_LENGTH,
          ERROR_MESSAGE_INVALID_NAME);
      return new AutoValue_Measure_MeasureDouble(name, description, unit, Type.DOUBLE);
    }

    @Override
    public abstract String getName();

    @Override
    public abstract String getDescription();

    @Override
    public abstract String getUnit();

    @Override
    public abstract Type getType();
  }

  /**
   * {@link Measure} with {@code Long} typed values.
   *
   * @since 0.1.0
   */
  @Immutable
  @AutoValue
  public abstract static class MeasureLong extends Measure {

    MeasureLong() {}

    /**
     * Constructs a new {@link MeasureLong}.
     *
     * @param name name of {@code Measure}. Suggested format: {@code <web_host>/<path>}.
     * @param description description of {@code Measure}.
     * @param unit unit of {@code Measure}.
     * @return a {@code MeasureLong}.
     * @since 0.1.0
     */
    public static MeasureLong create(String name, String description, String unit) {
      Utils.checkArgument(
          StringUtils.isPrintableString(name) && name.length() <= NAME_MAX_LENGTH,
          ERROR_MESSAGE_INVALID_NAME);
      return new AutoValue_Measure_MeasureLong(name, description, unit, Type.LONG);
    }

    @Override
    public abstract String getName();

    @Override
    public abstract String getDescription();

    @Override
    public abstract String getUnit();

    @Override
    public abstract Type getType();
  }

  /**
   * An enum that represents all the possible value types for a {@code Measure} or a {@code
   * Measurement}.
   *
   * @since 0.1.0
   */
  public enum Type {
    LONG,
    DOUBLE
  }
}
