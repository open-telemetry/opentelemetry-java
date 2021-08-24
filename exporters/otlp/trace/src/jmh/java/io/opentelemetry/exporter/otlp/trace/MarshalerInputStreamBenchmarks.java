/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.PooledByteBufAllocator;
import io.opentelemetry.exporter.otlp.internal.TraceRequestMarshaler;
import java.io.ByteArrayOutputStream;
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
public class MarshalerInputStreamBenchmarks {

  @Benchmark
  @Threads(1)
  public void marshalToNettyBuffer(RequestMarshalState state) throws IOException {
    MarshalerInputStream stream =
        new MarshalerInputStream(TraceRequestMarshaler.create(state.spanDataList));
    // Roughly reproduce how grpc-netty should behave.
    ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer(stream.available());
    stream.drainTo(new ByteBufOutputStream(buf));
    buf.release();
  }

  @Benchmark
  @Threads(1)
  public void marshalToByteArray(RequestMarshalState state) throws IOException {
    MarshalerInputStream stream =
        new MarshalerInputStream(TraceRequestMarshaler.create(state.spanDataList));
    stream.drainTo(new ByteArrayOutputStream(stream.available()));
  }
}
