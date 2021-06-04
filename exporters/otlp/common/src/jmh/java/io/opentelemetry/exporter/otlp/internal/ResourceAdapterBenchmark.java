/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import io.opentelemetry.sdk.extension.resources.HostResource;
import io.opentelemetry.sdk.extension.resources.OsResource;
import io.opentelemetry.sdk.extension.resources.ProcessResource;
import io.opentelemetry.sdk.extension.resources.ProcessRuntimeResource;
import io.opentelemetry.sdk.resources.Resource;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode({Mode.AverageTime})
@Fork(3)
@Measurement(iterations = 15, time = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
public class ResourceAdapterBenchmark {

  // A default resource, which is pretty big. Resource in practice will generally be even bigger by
  // containing cloud attributes.
  private static final Resource RESOURCE =
      ProcessResource.get()
          .merge(ProcessRuntimeResource.get())
          .merge(OsResource.get())
          .merge(HostResource.get())
          .merge(Resource.getDefault());

  @Benchmark
  public io.opentelemetry.proto.resource.v1.Resource toProto() {
    return ResourceAdapter.toProtoResource(RESOURCE);
  }
}
