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

import javax.annotation.concurrent.NotThreadSafe;
import openconsensus.opencensusshim.internal.Utils;
import openconsensus.opencensusshim.metrics.data.AttachmentValue;
import openconsensus.opencensusshim.metrics.data.AttachmentValue.AttachmentValueString;
import openconsensus.opencensusshim.stats.Measure.MeasureDouble;
import openconsensus.opencensusshim.stats.Measure.MeasureLong;
import openconsensus.opencensusshim.tags.TagContext;

/**
 * A map from {@link Measure}s to measured values to be recorded at the same time.
 *
 * @since 0.1.0
 */
@NotThreadSafe
public abstract class MeasureMap {

  /**
   * Associates the {@link MeasureDouble} with the given value. Subsequent updates to the same
   * {@link MeasureDouble} will overwrite the previous value.
   *
   * @param measure the {@link MeasureDouble}
   * @param value the value to be associated with {@code measure}
   * @return this
   * @since 0.1.0
   */
  public abstract MeasureMap put(MeasureDouble measure, double value);

  /**
   * Associates the {@link MeasureLong} with the given value. Subsequent updates to the same {@link
   * MeasureLong} will overwrite the previous value.
   *
   * @param measure the {@link MeasureLong}
   * @param value the value to be associated with {@code measure}
   * @return this
   * @since 0.1.0
   */
  public abstract MeasureMap put(MeasureLong measure, long value);

  /**
   * Associate the contextual information of an {@code Exemplar} to this {@link MeasureMap}.
   * Contextual information is represented as {@code String} key-value pairs.
   *
   * <p>If this method is called multiple times with the same key, only the last value will be kept.
   *
   * @param key the key of contextual information of an {@code Exemplar}.
   * @param value the string representation of contextual information of an {@code Exemplar}.
   * @return this
   * @since 0.1.0
   * @deprecated in favor of {@link #putAttachment(String, AttachmentValue)}.
   */
  @Deprecated
  public MeasureMap putAttachment(String key, String value) {
    return putAttachment(key, AttachmentValueString.create(value));
  }

  /**
   * Associate the contextual information of an {@code Exemplar} to this {@link MeasureMap}.
   * Contextual information is represented as a {@code String} key and an {@link AttachmentValue}.
   *
   * <p>If this method is called multiple times with the same key, only the last value will be kept.
   *
   * @param key the key of contextual information of an {@code Exemplar}.
   * @param value the value of contextual information of an {@code Exemplar}.
   * @return this
   * @since 0.1.0
   */
  public MeasureMap putAttachment(String key, AttachmentValue value) {
    // Provides a default no-op implementation to avoid breaking other existing sub-classes.
    Utils.checkNotNull(key, "key");
    Utils.checkNotNull(value, "value");
    return this;
  }

  /**
   * Records all of the measures at the same time, with the current {@link TagContext}.
   *
   * <p>This method records all of the stats in the {@code MeasureMap} every time it is called.
   *
   * @since 0.1.0
   */
  public abstract void record();

  /**
   * Records all of the measures at the same time, with an explicit {@link TagContext}.
   *
   * <p>This method records all of the stats in the {@code MeasureMap} every time it is called.
   *
   * @param tags the tags associated with the measurements.
   * @since 0.1.0
   */
  public abstract void record(TagContext tags);
}
