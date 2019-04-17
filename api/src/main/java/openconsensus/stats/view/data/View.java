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

package openconsensus.stats.view.data;

import com.google.auto.value.AutoValue;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import javax.annotation.concurrent.Immutable;
import openconsensus.internal.StringUtils;
import openconsensus.internal.Utils;
import openconsensus.stats.Measure;
import openconsensus.tags.data.TagKey;

/**
 * A View specifies an aggregation and a set of tag keys. The aggregation will be broken down by the
 * unique set of matching tag values for each measure.
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
@AutoValue.CopyAnnotations
public abstract class View {
  static final int NAME_MAX_LENGTH = 255;

  View() {}

  /**
   * Name of view. Must be unique.
   *
   * @return name of the view.
   * @since 0.1.0
   */
  public abstract Name getName();

  /**
   * More detailed description, for documentation purposes.
   *
   * @return description of the view.
   * @since 0.1.0
   */
  public abstract String getDescription();

  /**
   * Measure type of this view.
   *
   * @return measure type of this view.
   * @since 0.1.0
   */
  public abstract Measure getMeasure();

  /**
   * The {@link Aggregation} associated with this {@link View}.
   *
   * @return the {@link Aggregation} associated with this {@link View}.
   * @since 0.1.0
   */
  public abstract Aggregation getAggregation();

  /**
   * Columns (a.k.a Tag Keys) to match with the associated {@link Measure}.
   *
   * <p>{@link Measure} will be recorded in a "greedy" way. That is, every view aggregates every
   * measure. This is similar to doing a GROUPBY on view’s columns. Columns must be unique.
   *
   * @return columns (a.k.a Tag Keys) to match with the associated {@link Measure}.
   * @since 0.1.0
   */
  public abstract List<TagKey> getColumns();

  /**
   * Constructs a new {@link View}.
   *
   * @param name the {@link Name} of view. Must be unique.
   * @param description the description of view.
   * @param measure the {@link Measure} to be aggregated by this view.
   * @param aggregation the basic {@link Aggregation} that this view will support.
   * @param columns the {@link TagKey}s that this view will aggregate on. Columns should not contain
   *     duplicates.
   * @return a new {@link View}.
   * @since 0.1.0
   */
  public static View create(
      Name name,
      String description,
      Measure measure,
      Aggregation aggregation,
      List<TagKey> columns) {
    Utils.checkArgument(
        new HashSet<TagKey>(columns).size() == columns.size(), "Columns have duplicate.");
    return new AutoValue_View(
        name, description, measure, aggregation, Collections.unmodifiableList(columns));
  }

  /**
   * The name of a {@code View}.
   *
   * @since 0.1.0
   */
  // This type should be used as the key when associating data with Views.
  @Immutable
  @AutoValue
  public abstract static class Name {

    Name() {}

    /**
     * Returns the name as a {@code String}.
     *
     * @return the name as a {@code String}.
     * @since 0.1.0
     */
    public abstract String asString();

    /**
     * Creates a {@code View.Name} from a {@code String}. Should be a ASCII string with a length no
     * greater than 255 characters.
     *
     * <p>Suggested format for name: {@code <web_host>/<path>}.
     *
     * @param name the name {@code String}.
     * @return a {@code View.Name} with the given name {@code String}.
     * @since 0.1.0
     */
    public static Name create(String name) {
      Utils.checkArgument(
          StringUtils.isPrintableString(name) && name.length() <= NAME_MAX_LENGTH,
          "Name should be a ASCII string with a length no greater than 255 characters.");
      return new AutoValue_View_Name(name);
    }
  }
}
