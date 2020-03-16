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
import io.opentelemetry.metrics.LabelSet;
import io.opentelemetry.metrics.LongCounter;
import io.opentelemetry.metrics.LongMeasure;
import io.opentelemetry.metrics.Meter;

/**
 * This enum allows for iteration over all of the operations that we want to benchmark. To ensure that the enum cannot
 * change state, each enum holds a builder function- passing a meter in will return a wrapper for both bound and
 * unbound versions of that operation which can then be used in a benchmark.
 */
public enum MetricsTestOperationBuilder {
    LongCounterAdd(new OperationBuilder<LongCounter, LongCounter.BoundLongCounter>() {
        @Override
        public Operation<LongCounter, LongCounter.BoundLongCounter> build(Meter meter) {
            Operation<LongCounter, LongCounter.BoundLongCounter> op = new Operation<LongCounter, LongCounter.BoundLongCounter>() {
                @Override
                void perform(LabelSet labelSet) {
                    metric.add(5L, labelSet);
                }

                @Override
                void performBound() {
                    boundMetric.add(5L);
                }

                @Override
                protected void initialize(Meter meter) {
                    metric = meter.longCounterBuilder("long_counter").build();
                    boundMetric = meter.longCounterBuilder("bound_long_counter")
                            .build()
                            .bind(meter.createLabelSet("KEY", "VALUE"));
                }
            };
            op.initialize(meter);
            return op;
        }
    }),
    DoubleCounterAdd(new OperationBuilder<DoubleCounter, DoubleCounter.BoundDoubleCounter>() {
        @Override
        public Operation<DoubleCounter, DoubleCounter.BoundDoubleCounter> build(Meter meter) {
            Operation<DoubleCounter, DoubleCounter.BoundDoubleCounter> op = new Operation<DoubleCounter, DoubleCounter.BoundDoubleCounter>() {
                @Override
                void perform(LabelSet labelSet) {
                    metric.add(5.0d, labelSet);
                }

                @Override
                void performBound() {
                    boundMetric.add(5.0d);
                }

                @Override
                protected void initialize(Meter meter) {
                    metric = meter.doubleCounterBuilder("double_counter").build();
                    boundMetric = meter.doubleCounterBuilder("bound_double_counter")
                            .build()
                            .bind(meter.createLabelSet("KEY", "VALUE"));
                }
            };
            op.initialize(meter);
            return op;
        }
    }),
    DoubleMeasureRecord(new OperationBuilder<DoubleMeasure, DoubleMeasure.BoundDoubleMeasure>() {
        @Override
        public Operation<DoubleMeasure, DoubleMeasure.BoundDoubleMeasure> build(Meter meter) {
            Operation<DoubleMeasure, DoubleMeasure.BoundDoubleMeasure> op = new Operation<DoubleMeasure, DoubleMeasure.BoundDoubleMeasure>() {
                @Override
                void perform(LabelSet labelSet) {
                    metric.record(5.0d, labelSet);
                }

                @Override
                void performBound() {
                    boundMetric.record(5.0d);
                }

                @Override
                protected void initialize(Meter meter) {
                    metric = meter.doubleMeasureBuilder("double_measure").build();
                    boundMetric = meter.doubleMeasureBuilder("bound_double_measure")
                            .build()
                            .bind(meter.createLabelSet("KEY", "VALUE"));
                }
            };
            op.initialize(meter);
            return op;
        }
    }),
    LongMeasureRecord(new OperationBuilder<LongMeasure, LongMeasure.BoundLongMeasure>() {
        @Override
        public Operation<LongMeasure, LongMeasure.BoundLongMeasure> build(Meter meter) {
            Operation<LongMeasure, LongMeasure.BoundLongMeasure> op = new Operation<LongMeasure, LongMeasure.BoundLongMeasure>() {
                @Override
                void perform(LabelSet labelSet) {
                    metric.record(5L, labelSet);
                }

                @Override
                void performBound() {
                    boundMetric.record(5L);
                }

                @Override
                protected void initialize(Meter meter) {
                    metric = meter.longMeasureBuilder("long_measure").build();
                    boundMetric = meter.longMeasureBuilder("bound_long_measure")
                            .build()
                            .bind(meter.createLabelSet("KEY", "VALUE"));
                }
            };
            op.initialize(meter);
            return op;
        }
    });

    private final OperationBuilder<?, ?> builder;

    MetricsTestOperationBuilder(final OperationBuilder<?, ?> builder) {
        this.builder = builder;
    }

    public Operation<?, ?> build(Meter meter) {
        return this.builder.build(meter);
    }

    @Immutable
    private interface OperationBuilder<T, U> {
        Operation<T, U> build(Meter meter);
    }

    abstract static class Operation<T, U> {
        T metric;
        U boundMetric;

        abstract void perform(LabelSet labelSet);
        abstract void performBound();
        protected abstract void initialize(Meter meter);
    }

}
