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
import openconsensus.common.Function;
import openconsensus.internal.Utils;

/**
 * {@link Aggregation} is the process of combining a certain set of {@code MeasureValue}s for a
 * given {@code Measure} into an {@link AggregationData}.
 *
 * <p>{@link Aggregation} currently supports 4 types of basic aggregation:
 *
 * <ul>
 *   <li>Sum
 *   <li>Count
 *   <li>Distribution
 *   <li>LastValue
 * </ul>
 *
 * <p>When creating a {@link View}, one {@link Aggregation} needs to be specified as how to
 * aggregate {@code MeasureValue}s.
 *
 * @since 0.1.0
 */
@Immutable
public abstract class Aggregation {

  private Aggregation() {}

  /**
   * Applies the given match function to the underlying data type.
   *
   * @since 0.1.0
   */
  public abstract <T> T match(
      Function<? super Sum, T> p0,
      Function<? super Count, T> p1,
      Function<? super Distribution, T> p2,
      Function<? super LastValue, T> p3,
      Function<? super Aggregation, T> defaultFunction);

  /**
   * Calculate sum on aggregated {@code MeasureValue}s.
   *
   * @since 0.1.0
   */
  @Immutable
  @AutoValue
  public abstract static class Sum extends Aggregation {

    Sum() {}

    private static final Sum INSTANCE = new AutoValue_Aggregation_Sum();

    /**
     * Construct a {@code Sum}.
     *
     * @return a new {@code Sum}.
     * @since 0.1.0
     */
    public static Sum create() {
      return INSTANCE;
    }

    @Override
    public final <T> T match(
        Function<? super Sum, T> p0,
        Function<? super Count, T> p1,
        Function<? super Distribution, T> p2,
        Function<? super LastValue, T> p3,
        Function<? super Aggregation, T> defaultFunction) {
      return p0.apply(this);
    }
  }

  /**
   * Calculate count on aggregated {@code MeasureValue}s.
   *
   * @since 0.1.0
   */
  @Immutable
  @AutoValue
  public abstract static class Count extends Aggregation {

    Count() {}

    private static final Count INSTANCE = new AutoValue_Aggregation_Count();

    /**
     * Construct a {@code Count}.
     *
     * @return a new {@code Count}.
     * @since 0.1.0
     */
    public static Count create() {
      return INSTANCE;
    }

    @Override
    public final <T> T match(
        Function<? super Sum, T> p0,
        Function<? super Count, T> p1,
        Function<? super Distribution, T> p2,
        Function<? super LastValue, T> p3,
        Function<? super Aggregation, T> defaultFunction) {
      return p1.apply(this);
    }
  }

  /**
   * Calculate mean on aggregated {@code MeasureValue}s.
   *
   * @since 0.1.0
   * @deprecated since 0.13, use {@link Distribution} instead.
   */
  @Immutable
  @AutoValue
  @Deprecated
  @AutoValue.CopyAnnotations
  public abstract static class Mean extends Aggregation {

    Mean() {}

    private static final Mean INSTANCE = new AutoValue_Aggregation_Mean();

    /**
     * Construct a {@code Mean}.
     *
     * @return a new {@code Mean}.
     * @since 0.1.0
     */
    public static Mean create() {
      return INSTANCE;
    }

    @Override
    public final <T> T match(
        Function<? super Sum, T> p0,
        Function<? super Count, T> p1,
        Function<? super Distribution, T> p2,
        Function<? super LastValue, T> p3,
        Function<? super Aggregation, T> defaultFunction) {
      return defaultFunction.apply(this);
    }
  }

  /**
   * Calculate distribution stats on aggregated {@code MeasureValue}s. Distribution includes mean,
   * count, histogram, min, max and sum of squared deviations.
   *
   * @since 0.1.0
   */
  @Immutable
  @AutoValue
  public abstract static class Distribution extends Aggregation {

    Distribution() {}

    /**
     * Construct a {@code Distribution}.
     *
     * @return a new {@code Distribution}.
     * @since 0.1.0
     */
    public static Distribution create(BucketBoundaries bucketBoundaries) {
      Utils.checkNotNull(bucketBoundaries, "bucketBoundaries");
      return new AutoValue_Aggregation_Distribution(bucketBoundaries);
    }

    /**
     * Returns the {@code Distribution}'s bucket boundaries.
     *
     * @return the {@code Distribution}'s bucket boundaries.
     * @since 0.1.0
     */
    public abstract BucketBoundaries getBucketBoundaries();

    @Override
    public final <T> T match(
        Function<? super Sum, T> p0,
        Function<? super Count, T> p1,
        Function<? super Distribution, T> p2,
        Function<? super LastValue, T> p3,
        Function<? super Aggregation, T> defaultFunction) {
      return p2.apply(this);
    }
  }

  /**
   * Calculate the last value of aggregated {@code MeasureValue}s.
   *
   * @since 0.1.0
   */
  @Immutable
  @AutoValue
  public abstract static class LastValue extends Aggregation {

    LastValue() {}

    private static final LastValue INSTANCE = new AutoValue_Aggregation_LastValue();

    /**
     * Construct a {@code LastValue}.
     *
     * @return a new {@code LastValue}.
     * @since 0.1.0
     */
    public static LastValue create() {
      return INSTANCE;
    }

    @Override
    public final <T> T match(
        Function<? super Sum, T> p0,
        Function<? super Count, T> p1,
        Function<? super Distribution, T> p2,
        Function<? super LastValue, T> p3,
        Function<? super Aggregation, T> defaultFunction) {
      return p3.apply(this);
    }
  }
}
