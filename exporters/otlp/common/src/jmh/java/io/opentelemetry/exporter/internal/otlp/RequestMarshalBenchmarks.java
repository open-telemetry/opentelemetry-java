/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.exporter.internal.otlp.traces.LowAllocationTraceRequestMarshaler;
import io.opentelemetry.exporter.internal.otlp.traces.TraceRequestMarshaler;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode({Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(1)
public class RequestMarshalBenchmarks {

  private static final LowAllocationTraceRequestMarshaler MARSHALER =
      new LowAllocationTraceRequestMarshaler();
  private static final TestOutputStream OUTPUT = new TestOutputStream();

  @Benchmark
  @Threads(1)
  public int createCustomMarshal(RequestMarshalState state) {
    TraceRequestMarshaler requestMarshaler = TraceRequestMarshaler.create(state.spanDataList);
    return requestMarshaler.getBinarySerializedSize();
  }

  @Benchmark
  @Threads(1)
  public TestOutputStream marshalCustom(RequestMarshalState state) throws IOException {
    TraceRequestMarshaler requestMarshaler = TraceRequestMarshaler.create(state.spanDataList);
    OUTPUT.reset(requestMarshaler.getBinarySerializedSize());
    requestMarshaler.writeBinaryTo(OUTPUT);
    return OUTPUT;
  }

  @Benchmark
  @Threads(1)
  public TestOutputStream marshalJson(RequestMarshalState state) throws IOException {
    TraceRequestMarshaler requestMarshaler = TraceRequestMarshaler.create(state.spanDataList);
    OUTPUT.reset();
    requestMarshaler.writeJsonTo(OUTPUT);
    return OUTPUT;
  }

  @Benchmark
  @Threads(1)
  public int createCustomMarshalLowAllocation(RequestMarshalState state) {
    LowAllocationTraceRequestMarshaler requestMarshaler = MARSHALER;
    requestMarshaler.initialize(state.spanDataList);
    try {
      return requestMarshaler.getBinarySerializedSize();
    } finally {
      requestMarshaler.reset();
    }
  }

  @Benchmark
  @Threads(1)
  public TestOutputStream marshalCustomLowAllocation(RequestMarshalState state) throws IOException {
    LowAllocationTraceRequestMarshaler requestMarshaler = MARSHALER;
    requestMarshaler.initialize(state.spanDataList);
    try {
      OUTPUT.reset();
      requestMarshaler.writeBinaryTo(OUTPUT);
      return OUTPUT;
    } finally {
      requestMarshaler.reset();
    }
  }

  @Benchmark
  @Threads(1)
  public TestOutputStream marshalJsonLowAllocation(RequestMarshalState state) throws IOException {
    LowAllocationTraceRequestMarshaler requestMarshaler = MARSHALER;
    requestMarshaler.initialize(state.spanDataList);
    try {
      OUTPUT.reset();
      requestMarshaler.writeJsonTo(OUTPUT);
      return OUTPUT;
    } finally {
      requestMarshaler.reset();
    }
  }
}
