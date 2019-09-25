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
import io.opentelemetry.trace.SpanContext;
import java.util.List;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

/**
 * No-op implementations of {@link Meter}.
 *
 * @since 0.1.0
 */
public final class DefaultMeter implements Meter {
  private static final DefaultMeter INSTANCE = new DefaultMeter();

  /* VisibleForTesting */ static final int NAME_MAX_LENGTH = 255;
  /* VisibleForTesting */ static final String ERROR_MESSAGE_INVALID_NAME =
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
    Utils.checkArgument(
        StringUtils.isPrintableString(name) && name.length() <= NAME_MAX_LENGTH,
        ERROR_MESSAGE_INVALID_NAME);
    return new NoopGaugeLong.NoopBuilder();
  }

  @Override
  public GaugeDouble.Builder gaugeDoubleBuilder(String name) {
    Utils.checkNotNull(name, "name");
    Utils.checkArgument(
        StringUtils.isPrintableString(name) && name.length() <= NAME_MAX_LENGTH,
        ERROR_MESSAGE_INVALID_NAME);
    return new NoopGaugeDouble.NoopBuilder();
  }

  @Override
  public CounterDouble.Builder counterDoubleBuilder(String name) {
    Utils.checkNotNull(name, "name");
    Utils.checkArgument(
        StringUtils.isPrintableString(name) && name.length() <= NAME_MAX_LENGTH,
        ERROR_MESSAGE_INVALID_NAME);
    return new NoopCounterDouble.NoopBuilder();
  }

  @Override
  public CounterLong.Builder counterLongBuilder(String name) {
    Utils.checkNotNull(name, "name");
    Utils.checkArgument(
        StringUtils.isPrintableString(name) && name.length() <= NAME_MAX_LENGTH,
        ERROR_MESSAGE_INVALID_NAME);
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
  public ObserverDouble.Builder observerDoubleBuilder(String name) {
    Utils.checkNotNull(name, "name");
    Utils.checkArgument(
        StringUtils.isPrintableString(name) && name.length() <= NAME_MAX_LENGTH,
        ERROR_MESSAGE_INVALID_NAME);
    return new NoopObserverDouble.NoopBuilder();
  }

  @Override
  public ObserverLong.Builder observerLongBuilder(String name) {
    Utils.checkNotNull(name, "name");
    Utils.checkArgument(
        StringUtils.isPrintableString(name) && name.length() <= NAME_MAX_LENGTH,
        ERROR_MESSAGE_INVALID_NAME);
    return new NoopObserverLong.NoopBuilder();
  }

  @Override
  public MeasureBatchRecorder newMeasureBatchRecorder() {
    return new NoopMeasureBatchRecorder();
  }

  /** No-op implementations of GaugeLong class. */
  @Immutable
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
    public void removeHandle(List<String> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues");
    }

    /** No-op implementations of Handle class. */
    @Immutable
    private static final class NoopHandle implements Handle {
      private NoopHandle() {}

      @Override
      public void set(long val) {}
    }

    private static final class NoopBuilder extends NoopAbstractGaugeBuilder<Builder, GaugeLong>
        implements Builder {
      @Override
      protected Builder getThis() {
        return this;
      }

      @Override
      public GaugeLong build() {
        return new NoopGaugeLong(labelKeysSize);
      }
    }
  }

  /** No-op implementations of GaugeDouble class. */
  @Immutable
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
    public void removeHandle(List<String> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues");
    }

    /** No-op implementations of Handle class. */
    @Immutable
    private static final class NoopHandle implements Handle {
      private NoopHandle() {}

      @Override
      public void set(double val) {}
    }

    private static final class NoopBuilder extends NoopAbstractGaugeBuilder<Builder, GaugeDouble>
        implements Builder {
      @Override
      protected Builder getThis() {
        return this;
      }

      @Override
      public GaugeDouble build() {
        return new NoopGaugeDouble(labelKeysSize);
      }
    }
  }

  /** No-op implementations of CounterDouble class. */
  @Immutable
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
    public void removeHandle(List<String> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues");
    }

    /** No-op implementations of Handle class. */
    @Immutable
    private static final class NoopHandle implements Handle {
      private static final NoopHandle INSTANCE = new NoopHandle();

      private NoopHandle() {}

      @Override
      public void add(double delta) {}
    }

    private static final class NoopBuilder
        extends NoopAbstractCounterBuilder<Builder, CounterDouble> implements Builder {
      @Override
      protected Builder getThis() {
        return this;
      }

      @Override
      public CounterDouble build() {
        return new NoopCounterDouble(labelKeysSize);
      }
    }
  }

  /** No-op implementations of CounterLong class. */
  @Immutable
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
    public void removeHandle(List<String> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues");
    }

    /** No-op implementations of Handle class. */
    @Immutable
    private static final class NoopHandle implements Handle {
      private static final NoopHandle INSTANCE = new NoopHandle();

      private NoopHandle() {}

      @Override
      public void add(long delta) {}
    }

    private static final class NoopBuilder extends NoopAbstractCounterBuilder<Builder, CounterLong>
        implements Builder {
      @Override
      protected Builder getThis() {
        return this;
      }

      @Override
      public CounterLong build() {
        return new NoopCounterLong(labelKeysSize);
      }
    }
  }

  @Immutable
  private static final class NoopMeasureDouble implements MeasureDouble {
    private final int labelKeysSize;

    /** Creates a new {@code NoopHandle}. */
    private NoopMeasureDouble(int labelKeysSize) {
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
    public void removeHandle(List<String> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues");
    }

    /** No-op implementations of Handle class. */
    @Immutable
    private static final class NoopHandle implements Handle {
      private static final NoopHandle INSTANCE = new NoopHandle();

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
    }

    private static final class NoopBuilder extends NoopAbstractMetricBuilder<Builder, MeasureDouble>
        implements Builder {
      @Override
      protected Builder getThis() {
        return this;
      }

      @Override
      public MeasureDouble build() {
        return new NoopMeasureDouble(labelKeysSize);
      }
    }
  }

  @Immutable
  private static final class NoopMeasureLong implements MeasureLong {
    private final int labelKeysSize;

    private NoopMeasureLong(int labelKeysSize) {
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
    public void removeHandle(List<String> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues");
    }

    /** No-op implementations of Handle class. */
    @Immutable
    private static final class NoopHandle implements Handle {
      private static final NoopHandle INSTANCE = new NoopHandle();

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
    }

    private static final class NoopBuilder extends NoopAbstractMetricBuilder<Builder, MeasureLong>
        implements Builder {
      @Override
      protected Builder getThis() {
        return this;
      }

      @Override
      public MeasureLong build() {
        return new NoopMeasureLong(labelKeysSize);
      }
    }
  }

  @Immutable
  private static final class NoopObserverDouble implements ObserverDouble {
    private final int labelKeysSize;

    private NoopObserverDouble(int labelKeysSize) {
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
    public void removeHandle(List<String> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues");
    }

    @Override
    public void setCallback(Callback<Result> metricUpdater) {
      Utils.checkNotNull(metricUpdater, "metricUpdater");
    }

    /** No-op implementations of Handle class. */
    @Immutable
    private static final class NoopHandle implements Handle {
      private static final NoopHandle INSTANCE = new NoopHandle();
    }

    private static final class NoopBuilder
        extends NoopAbstractObserverBuilder<Builder, ObserverDouble> implements Builder {
      @Override
      protected Builder getThis() {
        return this;
      }

      @Override
      public ObserverDouble build() {
        return new NoopObserverDouble(labelKeysSize);
      }
    }
  }

  @Immutable
  private static final class NoopObserverLong implements ObserverLong {
    private final int labelKeysSize;

    private NoopObserverLong(int labelKeysSize) {
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
    public void removeHandle(List<String> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues");
    }

    @Override
    public void setCallback(Callback<Result> metricUpdater) {
      Utils.checkNotNull(metricUpdater, "metricUpdater");
    }

    /** No-op implementations of Handle class. */
    @Immutable
    private static final class NoopHandle implements Handle {
      private static final NoopHandle INSTANCE = new NoopHandle();
    }

    private static final class NoopBuilder
        extends NoopAbstractObserverBuilder<Builder, ObserverLong> implements Builder {
      @Override
      protected Builder getThis() {
        return this;
      }

      @Override
      public ObserverLong build() {
        return new NoopObserverLong(labelKeysSize);
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

  private abstract static class NoopAbstractGaugeBuilder<B extends Gauge.Builder<B, V>, V>
      extends NoopAbstractMetricBuilder<B, V> implements Gauge.Builder<B, V> {
    @Override
    public B setMonotonic(boolean monotonic) {
      return getThis();
    }
  }

  private abstract static class NoopAbstractCounterBuilder<B extends Counter.Builder<B, V>, V>
      extends NoopAbstractMetricBuilder<B, V> implements Counter.Builder<B, V> {
    @Override
    public B setMonotonic(boolean monotonic) {
      return getThis();
    }
  }

  private abstract static class NoopAbstractObserverBuilder<B extends Observer.Builder<B, V>, V>
      extends NoopAbstractMetricBuilder<B, V> implements Observer.Builder<B, V> {
    @Override
    public B setMonotonic(boolean monotonic) {
      return getThis();
    }
  }

  private abstract static class NoopAbstractMetricBuilder<B extends Metric.Builder<B, V>, V>
      implements Metric.Builder<B, V> {
    protected int labelKeysSize = 0;

    @Override
    public B setDescription(String description) {
      Utils.checkNotNull(description, "description");
      return getThis();
    }

    @Override
    public B setUnit(String unit) {
      Utils.checkNotNull(unit, "unit");
      return getThis();
    }

    @Override
    public B setLabelKeys(List<String> labelKeys) {
      Utils.checkListElementNotNull(Utils.checkNotNull(labelKeys, "labelKeys"), "labelKey");
      labelKeysSize = labelKeys.size();
      return getThis();
    }

    @Override
    public B setConstantLabels(Map<String, String> constantLabels) {
      Utils.checkMapKeysNotNull(
          Utils.checkNotNull(constantLabels, "constantLabels"), "constantLabel");
      return getThis();
    }

    protected abstract B getThis();
  }
}
