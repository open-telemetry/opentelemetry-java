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

package openconsensus.metrics.data;

import com.google.auto.value.AutoValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import openconsensus.common.ExperimentalApi;
import openconsensus.common.Function;
import openconsensus.common.Functions;
import openconsensus.internal.Utils;

/**
 * {@link Distribution} contains summary statistics for a population of values. It optionally
 * contains a histogram representing the distribution of those values across a set of buckets.
 *
 * @since 0.1.0
 */
@ExperimentalApi
@AutoValue
@Immutable
public abstract class Distribution {

  Distribution() {}

  /**
   * Creates a {@link Distribution}.
   *
   * @param count the count of the population values.
   * @param sum the sum of the population values.
   * @param sumOfSquaredDeviations the sum of squared deviations of the population values.
   * @param bucketOptions the bucket options used to create a histogram for the distribution.
   * @param buckets {@link Bucket}s of a histogram.
   * @return a {@code Distribution}.
   * @since 0.1.0
   */
  public static Distribution create(
      long count,
      double sum,
      double sumOfSquaredDeviations,
      BucketOptions bucketOptions,
      List<Bucket> buckets) {
    Utils.checkArgument(count >= 0, "count should be non-negative.");
    Utils.checkArgument(
        sumOfSquaredDeviations >= 0, "sum of squared deviations should be non-negative.");
    if (count == 0) {
      Utils.checkArgument(sum == 0, "sum should be 0 if count is 0.");
      Utils.checkArgument(
          sumOfSquaredDeviations == 0, "sum of squared deviations should be 0 if count is 0.");
    }
    Utils.checkNotNull(bucketOptions, "bucketOptions");
    List<Bucket> bucketsCopy =
        Collections.unmodifiableList(new ArrayList<Bucket>(Utils.checkNotNull(buckets, "buckets")));
    Utils.checkListElementNotNull(bucketsCopy, "bucket");
    return new AutoValue_Distribution(
        count, sum, sumOfSquaredDeviations, bucketOptions, bucketsCopy);
  }

  /**
   * Returns the aggregated count.
   *
   * @return the aggregated count.
   * @since 0.1.0
   */
  public abstract long getCount();

  /**
   * Returns the aggregated sum.
   *
   * @return the aggregated sum.
   * @since 0.1.0
   */
  public abstract double getSum();

  /**
   * Returns the aggregated sum of squared deviations.
   *
   * <p>The sum of squared deviations from the mean of the values in the population. For values x_i
   * this is:
   *
   * <p>Sum[i=1..n]((x_i - mean)^2)
   *
   * <p>If count is zero then this field must be zero.
   *
   * @return the aggregated sum of squared deviations.
   * @since 0.1.0
   */
  public abstract double getSumOfSquaredDeviations();

  /**
   * Returns bucket options used to create a histogram for the distribution.
   *
   * @return the {@code BucketOptions} associated with the {@code Distribution}, or {@code null} if
   *     there isn't one.
   * @since 0.1.0
   */
  @Nullable
  public abstract BucketOptions getBucketOptions();

  /**
   * Returns the aggregated histogram {@link Bucket}s.
   *
   * @return the aggregated histogram buckets.
   * @since 0.1.0
   */
  public abstract List<Bucket> getBuckets();

  /**
   * The bucket options used to create a histogram for the distribution.
   *
   * @since 0.1.0
   */
  @Immutable
  public abstract static class BucketOptions {

    private BucketOptions() {}

    /**
     * Returns a {@link ExplicitOptions}.
     *
     * <p>The bucket boundaries for that histogram are described by bucket_bounds. This defines
     * size(bucket_bounds) + 1 (= N) buckets. The boundaries for bucket index i are:
     *
     * <ul>
     *   <li>{@code [0, bucket_bounds[i]) for i == 0}
     *   <li>{@code [bucket_bounds[i-1], bucket_bounds[i]) for 0 < i < N-1}
     *   <li>{@code [bucket_bounds[i-1], +infinity) for i == N-1}
     * </ul>
     *
     * <p>If bucket_bounds has no elements (zero size), then there is no histogram associated with
     * the Distribution. If bucket_bounds has only one element, there are no finite buckets, and
     * that single element is the common boundary of the overflow and underflow buckets. The values
     * must be monotonically increasing.
     *
     * @param bucketBoundaries the bucket boundaries of a distribution (given explicitly). The
     *     values must be strictly increasing and should be positive values.
     * @return a {@code ExplicitOptions} {@code BucketOptions}.
     * @since 0.1.0
     */
    public static BucketOptions explicitOptions(List<Double> bucketBoundaries) {
      return ExplicitOptions.create(bucketBoundaries);
    }

