/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.Serializer;
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
public class StringMarshalBenchmark {
  private static final TestMarshaler MARSHALER_SAFE = new TestMarshaler(/* useUnsafe= */ false);
  private static final TestMarshaler MARSHALER_UNSAFE = new TestMarshaler(/* useUnsafe= */ true);
  private static final TestOutputStream OUTPUT = new TestOutputStream();

  @Benchmark
  @Threads(1)
  public int marshalAsciiStringStateful(StringMarshalState state) throws IOException {
    return marshalStateful(state.asciiString);
  }

  @Benchmark
  @Threads(1)
  public int marshalLatin1StringStateful(StringMarshalState state) throws IOException {
    return marshalStateful(state.latin1String);
  }

  @Benchmark
  @Threads(1)
  public int marshalUnicodeStringStateful(StringMarshalState state) throws IOException {
    return marshalStateful(state.unicodeString);
  }

  private static int marshalStateful(String string) throws IOException {
    OUTPUT.reset();
    Marshaler marshaler = StringAnyValueMarshaler.create(string);
    marshaler.writeBinaryTo(OUTPUT);
    return OUTPUT.getCount();
  }

  @Benchmark
  @Threads(1)
  public int marshalAsciiStringStatelessSafe(StringMarshalState state) throws IOException {
    return marshalStateless(MARSHALER_SAFE, state.asciiString);
  }

  @Benchmark
  @Threads(1)
  public int marshalAsciiStringStatelessUnsafe(StringMarshalState state) throws IOException {
    return marshalStateless(MARSHALER_UNSAFE, state.asciiString);
  }

  @Benchmark
  @Threads(1)
  public int marshalLatin1StringStatelessSafe(StringMarshalState state) throws IOException {
    return marshalStateless(MARSHALER_SAFE, state.latin1String);
  }

  @Benchmark
  @Threads(1)
  public int marshalLatin1StringStatelessUnsafe(StringMarshalState state) throws IOException {
    return marshalStateless(MARSHALER_UNSAFE, state.latin1String);
  }

  @Benchmark
  @Threads(1)
  public int marshalUnicodeStringStatelessSafe(StringMarshalState state) throws IOException {
    return marshalStateless(MARSHALER_SAFE, state.unicodeString);
  }

  @Benchmark
  @Threads(1)
  public int marshalUnicodeStringStatelessUnsafe(StringMarshalState state) throws IOException {
    return marshalStateless(MARSHALER_UNSAFE, state.unicodeString);
  }

  private static int marshalStateless(TestMarshaler marshaler, String string) throws IOException {
    OUTPUT.reset();
    try {
      marshaler.initialize(string);
      marshaler.writeBinaryTo(OUTPUT);
      return OUTPUT.getCount();
    } finally {
      marshaler.reset();
    }
  }

  private static class TestMarshaler extends Marshaler {
    private final MarshalerContext context;
    private int size;
    private String value;

    TestMarshaler(boolean useUnsafe) {
      context = new MarshalerContext(/* marshalStringNoAllocation= */ true, useUnsafe);
    }

    private void initialize(String string) {
      value = string;
      size = StringAnyValueStatelessMarshaler.INSTANCE.getBinarySerializedSize(string, context);
    }

    private void reset() {
      context.reset();
    }

    @Override
    public int getBinarySerializedSize() {
      return size;
    }

    @Override
    public void writeTo(Serializer output) throws IOException {
      StringAnyValueStatelessMarshaler.INSTANCE.writeTo(output, value, context);
    }
  }
}
