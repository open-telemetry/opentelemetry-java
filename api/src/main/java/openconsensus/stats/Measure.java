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
import openconsensus.internal.StringUtils;
import openconsensus.internal.Utils;

/**
 * The definition of the {@link Measurement} that is taken by OpenConsensus library.
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
public abstract class Measure {
  private static final int NAME_MAX_LENGTH = 255;
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
   * Returns a new {@link Measurement} for this {@code Measure}.
   *
   * @param value the corresponding value for the {@code Measurement}.
   * @return a new {@link Measurement} for this {@code Measure}.
   */
  public final Measurement createMeasurement(double value) {
    Utils.checkArgument(value >= 0.0, "Unsupported negative values.");
    return Measurement.create(this, value);
  }

  /**
   * Constructs a new {@link Measure}.
   *
   * @param name name of {@code Measure}. Suggested format: {@code <web_host>/<path>}.
   * @param description description of {@code Measure}.
   * @param unit unit of {@code Measure}.
   * @return a {@code Measure}.
   * @since 0.1.0
   */
  public static Measure create(String name, String description, String unit) {
    Utils.checkArgument(
        StringUtils.isPrintableString(name) && name.length() <= NAME_MAX_LENGTH,
        ERROR_MESSAGE_INVALID_NAME);
    return new AutoValue_Measure(name, description, unit);
  }

  protected Measure() {}
}