    /**
     * Returns a {@code Type} corresponding to the underlying representation of this {@code BucketOptions}.
     *
     * @return a {@code Type} corresponding to the underlying representation of this {@code BucketOptions}.
     * @since 0.1.0
     */
    public abstract Type getType();

    /**
     * An enum that represents all the possible value types for a {@code BucketOptions}.
     *
     * @since 0.1.0
     */
    public enum Type {
      EXPLICIT_OPTIONS,
    }

    /** A Bucket with explicit bounds {@link BucketOptions}. */
    @AutoValue
    @Immutable
    public abstract static class ExplicitOptions extends BucketOptions {

      ExplicitOptions() {}

      /**
       * Creates a {@link ExplicitOptions}.
       *
       * @param bucketBoundaries the bucket boundaries of a distribution (given explicitly). The
       *     values must be strictly increasing and should be positive.
       * @return a {@code ExplicitOptions}.
       * @since 0.1.0
       */
      private static ExplicitOptions create(List<Double> bucketBoundaries) {
        Utils.checkNotNull(bucketBoundaries, "bucketBoundaries");
        List<Double> bucketBoundariesCopy =
            Collections.unmodifiableList(new ArrayList<Double>(bucketBoundaries));
        checkBucketBoundsAreSorted(bucketBoundariesCopy);
        return new AutoValue_Distribution_BucketOptions_ExplicitOptions(bucketBoundariesCopy);
      }

      private static void checkBucketBoundsAreSorted(List<Double> bucketBoundaries) {
        if (bucketBoundaries.size() >= 1) {
          double previous = Utils.checkNotNull(bucketBoundaries.get(0), "bucketBoundary");
          Utils.checkArgument(previous > 0, "bucket boundary should be > 0");
          for (int i = 1; i < bucketBoundaries.size(); i++) {
            double next = Utils.checkNotNull(bucketBoundaries.get(i), "bucketBoundary");
            Utils.checkArgument(previous < next, "bucket boundaries not sorted.");
            previous = next;
          }
        }
      }

      /**
       * Returns the bucket boundaries of this distribution.
       *
       * @return the bucket boundaries of this distribution.
       * @since 0.1.0
       */
      public abstract List<Double> getBucketBoundaries();

      @Override
      public final Type getType() {
        return Type.EXPLICIT_OPTIONS;
      }
    }
  }

  /**
   * The histogram bucket of the population values.
   *
   * @since 0.1.0
   */
  @AutoValue
  @Immutable
  public abstract static class Bucket {

    Bucket() {}

    /**
     * Creates a {@link Bucket}.
     *
     * @param count the number of values in each bucket of the histogram.
     * @return a {@code Bucket}.
     * @since 0.1.0
     */
    public static Bucket create(long count) {
      Utils.checkArgument(count >= 0, "bucket count should be non-negative.");
      return new AutoValue_Distribution_Bucket(count, null);
    }

    /**
     * Creates a {@link Bucket} with an {@link Exemplar}.
     *
     * @param count the number of values in each bucket of the histogram.
     * @param exemplar the {@code Exemplar} of this {@code Bucket}.
     * @return a {@code Bucket}.
     * @since 0.1.0
     */
    public static Bucket create(long count, Exemplar exemplar) {
      Utils.checkArgument(count >= 0, "bucket count should be non-negative.");
      Utils.checkNotNull(exemplar, "exemplar");
      return new AutoValue_Distribution_Bucket(count, exemplar);
    }

    /**
     * Returns the number of values in each bucket of the histogram.
     *
     * @return the number of values in each bucket of the histogram.
     * @since 0.1.0
     */
    public abstract long getCount();

    /**
     * Returns the {@link Exemplar} associated with the {@link Bucket}, or {@code null} if there
     * isn't one.
     *
     * @return the {@code Exemplar} associated with the {@code Bucket}, or {@code null} if there
     *     isn't one.
     * @since 0.1.0
     */
    @Nullable
    public abstract Exemplar getExemplar();
  }
}
