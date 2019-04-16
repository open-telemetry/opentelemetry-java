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

package openconsensus.opencensusshim.stats;

import com.google.auto.value.AutoValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.concurrent.Immutable;
import openconsensus.opencensusshim.common.Function;
import openconsensus.opencensusshim.common.Functions;
import openconsensus.opencensusshim.common.Timestamp;
import openconsensus.opencensusshim.stats.Aggregation.Count;
import openconsensus.opencensusshim.stats.Aggregation.Distribution;
import openconsensus.opencensusshim.stats.Aggregation.LastValue;
import openconsensus.opencensusshim.stats.Aggregation.Sum;
import openconsensus.opencensusshim.stats.AggregationData.CountData;
import openconsensus.opencensusshim.stats.AggregationData.DistributionData;
import openconsensus.opencensusshim.stats.AggregationData.LastValueDataDouble;
import openconsensus.opencensusshim.stats.AggregationData.LastValueDataLong;
import openconsensus.opencensusshim.stats.AggregationData.SumDataDouble;
import openconsensus.opencensusshim.stats.AggregationData.SumDataLong;
import openconsensus.opencensusshim.stats.Measure.MeasureDouble;
import openconsensus.opencensusshim.stats.Measure.MeasureLong;
import openconsensus.opencensusshim.tags.TagValue;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

/**
 * The aggregated data for a particular {@link View}.
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
@AutoValue.CopyAnnotations
public abstract class ViewData {

  // Prevents this class from being subclassed anywhere else.
  ViewData() {}

  /**
   * The {@link View} associated with this {@link ViewData}.
   *
   * @since 0.1.0
   */
  public abstract View getView();

  /**
   * The {@link AggregationData} grouped by combination of tag values, associated with this {@link
   * ViewData}.
   *
   * @since 0.1.0
   */
  public abstract Map<List</*@Nullable*/ TagValue>, AggregationData> getAggregationMap();

  /**
   * Returns the start {@code Timestamp} for a {@link ViewData}.
   *
   * @return the start {@code Timestamp}.
   * @since 0.1.0
   */
  public abstract Timestamp getStart();

  /**
   * Returns the end {@code Timestamp} for a {@link ViewData}.
   *
   * @return the end {@code Timestamp}.
   * @since 0.1.0
   */
  public abstract Timestamp getEnd();

  /**
   * Constructs a new {@link ViewData}.
   *
   * @param view the {@link View} associated with this {@link ViewData}.
   * @param map the mapping from {@link TagValue} list to {@link AggregationData}.
   * @param start the start {@link Timestamp} for this {@link ViewData}.
   * @param end the end {@link Timestamp} for this {@link ViewData}.
   * @return a {@code ViewData}.
   * @throws IllegalArgumentException if the types of {@code Aggregation} and {@code
   *     AggregationData} don't match.
   * @since 0.1.0
   */
  public static ViewData create(
      View view,
      Map<? extends List</*@Nullable*/ TagValue>, ? extends AggregationData> map,
      Timestamp start,
      Timestamp end) {
    Map<List</*@Nullable*/ TagValue>, AggregationData> deepCopy =
        new HashMap<List</*@Nullable*/ TagValue>, AggregationData>();
    for (Entry<? extends List</*@Nullable*/ TagValue>, ? extends AggregationData> entry :
        map.entrySet()) {
      checkAggregation(view.getAggregation(), entry.getValue(), view.getMeasure());
      deepCopy.put(
          Collections.unmodifiableList(new ArrayList</*@Nullable*/ TagValue>(entry.getKey())),
          entry.getValue());
    }
    return createInternal(view, Collections.unmodifiableMap(deepCopy), start, end);
  }

  // Suppresses a nullness warning about calls to the AutoValue_ViewData constructor. The generated
  // constructor does not have the @Nullable annotation on TagValue.
  private static ViewData createInternal(
      View view,
      Map<List</*@Nullable*/ TagValue>, AggregationData> aggregationMap,
      Timestamp start,
      Timestamp end) {
    @SuppressWarnings("nullness")
    Map<List<TagValue>, AggregationData> map = aggregationMap;
    return new AutoValue_ViewData(view, map, start, end);
  }

  private static void checkAggregation(
      final Aggregation aggregation, final AggregationData aggregationData, final Measure measure) {
    aggregation.match(
        new Function<Sum, Void>() {
          @Override
          public Void apply(Sum arg) {
            measure.match(
                new Function<MeasureDouble, Void>() {
                  @Override
                  public Void apply(MeasureDouble arg) {
                    throwIfAggregationMismatch(
                        aggregationData instanceof SumDataDouble, aggregation, aggregationData);
                    return null;
                  }
                },
                new Function<MeasureLong, Void>() {
                  @Override
                  public Void apply(MeasureLong arg) {
                    throwIfAggregationMismatch(
                        aggregationData instanceof SumDataLong, aggregation, aggregationData);
                    return null;
                  }
                },
                Functions.</*@Nullable*/ Void>throwAssertionError());
            return null;
          }
        },
        new Function<Count, Void>() {
          @Override
          public Void apply(Count arg) {
            throwIfAggregationMismatch(
                aggregationData instanceof CountData, aggregation, aggregationData);
            return null;
          }
        },
        new Function<Distribution, Void>() {
          @Override
          public Void apply(Distribution arg) {
            throwIfAggregationMismatch(
                aggregationData instanceof DistributionData, aggregation, aggregationData);
            return null;
          }
        },
        new Function<LastValue, Void>() {
          @Override
          public Void apply(LastValue arg) {
            measure.match(
                new Function<MeasureDouble, Void>() {
                  @Override
                  public Void apply(MeasureDouble arg) {
                    throwIfAggregationMismatch(
                        aggregationData instanceof LastValueDataDouble,
                        aggregation,
                        aggregationData);
                    return null;
                  }
                },
                new Function<MeasureLong, Void>() {
                  @Override
                  public Void apply(MeasureLong arg) {
                    throwIfAggregationMismatch(
                        aggregationData instanceof LastValueDataLong, aggregation, aggregationData);
                    return null;
                  }
                },
                Functions.</*@Nullable*/ Void>throwAssertionError());
            return null;
          }
        },
        new Function<Aggregation, Void>() {
          @Override
          public Void apply(Aggregation arg) {
            throw new AssertionError();
          }
        });
  }

  private static void throwIfAggregationMismatch(
      boolean isValid, Aggregation aggregation, AggregationData aggregationData) {
    if (!isValid) {
      throw new IllegalArgumentException(
          createErrorMessageForAggregation(aggregation, aggregationData));
    }
  }

  private static String createErrorMessageForAggregation(
      Aggregation aggregation, AggregationData aggregationData) {
    return "Aggregation and AggregationData types mismatch. "
        + "Aggregation: "
        + aggregation.getClass().getSimpleName()
        + " AggregationData: "
        + aggregationData.getClass().getSimpleName();
  }
}
