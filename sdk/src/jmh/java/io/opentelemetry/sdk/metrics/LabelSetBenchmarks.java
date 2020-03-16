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

import io.opentelemetry.metrics.Meter;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.AverageTime})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(1)
public class LabelSetBenchmarks {
    @State(Scope.Thread)
    public static class BenchmarkState {
        @Param
        TestSdk sdk;


        private Meter meter;

        @Setup
        public void Setup() {
            this.meter = sdk.getMeter();
        }
    }

    @State(Scope.Thread)
    public static class LabelSetArgState {

        @Param({"1", "10", "100"})
        int argCount;

        private Map<String, String> labelSetArgs;

        @Setup
        public void Setup() {
            labelSetArgs = new HashMap<>(argCount);
            for (int i=0; i<argCount; i++) {
                labelSetArgs.put("key_" + i, "value_" + i);
            }
        }

    }

    @Benchmark
    public void createLabelSetVarargs(BenchmarkState state){
        state.meter.createLabelSet("KEY", "VALUE");
    }

    @Benchmark
    public void createLabelSetMap(BenchmarkState state, LabelSetArgState labelSetArgState){
        state.meter.createLabelSet(labelSetArgState.labelSetArgs);
    }

}
