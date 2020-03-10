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

package io.opentelemetry.metrics;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10, time = 1)
@Fork(1)
public class DoubleMeasureApiBenchmark {


    @State(Scope.Thread)
    public static class ThreadState {

        private String threadId;
        private DoubleMeasure measure;
        private LabelSet threadLabelSet;
        private LabelSet commonLabelSet;
        private DoubleMeasure.BoundDoubleMeasure boundMeasure;

        @Setup(Level.Trial)
        public void doSetup() {
            Meter meter = DefaultMeter.getInstance();
            measure = meter.doubleMeasureBuilder("benchmark_double_measure").build();
            threadId = Thread.currentThread().getName();
            threadLabelSet = meter.createLabelSet("thread_id", threadId);
            commonLabelSet = meter.createLabelSet("KEY", "VALUE");
            boundMeasure = meter.doubleMeasureBuilder("bound_benchmark_double_measure")
                    .build()
                    .bind(threadLabelSet);
        }

        @TearDown(Level.Trial)
        public void doTearDown() {
        }

    }

    @Benchmark
    public void recordSingleThread(ThreadState state) {
        state.measure.record(5.0d, state.commonLabelSet);
    }

    @Benchmark
    public void recordSingleThreadBound(ThreadState state) {
        state.boundMeasure.record(5.0d);
    }

    @Benchmark
    @Threads(8)
    public void record8ThreadsCommonLabelSet(ThreadState state) {
        state.measure.record(5.0d, state.commonLabelSet);
    }

    @Benchmark
    @Threads(8)
    public void record8ThreadsBound(ThreadState state) {
        state.boundMeasure.record(5.0d);
    }

    @Benchmark
    @Threads(8)
    public void record8ThreadsDifferentLabelSets(ThreadState state) {
        state.measure.record(5.0d, state.threadLabelSet);
    }
}
