/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

public interface ExponentialCounterFactory {
  /**
   * Constructs a new {@link io.opentelemetry.sdk.metrics.internal.state.ExponentialCounter} with
   * maximum bucket size.
   *
   * @param maxSize The maximum number of buckets allowed in the counter.
   */
  ExponentialCounter newCounter(int maxSize);

  /** Returns a deep-copy of an ExponentialCounter. */
  ExponentialCounter copy(ExponentialCounter other);

  /** Constructs exponential counters using {@link MapCounter}. */
  static ExponentialCounterFactory mapCounter() {
    return new ExponentialCounterFactory() {
      @Override
      public ExponentialCounter newCounter(int maxSize) {
        return new MapCounter(maxSize);
      }

      @Override
      public ExponentialCounter copy(ExponentialCounter other) {
        return new MapCounter(other);
      }

      @Override
      public String toString() {
        return "mapCounter";
      }
    };
  }
  /** Constructs exponential counters using {@link AdaptingCircularBufferCounter}. */
  static ExponentialCounterFactory circularBufferCounter() {
    return new ExponentialCounterFactory() {
      @Override
      public ExponentialCounter newCounter(int maxSize) {
        return new AdaptingCircularBufferCounter(maxSize);
      }

      @Override
      public ExponentialCounter copy(ExponentialCounter other) {
        return new AdaptingCircularBufferCounter(other);
      }

      @Override
      public String toString() {
        return "circularBufferCounter";
      }
    };
  }
}
