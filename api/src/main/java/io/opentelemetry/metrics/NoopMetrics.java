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

import io.opentelemetry.internal.Utils;
import io.opentelemetry.resource.Resource;
import java.util.List;
import java.util.Map;

/**
 * No-op implementations of metrics classes.
 *
 * @since 0.1.0
 */
public final class NoopMetrics {
  private NoopMetrics() {}

  /**
   * Returns an instance that is a no-op implementations for {@link Meter}.
   *
   * @return an instance that is a no-op implementations for {@link Meter}
   * @since 0.1.0
   */
  public static Meter newNoopMeter() {
    return new NoopMeter();
  }

  private static final class NoopMeter implements Meter {
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
  }

  /** No-op implementations of GaugeLong class. */
  private static final class NoopGaugeLong implements GaugeLong {
    private final int labelKeysSize;

    /** Creates a new {@code NoopTimeSeries}. */
    private NoopGaugeLong(int labelKeysSize) {
      this.labelKeysSize = labelKeysSize;
    }

    @Override
    public NoopTimeSeries getOrCreateTimeSeries(List<LabelValue> labelValues) {
      Utils.checkListElementNotNull(Utils.checkNotNull(labelValues, "labelValues"), "labelValue");
      Utils.checkArgument(
          labelKeysSize == labelValues.size(), "Label Keys and Label Values don't have same size.");
      return new NoopTimeSeries();
    }

    @Override
    public NoopTimeSeries getDefaultTimeSeries() {
      return new NoopTimeSeries();
    }

    @Override
    public void setCallback(Runnable metricUpdater) {
      Utils.checkNotNull(metricUpdater, "metricUpdater");
    }

    @Override
    public void removeTimeSeries(List<LabelValue> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues");
    }

    @Override
    public void clear() {}

    /** No-op implementations of TimeSeries class. */
    private static final class NoopTimeSeries implements TimeSeries {
      private NoopTimeSeries() {}

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
      public Builder setLabelKeys(List<LabelKey> labelKeys) {
        Utils.checkListElementNotNull(Utils.checkNotNull(labelKeys, "labelKeys"), "labelKey");
        labelKeysSize = labelKeys.size();
        return this;
      }

      @Override
      public Builder setConstantLabels(Map<LabelKey, LabelValue> constantLabels) {
        Utils.checkMapElementNotNull(
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

    /** Creates a new {@code NoopTimeSeries}. */
    private NoopGaugeDouble(int labelKeysSize) {
      this.labelKeysSize = labelKeysSize;
    }

    @Override
    public NoopTimeSeries getOrCreateTimeSeries(List<LabelValue> labelValues) {
      Utils.checkListElementNotNull(Utils.checkNotNull(labelValues, "labelValues"), "labelValue");
      Utils.checkArgument(
          labelKeysSize == labelValues.size(), "Label Keys and Label Values don't have same size.");
      return new NoopTimeSeries();
    }

    @Override
    public NoopTimeSeries getDefaultTimeSeries() {
      return new NoopTimeSeries();
    }

    @Override
    public void setCallback(Runnable metricUpdater) {
      Utils.checkNotNull(metricUpdater, "metricUpdater");
    }

    @Override
    public void removeTimeSeries(List<LabelValue> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues");
    }

    @Override
    public void clear() {}

    /** No-op implementations of TimeSeries class. */
    private static final class NoopTimeSeries implements TimeSeries {
      private NoopTimeSeries() {}

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
      public Builder setLabelKeys(List<LabelKey> labelKeys) {
        Utils.checkListElementNotNull(Utils.checkNotNull(labelKeys, "labelKeys"), "labelKey");
        labelKeysSize = labelKeys.size();
        return this;
      }

      @Override
      public Builder setConstantLabels(Map<LabelKey, LabelValue> constantLabels) {
        Utils.checkMapElementNotNull(
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

    /** Creates a new {@code NoopTimeSeries}. */
    private NoopCounterDouble(int labelKeysSize) {
      this.labelKeysSize = labelKeysSize;
    }

    @Override
    public NoopTimeSeries getOrCreateTimeSeries(List<LabelValue> labelValues) {
      Utils.checkListElementNotNull(Utils.checkNotNull(labelValues, "labelValues"), "labelValue");
      Utils.checkArgument(
          labelKeysSize == labelValues.size(), "Label Keys and Label Values don't have same size.");
      return NoopTimeSeries.INSTANCE;
    }

    @Override
    public NoopTimeSeries getDefaultTimeSeries() {
      return NoopTimeSeries.INSTANCE;
    }

    @Override
    public void setCallback(Runnable metricUpdater) {
      Utils.checkNotNull(metricUpdater, "metricUpdater");
    }

    @Override
    public void removeTimeSeries(List<LabelValue> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues");
    }

    @Override
    public void clear() {}

    /** No-op implementations of TimeSeries class. */
    private static final class NoopTimeSeries implements TimeSeries {
      private static final NoopTimeSeries INSTANCE = new NoopTimeSeries();

      private NoopTimeSeries() {}

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
      public Builder setLabelKeys(List<LabelKey> labelKeys) {
        Utils.checkListElementNotNull(Utils.checkNotNull(labelKeys, "labelKeys"), "labelKey");
        labelKeysSize = labelKeys.size();
        return this;
      }

      @Override
      public Builder setConstantLabels(Map<LabelKey, LabelValue> constantLabels) {
        Utils.checkMapElementNotNull(
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

    /** Creates a new {@code NoopTimeSeries}. */
    private NoopCounterLong(int labelKeysSize) {
      this.labelKeysSize = labelKeysSize;
    }

    @Override
    public NoopTimeSeries getOrCreateTimeSeries(List<LabelValue> labelValues) {
      Utils.checkListElementNotNull(Utils.checkNotNull(labelValues, "labelValues"), "labelValue");
      Utils.checkArgument(
          labelKeysSize == labelValues.size(), "Label Keys and Label Values don't have same size.");
      return NoopTimeSeries.INSTANCE;
    }

    @Override
    public NoopTimeSeries getDefaultTimeSeries() {
      return NoopTimeSeries.INSTANCE;
    }

    @Override
    public void setCallback(Runnable metricUpdater) {
      Utils.checkNotNull(metricUpdater, "metricUpdater");
    }

    @Override
    public void removeTimeSeries(List<LabelValue> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues");
    }

    @Override
    public void clear() {}

    /** No-op implementations of TimeSeries class. */
    private static final class NoopTimeSeries implements TimeSeries {
      private static final NoopTimeSeries INSTANCE = new NoopTimeSeries();

      private NoopTimeSeries() {}

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
      public Builder setLabelKeys(List<LabelKey> labelKeys) {
        Utils.checkListElementNotNull(Utils.checkNotNull(labelKeys, "labelKeys"), "labelKey");
        labelKeysSize = labelKeys.size();
        return this;
      }

      @Override
      public Builder setConstantLabels(Map<LabelKey, LabelValue> constantLabels) {
        Utils.checkMapElementNotNull(
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
}
