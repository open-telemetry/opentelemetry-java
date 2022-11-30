/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.StatusData;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import zipkin2.Endpoint;
import zipkin2.Span;

class ZipkinTestUtil {

  static final String TRACE_ID = "d239036e7d5cec116b562147388b35bf";
  static final String SPAN_ID = "9cc1e3049173be09";
  static final String PARENT_SPAN_ID = "8b03ab423da481c5";

  private static final Attributes attributes = Attributes.empty();
  private static final List<EventData> annotations =
      Collections.unmodifiableList(
          Arrays.asList(
              EventData.create(1505855799_433901068L, "RECEIVED", Attributes.empty()),
              EventData.create(1505855799_459486280L, "SENT", Attributes.empty())));

  private ZipkinTestUtil() {}

  static TestSpanData.Builder spanBuilder() {
    return TestSpanData.builder()
        .setSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault()))
        .setParentSpanContext(
            SpanContext.create(
                TRACE_ID, PARENT_SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault()))
        .setResource(
            Resource.create(
                Attributes.builder().put(ResourceAttributes.SERVICE_NAME, "tweetiebird").build()))
        .setStatus(StatusData.ok())
        .setKind(SpanKind.SERVER)
        .setName("Recv.helloworld.Greeter.SayHello")
        .setStartEpochNanos(1505855794_194009601L)
        .setEndEpochNanos(1505855799_465726528L)
        .setAttributes(attributes)
        .setTotalAttributeCount(attributes.size())
        .setTotalRecordedEvents(annotations.size())
        .setEvents(annotations)
        .setLinks(Collections.emptyList())
        .setHasEnded(true);
  }

  static Span zipkinSpan(@Nullable Span.Kind kind, InetAddress localIp) {
    return zipkinSpanBuilder(kind, localIp).build();
  }

  static Span.Builder zipkinSpanBuilder(@Nullable Span.Kind kind, InetAddress localIp) {
    return Span.newBuilder()
        .traceId(TRACE_ID)
        .parentId(PARENT_SPAN_ID)
        .id(SPAN_ID)
        .kind(kind)
        .name("Recv.helloworld.Greeter.SayHello")
        .timestamp(1505855794000000L + 194009601L / 1000)
        .duration((1505855799000000L + 465726528L / 1000) - (1505855794000000L + 194009601L / 1000))
        .localEndpoint(Endpoint.newBuilder().ip(localIp).serviceName("tweetiebird").build())
        .addAnnotation(1505855799000000L + 433901068L / 1000, "\"RECEIVED\":{}")
        .addAnnotation(1505855799000000L + 459486280L / 1000, "\"SENT\":{}");
  }
}
