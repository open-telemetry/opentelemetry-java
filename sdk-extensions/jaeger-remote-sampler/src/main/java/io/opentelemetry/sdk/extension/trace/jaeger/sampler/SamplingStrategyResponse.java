/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

final class SamplingStrategyResponse {

  enum SamplingStrategyType {
    PROBABILISTIC,
    RATE_LIMITING,
    UNRECOGNIZED,
  }

  static final class RateLimitingSamplingStrategy {
    final int maxTracesPerSecond;

    private RateLimitingSamplingStrategy(Builder builder) {
      this.maxTracesPerSecond = builder.maxTracesPerSecond;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (!(o instanceof RateLimitingSamplingStrategy)) {
        return false;
      }
      RateLimitingSamplingStrategy that = (RateLimitingSamplingStrategy) o;
      return maxTracesPerSecond == that.maxTracesPerSecond;
    }

    @Override
    public int hashCode() {
      return Integer.hashCode(maxTracesPerSecond);
    }

    static class Builder {
      private int maxTracesPerSecond;

      Builder setMaxTracesPerSecond(int maxTracesPerSecond) {
        this.maxTracesPerSecond = maxTracesPerSecond;
        return this;
      }

      RateLimitingSamplingStrategy build() {
        return new RateLimitingSamplingStrategy(this);
      }
    }
  }

  static final class ProbabilisticSamplingStrategy {
    final double samplingRate;

    ProbabilisticSamplingStrategy(Builder builder) {
      this.samplingRate = builder.samplingRate;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (!(o instanceof ProbabilisticSamplingStrategy)) {
        return false;
      }
      ProbabilisticSamplingStrategy that = (ProbabilisticSamplingStrategy) o;
      return Double.doubleToLongBits(samplingRate) == Double.doubleToLongBits(that.samplingRate);
    }

    @Override
    public int hashCode() {
      return Double.hashCode(samplingRate);
    }

    static class Builder {
      private double samplingRate;

      Builder setSamplingRate(double samplingRate) {
        this.samplingRate = samplingRate;
        return this;
      }

      ProbabilisticSamplingStrategy build() {
        return new ProbabilisticSamplingStrategy(this);
      }
    }
  }

  static final class PerOperationSamplingStrategies {
    final double defaultSamplingProbability;
    final double defaultLowerBoundTracesPerSecond;
    final double defaultUpperBoundTracesPerSecond;
    final List<OperationSamplingStrategy> strategies;

    private PerOperationSamplingStrategies(Builder builder) {
      this.defaultSamplingProbability = builder.defaultSamplingProbability;
      this.defaultLowerBoundTracesPerSecond = builder.defaultLowerBoundTracesPerSecond;
      this.defaultUpperBoundTracesPerSecond = builder.defaultUpperBoundTracesPerSecond;
      this.strategies = Collections.unmodifiableList(builder.strategies);
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (!(o instanceof PerOperationSamplingStrategies)) {
        return false;
      }
      PerOperationSamplingStrategies that = (PerOperationSamplingStrategies) o;
      return Double.doubleToLongBits(defaultSamplingProbability)
              == Double.doubleToLongBits(that.defaultSamplingProbability)
          && Double.doubleToLongBits(defaultLowerBoundTracesPerSecond)
              == Double.doubleToLongBits(that.defaultLowerBoundTracesPerSecond)
          && Double.doubleToLongBits(defaultUpperBoundTracesPerSecond)
              == Double.doubleToLongBits(that.defaultUpperBoundTracesPerSecond)
          && strategies.equals(that.strategies);
    }

    @Override
    public int hashCode() {
      int result = Double.hashCode(defaultSamplingProbability);
      result = 31 * result + Double.hashCode(defaultLowerBoundTracesPerSecond);
      result = 31 * result + Double.hashCode(defaultUpperBoundTracesPerSecond);
      result = 31 * result + strategies.hashCode();
      return result;
    }

    static class Builder {
      private double defaultSamplingProbability;
      private double defaultLowerBoundTracesPerSecond;
      private double defaultUpperBoundTracesPerSecond;
      private final List<OperationSamplingStrategy> strategies = new ArrayList<>();

      Builder setDefaultSamplingProbability(double defaultSamplingProbability) {
        this.defaultSamplingProbability = defaultSamplingProbability;
        return this;
      }

      Builder setDefaultLowerBoundTracesPerSecond(double defaultLowerBoundTracesPerSecond) {
        this.defaultLowerBoundTracesPerSecond = defaultLowerBoundTracesPerSecond;
        return this;
      }

      Builder setDefaultUpperBoundTracesPerSecond(double defaultUpperBoundTracesPerSecond) {
        this.defaultUpperBoundTracesPerSecond = defaultUpperBoundTracesPerSecond;
        return this;
      }

      Builder addOperationStrategy(OperationSamplingStrategy operationSamplingStrategy) {
        this.strategies.add(operationSamplingStrategy);
        return this;
      }

