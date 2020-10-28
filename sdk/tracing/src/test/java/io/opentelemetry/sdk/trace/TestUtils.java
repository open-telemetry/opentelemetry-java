/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.SpanData.Status;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** Common utilities for unit tests. */
public final class TestUtils {

  private TestUtils() {}

  /**
   * Generates some random attributes used for testing.
   *
   * @return some {@link io.opentelemetry.api.common.Attributes}
   */
  static Attributes generateRandomAttributes() {
    return Attributes.of(stringKey(UUID.randomUUID().toString()), UUID.randomUUID().toString());
  }

  /**
   * Create a very basic SpanData instance, suitable for testing. It has the bare minimum viable
   * data.
   *
   * @return A SpanData instance.
   */
  public static SpanData makeBasicSpan() {
    return TestSpanData.builder()
        .setHasEnded(true)
        .setTraceId(TraceId.getInvalid())
        .setSpanId(SpanId.getInvalid())
        .setName("span")
        .setKind(Kind.SERVER)
        .setStartEpochNanos(TimeUnit.SECONDS.toNanos(100) + 100)
        .setStatus(Status.ok())
        .setEndEpochNanos(TimeUnit.SECONDS.toNanos(200) + 200)
        .setTotalRecordedLinks(0)
        .setTotalRecordedEvents(0)
        .build();
  }

  /**
   * Create a very basic SpanData instance, suitable for testing. It has the bare minimum viable
   * data.
   *
   * @return A SpanData instance.
   */
  public static Span.Builder startSpanWithSampler(
      TracerSdkManagement tracerSdkManagement, Tracer tracer, String spanName, Sampler sampler) {
    return startSpanWithSampler(
        tracerSdkManagement, tracer, spanName, sampler, Collections.emptyMap());
  }

  /**
   * Create a very basic SpanData instance, suitable for testing. It has the bare minimum viable
   * data.
   *
   * @return A SpanData instance.
   */
  public static Span.Builder startSpanWithSampler(
      TracerSdkManagement tracerSdkManagement,
      Tracer tracer,
      String spanName,
      Sampler sampler,
      Map<String, String> attributes) {
    TraceConfig originalConfig = tracerSdkManagement.getActiveTraceConfig();
    tracerSdkManagement.updateActiveTraceConfig(
        originalConfig.toBuilder().setSampler(sampler).build());
    try {
      Span.Builder builder = tracer.spanBuilder(spanName);
      attributes.forEach(builder::setAttribute);

      return builder;
    } finally {
      tracerSdkManagement.updateActiveTraceConfig(originalConfig);
    }
  }
}
