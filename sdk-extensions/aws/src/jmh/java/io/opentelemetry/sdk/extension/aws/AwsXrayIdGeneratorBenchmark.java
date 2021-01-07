/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.aws;

import io.opentelemetry.sdk.extension.aws.trace.AwsXrayIdGenerator;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Benchmark)
public class AwsXrayIdGeneratorBenchmark {
  private final AwsXrayIdGenerator idGenerator = AwsXrayIdGenerator.getInstance();

  @Benchmark
  @Measurement(iterations = 15, time = 1)
  @Warmup(iterations = 5, time = 1)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @BenchmarkMode(Mode.AverageTime)
  @Fork(1)
  public String generateTraceId() {
    return idGenerator.generateTraceId();
  }

  @Benchmark
  @Measurement(iterations = 15, time = 1)
  @Warmup(iterations = 5, time = 1)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  @BenchmarkMode(Mode.AverageTime)
  @Fork(1)
  public String generateSpanId() {
    return idGenerator.generateSpanId();
  }
}
