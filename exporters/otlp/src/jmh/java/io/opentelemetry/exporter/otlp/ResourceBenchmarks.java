/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp;

import static io.opentelemetry.api.common.AttributeKey.booleanArrayKey;
import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleArrayKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longArrayKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import com.google.protobuf.CodedOutputStream;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode({Mode.AverageTime})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(1)
@State(Scope.Benchmark)
public class ResourceBenchmarks {
  private final Resource resource =
      Resource.create(
          Attributes.builder()
              .put(booleanKey("key_bool_1"), true)
              .put(stringKey("key_string_1"), "string")
              .put(longKey("key_int_1"), 100L)
              .put(doubleKey("key_double_1"), 100.3)
              .put(stringArrayKey("key_string_array_1"), Arrays.asList("string", "string"))
              .put(longArrayKey("key_string_array_1"), Arrays.asList(12L, 23L))
              .put(doubleArrayKey("key_string_array_1"), Arrays.asList(12.3, 23.1))
              .put(booleanArrayKey("key_string_array_1"), Arrays.asList(true, false))
              .put(booleanKey("key_bool_2"), true)
              .put(stringKey("key_string_2"), "string")
              .put(longKey("key_int_2"), 100L)
              .put(doubleKey("key_double_2"), 100.3)
              .put(stringArrayKey("key_string_array_2"), Arrays.asList("string", "string"))
              .put(longArrayKey("key_string_array_2"), Arrays.asList(12L, 23L))
              .put(doubleArrayKey("key_string_array_2"), Arrays.asList(12.3, 23.1))
              .put(booleanArrayKey("key_string_array_2"), Arrays.asList(true, false))
              .build());

  @Benchmark
  @Threads(1)
  public ByteBuffer createProtoMarshal() throws IOException {
    io.opentelemetry.proto.resource.v1.Resource protoResource =
        ResourceAdapter.toProtoResource(resource);
    return ByteBuffer.allocate(protoResource.getSerializedSize());
  }

  @Benchmark
  @Threads(1)
  public ByteBuffer createCustomMarshal() throws IOException {
    ResourceMarshaler resourceMarshaler = ResourceMarshaler.create(resource);
    return ByteBuffer.allocate(resourceMarshaler.getSerializedSize());
  }

  @Benchmark
  @Threads(1)
  public ByteBuffer marshalProto() throws IOException {
    io.opentelemetry.proto.resource.v1.Resource protoResource =
        ResourceAdapter.toProtoResource(resource);
    ByteBuffer protoOutput = ByteBuffer.allocate(protoResource.getSerializedSize());
    protoResource.writeTo(CodedOutputStream.newInstance(protoOutput));
    return protoOutput;
  }

  @Benchmark
  @Threads(1)
  public ByteBuffer marshalCustom() throws IOException {
    ResourceMarshaler resourceMarshaler = ResourceMarshaler.create(resource);
    ByteBuffer customOutput = ByteBuffer.allocate(resourceMarshaler.getSerializedSize());
    resourceMarshaler.writeTo(CodedOutputStream.newInstance(customOutput));
    return customOutput;
  }
}
