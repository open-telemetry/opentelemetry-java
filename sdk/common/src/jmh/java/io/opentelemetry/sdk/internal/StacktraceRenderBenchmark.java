/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

/**
 * This benchmark compares the performance of {@link StackTraceRenderer}, the custom length limit
 * aware exception render, to the built-in JDK stacktrace renderer {@link
 * Throwable#printStackTrace(PrintStream)}.
 */
@BenchmarkMode({Mode.AverageTime})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
@SuppressWarnings("StaticAssignmentOfThrowable")
public class StacktraceRenderBenchmark {

  private static final Exception simple = new Exception("error");
  private static final Exception complex =
      new Exception("error", new Exception("cause1", new Exception("cause2")));

  static {
    complex.addSuppressed(new Exception("suppressed1"));
    complex.addSuppressed(new Exception("suppressed2", new Exception("cause")));
  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {

    @Param Renderer renderer;
    @Param ExceptionParam exceptionParam;

    @Param({"10", "1000", "100000"})
    int lengthLimit;
  }

  @SuppressWarnings("ImmutableEnumChecker")
  public enum Renderer {
    JDK(
        (throwable, limit) -> {
          StringWriter stringWriter = new StringWriter();
          try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
            throwable.printStackTrace(printWriter);
          }
          String stacktrace = stringWriter.toString();
          return stacktrace.substring(0, Math.min(stacktrace.length(), limit));
        }),
    CUSTOM((throwable, limit) -> new StackTraceRenderer(throwable, limit).render());

    private final BiFunction<Throwable, Integer, String> renderer;

    Renderer(BiFunction<Throwable, Integer, String> renderer) {
      this.renderer = renderer;
    }

    BiFunction<Throwable, Integer, String> renderer() {
      return renderer;
    }
  }

  @SuppressWarnings("ImmutableEnumChecker")
  public enum ExceptionParam {
    SIMPLE(simple),
    COMPLEX(complex);

    private final Throwable throwable;

    ExceptionParam(Throwable throwable) {
      this.throwable = throwable;
    }

    Throwable throwable() {
      return throwable;
    }
  }

  @Benchmark
  @Threads(1)
  @SuppressWarnings("ReturnValueIgnored")
  public void render(BenchmarkState benchmarkState) {
    BiFunction<Throwable, Integer, String> renderer = benchmarkState.renderer.renderer();
    Throwable throwable = benchmarkState.exceptionParam.throwable();
    int limit = benchmarkState.lengthLimit;

    renderer.apply(throwable, limit);
  }
}
