/*
 * Copyright 2019, OpenTelemetry Authors
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

package io.opentelemetry.metrics;

import io.opentelemetry.distributedcontext.DistributedContext;
import io.opentelemetry.internal.StringUtils;
import io.opentelemetry.internal.Utils;
import io.opentelemetry.resources.Resource;
import io.opentelemetry.trace.SpanContext;
import java.util.List;
import java.util.Map;
import javax.annotation.concurrent.ThreadSafe;

/**
 * No-op implementations of {@link Meter}.
 *
 * @since 0.1.0
 */
public final class DefaultMeter implements Meter {
  private static final DefaultMeter INSTANCE = new DefaultMeter();

  /* VisibleForTesting */ static final int NAME_MAX_LENGTH = 255;
  private static final String ERROR_MESSAGE_INVALID_NAME =
      "Name should be a ASCII string with a length no greater than "
          + NAME_MAX_LENGTH
          + " characters.";

  /**
   * Returns a {@code Meter} singleton that is the default implementations for {@link Meter}.
   *
   * @return a {@code Meter} singleton that is the default implementations for {@link Meter}.
   * @since 0.1.0
   */
  public static Meter getInstance() {
    return INSTANCE;
  }

  @Override
  public GaugeLong.Builder gaugeLongBuilder(String name) {
    Utils.checkNotNull(name, "name");
    return new NoopGaugeLong.NoopBuilder();
  }

  @Override
  public GaugeDouble.Builder gaugeDoubleBuilder(String name) {
    Utils.checkNotNull(name, "name");
    return new NoopGaugeDouble.NoopBuilder();
  }

  @Override
  public CounterDouble.Builder counterDoubleBuilder(String name) {
    Utils.checkNotNull(name, "name");
    return new NoopCounterDouble.NoopBuilder();
  }

  @Override
  public CounterLong.Builder counterLongBuilder(String name) {
    Utils.checkNotNull(name, "name");
    return new NoopCounterLong.NoopBuilder();
  }

  @Override
  public MeasureDouble.Builder measureDoubleBuilder(String name) {
    Utils.checkNotNull(name, "name");
    Utils.checkArgument(
        StringUtils.isPrintableString(name) && name.length() <= NAME_MAX_LENGTH,
        ERROR_MESSAGE_INVALID_NAME);
    return new NoopMeasureDouble.NoopBuilder();
  }

  @Override
  public MeasureLong.Builder measureLongBuilder(String name) {
    Utils.checkNotNull(name, "name");
    Utils.checkArgument(
        StringUtils.isPrintableString(name) && name.length() <= NAME_MAX_LENGTH,
        ERROR_MESSAGE_INVALID_NAME);
    return new NoopMeasureLong.NoopBuilder();
  }

  @Override
  public MeasureBatchRecorder newMeasureBatchRecorder() {
    return new NoopMeasureBatchRecorder();
  }

  /** No-op implementations of GaugeLong class. */
  private static final class NoopGaugeLong implements GaugeLong {
    private final int labelKeysSize;

    /** Creates a new {@code NoopHandle}. */
    private NoopGaugeLong(int labelKeysSize) {
      this.labelKeysSize = labelKeysSize;
    }

    @Override
    public NoopHandle getHandle(List<String> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues");
      Utils.checkArgument(
          labelKeysSize == labelValues.size(), "Label Keys and Label Values don't have same size.");
      return new NoopHandle();
    }

    @Override
    public NoopHandle getDefaultHandle() {
      return new NoopHandle();
    }

    @Override
    public void setCallback(Runnable metricUpdater) {
      Utils.checkNotNull(metricUpdater, "metricUpdater");
    }

    @Override
    public void removeHandle(List<String> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues");
    }

    /** No-op implementations of Handle class. */
    private static final class NoopHandle implements Handle {
      private NoopHandle() {}

      @Override
      public void add(long amt) {}

      @Override
      public void set(long val) {}
    }

    private static final class NoopBuilder implements GaugeLong.Builder {
      private int labelKeysSize = 0;

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
      public Builder setLabelKeys(List<String> labelKeys) {
        Utils.checkListElementNotNull(Utils.checkNotNull(labelKeys, "labelKeys"), "labelKey");
        labelKeysSize = labelKeys.size();
        return this;
      }

      @Override
      public Builder setConstantLabels(Map<String, String> constantLabels) {
        Utils.checkMapKeysNotNull(
            Utils.checkNotNull(constantLabels, "constantLabels"), "constantLabel");
        return this;
      }

      @Override
      public Builder setComponent(String component) {
        Utils.checkNotNull(component, "component");
        return this;
      }

