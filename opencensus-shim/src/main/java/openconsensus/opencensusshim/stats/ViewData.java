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
import openconsensus.opencensusshim.common.Duration;
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
@SuppressWarnings("deprecation")
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
   * Returns the {@link AggregationWindowData} associated with this {@link ViewData}.
   *
   * <p>{@link AggregationWindowData} is deprecated since 0.13, please avoid using this method. Use
   * {@link #getStart()} and {@link #getEnd()} instead.
   *
   * @return the {@code AggregationWindowData}.
   * @since 0.1.0
   * @deprecated in favor of {@link #getStart()} and {@link #getEnd()}.
   */
  @Deprecated
  public abstract AggregationWindowData getWindowData();

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
   * @param windowData the {@link AggregationWindowData}.
   * @return a {@code ViewData}.
   * @throws IllegalArgumentException if the types of {@code Aggregation} and {@code
   *     AggregationData} don't match, or the types of {@code Window} and {@code WindowData} don't
   *     match.
   * @since 0.1.0
   * @deprecated in favor of {@link #create(View, Map, Timestamp, Timestamp)}.
   */
  @Deprecated
  public static ViewData create(
      final View view,
      Map<? extends List</*@Nullable*/ TagValue>, ? extends AggregationData> map,
      final AggregationWindowData windowData) {
    checkWindow(view.getWindow(), windowData);
    final Map<List</*@Nullable*/ TagValue>, AggregationData> deepCopy =
        new HashMap<List</*@Nullable*/ TagValue>, AggregationData>();
    for (Entry<? extends List</*@Nullable*/ TagValue>, ? extends AggregationData> entry :
        map.entrySet()) {
      checkAggregation(view.getAggregation(), entry.getValue(), view.getMeasure());
      deepCopy.put(
          Collections.unmodifiableList(new ArrayList</*@Nullable*/ TagValue>(entry.getKey())),
          entry.getValue());
    }
    return windowData.match(
        new Function<ViewData.AggregationWindowData.CumulativeData, ViewData>() {
          @Override
          public ViewData apply(ViewData.AggregationWindowData.CumulativeData arg) {
            return createInternal(
                view, Collections.unmodifiableMap(deepCopy), arg, arg.getStart(), arg.getEnd());
          }
        },
        new Function<ViewData.AggregationWindowData.IntervalData, ViewData>() {
          @Override
          public ViewData apply(ViewData.AggregationWindowData.IntervalData arg) {
            Duration duration = ((View.AggregationWindow.Interval) view.getWindow()).getDuration();
            return createInternal(
                view,
                Collections.unmodifiableMap(deepCopy),
                arg,
                arg.getEnd()
                    .addDuration(Duration.create(-duration.getSeconds(), -duration.getNanos())),
                arg.getEnd());
          }
        },
        Functions.<ViewData>throwAssertionError());
  }

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
    return createInternal(
        view,
        Collections.unmodifiableMap(deepCopy),
        AggregationWindowData.CumulativeData.create(start, end),
        start,
        end);
  }

  // Suppresses a nullness warning about calls to the AutoValue_ViewData constructor. The generated
  // constructor does not have the @Nullable annotation on TagValue.
  private static ViewData createInternal(
      View view,
      Map<List</*@Nullable*/ TagValue>, AggregationData> aggregationMap,
      AggregationWindowData window,
      Timestamp start,
      Timestamp end) {
    @SuppressWarnings("nullness")
    Map<List<TagValue>, AggregationData> map = aggregationMap;
    return new AutoValue_ViewData(view, map, window, start, end);
  }

  private static void checkWindow(
      View.AggregationWindow window, final AggregationWindowData windowData) {
    window.match(
        new Function<View.AggregationWindow.Cumulative, Void>() {
          @Override
          public Void apply(View.AggregationWindow.Cumulative arg) {
            throwIfWindowMismatch(
                windowData instanceof AggregationWindowData.CumulativeData, arg, windowData);
            return null;
          }
        },
        new Function<View.AggregationWindow.Interval, Void>() {
          @Override
          public Void apply(View.AggregationWindow.Interval arg) {
            throwIfWindowMismatch(
                windowData instanceof AggregationWindowData.IntervalData, arg, windowData);
            return null;
          }
        },
        Functions.</*@Nullable*/ Void>throwAssertionError());
  }

  private static void throwIfWindowMismatch(
      boolean isValid, View.AggregationWindow window, AggregationWindowData windowData) {
    if (!isValid) {
      throw new IllegalArgumentException(createErrorMessageForWindow(window, windowData));
    }
  }

  private static String createErrorMessageForWindow(
      View.AggregationWindow window, AggregationWindowData windowData) {
    return "AggregationWindow and AggregationWindowData types mismatch. "
        + "AggregationWindow: "
        + window.getClass().getSimpleName()
        + " AggregationWindowData: "
        + windowData.getClass().getSimpleName();
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
            // TODO(songya): remove this once Mean aggregation is completely removed. Before that
            // we need to continue supporting Mean, since it could still be used by users and some
            // deprecated RPC views.
            if (arg instanceof Aggregation.Mean) {
              throwIfAggregationMismatch(
                  aggregationData instanceof AggregationData.MeanData,
                  aggregation,
                  aggregationData);
              return null;
            }
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

  /**
   * The {@code AggregationWindowData} for a {@link ViewData}.
   *
   * @since 0.1.0
   * @deprecated since 0.13, please use start and end {@link Timestamp} instead.
   */
  @Deprecated
  @Immutable
  public abstract static class AggregationWindowData {

    private AggregationWindowData() {}

    /**
     * Applies the given match function to the underlying data type.
     *
     * @since 0.1.0
     */
    public abstract <T> T match(
        Function<? super CumulativeData, T> p0,
        Function<? super IntervalData, T> p1,
        Function<? super AggregationWindowData, T> defaultFunction);

    /**
     * Cumulative {@code AggregationWindowData}.
     *
     * @since 0.1.0
     * @deprecated since 0.13, please use start and end {@link Timestamp} instead.
     */
    @Deprecated
    @Immutable
    @AutoValue
    @AutoValue.CopyAnnotations
    public abstract static class CumulativeData extends AggregationWindowData {

      CumulativeData() {}

      /**
       * Returns the start {@code Timestamp} for a {@link CumulativeData}.
       *
       * @return the start {@code Timestamp}.
       * @since 0.1.0
       */
      public abstract Timestamp getStart();

      /**
       * Returns the end {@code Timestamp} for a {@link CumulativeData}.
       *
       * @return the end {@code Timestamp}.
       * @since 0.1.0
       */
      public abstract Timestamp getEnd();

      @Override
      public final <T> T match(
          Function<? super CumulativeData, T> p0,
          Function<? super IntervalData, T> p1,
          Function<? super AggregationWindowData, T> defaultFunction) {
        return p0.apply(this);
      }

      /**
       * Constructs a new {@link CumulativeData}.
       *
       * @since 0.1.0
       */
      public static CumulativeData create(Timestamp start, Timestamp end) {
        if (start.compareTo(end) > 0) {
          throw new IllegalArgumentException("Start time is later than end time.");
        }
        return new AutoValue_ViewData_AggregationWindowData_CumulativeData(start, end);
      }
    }

    /**
     * Interval {@code AggregationWindowData}.
     *
     * @since 0.1.0
     * @deprecated since 0.13, please use start and end {@link Timestamp} instead.
     */
    @Deprecated
    @Immutable
    @AutoValue
    @AutoValue.CopyAnnotations
    public abstract static class IntervalData extends AggregationWindowData {

      IntervalData() {}

      /**
       * Returns the end {@code Timestamp} for an {@link IntervalData}.
       *
       * @return the end {@code Timestamp}.
       * @since 0.1.0
       */
      public abstract Timestamp getEnd();

      @Override
      public final <T> T match(
          Function<? super CumulativeData, T> p0,
          Function<? super IntervalData, T> p1,
          Function<? super AggregationWindowData, T> defaultFunction) {
        return p1.apply(this);
      }

      /**
       * Constructs a new {@link IntervalData}.
       *
       * @since 0.1.0
       */
      public static IntervalData create(Timestamp end) {
        return new AutoValue_ViewData_AggregationWindowData_IntervalData(end);
      }
    }
  }
}
