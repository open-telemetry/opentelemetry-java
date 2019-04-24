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

import java.util.List;
import openconsensus.stats.Measure.MeasureDouble;
import openconsensus.stats.Measure.MeasureLong;
import openconsensus.tags.TagMap;
import openconsensus.trace.SpanContext;

/**
 * Provides methods to record stats against tags.
 *
 * @since 0.1.0
 */
public abstract class StatsRecorder {
  /**
   * Records all given measurements, with the current {@link
   * openconsensus.tags.Tagger#getCurrentTagMap}.
   *
   * @param measurements the list of {@code Measurement}s to record.
   * @since 0.1.0
   */
  public abstract void record(List<Measurement> measurements);

  /**
   * Records all given measurements, with an explicit {@link TagMap}.
   *
   * @param measurements the list of {@code Measurement}s to record.
   * @param tags the tags associated with the measurements.
   * @since 0.1.0
   */
  public abstract void record(List<Measurement> measurements, TagMap tags);

  /**
   * Records all given measurements, with an explicit {@link TagMap}. These measurements are
   * associated with the given {@code SpanContext}.
   *
   * @param measurements the list of {@code Measurement}s to record.
   * @param tags the tags associated with the measurements.
   * @param spanContext the {@code SpanContext} that identifies the {@code Span} for which the
   *     measurements are associated with.
   * @since 0.1.0
   */
  public abstract void record(List<Measurement> measurements, TagMap tags, SpanContext spanContext);

  /**
   * Constructs a new {@link MeasureDouble}.
   *
   * @param name name of {@code Measure}. Suggested format: {@code <web_host>/<path>}.
   * @param description description of {@code Measure}.
   * @param unit unit of {@code Measure}.
   * @return a {@code MeasureDouble}.
   * @since 0.1.0
   */
  public abstract MeasureDouble createMeasureDouble(String name, String description, String unit);

  /**
   * Constructs a new {@link MeasureLong}.
   *
   * @param name name of {@code Measure}. Suggested format: {@code <web_host>/<path>}.
   * @param description description of {@code Measure}.
   * @param unit unit of {@code Measure}.
   * @return a {@code MeasureLong}.
   * @since 0.1.0
   */
  public abstract MeasureLong createMeasureLong(String name, String description, String unit);
}
