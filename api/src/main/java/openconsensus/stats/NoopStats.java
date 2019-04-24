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
import javax.annotation.concurrent.Immutable;
import openconsensus.internal.Utils;
import openconsensus.stats.Measure.MeasureDouble;
import openconsensus.stats.Measure.MeasureLong;
import openconsensus.tags.TagMap;
import openconsensus.trace.SpanContext;

/**
 * No-op implementations of stats classes.
 *
 * @since 0.1.0
 */
public final class NoopStats {

  private NoopStats() {}

  /**
   * Returns a {@code StatsRecorder} that is no-op implementation for {@link StatsRecorder}.
   *
   * @return a {@code StatsRecorder} that is no-op implementation for {@code StatsRecorder}.
   * @since 0.1.0
   */
  public static StatsRecorder newNoopStatsRecorder() {
    return new NoopStatsRecorder();
  }

  @Immutable
  private static final class NoopStatsRecorder extends StatsRecorder {

    @Override
    public void record(List<Measurement> measurements) {
      Utils.checkNotNull(measurements, "measurements");
    }

    @Override
    public void record(List<Measurement> measurements, TagMap tags) {
      Utils.checkNotNull(measurements, "measurements");
      Utils.checkNotNull(tags, "tags");
    }

    @Override
    public void record(List<Measurement> measurements, TagMap tags, SpanContext spanContext) {
      Utils.checkNotNull(tags, "tags");
      Utils.checkNotNull(measurements, "measurements");
      Utils.checkNotNull(spanContext, "spanContext");
    }

    @Override
    public MeasureDouble createMeasureDouble(String name, String description, String unit) {
      return MeasureDouble.create(name, description, unit);
    }

    @Override
    public MeasureLong createMeasureLong(String name, String description, String unit) {
      return MeasureLong.create(name, description, unit);
    }
  }
}
