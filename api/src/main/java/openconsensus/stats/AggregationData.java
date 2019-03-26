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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;
import openconsensus.common.Function;
import openconsensus.internal.Utils;
import openconsensus.metrics.data.Exemplar;

/**
 * {@link AggregationData} is the result of applying a given {@link Aggregation} to a set of {@code
 * MeasureValue}s.
 *
 * <p>{@link AggregationData} currently supports 6 types of basic aggregation values:
 *
 * <ul>
 *   <li>SumDataDouble
 *   <li>SumDataLong
 *   <li>CountData
 *   <li>DistributionData
 *   <li>LastValueDataDouble
 *   <li>LastValueDataLong
 * </ul>
 *
 * <p>{@link ViewData} will contain one {@link AggregationData}, corresponding to its {@link
 * Aggregation} definition in {@link View}.
 *
 * @since 0.1.0
 */
@Immutable
public abstract class AggregationData {

  private AggregationData() {}

  /**
   * Applies the given match function to the underlying data type.
   *
   * @since 0.1.0
   */
  public abstract <T> T match(
      Function<? super SumDataDouble, T> p0,
      Function<? super SumDataLong, T> p1,
      Function<? super CountData, T> p2,
      Function<? super DistributionData, T> p3,
      Function<? super LastValueDataDouble, T> p4,
      Function<? super LastValueDataLong, T> p5,
      Function<? super AggregationData, T> defaultFunction);

  /**
   * The sum value of aggregated {@code MeasureValueDouble}s.
   *
   * @since 0.1.0
   */
  @Immutable
  @AutoValue
  public abstract static class SumDataDouble extends AggregationData {

    SumDataDouble() {}

    /**
     * Creates a {@code SumDataDouble}.
     *
     * @param sum the aggregated sum.
     * @return a {@code SumDataDouble}.
     * @since 0.1.0
     */
    public static SumDataDouble create(double sum) {
      return new AutoValue_AggregationData_SumDataDouble(sum);
    }

    /**
     * Returns the aggregated sum.
     *
     * @return the aggregated sum.
     * @since 0.1.0
     */
    public abstract double getSum();

    @Override
    public final <T> T match(
        Function<? super SumDataDouble, T> p0,
        Function<? super SumDataLong, T> p1,
        Function<? super CountData, T> p2,
        Function<? super DistributionData, T> p3,
        Function<? super LastValueDataDouble, T> p4,
        Function<? super LastValueDataLong, T> p5,
        Function<? super AggregationData, T> defaultFunction) {
      return p0.apply(this);
    }
  }

  /**
   * The sum value of aggregated {@code MeasureValueLong}s.
   *
   * @since 0.1.0
   */
  @Immutable
  @AutoValue
  public abstract static class SumDataLong extends AggregationData {

    SumDataLong() {}

    /**
     * Creates a {@code SumDataLong}.
     *
     * @param sum the aggregated sum.
     * @return a {@code SumDataLong}.
     * @since 0.1.0
     */
    public static SumDataLong create(long sum) {
      return new AutoValue_AggregationData_SumDataLong(sum);
    }

    /**
     * Returns the aggregated sum.
     *
     * @return the aggregated sum.
     * @since 0.1.0
     */
    public abstract long getSum();

    @Override
    public final <T> T match(
        Function<? super SumDataDouble, T> p0,
        Function<? super SumDataLong, T> p1,
        Function<? super CountData, T> p2,
        Function<? super DistributionData, T> p3,
        Function<? super LastValueDataDouble, T> p4,
        Function<? super LastValueDataLong, T> p5,
        Function<? super AggregationData, T> defaultFunction) {
      return p1.apply(this);
    }
  }

  /**
   * The count value of aggregated {@code MeasureValue}s.
   *
   * @since 0.1.0
   */
  @Immutable
  @AutoValue
  public abstract static class CountData extends AggregationData {

    CountData() {}

    /**
     * Creates a {@code CountData}.
     *
     * @param count the aggregated count.
     * @return a {@code CountData}.
     * @since 0.1.0
     */
    public static CountData create(long count) {
      return new AutoValue_AggregationData_CountData(count);
    }

    /**
     * Returns the aggregated count.
     *
     * @return the aggregated count.
     * @since 0.1.0
     */
    public abstract long getCount();

    @Override
    public final <T> T match(
        Function<? super SumDataDouble, T> p0,
        Function<? super SumDataLong, T> p1,
        Function<? super CountData, T> p2,
        Function<? super DistributionData, T> p3,
        Function<? super LastValueDataDouble, T> p4,
        Function<? super LastValueDataLong, T> p5,
        Function<? super AggregationData, T> defaultFunction) {
      return p2.apply(this);
    }
  }

