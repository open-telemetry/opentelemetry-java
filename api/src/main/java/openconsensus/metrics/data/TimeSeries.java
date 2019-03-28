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
import openconsensus.common.Timestamp;
import openconsensus.internal.Utils;

/**
 * A collection of data points that describes the time-varying values of a {@code Metric}.
 *
 * @since 0.1.0
 */
@ExperimentalApi
@Immutable
@AutoValue
public abstract class TimeSeries {

  TimeSeries() {}

  /**
   * Creates a {@link TimeSeries}.
   *
   * @param labelValues the {@code LabelValue}s that uniquely identify this {@code TimeSeries}.
   * @param points the data {@code Point}s of this {@code TimeSeries}.
   * @param startTimestamp the start {@code Timestamp} of this {@code TimeSeries}. Must be non-null
   *     for cumulative {@code Point}s.
   * @return a {@code TimeSeries}.
   * @since 0.1.0
   */
  public static TimeSeries create(
      List<LabelValue> labelValues, List<Point> points, @Nullable Timestamp startTimestamp) {
    Utils.checkListElementNotNull(Utils.checkNotNull(points, "points"), "point");
    return createInternal(
        labelValues, Collections.unmodifiableList(new ArrayList<Point>(points)), startTimestamp);
  }

  /**
   * Creates a {@link TimeSeries} with empty(or no) points.
   *
   * @param labelValues the {@code LabelValue}s that uniquely identify this {@code TimeSeries}.
   * @return a {@code TimeSeries}.
   * @since 0.1.0
   */
  public static TimeSeries create(List<LabelValue> labelValues) {
    return createInternal(labelValues, Collections.<Point>emptyList(), null);
  }

  /**
   * Creates a {@link TimeSeries}.
   *
   * @param labelValues the {@code LabelValue}s that uniquely identify this {@code TimeSeries}.
   * @param point the single data {@code Point} of this {@code TimeSeries}.
   * @param startTimestamp the start {@code Timestamp} of this {@code TimeSeries}. Must be non-null
   *     for cumulative {@code Point}s.
   * @return a {@code TimeSeries}.
   * @since 0.1.0
   */
  public static TimeSeries createWithOnePoint(
      List<LabelValue> labelValues, Point point, @Nullable Timestamp startTimestamp) {
    Utils.checkNotNull(point, "point");
    return createInternal(labelValues, Collections.singletonList(point), startTimestamp);
  }

  /**
   * Sets the {@code Point} of the {@link TimeSeries}.
   *
   * @param point the single data {@code Point} of this {@code TimeSeries}.
   * @return a {@code TimeSeries}.
   * @since 0.1.0
   */
  public TimeSeries setPoint(Point point) {
    Utils.checkNotNull(point, "point");
    return new AutoValue_TimeSeries(getLabelValues(), Collections.singletonList(point), null);
  }

  /**
   * Creates a {@link TimeSeries}.
   *
   * @param labelValues the {@code LabelValue}s that uniquely identify this {@code TimeSeries}.
   * @param points the data {@code Point}s of this {@code TimeSeries}.
   * @param startTimestamp the start {@code Timestamp} of this {@code TimeSeries}. Must be non-null
   *     for cumulative {@code Point}s.
   * @return a {@code TimeSeries}.
   */
  private static TimeSeries createInternal(
      List<LabelValue> labelValues, List<Point> points, @Nullable Timestamp startTimestamp) {
    // Fail fast on null lists to prevent NullPointerException when copying the lists.
    Utils.checkListElementNotNull(Utils.checkNotNull(labelValues, "labelValues"), "labelValue");
    return new AutoValue_TimeSeries(
        Collections.unmodifiableList(new ArrayList<LabelValue>(labelValues)),
        points,
        startTimestamp);
  }

  /**
   * Returns the set of {@link LabelValue}s that uniquely identify this {@link TimeSeries}.
   *
   * <p>Apply to all {@link Point}s.
   *
   * <p>The order of {@link LabelValue}s must match that of {@link LabelKey}s in the {@code
   * MetricDescriptor}.
   *
   * @return the {@code LabelValue}s.
   * @since 0.1.0
   */
  public abstract List<LabelValue> getLabelValues();

  /**
   * Returns the data {@link Point}s of this {@link TimeSeries}.
   *
   * @return the data {@code Point}s.
   * @since 0.1.0
   */
  public abstract List<Point> getPoints();

  /**
   * Returns the start {@link Timestamp} of this {@link TimeSeries} if the {@link Point}s are
   * cumulative, or {@code null} if the {@link Point}s are gauge.
   *
   * @return the start {@code Timestamp} or {@code null}.
   * @since 0.1.0
   */
  @Nullable
  public abstract Timestamp getStartTimestamp();
}
