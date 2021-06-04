/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import io.opentelemetry.proto.common.v1.InstrumentationLibrary;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode({Mode.AverageTime})
@Fork(3)
@Measurement(iterations = 15, time = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
public class CommonAdapterBenchmark {

  private static final InstrumentationLibraryInfo INFO =
      InstrumentationLibraryInfo.create("io.opentelemetry.instrumentation.benchmark-1.0", "1.2.0");

  @Benchmark
  public InstrumentationLibrary toProto() {
    return CommonAdapter.toProtoInstrumentationLibrary(INFO);
  }
}
