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
  private static final TestMarshaler MARSHALER = new TestMarshaler();
  private static final TestOutputStream OUTPUT = new TestOutputStream();

  @Benchmark
  @Threads(1)
  public int marshalAsciiString(StringMarshalState state) throws IOException {
    OUTPUT.reset();
    Marshaler marshaler = StringAnyValueMarshaler.create(state.asciiString);
    marshaler.writeBinaryTo(OUTPUT);
    return OUTPUT.getCount();
  }

  @Benchmark
  @Threads(1)
  public int marshalLatin1String(StringMarshalState state) throws IOException {
    OUTPUT.reset();
    Marshaler marshaler = StringAnyValueMarshaler.create(state.latin1String);
    marshaler.writeBinaryTo(OUTPUT);
    return OUTPUT.getCount();
  }

  @Benchmark
  @Threads(1)
  public int marshalUnicodeString(StringMarshalState state) throws IOException {
    OUTPUT.reset();
    Marshaler marshaler = StringAnyValueMarshaler.create(state.unicodeString);
    marshaler.writeBinaryTo(OUTPUT);
    return OUTPUT.getCount();
  }

  @Benchmark
  @Threads(1)
  public int marshalAsciiStringLowAllocation(StringMarshalState state) throws IOException {
    OUTPUT.reset();
    try {
      MARSHALER.initialize(state.asciiString);
      MARSHALER.writeBinaryTo(OUTPUT);
      return OUTPUT.getCount();
    } finally {
      MARSHALER.reset();
    }
  }

  @Benchmark
  @Threads(1)
  public int marshalLatin1StringLowAllocation(StringMarshalState state) throws IOException {
    OUTPUT.reset();
    try {
      MARSHALER.initialize(state.latin1String);
      MARSHALER.writeBinaryTo(OUTPUT);
      return OUTPUT.getCount();
    } finally {
      MARSHALER.reset();
    }
  }

  @Benchmark
  @Threads(1)
  public int marshalUnicodeStringLowAllocation(StringMarshalState state) throws IOException {
    OUTPUT.reset();
    try {
      MARSHALER.initialize(state.unicodeString);
      MARSHALER.writeBinaryTo(OUTPUT);
      return OUTPUT.getCount();
    } finally {
      MARSHALER.reset();
    }
  }

  private static class TestMarshaler extends Marshaler {
    private final MarshalerContext context = new MarshalerContext();
    private int size;
    private String value;

    public void initialize(String string) {
      value = string;
      size = StringAnyValueStatelessMarshaler.INSTANCE.getBinarySerializedSize(string, context);
    }

    public void reset() {
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
