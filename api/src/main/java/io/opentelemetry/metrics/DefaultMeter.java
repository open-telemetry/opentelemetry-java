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
import java.util.Map;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * No-op implementations of {@link Meter}.
 *
 * @since 0.1.0
 */
@ThreadSafe
public final class DefaultMeter implements Meter {

  private static final DefaultMeter INSTANCE = new DefaultMeter();

  /* VisibleForTesting */ static final String ERROR_MESSAGE_INVALID_NAME =
      "Name should be a ASCII string with a length no greater than "
          + StringUtils.NAME_MAX_LENGTH
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
    Utils.checkArgument(StringUtils.isValidMetricName(name), ERROR_MESSAGE_INVALID_NAME);
    return new NoopDoubleCounter.NoopBuilder();
  }

  @Override
  public LongCounter.Builder longCounterBuilder(String name) {
    Utils.checkNotNull(name, "name");
    Utils.checkArgument(StringUtils.isValidMetricName(name), ERROR_MESSAGE_INVALID_NAME);
    return new NoopLongCounter.NoopBuilder();
  }

  @Override
  public DoubleMeasure.Builder doubleMeasureBuilder(String name) {
    Utils.checkNotNull(name, "name");
    Utils.checkArgument(StringUtils.isValidMetricName(name), ERROR_MESSAGE_INVALID_NAME);
    return new NoopDoubleMeasure.NoopBuilder();
  }

  @Override
  public LongMeasure.Builder longMeasureBuilder(String name) {
    Utils.checkNotNull(name, "name");
    Utils.checkArgument(StringUtils.isValidMetricName(name), ERROR_MESSAGE_INVALID_NAME);
    return new NoopLongMeasure.NoopBuilder();
  }

  @Override
  public DoubleObserver.Builder doubleObserverBuilder(String name) {
    Utils.checkNotNull(name, "name");
    Utils.checkArgument(StringUtils.isValidMetricName(name), ERROR_MESSAGE_INVALID_NAME);
    return new NoopDoubleObserver.NoopBuilder();
  }

  @Override
  public LongObserver.Builder longObserverBuilder(String name) {
    Utils.checkNotNull(name, "name");
    Utils.checkArgument(StringUtils.isValidMetricName(name), ERROR_MESSAGE_INVALID_NAME);
    return new NoopLongObserver.NoopBuilder();
  }

  @Override
  public BatchRecorder newBatchRecorder(String... keyValuePairs) {
    Utils.validateLabelPairs(keyValuePairs);
    return new NoopBatchRecorder();
  }

  private DefaultMeter() {}

  /** No-op implementation of DoubleCounter interface. */
  @Immutable
  private static final class NoopDoubleCounter implements DoubleCounter {

    /** Creates a new {@code NoopBound}. */
    private NoopDoubleCounter() {}

    @Override
    public void add(double delta, String... labelKeyValuePairs) {
      Utils.validateLabelPairs(labelKeyValuePairs);
    }

    @Override
    public NoopBoundDoubleCounter bind(String... labelKeyValuePairs) {
      Utils.validateLabelPairs(labelKeyValuePairs);
      return NoopBoundDoubleCounter.INSTANCE;
    }

    /** No-op implementation of BoundDoubleCounter interface. */
    @Immutable
    private enum NoopBoundDoubleCounter implements BoundDoubleCounter {
      INSTANCE;

      @Override
      public void add(double delta) {}

      @Override
      public void unbind() {}
    }

    private static final class NoopBuilder extends NoopAbstractCounterBuilder<NoopBuilder>
        implements Builder {

      @Override
      protected NoopBuilder getThis() {
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
    public void add(long delta, String... labelKeyValuePairs) {}

    @Override
    public NoopBoundLongCounter bind(String... labelKeyValuePairs) {
      Utils.validateLabelPairs(labelKeyValuePairs);
      return NoopBoundLongCounter.INSTANCE;
    }

    /** No-op implementation of BoundLongCounter interface. */
    @Immutable
    private enum NoopBoundLongCounter implements BoundLongCounter {
      INSTANCE;

      @Override
      public void add(long delta) {}

      @Override
      public void unbind() {}
    }

    private static final class NoopBuilder extends NoopAbstractCounterBuilder<NoopBuilder>
        implements Builder {

      @Override
      protected NoopBuilder getThis() {
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
    public void record(double value, String... labelKeyValuePairs) {
      Utils.checkArgument(value >= 0.0, "Unsupported negative values.");
      Utils.validateLabelPairs(labelKeyValuePairs);
    }

    @Override
    public NoopBoundDoubleMeasure bind(String... labelKeyValuePairs) {
      Utils.validateLabelPairs(labelKeyValuePairs);
      return NoopBoundDoubleMeasure.INSTANCE;
    }

    /** No-op implementation of BoundDoubleMeasure interface. */
    @Immutable
    private enum NoopBoundDoubleMeasure implements BoundDoubleMeasure {
      INSTANCE;

      @Override
      public void record(double value) {
        Utils.checkArgument(value >= 0.0, "Unsupported negative values.");
      }

      @Override
      public void unbind() {}
    }

    private static final class NoopBuilder extends NoopAbstractInstrumentBuilder<NoopBuilder>
        implements Builder {

      @Override
      protected NoopBuilder getThis() {
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
    public void record(long value, String... labelKeyValuePairs) {
      Utils.checkArgument(value >= 0, "Unsupported negative values.");
      Utils.validateLabelPairs(labelKeyValuePairs);
    }

    @Override
    public NoopBoundLongMeasure bind(String... labelKeyValuePairs) {
      Utils.validateLabelPairs(labelKeyValuePairs);
      return NoopBoundLongMeasure.INSTANCE;
    }

    /** No-op implementations of BoundLongMeasure interface. */
    @Immutable
    private enum NoopBoundLongMeasure implements BoundLongMeasure {
      INSTANCE;

      @Override
      public void record(long value) {
        Utils.checkArgument(value >= 0, "Unsupported negative values.");
      }

      @Override
      public void unbind() {}
    }

    private static final class NoopBuilder extends NoopAbstractInstrumentBuilder<NoopBuilder>
        implements Builder {

      @Override
      protected NoopBuilder getThis() {
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

    private static final class NoopBuilder extends NoopAbstractObserverBuilder<NoopBuilder>
        implements Builder {

      @Override
      protected NoopBuilder getThis() {
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

    private static final class NoopBuilder extends NoopAbstractObserverBuilder<NoopBuilder>
        implements Builder {

      @Override
      protected NoopBuilder getThis() {
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
      return this;
    }

    @Override
    public BatchRecorder put(DoubleMeasure measure, double value) {
      Utils.checkNotNull(measure, "measure");
      return this;
    }

    @Override
    public BatchRecorder put(LongCounter counter, long value) {
      Utils.checkNotNull(counter, "counter");
      return this;
    }

    @Override
    public BatchRecorder put(DoubleCounter counter, double value) {
      Utils.checkNotNull(counter, "counter");
      return this;
    }

    @Override
    public void record() {}
  }

  private abstract static class NoopAbstractCounterBuilder<B extends NoopAbstractCounterBuilder<B>>
      extends NoopAbstractInstrumentBuilder<B> implements Counter.Builder {

    @Override
    public B setMonotonic(boolean monotonic) {
      return getThis();
    }
  }

  private abstract static class NoopAbstractObserverBuilder<
          B extends NoopAbstractObserverBuilder<B>>
      extends NoopAbstractInstrumentBuilder<B> implements Observer.Builder {

    @Override
    public B setMonotonic(boolean monotonic) {
      return getThis();
    }
  }

  private abstract static class NoopAbstractInstrumentBuilder<
          B extends NoopAbstractInstrumentBuilder<B>>
      implements Instrument.Builder {

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
    public B setConstantLabels(Map<String, String> constantLabels) {
      Utils.checkMapKeysNotNull(
          Utils.checkNotNull(constantLabels, "constantLabels"), "constantLabel");
      return getThis();
    }

    protected abstract B getThis();
  }
}
