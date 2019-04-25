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
import javax.annotation.concurrent.ThreadSafe;
import openconsensus.internal.StringUtils;
import openconsensus.internal.Utils;
import openconsensus.stats.Measurement.MeasurementDouble;
import openconsensus.stats.Measurement.MeasurementLong;
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
  private static final class NoopStatsRecorder implements StatsRecorder {
    /* VisibleForTesting */ static final int NAME_MAX_LENGTH = 255;
    private static final String ERROR_MESSAGE_INVALID_NAME =
        "Name should be a ASCII string with a length no greater than "
            + NAME_MAX_LENGTH
            + " characters.";

    @Override
    public Measure.Builder buildMeasure(String name) {
      Utils.checkArgument(
          StringUtils.isPrintableString(name) && name.length() <= NAME_MAX_LENGTH,
          ERROR_MESSAGE_INVALID_NAME);
      return new NoopMeasure.NoopBuilder();
    }

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
  }

  @ThreadSafe
  private static final class NoopMeasure implements Measure {
    private final Type type;

    private NoopMeasure(Type type) {
      this.type = type;
    }

    @Override
    public MeasurementDouble createDoubleMeasurement(double value) {
      if (type != Type.DOUBLE) {
        throw new UnsupportedOperationException("This type can only create double measurement");
      }
      Utils.checkArgument(value >= 0.0, "Unsupported negative values.");
      return MeasurementDouble.create(this, value);
    }

    @Override
    public MeasurementLong createLongMeasurement(long value) {
      if (type != Type.LONG) {
        throw new UnsupportedOperationException("This type can only create long measurement");
      }
      Utils.checkArgument(value >= 0, "Unsupported negative values.");
      return MeasurementLong.create(this, value);
    }

    private static final class NoopBuilder implements Measure.Builder {
      private Type type = Type.DOUBLE;

      @Override
      public Builder setDescription(String description) {
        Utils.checkNotNull(description, "description");
        return this;
      }

      @Override
      public Builder setUnit(String unit) {
        Utils.checkNotNull(unit, "unit");
        return this;
      }

      @Override
      public Builder setType(Type type) {
        this.type = Utils.checkNotNull(type, "type");
        return this;
      }

      @Override
      public Measure build() {
        return new NoopMeasure(type);
      }
    }
  }
}