  /**
   * The mean value of aggregated {@code MeasureValue}s.
   *
   * @since 0.1.0
   * @deprecated since 0.13, use {@link DistributionData} instead.
   */
  @Immutable
  @AutoValue
  @Deprecated
  @AutoValue.CopyAnnotations
  public abstract static class MeanData extends AggregationData {

    MeanData() {}

    /**
     * Creates a {@code MeanData}.
     *
     * @param mean the aggregated mean.
     * @param count the aggregated count.
     * @return a {@code MeanData}.
     * @since 0.1.0
     */
    public static MeanData create(double mean, long count) {
      return new AutoValue_AggregationData_MeanData(mean, count);
    }

    /**
     * Returns the aggregated mean.
     *
     * @return the aggregated mean.
     * @since 0.1.0
     */
    public abstract double getMean();

    /**
     * Returns the aggregated count.
     *
     * @return the aggregated count.
     * @since 0.1.0
     */
    public abstract long getCount();

    @Override
    public final <T> T match(
        Function<? super SumDataDouble, T> p0,
        Function<? super SumDataLong, T> p1,
        Function<? super CountData, T> p2,
        Function<? super DistributionData, T> p3,
        Function<? super LastValueDataDouble, T> p4,
        Function<? super LastValueDataLong, T> p5,
        Function<? super AggregationData, T> defaultFunction) {
      return defaultFunction.apply(this);
    }
  }

  /**
   * The distribution stats of aggregated {@code MeasureValue}s. Distribution stats include mean,
   * count, histogram, min, max and sum of squared deviations.
   *
   * @since 0.1.0
   */
  @Immutable
  @AutoValue
  public abstract static class DistributionData extends AggregationData {

    DistributionData() {}

    /**
     * Creates a {@code DistributionData}.
     *
     * @param mean mean value.
     * @param count count value.
     * @param min min value.
     * @param max max value.
     * @param sumOfSquaredDeviations sum of squared deviations.
     * @param bucketCounts histogram bucket counts.
     * @param exemplars the exemplars associated with histogram buckets.
     * @return a {@code DistributionData}.
     * @since 0.1.0
     * @deprecated since 0.17. Use {@link #create(double, long, double, List, List)}
     */
    @Deprecated
    @SuppressWarnings("InconsistentOverloads")
    public static DistributionData create(
        double mean,
        long count,
        double min,
        double max,
        double sumOfSquaredDeviations,
        List<Long> bucketCounts,
        List<Exemplar> exemplars) {
      return create(mean, count, sumOfSquaredDeviations, bucketCounts, exemplars);
    }

    /**
     * Creates a {@code DistributionData}.
     *
     * @param mean mean value.
     * @param count count value.
     * @param sumOfSquaredDeviations sum of squared deviations.
     * @param bucketCounts histogram bucket counts.
     * @param exemplars the exemplars associated with histogram buckets.
     * @return a {@code DistributionData}.
     * @since 0.1.0
     */
    public static DistributionData create(
        double mean,
        long count,
        double sumOfSquaredDeviations,
        List<Long> bucketCounts,
        List<Exemplar> exemplars) {
      List<Long> bucketCountsCopy =
          Collections.unmodifiableList(
              new ArrayList<Long>(Utils.checkNotNull(bucketCounts, "bucketCounts")));
      for (Long bucketCount : bucketCountsCopy) {
        Utils.checkNotNull(bucketCount, "bucketCount");
      }

      Utils.checkNotNull(exemplars, "exemplars");
      for (Exemplar exemplar : exemplars) {
        Utils.checkNotNull(exemplar, "exemplar");
      }

      return new AutoValue_AggregationData_DistributionData(
          mean,
          count,
          sumOfSquaredDeviations,
          bucketCountsCopy,
          Collections.<Exemplar>unmodifiableList(new ArrayList<Exemplar>(exemplars)));
    }

    /**
     * Creates a {@code DistributionData}.
     *
     * @param mean mean value.
     * @param count count value.
     * @param min min value.
     * @param max max value.
     * @param sumOfSquaredDeviations sum of squared deviations.
     * @param bucketCounts histogram bucket counts.
     * @return a {@code DistributionData}.
     * @since 0.1.0
     * @deprecated since 0.17. Use {@link #create(double, long, double, List)}.
     */
    @Deprecated
    @SuppressWarnings("InconsistentOverloads")
    public static DistributionData create(
        double mean,
        long count,
        double min,
        double max,
        double sumOfSquaredDeviations,
        List<Long> bucketCounts) {
      return create(
          mean, count, sumOfSquaredDeviations, bucketCounts, Collections.<Exemplar>emptyList());
    }

