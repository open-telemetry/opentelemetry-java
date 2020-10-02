/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import com.google.errorprone.annotations.Immutable;
import io.opentelemetry.common.Labels;
import io.opentelemetry.metrics.DoubleCounter;
import io.opentelemetry.metrics.DoubleValueRecorder;
import io.opentelemetry.metrics.DoubleValueRecorder.BoundDoubleValueRecorder;
import io.opentelemetry.metrics.LongCounter;
import io.opentelemetry.metrics.LongValueRecorder;
import io.opentelemetry.metrics.LongValueRecorder.BoundLongValueRecorder;
import io.opentelemetry.metrics.Meter;

/**
 * This enum allows for iteration over all of the operations that we want to benchmark. To ensure
 * that the enum cannot change state, each enum holds a builder function- passing a meter in will
 * return a wrapper for both bound and unbound versions of that operation which can then be used in
 * a benchmark.
 */
public enum MetricsTestOperationBuilder {
  LongCounterAdd(
      new OperationBuilder() {
        @Override
        public Operation build(final Meter meter) {
          return new Operation() {
            LongCounter metric = meter.longCounterBuilder("long_counter").build();
            LongCounter.BoundLongCounter boundMetric =
                meter
                    .longCounterBuilder("bound_long_counter")
                    .build()
                    .bind(Labels.of("KEY", "VALUE"));

            @Override
            public void perform(Labels labels) {
              metric.add(5L, labels);
            }

            @Override
            public void performBound() {
              boundMetric.add(5L);
            }
          };
        }
      }),
  DoubleCounterAdd(
      new OperationBuilder() {
        @Override
        public Operation build(final Meter meter) {
          return new Operation() {
            DoubleCounter metric = meter.doubleCounterBuilder("double_counter").build();
            DoubleCounter.BoundDoubleCounter boundMetric =
                meter
                    .doubleCounterBuilder("bound_double_counter")
                    .build()
                    .bind(Labels.of("KEY", "VALUE"));

            @Override
            public void perform(Labels labels) {
              metric.add(5.0d, labels);
            }

            @Override
            public void performBound() {
              boundMetric.add(5.0d);
            }
          };
        }
      }),
  DoubleValueRecorderRecord(
      new OperationBuilder() {
        @Override
        public Operation build(final Meter meter) {
          return new Operation() {
            DoubleValueRecorder metric =
                meter.doubleValueRecorderBuilder("double_value_recorder").build();
            BoundDoubleValueRecorder boundMetric =
                meter
                    .doubleValueRecorderBuilder("bound_double_value_recorder")
                    .build()
                    .bind(Labels.of("KEY", "VALUE"));

            @Override
            public void perform(Labels labels) {
              metric.record(5.0d, labels);
            }

            @Override
            public void performBound() {
              boundMetric.record(5.0d);
            }
          };
        }
      }),
  LongValueRecorderRecord(
      new OperationBuilder() {
        @Override
        public Operation build(final Meter meter) {
          return new Operation() {
            LongValueRecorder metric =
                meter.longValueRecorderBuilder("long_value_recorder").build();
            BoundLongValueRecorder boundMetric =
                meter
                    .longValueRecorderBuilder("bound_long_value_recorder")
                    .build()
                    .bind(Labels.of("KEY", "VALUE"));

            @Override
            public void perform(Labels labels) {
              metric.record(5L, labels);
            }

            @Override
            public void performBound() {
              boundMetric.record(5L);
            }
          };
        }
      });

  private final OperationBuilder builder;

  MetricsTestOperationBuilder(final OperationBuilder builder) {
    this.builder = builder;
  }

  public Operation build(Meter meter) {
    return this.builder.build(meter);
  }

  @Immutable
  private interface OperationBuilder {
    Operation build(Meter meter);
  }

  interface Operation {
    abstract void perform(Labels labels);

    abstract void performBound();
  }
}