      PerOperationSamplingStrategies build() {
        return new PerOperationSamplingStrategies(this);
      }
    }
  }

  static final class OperationSamplingStrategy {
    final String operation;
    final ProbabilisticSamplingStrategy probabilisticSamplingStrategy;

    private OperationSamplingStrategy(Builder builder) {
      this.operation = builder.operation;
      this.probabilisticSamplingStrategy = builder.probabilisticSamplingStrategy;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (!(o instanceof OperationSamplingStrategy)) {
        return false;
      }
      OperationSamplingStrategy that = (OperationSamplingStrategy) o;
      return operation.equals(that.operation)
          && probabilisticSamplingStrategy.equals(that.probabilisticSamplingStrategy);
    }

    @Override
    public int hashCode() {
      return 31 * operation.hashCode() + probabilisticSamplingStrategy.hashCode();
    }

    static class Builder {
      private String operation = "";
      private ProbabilisticSamplingStrategy probabilisticSamplingStrategy =
          new ProbabilisticSamplingStrategy.Builder().build();

      Builder setOperation(String operation) {
        this.operation = operation;
        return this;
      }

      Builder setProbabilisticSamplingStrategy(
          ProbabilisticSamplingStrategy probabilisticSamplingStrategy) {
        this.probabilisticSamplingStrategy = probabilisticSamplingStrategy;
        return this;
      }

      OperationSamplingStrategy build() {
        return new OperationSamplingStrategy(this);
      }
    }
  }

  final SamplingStrategyType strategyType;
  final RateLimitingSamplingStrategy rateLimitingSamplingStrategy;
  final ProbabilisticSamplingStrategy probabilisticSamplingStrategy;
  final PerOperationSamplingStrategies perOperationSamplingStrategies;

  private SamplingStrategyResponse(Builder builder) {
    this.strategyType = builder.samplingStrategyType;
    this.rateLimitingSamplingStrategy = builder.rateLimitingSamplingStrategy;
    this.probabilisticSamplingStrategy = builder.probabilisticSamplingStrategy;
    this.perOperationSamplingStrategies = builder.perOperationSamplingStrategies;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (!(o instanceof SamplingStrategyResponse)) {
      return false;
    }
    SamplingStrategyResponse that = (SamplingStrategyResponse) o;
    return strategyType == that.strategyType
        && rateLimitingSamplingStrategy.equals(that.rateLimitingSamplingStrategy)
        && probabilisticSamplingStrategy.equals(that.probabilisticSamplingStrategy)
        && perOperationSamplingStrategies.equals(that.perOperationSamplingStrategies);
  }

  @Override
  public int hashCode() {
    int result = strategyType.hashCode();
    result = 31 * result + rateLimitingSamplingStrategy.hashCode();
    result = 31 * result + probabilisticSamplingStrategy.hashCode();
    result = 31 * result + perOperationSamplingStrategies.hashCode();
    return result;
  }

  static class Builder {
    private SamplingStrategyType samplingStrategyType = SamplingStrategyType.UNRECOGNIZED;
    private RateLimitingSamplingStrategy rateLimitingSamplingStrategy =
        new RateLimitingSamplingStrategy.Builder().build();
    private ProbabilisticSamplingStrategy probabilisticSamplingStrategy =
        new ProbabilisticSamplingStrategy.Builder().build();
    private PerOperationSamplingStrategies perOperationSamplingStrategies =
        new PerOperationSamplingStrategies.Builder().build();

    Builder setSamplingStrategyType(SamplingStrategyType samplingStrategyType) {
      this.samplingStrategyType = samplingStrategyType;
      return this;
    }

    Builder setRateLimitingSamplingStrategy(
        RateLimitingSamplingStrategy rateLimitingSamplingStrategy) {
      this.rateLimitingSamplingStrategy = rateLimitingSamplingStrategy;
      // https://github.com/open-telemetry/opentelemetry-java/issues/4319
      // Jaeger does not always return the samplingType (e.g., for the default strategy)
      // We explicitly assign the correct value to samplingType
      return setSamplingStrategyType(SamplingStrategyType.RATE_LIMITING);
    }

    Builder setProbabilisticSamplingStrategy(
        ProbabilisticSamplingStrategy probabilisticSamplingStrategy) {
      this.probabilisticSamplingStrategy = probabilisticSamplingStrategy;
      // https://github.com/open-telemetry/opentelemetry-java/issues/4319
      // Jaeger does not always return the samplingType (e.g., for the default strategy)
      // We explicitly assign the correct value to samplingType
      return setSamplingStrategyType(SamplingStrategyType.PROBABILISTIC);
    }

    Builder setPerOperationSamplingStrategies(
        PerOperationSamplingStrategies perOperationSamplingStrategies) {
      this.perOperationSamplingStrategies = perOperationSamplingStrategies;
      return this;
    }

    SamplingStrategyResponse build() {
      return new SamplingStrategyResponse(this);
    }
  }
}
