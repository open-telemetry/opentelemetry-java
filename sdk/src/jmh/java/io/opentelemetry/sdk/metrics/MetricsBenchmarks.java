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
import java.util.concurrent.TimeUnit;
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
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.ThreadParams;

@BenchmarkMode({Mode.AverageTime})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(1)
public class MetricsBenchmarks {

  @State(Scope.Thread)
  public static class ThreadState {

    @Param TestSdk sdk;

    @Param MetricsTestOperationBuilder opBuilder;

    MetricsTestOperationBuilder.Operation op;
    final String[] sharedLabelSet = {"KEY", "VALUE"};
    String[] threadUniqueLabelSet;

    @Setup
    public void setup(ThreadParams threadParams) {
      Meter meter = sdk.getMeter();
      op = opBuilder.build(meter);
      threadUniqueLabelSet = new String[] {"KEY", String.valueOf(threadParams.getThreadIndex())};
    }
  }

  @Benchmark
  @Threads(1)
  public void oneThread(ThreadState threadState) {
    threadState.op.perform(threadState.sharedLabelSet);
  }

  @Benchmark
  @Threads(1)
  public void oneThreadBound(ThreadState threadState) {
    threadState.op.performBound();
  }

  @Benchmark
  @Threads(8)
  public void eightThreadsCommonLabelSet(ThreadState threadState) {
    threadState.op.perform(threadState.sharedLabelSet);
  }

  @Benchmark
  @Threads(8)
  public void eightThreadsSeparateLabelSets(ThreadState threadState) {
    threadState.op.perform(threadState.threadUniqueLabelSet);
  }

  @Benchmark
  @Threads(8)
  public void eightThreadsBound(ThreadState threadState) {
    threadState.op.performBound();
  }
}
