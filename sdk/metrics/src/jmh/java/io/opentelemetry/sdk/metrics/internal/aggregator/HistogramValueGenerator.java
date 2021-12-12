/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;

/** Methods of generating values for histogram benchmarks. */
@SuppressWarnings("ImmutableEnumChecker")
public enum HistogramValueGenerator {
  SAME_VALUE(() -> 100.0056),
  RANDOM_WITHIN_2K(() -> ThreadLocalRandom.current().nextDouble(2000));

  private final DoubleSupplier generator;

  private HistogramValueGenerator(DoubleSupplier generator) {
    this.generator = generator;
  }

  public final double generateValue() {
    return this.generator.getAsDouble();
  }
}
