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
  private static final String COUNTERS_CAN_ONLY_INCREASE = "Counters can only increase";

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
  public DoubleUpDownCounter.Builder doubleUpDownCounterBuilder(String name) {
    Utils.checkNotNull(name, "name");
    Utils.checkArgument(StringUtils.isValidMetricName(name), ERROR_MESSAGE_INVALID_NAME);
    return new NoopDoubleUpDownCounter.NoopBuilder();
  }

  @Override
  public LongUpDownCounter.Builder longUpDownCounterBuilder(String name) {
    Utils.checkNotNull(name, "name");
    Utils.checkArgument(StringUtils.isValidMetricName(name), ERROR_MESSAGE_INVALID_NAME);
    return new NoopLongUpDownCounter.NoopBuilder();
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
  public DoubleSumObserver.Builder doubleSumObserverBuilder(String name) {
    Utils.checkNotNull(name, "name");
    Utils.checkArgument(StringUtils.isValidMetricName(name), ERROR_MESSAGE_INVALID_NAME);
    return new NoopDoubleSumObserver.NoopBuilder();
  }

  @Override
  public LongSumObserver.Builder longSumObserverBuilder(String name) {
    Utils.checkNotNull(name, "name");
    Utils.checkArgument(StringUtils.isValidMetricName(name), ERROR_MESSAGE_INVALID_NAME);
    return new NoopLongSumObserver.NoopBuilder();
  }

  @Override
  public BatchRecorder newBatchRecorder(String... keyValuePairs) {
    Utils.validateLabelPairs(keyValuePairs);
    return NoopBatchRecorder.INSTANCE;
  }

  private DefaultMeter() {}

  /** No-op implementation of DoubleCounter interface. */
  @Immutable
  private static final class NoopDoubleCounter implements DoubleCounter {

    /** Creates a new {@code NoopBound}. */
    private NoopDoubleCounter() {}

    @Override
    public void add(double increment, String... labelKeyValuePairs) {
      Utils.validateLabelPairs(labelKeyValuePairs);
      Utils.checkArgument(increment >= 0.0, COUNTERS_CAN_ONLY_INCREASE);
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
      public void add(double increment) {
        Utils.checkArgument(increment >= 0.0, COUNTERS_CAN_ONLY_INCREASE);
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
    public void add(long increment, String... labelKeyValuePairs) {
      Utils.validateLabelPairs(labelKeyValuePairs);
      Utils.checkArgument(increment >= 0, COUNTERS_CAN_ONLY_INCREASE);
    }

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
      public void add(long increment) {
        Utils.checkArgument(increment >= 0, COUNTERS_CAN_ONLY_INCREASE);
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
      public LongCounter build() {
        return new NoopLongCounter();
      }
    }
  }

  /** No-op implementation of DoubleUpDownCounter interface. */
  @Immutable
  private static final class NoopDoubleUpDownCounter implements DoubleUpDownCounter {

    /** Creates a new {@code NoopBound}. */
    private NoopDoubleUpDownCounter() {}

    @Override
    public void add(double increment, String... labelKeyValuePairs) {
      Utils.validateLabelPairs(labelKeyValuePairs);
    }

    @Override
    public NoopBoundDoubleUpDownCounter bind(String... labelKeyValuePairs) {
      Utils.validateLabelPairs(labelKeyValuePairs);
      return NoopBoundDoubleUpDownCounter.INSTANCE;
    }

    /** No-op implementation of BoundDoubleUpDownCounter interface. */
    @Immutable
    private enum NoopBoundDoubleUpDownCounter implements BoundDoubleUpDownCounter {
      INSTANCE;

      @Override
      public void add(double increment) {}

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
      public DoubleUpDownCounter build() {
        return new NoopDoubleUpDownCounter();
      }
    }
  }

  /** No-op implementation of LongUpDownCounter interface. */
  @Immutable
  private static final class NoopLongUpDownCounter implements LongUpDownCounter {

    /** Creates a new {@code NoopBound}. */
    private NoopLongUpDownCounter() {}

    @Override
    public void add(long increment, String... labelKeyValuePairs) {}

    @Override
    public NoopBoundLongUpDownCounter bind(String... labelKeyValuePairs) {
      Utils.validateLabelPairs(labelKeyValuePairs);
      return NoopBoundLongUpDownCounter.INSTANCE;
    }

    /** No-op implementation of BoundLongUpDownCounter interface. */
    @Immutable
    private enum NoopBoundLongUpDownCounter implements BoundLongUpDownCounter {
      INSTANCE;

      @Override
      public void add(long increment) {}

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
      public LongUpDownCounter build() {
        return new NoopLongUpDownCounter();
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
  private static final class NoopDoubleSumObserver implements DoubleSumObserver {

    private NoopDoubleSumObserver() {}

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
      public DoubleSumObserver build() {
        return new NoopDoubleSumObserver();
      }
    }
  }

  @Immutable
  private static final class NoopLongSumObserver implements LongSumObserver {

    private NoopLongSumObserver() {}

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
      public LongSumObserver build() {
        return new NoopLongSumObserver();
      }
    }
  }

  private enum NoopBatchRecorder implements BatchRecorder {
    INSTANCE;

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
      Utils.checkArgument(value >= 0, COUNTERS_CAN_ONLY_INCREASE);
      return this;
    }

    @Override
    public BatchRecorder put(DoubleCounter counter, double value) {
      Utils.checkNotNull(counter, "counter");
      Utils.checkArgument(value >= 0.0, COUNTERS_CAN_ONLY_INCREASE);
      return this;
    }

    @Override
    public BatchRecorder put(LongUpDownCounter upDownCounter, long value) {
      Utils.checkNotNull(upDownCounter, "upDownCounter");
      return this;
    }

    @Override
    public BatchRecorder put(DoubleUpDownCounter upDownCounter, double value) {
      Utils.checkNotNull(upDownCounter, "upDownCounter");
      return this;
    }

    @Override
    public void record() {}
  }

  private abstract static class NoopAbstractObserverBuilder<
          B extends NoopAbstractObserverBuilder<B>>
      extends NoopAbstractInstrumentBuilder<B> implements AsynchronousInstrument.Builder {

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
