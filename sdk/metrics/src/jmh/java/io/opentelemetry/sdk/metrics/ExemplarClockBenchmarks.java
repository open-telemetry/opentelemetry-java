/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.sdk.common.Clock;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;

/**
 * {@code io.opentelemetry.sdk.metrics.internal.exemplar.ReservoirCell} relies on {@link Clock} to
 * obtain the measurement time when storing exemplar values. This benchmark illustrates the
 * performance impact of using the higher precision {@link Clock#now()} instead of {@link
 * Clock#now(boolean)} with {@code highPrecision=false}.
 */
@BenchmarkMode({Mode.AverageTime})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(1)
public class ExemplarClockBenchmarks {

  private static final Clock clock = Clock.getDefault();

  @SuppressWarnings("ReturnValueIgnored")
  @Benchmark
  public void now_lowPrecision() {
    clock.now(false);
  }

  @SuppressWarnings("ReturnValueIgnored")
  @Benchmark
  public void now_highPrecision() {
    clock.now(true);
  }
}
