/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.BoundDoubleCounter;
import io.opentelemetry.api.metrics.BoundDoubleHistogram;
import io.opentelemetry.api.metrics.BoundLongCounter;
import io.opentelemetry.api.metrics.BoundLongHistogram;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;

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
          final BoundLongCounter boundMetric =
              meter
                  .counterBuilder("bound_long_counter")
                  .build()
                  .bind(Attributes.builder().put("KEY", "VALUE").build());

          @Override
          public void perform(Attributes labels) {
            metric.add(5L, labels);
          }

          @Override
          public void performBound() {
            boundMetric.add(5L);
          }
        };
      }),
  DoubleCounterAdd(
      meter -> {
        return new Operation() {
          final DoubleCounter metric = meter.counterBuilder("double_counter").ofDoubles().build();
          final BoundDoubleCounter boundMetric =
              meter
                  .counterBuilder("bound_double_counter")
                  .ofDoubles()
                  .build()
                  .bind(Attributes.builder().put("KEY", "VALUE").build());

          @Override
          public void perform(Attributes labels) {
            metric.add(5.0d, labels);
          }

          @Override
          public void performBound() {
            boundMetric.add(5.0d);
          }
        };
      }),
  DoubleHistogramRecord(
      meter -> {
        return new Operation() {
          final DoubleHistogram metric = meter.histogramBuilder("double_histogram_recorder").build();
          final BoundDoubleHistogram boundMetric =
              meter
                  .histogramBuilder("bound_double_histogram_recorder")
                  .build()
                  .bind(Attributes.builder().put("KEY", "VALUE").build());

          @Override
          public void perform(Attributes labels) {
            metric.record(5.0d, labels);
          }

          @Override
          public void performBound() {
            boundMetric.record(5.0d);
          }
        };
      }),
  LongHistogramRecord(
      meter -> {
        return new Operation() {
          final LongHistogram metric =
              meter.histogramBuilder("long_value_recorder").ofLongs().build();
          final BoundLongHistogram boundMetric =
              meter
                  .histogramBuilder("bound_long_value_recorder")
                  .ofLongs()
                  .build()
                  .bind(Attributes.builder().put("KEY", "VALUE").build());

          @Override
          public void perform(Attributes labels) {
            metric.record(5L, labels);
          }

          @Override
          public void performBound() {
            boundMetric.record(5L);
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

    void performBound();
  }
}
