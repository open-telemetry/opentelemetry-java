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

import io.opentelemetry.internal.StringUtils;
import io.opentelemetry.internal.Utils;
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
  public DoubleCounter.Builder doubleCounterBuilder(String name) {
    Utils.checkNotNull(name, "name");
    Utils.checkArgument(
        StringUtils.isPrintableString(name) && name.length() <= NAME_MAX_LENGTH,
        ERROR_MESSAGE_INVALID_NAME);
    return new NoopDoubleCounter.NoopBuilder();
  }

  @Override
  public LongCounter.Builder longCounterBuilder(String name) {
    Utils.checkNotNull(name, "name");
    Utils.checkArgument(
        StringUtils.isPrintableString(name) && name.length() <= NAME_MAX_LENGTH,
        ERROR_MESSAGE_INVALID_NAME);
    return new NoopLongCounter.NoopBuilder();
  }

  @Override
  public DoubleMeasure.Builder doubleMeasureBuilder(String name) {
    Utils.checkNotNull(name, "name");
    Utils.checkArgument(
        StringUtils.isPrintableString(name) && name.length() <= NAME_MAX_LENGTH,
        ERROR_MESSAGE_INVALID_NAME);
    return new NoopDoubleMeasure.NoopBuilder();
  }

  @Override
  public LongMeasure.Builder longMeasureBuilder(String name) {
    Utils.checkNotNull(name, "name");
    Utils.checkArgument(
        StringUtils.isPrintableString(name) && name.length() <= NAME_MAX_LENGTH,
        ERROR_MESSAGE_INVALID_NAME);
    return new NoopLongMeasure.NoopBuilder();
  }

  @Override
  public DoubleObserver.Builder doubleObserverBuilder(String name) {
    Utils.checkNotNull(name, "name");
    Utils.checkArgument(
        StringUtils.isPrintableString(name) && name.length() <= NAME_MAX_LENGTH,
        ERROR_MESSAGE_INVALID_NAME);
    return new NoopDoubleObserver.NoopBuilder();
  }

  @Override
  public LongObserver.Builder longObserverBuilder(String name) {
    Utils.checkNotNull(name, "name");
    Utils.checkArgument(
        StringUtils.isPrintableString(name) && name.length() <= NAME_MAX_LENGTH,
        ERROR_MESSAGE_INVALID_NAME);
    return new NoopLongObserver.NoopBuilder();
  }

  @Override
  public BatchRecorder newBatchRecorder() {
    return new NoopBatchRecorder();
  }

  @Override
  public LabelSet createLabelSet(String... keyValuePairs) {
    Utils.checkArgument(
        keyValuePairs.length % 2 == 0,
        "You must provide an even number of key/value pair arguments.");
    for (int i = 0; i < keyValuePairs.length; i += 2) {
      String key = keyValuePairs[i];
      Utils.checkNotNull(key, "You cannot provide null keys for LabelSet creation.");
    }
    return NoopLabelSet.INSTANCE;
  }

  @Override
  public LabelSet createLabelSet(Map<String, String> labels) {
    Utils.checkNotNull(labels, "labels");
    Utils.checkMapKeysNotNull(labels, "Null map keys are not allowed for LabelSet creation");
    return NoopLabelSet.INSTANCE;
  }

  /** No-op implementation of DoubleCounter interface. */
  @Immutable
  private static final class NoopDoubleCounter implements DoubleCounter {

    /** Creates a new {@code NoopBound}. */
    private NoopDoubleCounter() {}

    @Override
    public void add(double delta, LabelSet labelSet) {}

    @Override
    public NoopBoundDoubleCounter bind(LabelSet labelSet) {
      Utils.checkNotNull(labelSet, "labelSet");
      return NoopBoundDoubleCounter.INSTANCE;
    }

    @Override
    public void unbind(BoundDoubleCounter boundInstrument) {
      Utils.checkNotNull(boundInstrument, "boundDoubleCounter");
    }

    /** No-op implementation of BoundDoubleCounter interface. */
    @Immutable
    private enum NoopBoundDoubleCounter implements BoundDoubleCounter {
      INSTANCE;

      @Override
      public void add(double delta) {}
    }

    private static final class NoopBuilder
        extends NoopAbstractCounterBuilder<Builder, DoubleCounter> implements Builder {

      @Override
      protected Builder getThis() {
        return this;
      }

      @Override
      public DoubleCounter build() {
        return new NoopDoubleCounter();
      }
    }
  }

  /** No-op implementation of CounterLong interface. */
  @Immutable
  private static final class NoopLongCounter implements LongCounter {

    /** Creates a new {@code NoopBound}. */
    private NoopLongCounter() {}

    @Override
    public void add(long delta, LabelSet labelSet) {}

    @Override
    public NoopBoundLongCounter bind(LabelSet labelSet) {
      Utils.checkNotNull(labelSet, "labelSet");
      return NoopBoundLongCounter.INSTANCE;
    }

    @Override
    public void unbind(BoundLongCounter boundInstrument) {
      Utils.checkNotNull(boundInstrument, "boundLongCounter");
    }

    /** No-op implementation of BoundLongCounter interface. */
    @Immutable
    private enum NoopBoundLongCounter implements BoundLongCounter {
      INSTANCE;

      @Override
      public void add(long delta) {}
    }

    private static final class NoopBuilder extends NoopAbstractCounterBuilder<Builder, LongCounter>
        implements Builder {

      @Override
      protected Builder getThis() {
        return this;
      }

      @Override
      public LongCounter build() {
        return new NoopLongCounter();
      }
    }
  }

  @Immutable
  private static final class NoopDoubleMeasure implements DoubleMeasure {

    /** Creates a new {@code NoopDoubleMeasure}. */
    private NoopDoubleMeasure() {}

    @Override
    public void record(double value, LabelSet labelSet) {
      Utils.checkArgument(value >= 0.0, "Unsupported negative values.");
    }

    @Override
    public NoopBoundDoubleMeasure bind(LabelSet labelSet) {
      Utils.checkNotNull(labelSet, "labelSet");
      return NoopBoundDoubleMeasure.INSTANCE;
    }

    @Override
    public void unbind(BoundDoubleMeasure boundInstrument) {
      Utils.checkNotNull(boundInstrument, "boundDoubleMeasure");
    }

    /** No-op implementation of BoundDoubleMeasure interface. */
    @Immutable
    private enum NoopBoundDoubleMeasure implements BoundDoubleMeasure {
      INSTANCE;

      @Override
      public void record(double value) {
        Utils.checkArgument(value >= 0.0, "Unsupported negative values.");
      }
    }

    private static final class NoopBuilder
        extends NoopAbstractInstrumentBuilder<Builder, DoubleMeasure> implements Builder {

      @Override
      protected Builder getThis() {
        return this;
      }

      @Override
      public DoubleMeasure build() {
        return new NoopDoubleMeasure();
      }

      @Override
      public Builder setAbsolute(boolean absolute) {
        return this;
      }
    }
  }

  @Immutable
  private static final class NoopLongMeasure implements LongMeasure {

    private NoopLongMeasure() {}

    @Override
    public void record(long value, LabelSet labelSet) {
      Utils.checkArgument(value >= 0, "Unsupported negative values.");
    }

    @Override
    public NoopBoundLongMeasure bind(LabelSet labelSet) {
      Utils.checkNotNull(labelSet, "labelSet");
      return NoopBoundLongMeasure.INSTANCE;
    }

    @Override
    public void unbind(BoundLongMeasure boundInstrument) {
      Utils.checkNotNull(boundInstrument, "boundLongMeasure");
    }

    /** No-op implementations of BoundLongMeasure interface. */
    @Immutable
    private enum NoopBoundLongMeasure implements BoundLongMeasure {
      INSTANCE;

      @Override
      public void record(long value) {
        Utils.checkArgument(value >= 0, "Unsupported negative values.");
      }
    }

    private static final class NoopBuilder
        extends NoopAbstractInstrumentBuilder<Builder, LongMeasure> implements Builder {

      @Override
      protected Builder getThis() {
        return this;
      }

      @Override
      public LongMeasure build() {
        return new NoopLongMeasure();
      }

      @Override
      public Builder setAbsolute(boolean absolute) {
        return this;
      }
    }
  }

  @Immutable
  private static final class NoopDoubleObserver implements DoubleObserver {

    private NoopDoubleObserver() {}

    @Override
    public void setCallback(Callback<ResultDoubleObserver> metricUpdater) {
      Utils.checkNotNull(metricUpdater, "metricUpdater");
    }

    private static final class NoopBuilder
        extends NoopAbstractObserverBuilder<Builder, DoubleObserver> implements Builder {

      @Override
      protected Builder getThis() {
        return this;
      }

      @Override
      public DoubleObserver build() {
        return new NoopDoubleObserver();
      }
    }
  }

  @Immutable
  private static final class NoopLongObserver implements LongObserver {

    private NoopLongObserver() {}

    @Override
    public void setCallback(Callback<ResultLongObserver> metricUpdater) {
      Utils.checkNotNull(metricUpdater, "metricUpdater");
    }

    private static final class NoopBuilder
        extends NoopAbstractObserverBuilder<Builder, LongObserver> implements Builder {

      @Override
      protected Builder getThis() {
        return this;
      }

      @Override
      public LongObserver build() {
        return new NoopLongObserver();
      }
    }
  }

  private static final class NoopBatchRecorder implements BatchRecorder {

    private NoopBatchRecorder() {}

    @Override
    public BatchRecorder put(LongMeasure measure, long value) {
      Utils.checkNotNull(measure, "measure");
      Utils.checkArgument(value >= 0, "Unsupported negative values.");
      return this;
    }

    @Override
    public BatchRecorder put(DoubleMeasure measure, double value) {
      Utils.checkNotNull(measure, "measure");
      Utils.checkArgument(value >= 0.0, "Unsupported negative values.");
      return this;
    }

    @Override
    public void record() {}
  }

  private abstract static class NoopAbstractCounterBuilder<B extends Counter.Builder<B, V>, V>
      extends NoopAbstractInstrumentBuilder<B, V> implements Counter.Builder<B, V> {

    @Override
    public B setMonotonic(boolean monotonic) {
      return getThis();
    }
  }

  private abstract static class NoopAbstractObserverBuilder<B extends Observer.Builder<B, V>, V>
      extends NoopAbstractInstrumentBuilder<B, V> implements Observer.Builder<B, V> {

    @Override
    public B setMonotonic(boolean monotonic) {
      return getThis();
    }
  }

  private abstract static class NoopAbstractInstrumentBuilder<B extends Instrument.Builder<B, V>, V>
      implements Instrument.Builder<B, V> {

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

  private enum NoopLabelSet implements LabelSet {
    INSTANCE
  }
}
