/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.extension.resources.HostResource;
import io.opentelemetry.sdk.extension.resources.OsResource;
import io.opentelemetry.sdk.extension.resources.ProcessResource;
import io.opentelemetry.sdk.extension.resources.ProcessRuntimeResource;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)
public class SpanAdapterBenchmark {
  // A default resource, which is pretty big. Resource in practice will generally be even bigger by
  // containing cloud attributes.
  private static final Resource RESOURCE =
      ProcessResource.get()
          .merge(ProcessRuntimeResource.get())
          .merge(OsResource.get())
          .merge(HostResource.get())
          .merge(Resource.getDefault());

  private static final InstrumentationLibraryInfo LIBRARY1 =
      InstrumentationLibraryInfo.create("io.opentelemetry.instrumentation.benchmark-1.0", "1.2.0");
  private static final InstrumentationLibraryInfo LIBRARY2 =
      InstrumentationLibraryInfo.create("io.opentelemetry.instrumentation.benchmark-2.0", "1.3.0");
  private static final InstrumentationLibraryInfo LIBRARY3 =
      InstrumentationLibraryInfo.create("io.opentelemetry.instrumentation.benchmark-3.0", "1.4.0");

  private static final String TRACE_ID = "0102030405060708090a0b0c0d0e0f00";
  private static final String SPAN_ID = "090a0b0c0d0e0f00";
  private static final SpanContext SPAN_CONTEXT =
      SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault());

  // One resource, 3 libraries, 2 spans each.
  private static final List<SpanData> SPANS =
      Arrays.asList(
          spanData(LIBRARY1),
          spanData(LIBRARY1),
          spanData(LIBRARY2),
          spanData(LIBRARY2),
          spanData(LIBRARY3),
          spanData(LIBRARY3));

  @Benchmark
  public List<ResourceSpans> toProto() {
    return SpanAdapter.toProtoResourceSpans(SPANS);
  }

  private static SpanData spanData(InstrumentationLibraryInfo library) {
    return TestSpanData.builder()
        .setHasEnded(true)
        .setSpanContext(SPAN_CONTEXT)
        .setParentSpanContext(SpanContext.getInvalid())
        .setName("GET /api/endpoint")
        .setKind(SpanKind.SERVER)
        .setStartEpochNanos(12345)
        .setEndEpochNanos(12349)
        .setAttributes(Attributes.of(booleanKey("key"), true))
        .setTotalAttributeCount(2)
        .setEvents(
            Collections.singletonList(EventData.create(12347, "my_event", Attributes.empty())))
        .setTotalRecordedEvents(3)
        .setLinks(Collections.singletonList(LinkData.create(SPAN_CONTEXT)))
        .setTotalRecordedLinks(2)
        .setStatus(StatusData.ok())
        .setResource(RESOURCE)
        .setInstrumentationLibraryInfo(library)
        .build();
  }
}