    /**
     * Creates a {@code DistributionData}.
     *
     * @param mean mean value.
     * @param count count value.
     * @param sumOfSquaredDeviations sum of squared deviations.
     * @param bucketCounts histogram bucket counts.
     * @return a {@code DistributionData}.
     * @since 0.1.0
     */
    public static DistributionData create(
        double mean, long count, double sumOfSquaredDeviations, List<Long> bucketCounts) {
      return create(
          mean, count, sumOfSquaredDeviations, bucketCounts, Collections.<Exemplar>emptyList());
    }

    /**
     * Returns the aggregated mean.
     *
     * @return the aggregated mean.
     * @since 0.1.0
     */
    public abstract double getMean();

    /**
     * Returns the aggregated count.
     *
     * @return the aggregated count.
     * @since 0.1.0
     */
    public abstract long getCount();

    /**
     * Returns the minimum of the population values.
     *
     * @return the minimum of the population values.
     * @since 0.1.0
     * @deprecated since 0.17. Returns {@code 0}.
     */
    @Deprecated
    public double getMin() {
      return 0;
    }

    /**
     * Returns the maximum of the population values.
     *
     * @return the maximum of the population values.
     * @since 0.1.0
     * @deprecated since 0.17. Returns {@code 0}.
     */
    @Deprecated
    public double getMax() {
      return 0;
    }

    /**
     * Returns the aggregated sum of squared deviations.
     *
     * @return the aggregated sum of squared deviations.
     * @since 0.1.0
     */
    public abstract double getSumOfSquaredDeviations();

    /**
     * Returns the aggregated bucket counts. The returned list is immutable, trying to update it
     * will throw an {@code UnsupportedOperationException}.
     *
     * @return the aggregated bucket counts.
     * @since 0.1.0
     */
    public abstract List<Long> getBucketCounts();

    /**
     * Returns the {@link Exemplar}s associated with histogram buckets.
     *
     * @return the {@code Exemplar}s associated with histogram buckets.
     * @since 0.1.0
     */
    public abstract List<Exemplar> getExemplars();

    @Override
    public final <T> T match(
        Function<? super SumDataDouble, T> p0,
        Function<? super SumDataLong, T> p1,
        Function<? super CountData, T> p2,
        Function<? super DistributionData, T> p3,
        Function<? super LastValueDataDouble, T> p4,
        Function<? super LastValueDataLong, T> p5,
        Function<? super AggregationData, T> defaultFunction) {
      return p3.apply(this);
    }
  }

  /**
   * The last value of aggregated {@code MeasureValueDouble}s.
   *
   * @since 0.1.0
   */
  @Immutable
  @AutoValue
  public abstract static class LastValueDataDouble extends AggregationData {

    LastValueDataDouble() {}

    /**
     * Creates a {@code LastValueDataDouble}.
     *
     * @param lastValue the last value.
     * @return a {@code LastValueDataDouble}.
     * @since 0.1.0
     */
    public static LastValueDataDouble create(double lastValue) {
      return new AutoValue_AggregationData_LastValueDataDouble(lastValue);
    }

    /**
     * Returns the last value.
     *
     * @return the last value.
     * @since 0.1.0
     */
    public abstract double getLastValue();

    @Override
    public final <T> T match(
        Function<? super SumDataDouble, T> p0,
        Function<? super SumDataLong, T> p1,
        Function<? super CountData, T> p2,
        Function<? super DistributionData, T> p3,
        Function<? super LastValueDataDouble, T> p4,
        Function<? super LastValueDataLong, T> p5,
        Function<? super AggregationData, T> defaultFunction) {
      return p4.apply(this);
    }
  }

  /**
   * The last value of aggregated {@code MeasureValueLong}s.
   *
   * @since 0.1.0
   */
  @Immutable
  @AutoValue
  public abstract static class LastValueDataLong extends AggregationData {

    LastValueDataLong() {}

    /**
     * Creates a {@code LastValueDataLong}.
     *
     * @param lastValue the last value.
     * @return a {@code LastValueDataLong}.
     * @since 0.1.0
     */
    public static LastValueDataLong create(long lastValue) {
      return new AutoValue_AggregationData_LastValueDataLong(lastValue);
    }

    /**
     * Returns the last value.
     *
     * @return the last value.
     * @since 0.1.0
     */
    public abstract long getLastValue();

    @Override
    public final <T> T match(
        Function<? super SumDataDouble, T> p0,
        Function<? super SumDataLong, T> p1,
        Function<? super CountData, T> p2,
        Function<? super DistributionData, T> p3,
        Function<? super LastValueDataDouble, T> p4,
        Function<? super LastValueDataLong, T> p5,
        Function<? super AggregationData, T> defaultFunction) {
      return p5.apply(this);
    }
  }
}
