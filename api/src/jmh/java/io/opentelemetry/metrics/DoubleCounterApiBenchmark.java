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

import io.opentelemetry.metrics.DoubleCounter.BoundDoubleCounter;
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
public class DoubleCounterApiBenchmark {

  private static final String KEY = "key";
  private static final String VALUE = "value";

  private static final Meter METER = DefaultMeter.getInstance();

  private static final DoubleCounter doubleCounter =
      METER.doubleCounterBuilder("benchmark_double_counter").build();
  private static final BoundDoubleCounter boundDoubleCounter =
      doubleCounter.bind(METER.createLabelSet(KEY, VALUE));

  @State(Scope.Thread)
  public static class ThreadState {

    @Setup(Level.Trial)
    public void doSetup() {
      threadKey = KEY + "_" + Thread.currentThread().getId();
      boundDoubleCounter = doubleCounter.bind(METER.createLabelSet(threadKey, VALUE));
    }

    @TearDown(Level.Trial)
    public void doTearDown() {
      boundDoubleCounter.unbind();
    }

    private String threadKey;
    private BoundDoubleCounter boundDoubleCounter;
  }

  @Benchmark
  @Threads(value = 1)
  public void addOneThread() {
    doubleCounter.add(100, METER.createLabelSet(KEY, VALUE));
  }

  @Benchmark
  @Threads(value = 8)
  public void addEightThreadsCommonLabelSet() {
    doubleCounter.add(100, METER.createLabelSet(KEY, VALUE));
  }

  @Benchmark
  @Threads(value = 8)
  public void addEightThreadsDifferentLabelSets(ThreadState state) {
    doubleCounter.add(100.0, METER.createLabelSet(state.threadKey, VALUE));
  }

  @Benchmark
  @Threads(value = 1)
  public void addOneThreadBound() {
    boundDoubleCounter.add(100.0);
  }

  @Benchmark
  @Threads(value = 8)
  public void addEightThreadsBound() {
    boundDoubleCounter.add(100.0);
  }

  @Benchmark
  @Threads(value = 8)
  public void addEightThreadsDifferentLabelSetsBound(ThreadState state) {
    state.boundDoubleCounter.add(100.0);
  }
}
