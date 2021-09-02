/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import io.opentelemetry.exporter.otlp.internal.CodedOutputStream;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.exporter.otlp.internal.SpanAdapter;
import io.opentelemetry.exporter.otlp.internal.TraceRequestMarshaler;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
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
public class RequestMarshalBenchmarks {

  @Benchmark
  @Threads(1)
  public ByteArrayOutputStream createProtoMarshal(RequestMarshalState state) {
    ExportTraceServiceRequest protoRequest =
        ExportTraceServiceRequest.newBuilder()
            .addAllResourceSpans(SpanAdapter.toProtoResourceSpans(state.spanDataList))
            .build();
    return new ByteArrayOutputStream(protoRequest.getSerializedSize());
  }

  @Benchmark
  @Threads(1)
  public ByteArrayOutputStream marshalProto(RequestMarshalState state) throws IOException {
    ExportTraceServiceRequest protoRequest =
        ExportTraceServiceRequest.newBuilder()
            .addAllResourceSpans(SpanAdapter.toProtoResourceSpans(state.spanDataList))
            .build();
    ByteArrayOutputStream protoOutput = new ByteArrayOutputStream(protoRequest.getSerializedSize());
    protoRequest.writeTo(protoOutput);
    return protoOutput;
  }

  @Benchmark
  @Threads(1)
  public ByteArrayOutputStream createCustomMarshal(RequestMarshalState state) {
    TraceRequestMarshaler requestMarshaler = TraceRequestMarshaler.create(state.spanDataList);
    return new ByteArrayOutputStream(requestMarshaler.getProtoSerializedSize());
  }

  @Benchmark
  @Threads(1)
  public ByteArrayOutputStream marshalCustom(RequestMarshalState state) throws IOException {
    TraceRequestMarshaler requestMarshaler = TraceRequestMarshaler.create(state.spanDataList);
    ByteArrayOutputStream customOutput =
        new ByteArrayOutputStream(requestMarshaler.getProtoSerializedSize());
    CodedOutputStream cos = CodedOutputStream.newInstance(customOutput);
    requestMarshaler.writeTo(Serializer.createProtoSerializer(cos));
    cos.flush();
    return customOutput;
  }
}
