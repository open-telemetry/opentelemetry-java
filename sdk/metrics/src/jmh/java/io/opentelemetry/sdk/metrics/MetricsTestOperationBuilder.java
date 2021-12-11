/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This enum allows for iteration over all of the operations that we want to benchmark. To ensure
 * that the enum cannot change state, each enum holds a builder function- passing a meter in will
 * return a wrapper for both bound and unbound versions of that operation which can then be used in
 * a benchmark.
 */
@SuppressWarnings("ImmutableEnumChecker")
public enum MetricsTestOperationBuilder {
  LongCounterAdd(
      meter -> {
        return new Operation() {
          final LongCounter metric = meter.counterBuilder("long_counter").build();

          @Override
          public void perform(Attributes labels) {
            metric.add(5L, labels);
          }
        };
      }),
  DoubleCounterAdd(
      meter -> {
        return new Operation() {
          final DoubleCounter metric = meter.counterBuilder("double_counter").ofDoubles().build();

          @Override
          public void perform(Attributes labels) {
            metric.add(5.0d, labels);
          }
        };
      }),
  DoubleHistogramRecord(
      meter -> {
        return new Operation() {
          final DoubleHistogram metric =
              meter.histogramBuilder("double_histogram_recorder").build();

          @Override
          public void perform(Attributes labels) {
            // We record different values to try to hit more areas of the histogram buckets.
            metric.record(ThreadLocalRandom.current().nextDouble(0, 20_000d), labels);
          }
        };
      }),
  LongHistogramRecord(
      meter -> {
        return new Operation() {
          final LongHistogram metric =
              meter.histogramBuilder("long_value_recorder").ofLongs().build();

          @Override
          public void perform(Attributes labels) {
            metric.record(ThreadLocalRandom.current().nextLong(0, 20_000L), labels);
          }
        };
      });

  private final OperationBuilder builder;

  MetricsTestOperationBuilder(final OperationBuilder builder) {
    this.builder = builder;
  }

  public Operation build(Meter meter) {
    return this.builder.build(meter);
  }

  private interface OperationBuilder {
    Operation build(Meter meter);
  }

  interface Operation {
    void perform(Attributes labels);
  }
}
