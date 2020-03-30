/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.sdk.metrics;

import com.google.errorprone.annotations.Immutable;
import io.opentelemetry.metrics.DoubleCounter;
import io.opentelemetry.metrics.DoubleMeasure;
import io.opentelemetry.metrics.LongCounter;
import io.opentelemetry.metrics.LongMeasure;
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
                meter.longCounterBuilder("bound_long_counter").build().bind("KEY", "VALUE");

            @Override
            public void perform(String... args) {
              metric.add(5L, args);
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
                meter.doubleCounterBuilder("bound_double_counter").build().bind("KEY", "VALUE");

            @Override
            public void perform(String... args) {
              metric.add(5.0d, args);
            }

            @Override
            public void performBound() {
              boundMetric.add(5.0d);
            }
          };
        }
      }),
  DoubleMeasureRecord(
      new OperationBuilder() {
        @Override
        public Operation build(final Meter meter) {
          return new Operation() {
            DoubleMeasure metric = meter.doubleMeasureBuilder("double_measure").build();
            DoubleMeasure.BoundDoubleMeasure boundMetric =
                meter.doubleMeasureBuilder("bound_double_measure").build().bind("KEY", "VALUE");

            @Override
            public void perform(String... args) {
              metric.record(5.0d, args);
            }

            @Override
            public void performBound() {
              boundMetric.record(5.0d);
            }
          };
        }
      }),
  LongMeasureRecord(
      new OperationBuilder() {
        @Override
        public Operation build(final Meter meter) {
          return new Operation() {
            LongMeasure metric = meter.longMeasureBuilder("long_measure").build();
            LongMeasure.BoundLongMeasure boundMetric =
                meter.longMeasureBuilder("bound_long_measure").build().bind("KEY", "VALUE");

            @Override
            public void perform(String... args) {
              metric.record(5L, args);
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
    abstract void perform(String... args);

    abstract void performBound();
  }
}
