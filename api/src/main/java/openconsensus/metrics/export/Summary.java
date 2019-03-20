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

package openconsensus.metrics.export;

import com.google.auto.value.AutoValue;
import openconsensus.common.ExperimentalApi;
import openconsensus.internal.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Implementation of the {@link Distribution} as a summary of observations.
 *
 * <p>This is not recommended, since it cannot be aggregated.
 *
 * @since 0.1.0
 */
@ExperimentalApi
@AutoValue
@Immutable
public abstract class Summary {
  Summary() {}

  /**
   * Creates a {@link Summary}.
   *
   * @param count the count of the population values.
   * @param sum the sum of the population values.
   * @param snapshot bucket boundaries of a histogram.
   * @return a {@code Summary} with the given values.
   * @since 0.1.0
   */
  public static Summary create(@Nullable Long count, @Nullable Double sum, Snapshot snapshot) {
    checkCountAndSum(count, sum);
    Utils.checkNotNull(snapshot, "snapshot");
    return new AutoValue_Summary(count, sum, snapshot);
  }

  /**
   * Returns the aggregated count. If not available returns {@code null}.
   *
   * @return the aggregated count.
   * @since 0.1.0
   */
  @Nullable
  public abstract Long getCount();

  /**
   * Returns the aggregated sum. If not available returns {@code null}.
   *
   * @return the aggregated sum.
   * @since 0.1.0
   */
  @Nullable
  public abstract Double getSum();

  /**
   * Returns the {@link Snapshot}.
   *
   * @return the {@code Snapshot}.
   * @since 0.1.0
   */
  public abstract Snapshot getSnapshot();

  /**
   * Represents the summary observation of the recorded events over a sliding time window.
   *
   * @since 0.1.0
   */
  @Immutable
  @AutoValue
  public abstract static class Snapshot {
    /**
     * Returns the number of values in this {@code Snapshot}. If not available returns {@code null}.
     *
     * @return the number of values in this {@code Snapshot}.
     * @since 0.1.0
     */
    @Nullable
    public abstract Long getCount();

    /**
     * Returns the sum of values in this {@code Snapshot}. If not available returns {@code null}.
     *
     * @return the sum of values in this {@code Snapshot}.
     * @since 0.1.0
     */
    @Nullable
    public abstract Double getSum();

    /**
     * Returns the list of {@code ValueAtPercentile}s in this {@code Snapshot}.
     *
     * @return the list of {@code ValueAtPercentile}s in this {@code Snapshot}.
     * @since 0.1.0
     */
    public abstract List<ValueAtPercentile> getValueAtPercentiles();

    /**
     * Creates a {@link Snapshot}.
     *
     * @param count the number of values in this {@code Snapshot}.
     * @param sum the number of values in this {@code Snapshot}.
     * @param valueAtPercentiles the list of {@code ValueAtPercentile}.
     * @return a {@code Snapshot} with the given values.
     * @since 0.1.0
     */
    public static Snapshot create(
        @Nullable Long count, @Nullable Double sum, List<ValueAtPercentile> valueAtPercentiles) {
      checkCountAndSum(count, sum);
      Utils.checkListElementNotNull(
          Utils.checkNotNull(valueAtPercentiles, "valueAtPercentiles"), "valueAtPercentile");
      return new AutoValue_Summary_Snapshot(
          count,
          sum,
          Collections.unmodifiableList(new ArrayList<ValueAtPercentile>(valueAtPercentiles)));
    }

    /**
     * Represents the value at a given percentile of a distribution.
     *
     * @since 0.1.0
     */
    @Immutable
    @AutoValue
    public abstract static class ValueAtPercentile {
      /**
       * Returns the percentile in this {@code ValueAtPercentile}.
       *
       * <p>Must be in the interval (0.0, 100.0].
       *
       * @return the percentile in this {@code ValueAtPercentile}.
       * @since 0.1.0
       */
      public abstract double getPercentile();

      /**
       * Returns the value in this {@code ValueAtPercentile}.
       *
       * @return the value in this {@code ValueAtPercentile}.
       * @since 0.1.0
       */
      public abstract double getValue();

      /**
       * Creates a {@link ValueAtPercentile}.
       *
       * @param percentile the percentile in this {@code ValueAtPercentile}.
       * @param value the value in this {@code ValueAtPercentile}.
       * @return a {@code ValueAtPercentile} with the given values.
       * @since 0.1.0
       */
      public static ValueAtPercentile create(double percentile, double value) {
        Utils.checkArgument(
            0 < percentile && percentile <= 100.0,
            "percentile must be in the interval (0.0, 100.0]");
        Utils.checkArgument(value >= 0, "value must be non-negative");
        return new AutoValue_Summary_Snapshot_ValueAtPercentile(percentile, value);
      }
    }
  }

  private static void checkCountAndSum(@Nullable Long count, @Nullable Double sum) {
    Utils.checkArgument(count == null || count >= 0, "count must be non-negative.");
    Utils.checkArgument(sum == null || sum >= 0, "sum must be non-negative.");
    if (count != null && count == 0) {
      Utils.checkArgument(sum == null || sum == 0, "sum must be 0 if count is 0.");
    }
  }
}
