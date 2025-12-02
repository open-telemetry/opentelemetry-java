/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StringEncoder;
import io.opentelemetry.exporter.internal.marshal.StringEncoderHolder;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode({Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(1)
@State(Scope.Benchmark)
public class StringMarshalBenchmark {

  @Param({"FallbackStringEncoder", "UnsafeStringEncoder", "VarHandleStringEncoder"})
  private String encoderImplementation;

  private TestMarshaler marshaler;
  private static final TestOutputStream OUTPUT = new TestOutputStream();

  @Setup
  public void setup() {
    StringEncoder encoder;
    switch (encoderImplementation) {
      case "FallbackStringEncoder":
        encoder = StringEncoderHolder.createFallbackEncoder();
        break;
      case "UnsafeStringEncoder":
        encoder = StringEncoderHolder.createUnsafeEncoder();
        if (encoder == null) {
          throw new IllegalStateException(
              "UnsafeStringEncoder is not available (requires Java 9+)");
        }
        break;
      case "VarHandleStringEncoder":
        encoder = StringEncoderHolder.createVarHandleEncoder();
        if (encoder == null) {
          throw new IllegalStateException(
              "VarHandleStringEncoder is not available (requires Java 9+"
                  + " and -jvmArgs=\"--add-opens=java.base/java.lang=ALL-UNNAMED\"");
        }
        break;
      default:
        throw new IllegalStateException("Unknown encoder implementation: " + encoderImplementation);
    }
    marshaler = new TestMarshaler(encoder);
  }

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
  public int marshalAsciiStringStateless(StringMarshalState state) throws IOException {
    return marshalStateless(marshaler, state.asciiString);
  }

  @Benchmark
  @Threads(1)
  public int marshalLatin1StringStateless(StringMarshalState state) throws IOException {
    return marshalStateless(marshaler, state.latin1String);
  }

  @Benchmark
  @Threads(1)
  public int marshalUnicodeStringStateless(StringMarshalState state) throws IOException {
    return marshalStateless(marshaler, state.unicodeString);
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

    TestMarshaler(StringEncoder encoder) {
      context = new MarshalerContext(/* marshalStringNoAllocation= */ true, encoder);
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
