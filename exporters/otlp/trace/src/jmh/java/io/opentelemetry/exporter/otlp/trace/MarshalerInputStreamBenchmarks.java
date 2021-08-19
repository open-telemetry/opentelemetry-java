/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import io.grpc.HasByteBuffer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.PooledByteBufAllocator;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
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
  public void marshalToNettyBuffer(RequestMarshalState state) {
    MarshalerInputStream stream =
        new MarshalerInputStream(TraceMarshaler.RequestMarshaler.create(state.spanDataList));
    // Roughly reproduce how grpc-netty should behave, it's classes are internal so hard to use
    // directly.
    ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer(stream.available());
    try (NettyOutputStream nettyStream = new NettyOutputStream(buf)) {
      stream.drainTo(nettyStream);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static class NettyOutputStream extends ByteBufOutputStream implements HasByteBuffer {

    private final ByteBuffer byteBuffer;

    NettyOutputStream(ByteBuf buf) {
      super(buf);
      byteBuffer = buf.nioBuffer(0, buf.writableBytes());
    }

    @Override
    public boolean byteBufferSupported() {
      return true;
    }

    @Nullable
    @Override
    public ByteBuffer getByteBuffer() {
      return byteBuffer;
    }

    @Override
    public void close() {
      buffer().release();
    }
  }
}