      @Override
      public Builder setResource(Resource resource) {
        Utils.checkNotNull(resource, "resource");
        return this;
      }

      @Override
      public GaugeLong build() {
        return new NoopGaugeLong(labelKeysSize);
      }
    }
  }

  /** No-op implementations of GaugeDouble class. */
  private static final class NoopGaugeDouble implements GaugeDouble {
    private final int labelKeysSize;

    /** Creates a new {@code NoopHandle}. */
    private NoopGaugeDouble(int labelKeysSize) {
      this.labelKeysSize = labelKeysSize;
    }

    @Override
    public NoopHandle getHandle(List<String> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues");
      Utils.checkArgument(
          labelKeysSize == labelValues.size(), "Label Keys and Label Values don't have same size.");
      return new NoopHandle();
    }

    @Override
    public NoopHandle getDefaultHandle() {
      return new NoopHandle();
    }

    @Override
    public void setCallback(Runnable metricUpdater) {
      Utils.checkNotNull(metricUpdater, "metricUpdater");
    }

    @Override
    public void removeHandle(List<String> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues");
    }

    /** No-op implementations of Handle class. */
    private static final class NoopHandle implements Handle {
      private NoopHandle() {}

      @Override
      public void add(double amt) {}

      @Override
      public void set(double val) {}
    }

    private static final class NoopBuilder implements GaugeDouble.Builder {
      private int labelKeysSize = 0;

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
      public Builder setLabelKeys(List<String> labelKeys) {
        Utils.checkListElementNotNull(Utils.checkNotNull(labelKeys, "labelKeys"), "labelKey");
        labelKeysSize = labelKeys.size();
        return this;
      }

      @Override
      public Builder setConstantLabels(Map<String, String> constantLabels) {
        Utils.checkMapKeysNotNull(
            Utils.checkNotNull(constantLabels, "constantLabels"), "constantLabel");
        return this;
      }

      @Override
      public Builder setComponent(String component) {
        Utils.checkNotNull(component, "component");
        return this;
      }

      @Override
      public Builder setResource(Resource resource) {
        Utils.checkNotNull(resource, "resource");
        return this;
      }

      @Override
      public GaugeDouble build() {
        return new NoopGaugeDouble(labelKeysSize);
      }
    }
  }

  /** No-op implementations of CounterDouble class. */
  private static final class NoopCounterDouble implements CounterDouble {
    private final int labelKeysSize;

    /** Creates a new {@code NoopHandle}. */
    private NoopCounterDouble(int labelKeysSize) {
      this.labelKeysSize = labelKeysSize;
    }

    @Override
    public NoopHandle getHandle(List<String> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues");
      Utils.checkArgument(
          labelKeysSize == labelValues.size(), "Label Keys and Label Values don't have same size.");
      return NoopHandle.INSTANCE;
    }

    @Override
    public NoopHandle getDefaultHandle() {
      return NoopHandle.INSTANCE;
    }

    @Override
    public void setCallback(Runnable metricUpdater) {
      Utils.checkNotNull(metricUpdater, "metricUpdater");
    }

    @Override
    public void removeHandle(List<String> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues");
    }

    /** No-op implementations of Handle class. */
    private static final class NoopHandle implements Handle {
      private static final NoopHandle INSTANCE = new NoopHandle();

      private NoopHandle() {}

      @Override
      public void add(double delta) {}

      @Override
      public void set(double val) {}
    }

    private static final class NoopBuilder implements CounterDouble.Builder {
      private int labelKeysSize = 0;

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
      public Builder setLabelKeys(List<String> labelKeys) {
        Utils.checkListElementNotNull(Utils.checkNotNull(labelKeys, "labelKeys"), "labelKey");
        labelKeysSize = labelKeys.size();
        return this;
      }

      @Override
      public Builder setConstantLabels(Map<String, String> constantLabels) {
        Utils.checkMapKeysNotNull(
            Utils.checkNotNull(constantLabels, "constantLabels"), "constantLabel");
        return this;
      }

      @Override
      public Builder setComponent(String component) {
        Utils.checkNotNull(component, "component");
        return this;
      }

      @Override
      public Builder setResource(Resource resource) {
        Utils.checkNotNull(resource, "resource");
        return this;
      }

      @Override
      public CounterDouble build() {
        return new NoopCounterDouble(labelKeysSize);
      }
    }
  }

  /** No-op implementations of CounterLong class. */
  private static final class NoopCounterLong implements CounterLong {
    private final int labelKeysSize;

    /** Creates a new {@code NoopHandle}. */
    private NoopCounterLong(int labelKeysSize) {
      this.labelKeysSize = labelKeysSize;
    }

