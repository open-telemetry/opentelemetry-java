/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.booleanstate;

import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(1)
@State(Scope.Benchmark)
public class BooleanStateBenchmark {

  @Param({
    "NonVolatileBooleanState",
    "ImmediateBooleanState",
    "EventualBooleanState",
    "VarHandleImmediateBooleanState", // available with -PjmhJavaVersion=11 and higher
    "VarHandleEventualBooleanState" // available with -PjmhJavaVersion=11 and higher
  })
  private String implementation;

  private BooleanState state;

  @Setup(Level.Trial)
  public void setup() {
    switch (implementation) {
      case "NonVolatileBooleanState":
        state = new NonVolatileBooleanState();
        break;
      case "ImmediateBooleanState":
        state = new ImmediateBooleanState();
        break;
      case "EventualBooleanState":
        state = new EventualBooleanState();
        break;
      case "VarHandleImmediateBooleanState":
        state = createVarHandleImmediateBooleanState();
        break;
      case "VarHandleEventualBooleanState":
        state = createVarHandleEventualBooleanState();
        break;
      default:
        throw new IllegalArgumentException("Unknown implementation: " + implementation);
    }
  }

  private static BooleanState createVarHandleEventualBooleanState() {
    try {
      Class<?> clazz =
          Class.forName("io.opentelemetry.sdk.logs.booleanstate.VarHandleEventualBooleanState");
      return (BooleanState) clazz.getConstructor().newInstance();
    } catch (Exception e) {
      throw new IllegalStateException(
          "VarHandleEventualBooleanState not available on this Java version", e);
    }
  }

  private static BooleanState createVarHandleImmediateBooleanState() {
    try {
      Class<?> clazz =
          Class.forName("io.opentelemetry.sdk.logs.booleanstate.VarHandleImmediateBooleanState");
      return (BooleanState) clazz.getConstructor().newInstance();
    } catch (Exception e) {
      throw new IllegalStateException(
          "VarHandleImmediateBooleanState not available on this Java version", e);
    }
  }

  @Benchmark
  @Threads(1)
  public int read_singleThread() {
    int count = 0;
    for (int i = 0; i < 100; i++) {
      if (state.get()) {
        count++;
      }
    }
    return count;
  }

  @Benchmark
  @Threads(2) // not expecting significant concurrent access
  public int read_twoThreads() {
    int count = 0;
    for (int i = 0; i < 100; i++) {
      if (state.get()) {
        count++;
      }
    }
    return count;
  }
}
