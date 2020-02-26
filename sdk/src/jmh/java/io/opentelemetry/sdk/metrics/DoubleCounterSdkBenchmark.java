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

import io.opentelemetry.metrics.DoubleCounter;
import io.opentelemetry.metrics.DoubleCounter.BoundDoubleCounter;
import io.opentelemetry.metrics.Meter;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.MillisClock;
import io.opentelemetry.sdk.resources.Resource;
import java.util.concurrent.TimeUnit;
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

@BenchmarkMode({Mode.AverageTime})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(1)
public class DoubleCounterSdkBenchmark {

  private static final MeterProviderSharedState METER_PROVIDER_SHARED_STATE =
      MeterProviderSharedState.create(MillisClock.getInstance(), Resource.getEmpty());
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create("io.opentelemetry.sdk.metrics", null);
  private static final String KEY = "key";
  private static final String VALUE = "value";

  private static final Meter meter =
      new MeterSdk(METER_PROVIDER_SHARED_STATE, INSTRUMENTATION_LIBRARY_INFO);
  private static final DoubleCounter doubleCounter =
      meter.doubleCounterBuilder("benchmark_double_counter").build();
  private static final BoundDoubleCounter boundDoubleCounter =
      doubleCounter.bind(meter.createLabelSet(KEY, VALUE));

  @State(Scope.Thread)
  public static class ThreadState {

    @Setup(Level.Trial)
    public void doSetup() {
      threadKey = KEY + "_" + Thread.currentThread().getId();
      boundDoubleCounter = doubleCounter.bind(meter.createLabelSet(threadKey, VALUE));
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
  public void add_1Threads() {
    doubleCounter.add(100.1d, meter.createLabelSet(KEY, VALUE));
  }

  @Benchmark
  @Threads(value = 8)
  public void add_8Threads_SameLabelSet() {
    doubleCounter.add(100.1d, meter.createLabelSet(KEY, VALUE));
  }

  @Benchmark
  @Threads(value = 8)
  public void add_8Threads_DifferentLabelSet(ThreadState state) {
    doubleCounter.add(100.1d, meter.createLabelSet(state.threadKey, VALUE));
  }

  @Benchmark
  @Threads(value = 1)
  public void bindingAdd_1Threads() {
    boundDoubleCounter.add(100.1d);
  }

  @Benchmark
  @Threads(value = 8)
  public void bindingAdd_8Threads_SameLabelSet() {
    boundDoubleCounter.add(100.1d);
  }

  @Benchmark
  @Threads(value = 8)
  public void bindingAdd_8Threads_DifferentLabelSet(ThreadState state) {
    state.boundDoubleCounter.add(100.1d);
  }
}