    @Override
    public NoopHandle getHandle(List<String> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues");
      Utils.checkArgument(
          labelKeysSize == labelValues.size(), "Label Keys and Label Values don't have same size.");
      return NoopHandle.INSTANCE;
    }

    @Override
    public NoopHandle getDefaultHandle() {
      return NoopHandle.INSTANCE;
    }

    @Override
    public void setCallback(Runnable metricUpdater) {
      Utils.checkNotNull(metricUpdater, "metricUpdater");
    }

    @Override
    public void removeHandle(List<String> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues");
    }

    /** No-op implementations of Handle class. */
    private static final class NoopHandle implements Handle {
      private static final NoopHandle INSTANCE = new NoopHandle();

      private NoopHandle() {}

      @Override
      public void add(long delta) {}

      @Override
      public void set(long val) {}
    }

    private static final class NoopBuilder implements CounterLong.Builder {
      private int labelKeysSize = 0;

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
      public Builder setLabelKeys(List<String> labelKeys) {
        Utils.checkListElementNotNull(Utils.checkNotNull(labelKeys, "labelKeys"), "labelKey");
        labelKeysSize = labelKeys.size();
        return this;
      }

      @Override
      public Builder setConstantLabels(Map<String, String> constantLabels) {
        Utils.checkMapKeysNotNull(
            Utils.checkNotNull(constantLabels, "constantLabels"), "constantLabel");
        return this;
      }

      @Override
      public Builder setComponent(String component) {
        Utils.checkNotNull(component, "component");
        return this;
      }

      @Override
      public Builder setResource(Resource resource) {
        Utils.checkNotNull(resource, "resource");
        return this;
      }

      @Override
      public CounterLong build() {
        return new NoopCounterLong(labelKeysSize);
      }
    }
  }

  @ThreadSafe
  private static final class NoopMeasureDouble implements MeasureDouble {
    private NoopMeasureDouble() {}

    @Override
    public void record(double value) {
      Utils.checkArgument(value >= 0.0, "Unsupported negative values.");
    }

    @Override
    public void record(double value, DistributedContext distContext) {
      Utils.checkArgument(value >= 0.0, "Unsupported negative values.");
      Utils.checkNotNull(distContext, "distContext");
    }

    @Override
    public void record(double value, DistributedContext distContext, SpanContext spanContext) {
      Utils.checkArgument(value >= 0.0, "Unsupported negative values.");
      Utils.checkNotNull(distContext, "distContext");
      Utils.checkNotNull(spanContext, "spanContext");
    }

    private static final class NoopBuilder implements MeasureDouble.Builder {
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
      public Builder setConstantLabels(Map<String, String> constantLabels) {
        Utils.checkNotNull(constantLabels, "constantLabels");
        return this;
      }

      @Override
      public NoopMeasureDouble build() {
        return new NoopMeasureDouble();
      }
    }
  }

  @ThreadSafe
  private static final class NoopMeasureLong implements MeasureLong {
    private NoopMeasureLong() {}

    @Override
    public void record(long value) {
      Utils.checkArgument(value >= 0, "Unsupported negative values.");
    }

    @Override
    public void record(long value, DistributedContext distContext) {
      Utils.checkArgument(value >= 0, "Unsupported negative values.");
      Utils.checkNotNull(distContext, "distContext");
    }

    @Override
    public void record(long value, DistributedContext distContext, SpanContext spanContext) {
      Utils.checkArgument(value >= 0, "Unsupported negative values.");
      Utils.checkNotNull(distContext, "distContext");
      Utils.checkNotNull(spanContext, "spanContext");
    }

    private static final class NoopBuilder implements MeasureLong.Builder {
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
      public Builder setConstantLabels(Map<String, String> constantLabels) {
        Utils.checkNotNull(constantLabels, "constantLabels");
        return this;
      }

      @Override
      public NoopMeasureLong build() {
        return new NoopMeasureLong();
      }
    }
  }

  private static final class NoopMeasureBatchRecorder implements MeasureBatchRecorder {
    private NoopMeasureBatchRecorder() {}

    @Override
    public MeasureBatchRecorder put(MeasureLong measure, long value) {
      Utils.checkNotNull(measure, "measure");
      Utils.checkArgument(value >= 0, "Unsupported negative values.");
      return this;
    }

    @Override
    public MeasureBatchRecorder put(MeasureDouble measure, double value) {
      Utils.checkNotNull(measure, "measure");
      Utils.checkArgument(value >= 0.0, "Unsupported negative values.");
      return this;
    }

    @Override
    public MeasureBatchRecorder setDistributedContext(DistributedContext distContext) {
      Utils.checkNotNull(distContext, "distContext");
      return this;
    }

    @Override
    public void record() {}
  }
}
