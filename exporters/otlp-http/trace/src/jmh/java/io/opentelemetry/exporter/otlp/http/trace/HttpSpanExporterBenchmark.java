/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.trace;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.RequestBody;
import okio.Buffer;
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
public class HttpSpanExporterBenchmark {

  @Benchmark
  @Threads(1)
  public Buffer writeByteArray(TraceBenchmarkState state) throws IOException {
    try (Buffer buffer = new Buffer()) {
      RequestBody body = RequestBody.create(state.request.toByteArray());
      body.writeTo(buffer);
      return buffer;
    }
  }

  @Benchmark
  @Threads(1)
  public Buffer writeDirect(TraceBenchmarkState state) throws IOException {
    try (Buffer buffer = new Buffer()) {
      RequestBody body = new ProtoRequestBody(state.request);
      body.writeTo(buffer);
      return buffer;
    }
  }
}
