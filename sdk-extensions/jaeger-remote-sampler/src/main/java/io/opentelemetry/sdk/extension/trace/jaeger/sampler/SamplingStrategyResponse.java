/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class SamplingStrategyResponse {

  enum SamplingStrategyType {
    PROBABILISTIC,
    RATE_LIMITING,
    UNRECOGNIZED,
  }

  static class RateLimitingSamplingStrategy {
    final int maxTracesPerSecond;

    private RateLimitingSamplingStrategy(Builder builder) {
      this.maxTracesPerSecond = builder.maxTracesPerSecond;
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

  static class ProbabilisticSamplingStrategy {
    final double samplingRate;

    ProbabilisticSamplingStrategy(Builder builder) {
      this.samplingRate = builder.samplingRate;
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

  static class PerOperationSamplingStrategies {
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

  static class OperationSamplingStrategy {
    final String operation;
    final ProbabilisticSamplingStrategy probabilisticSamplingStrategy;

    private OperationSamplingStrategy(Builder builder) {
      this.operation = builder.operation;
      this.probabilisticSamplingStrategy = builder.probabilisticSamplingStrategy;
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
