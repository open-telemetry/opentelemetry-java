/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp;

import com.google.protobuf.CodedOutputStream;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import java.io.IOException;
import java.nio.ByteBuffer;
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
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(1)
public class RequestMarshalBenchmarks {

  @Benchmark
  @Threads(1)
  public ByteBuffer createProtoMarshal(RequestMarshalState state) {
    ExportTraceServiceRequest protoRequest =
        ExportTraceServiceRequest.newBuilder()
            .addAllResourceSpans(SpanAdapter.toProtoResourceSpans(state.spanDataList))
            .build();
    return ByteBuffer.allocate(protoRequest.getSerializedSize());
  }

  @Benchmark
  @Threads(1)
  public ByteBuffer marshalProto(RequestMarshalState state) throws IOException {
    ExportTraceServiceRequest protoRequest =
        ExportTraceServiceRequest.newBuilder()
            .addAllResourceSpans(SpanAdapter.toProtoResourceSpans(state.spanDataList))
            .build();
    ByteBuffer protoOutput = ByteBuffer.allocate(protoRequest.getSerializedSize());
    protoRequest.writeTo(CodedOutputStream.newInstance(protoOutput));
    return protoOutput;
  }

  @Benchmark
  @Threads(1)
  public ByteBuffer createCustomMarshal(RequestMarshalState state) {
    TraceMarshaler.RequestMarshaler requestMarshaler =
        TraceMarshaler.RequestMarshaler.create(state.spanDataList);
    return ByteBuffer.allocate(requestMarshaler.getSerializedSize());
  }

  @Benchmark
  @Threads(1)
  public ByteBuffer marshalCustom(RequestMarshalState state) throws IOException {
    TraceMarshaler.RequestMarshaler requestMarshaler =
        TraceMarshaler.RequestMarshaler.create(state.spanDataList);
    ByteBuffer customOutput = ByteBuffer.allocate(requestMarshaler.getSerializedSize());
    requestMarshaler.writeTo(CodedOutputStream.newInstance(customOutput));
    return customOutput;
  }

  @Benchmark
  @Threads(1)
  public ByteBuffer marshalProtoCustom(RequestMarshalState state) throws IOException {
    ExportTraceServiceRequest protoRequest =
        TraceMarshaler.RequestMarshaler.create(state.spanDataList).toRequest();
    ByteBuffer protoOutput = ByteBuffer.allocate(protoRequest.getSerializedSize());
    protoRequest.writeTo(CodedOutputStream.newInstance(protoOutput));
    return protoOutput;
  }
}
