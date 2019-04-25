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
    public DerivedLongGauge.Builder derivedLongGaugeBuilder(String name) {
      Utils.checkNotNull(name, "name");
      return new NoopDerivedLongGauge.NoopBuilder();
    }

    @Override
    public DerivedDoubleGauge.Builder derivedDoubleGaugeBuilder(String name) {
      Utils.checkNotNull(name, "name");
      return new NoopDerivedDoubleGauge.NoopBuilder();
    }

    @Override
    public DoubleCumulative.Builder doubleCumulativeBuilder(String name) {
      Utils.checkNotNull(name, "name");
      return new NoopDoubleCumulative.NoopBuilder();
    }

    @Override
    public DerivedDoubleCumulative.Builder derivedDoubleCumulativeBuilder(String name) {
      Utils.checkNotNull(name, "name");
      return new NoopDerivedDoubleCumulative.NoopBuilder();
    }

    @Override
    public LongCumulative.Builder longCumulativeBuilder(String name) {
      Utils.checkNotNull(name, "name");
      return new NoopLongCumulative.NoopBuilder();
    }

    @Override
    public DerivedLongCumulative.Builder derivedLongCumulativeBuilder(String name) {
      Utils.checkNotNull(name, "name");
      return new NoopDerivedLongCumulative.NoopBuilder();
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

    /** Creates a new {@code NoopLongPoint}. */
    private NoopLongGauge(int labelKeysSize) {
      this.labelKeysSize = labelKeysSize;
    }

    @Override
    public NoopLongPoint getOrCreateTimeSeries(List<LabelValue> labelValues) {
      Utils.checkListElementNotNull(Utils.checkNotNull(labelValues, "labelValues"), "labelValue");
      Utils.checkArgument(
          labelKeysSize == labelValues.size(), "Label Keys and Label Values don't have same size.");
      return new NoopLongPoint();
    }

    @Override
    public NoopLongPoint getDefaultTimeSeries() {
      return new NoopLongPoint();
    }

    @Override
    public void removeTimeSeries(List<LabelValue> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues");
    }

    @Override
    public void clear() {}

    /** No-op implementations of LongPoint class. */
    private static final class NoopLongPoint implements LongPoint {
      private NoopLongPoint() {}

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

    /** Creates a new {@code NoopDoublePoint}. */
    private NoopDoubleGauge(int labelKeysSize) {
      this.labelKeysSize = labelKeysSize;
    }

    @Override
    public NoopDoublePoint getOrCreateTimeSeries(List<LabelValue> labelValues) {
      Utils.checkListElementNotNull(Utils.checkNotNull(labelValues, "labelValues"), "labelValue");
      Utils.checkArgument(
          labelKeysSize == labelValues.size(), "Label Keys and Label Values don't have same size.");
      return new NoopDoublePoint();
    }

    @Override
    public NoopDoublePoint getDefaultTimeSeries() {
      return new NoopDoublePoint();
    }

    @Override
    public void removeTimeSeries(List<LabelValue> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues");
    }

    @Override
    public void clear() {}

    /** No-op implementations of DoublePoint class. */
    private static final class NoopDoublePoint implements DoublePoint {
      private NoopDoublePoint() {}

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

  /** No-op implementations of DerivedLongGauge class. */
  private static final class NoopDerivedLongGauge implements DerivedLongGauge {
    private final int labelKeysSize;

    /** Creates a new {@code NoopDerivedLongGauge}. */
    NoopDerivedLongGauge(int labelKeysSize) {
      this.labelKeysSize = labelKeysSize;
    }

    @Override
    public <T> void createTimeSeries(
        List<LabelValue> labelValues, T obj, ToLongFunction<T> function) {
      Utils.checkListElementNotNull(Utils.checkNotNull(labelValues, "labelValues"), "labelValue");
      Utils.checkArgument(
          labelKeysSize == labelValues.size(), "Label Keys and Label Values don't have same size.");
      Utils.checkNotNull(function, "function");
    }

    @Override
    public void removeTimeSeries(List<LabelValue> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues");
    }

    @Override
    public void clear() {}

    private static final class NoopBuilder implements DerivedLongGauge.Builder {
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
      public DerivedLongGauge build() {
        return new NoopDerivedLongGauge(labelKeysSize);
      }
    }
  }

  /** No-op implementations of DerivedDoubleGauge class. */
  private static final class NoopDerivedDoubleGauge implements DerivedDoubleGauge {
    private final int labelKeysSize;

    /** Creates a new {@code NoopDerivedDoubleGauge}. */
    private NoopDerivedDoubleGauge(int labelKeysSize) {
      this.labelKeysSize = labelKeysSize;
    }

    @Override
    public <T> void createTimeSeries(
        List<LabelValue> labelValues, T obj, ToDoubleFunction<T> function) {
      Utils.checkListElementNotNull(Utils.checkNotNull(labelValues, "labelValues"), "labelValue");
      Utils.checkArgument(
          labelKeysSize == labelValues.size(), "Label Keys and Label Values don't have same size.");
      Utils.checkNotNull(function, "function");
    }

    @Override
    public void removeTimeSeries(List<LabelValue> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues");
    }

    @Override
    public void clear() {}

    private static final class NoopBuilder implements NoopDerivedDoubleGauge.Builder {
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
      public DerivedDoubleGauge build() {
        return new NoopDerivedDoubleGauge(labelKeysSize);
      }
    }
  }

  /** No-op implementations of DoubleCumulative class. */
  private static final class NoopDoubleCumulative implements DoubleCumulative {
    private final int labelKeysSize;

    /** Creates a new {@code NoopDoublePoint}. */
    private NoopDoubleCumulative(int labelKeysSize) {
      this.labelKeysSize = labelKeysSize;
    }

    @Override
    public NoopDoublePoint getOrCreateTimeSeries(List<LabelValue> labelValues) {
      Utils.checkListElementNotNull(Utils.checkNotNull(labelValues, "labelValues"), "labelValue");
      Utils.checkArgument(
          labelKeysSize == labelValues.size(), "Label Keys and Label Values don't have same size.");
      return NoopDoublePoint.INSTANCE;
    }

    @Override
    public NoopDoublePoint getDefaultTimeSeries() {
      return NoopDoublePoint.INSTANCE;
    }

    @Override
    public void removeTimeSeries(List<LabelValue> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues");
    }

    @Override
    public void clear() {}

    /** No-op implementations of DoublePoint class. */
    private static final class NoopDoublePoint implements DoublePoint {
      private static final NoopDoublePoint INSTANCE = new NoopDoublePoint();

      private NoopDoublePoint() {}

      @Override
      public void add(double delta) {}
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

  /** No-op implementations of DerivedDoubleCumulative class. */
  private static final class NoopDerivedDoubleCumulative implements DerivedDoubleCumulative {
    private final int labelKeysSize;

    /** Creates a new {@code NoopDerivedDoubleCumulative}. */
    private NoopDerivedDoubleCumulative(int labelKeysSize) {
      this.labelKeysSize = labelKeysSize;
    }

    @Override
    public <T> void createTimeSeries(
        List<LabelValue> labelValues, T obj, ToDoubleFunction<T> function) {
      Utils.checkListElementNotNull(Utils.checkNotNull(labelValues, "labelValues"), "labelValue");
      Utils.checkArgument(
          labelKeysSize == labelValues.size(), "Label Keys and Label Values don't have same size.");
      Utils.checkNotNull(function, "function");
    }

    @Override
    public void removeTimeSeries(List<LabelValue> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues");
    }

    @Override
    public void clear() {}

    private static final class NoopBuilder implements DerivedDoubleCumulative.Builder {
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
      public DerivedDoubleCumulative build() {
        return new NoopDerivedDoubleCumulative(labelKeysSize);
      }
    }
  }

  /** No-op implementations of LongCumulative class. */
  private static final class NoopLongCumulative implements LongCumulative {
    private final int labelKeysSize;

    /** Creates a new {@code NoopLongPoint}. */
    private NoopLongCumulative(int labelKeysSize) {
      this.labelKeysSize = labelKeysSize;
    }

    @Override
    public NoopLongPoint getOrCreateTimeSeries(List<LabelValue> labelValues) {
      Utils.checkListElementNotNull(Utils.checkNotNull(labelValues, "labelValues"), "labelValue");
      Utils.checkArgument(
          labelKeysSize == labelValues.size(), "Label Keys and Label Values don't have same size.");
      return NoopLongPoint.INSTANCE;
    }

    @Override
    public NoopLongPoint getDefaultTimeSeries() {
      return NoopLongPoint.INSTANCE;
    }

    @Override
    public void removeTimeSeries(List<LabelValue> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues");
    }

    @Override
    public void clear() {}

    /** No-op implementations of LongPoint class. */
    private static final class NoopLongPoint implements LongPoint {
      private static final NoopLongPoint INSTANCE = new NoopLongPoint();

      private NoopLongPoint() {}

      @Override
      public void add(long delta) {}
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

  /** No-op implementations of DerivedLongCumulative class. */
  private static final class NoopDerivedLongCumulative implements DerivedLongCumulative {
    private final int labelKeysSize;

    /** Creates a new {@code NoopDerivedLongCumulative}. */
    NoopDerivedLongCumulative(int labelKeysSize) {
      this.labelKeysSize = labelKeysSize;
    }

    @Override
    public <T> void createTimeSeries(
        List<LabelValue> labelValues, T obj, ToLongFunction<T> function) {
      Utils.checkListElementNotNull(Utils.checkNotNull(labelValues, "labelValues"), "labelValue");
      Utils.checkArgument(
          labelKeysSize == labelValues.size(), "Label Keys and Label Values don't have same size.");
      Utils.checkNotNull(function, "function");
    }

    @Override
    public void removeTimeSeries(List<LabelValue> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues");
    }

    @Override
    public void clear() {}

    private static final class NoopBuilder implements DerivedLongCumulative.Builder {
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
      public DerivedLongCumulative build() {
        return new NoopDerivedLongCumulative(labelKeysSize);
      }
    }
  }
}
