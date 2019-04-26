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

package openconsensus.metrics;

import java.util.List;
import java.util.Map;
import openconsensus.internal.Utils;
import openconsensus.resource.Resource;

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
    public MetricRegistry.Builder metricRegistryBuilder() {
      return new NoopMetricCollection.Builder();
    }
  }

  private static final class NoopMetricCollection implements MetricRegistry {
    @Override
    public LongGauge.Builder longGaugeBuilder(String name) {
      Utils.checkNotNull(name, "name");
      return new NoopLongGauge.NoopBuilder();
    }

    @Override
    public DoubleGauge.Builder doubleGaugeBuilder(String name) {
      Utils.checkNotNull(name, "name");
      return new NoopDoubleGauge.NoopBuilder();
    }

    @Override
    public DoubleCumulative.Builder doubleCumulativeBuilder(String name) {
      Utils.checkNotNull(name, "name");
      return new NoopDoubleCumulative.NoopBuilder();
    }

    @Override
    public LongCumulative.Builder longCumulativeBuilder(String name) {
      Utils.checkNotNull(name, "name");
      return new NoopLongCumulative.NoopBuilder();
    }

    private static final class Builder implements MetricRegistry.Builder {
      private static final MetricRegistry METRIC_COLLECTION = new NoopMetricCollection();

      @Override
      public MetricRegistry.Builder setComponent(String component) {
        Utils.checkNotNull(component, "component");
        return this;
      }

      @Override
      public MetricRegistry.Builder setResource(Resource resource) {
        Utils.checkNotNull(resource, "resource");
        return this;
      }

      @Override
      public MetricRegistry build() {
        return METRIC_COLLECTION;
      }
    }
  }

  /** No-op implementations of LongGauge class. */
  private static final class NoopLongGauge implements LongGauge {
    private final int labelKeysSize;

    /** Creates a new {@code NoopTimeSeries}. */
    private NoopLongGauge(int labelKeysSize) {
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

    private static final class NoopBuilder implements LongGauge.Builder {
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
      public LongGauge build() {
        return new NoopLongGauge(labelKeysSize);
      }
    }
  }

  /** No-op implementations of DoubleGauge class. */
  private static final class NoopDoubleGauge implements DoubleGauge {
    private final int labelKeysSize;

    /** Creates a new {@code NoopTimeSeries}. */
    private NoopDoubleGauge(int labelKeysSize) {
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

    private static final class NoopBuilder implements DoubleGauge.Builder {
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
      public DoubleGauge build() {
        return new NoopDoubleGauge(labelKeysSize);
      }
    }
  }

  /** No-op implementations of DoubleCumulative class. */
  private static final class NoopDoubleCumulative implements DoubleCumulative {
    private final int labelKeysSize;

    /** Creates a new {@code NoopTimeSeries}. */
    private NoopDoubleCumulative(int labelKeysSize) {
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

    private static final class NoopBuilder implements DoubleCumulative.Builder {
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
      public DoubleCumulative build() {
        return new NoopDoubleCumulative(labelKeysSize);
      }
    }
  }

  /** No-op implementations of LongCumulative class. */
  private static final class NoopLongCumulative implements LongCumulative {
    private final int labelKeysSize;

    /** Creates a new {@code NoopTimeSeries}. */
    private NoopLongCumulative(int labelKeysSize) {
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

    private static final class NoopBuilder implements LongCumulative.Builder {
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
      public LongCumulative build() {
        return new NoopLongCumulative(labelKeysSize);
      }
    }
  }
}
