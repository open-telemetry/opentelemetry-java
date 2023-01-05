/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.testing.internal;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.logs.TestLogRecordData;
import io.opentelemetry.sdk.testing.trace.TestSpanData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class FakeTelemetryUtil {

  private static final String TRACE_ID = "00000000000000000000000000abc123";
  private static final String SPAN_ID = "0000000000def456";

  /** Generate a fake {@link MetricData}. */
  public static MetricData generateFakeMetricData() {
    long startNs = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
    long endNs = startNs + TimeUnit.MILLISECONDS.toNanos(900);
    return ImmutableMetricData.createLongSum(
        Resource.empty(),
        InstrumentationScopeInfo.empty(),
        "name",
        "description",
        "1",
        ImmutableSumData.create(
            /* isMonotonic= */ true,
            AggregationTemporality.CUMULATIVE,
            Collections.singletonList(
                ImmutableLongPointData.create(
                    startNs, endNs, Attributes.of(stringKey("k"), "v"), 5))));
  }

  /** Generate a fake {@link SpanData}. */
  public static SpanData generateFakeSpanData() {
    long duration = TimeUnit.MILLISECONDS.toNanos(900);
    long startNs = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
    long endNs = startNs + duration;
    return TestSpanData.builder()
        .setHasEnded(true)
        .setSpanContext(
            SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TraceState.getDefault()))
        .setName("GET /api/endpoint")
        .setStartEpochNanos(startNs)
        .setEndEpochNanos(endNs)
        .setStatus(StatusData.ok())
        .setKind(SpanKind.SERVER)
        .setLinks(Collections.emptyList())
        .setTotalRecordedLinks(0)
        .setTotalRecordedEvents(0)
        .setInstrumentationScopeInfo(
            InstrumentationScopeInfo.builder("testLib")
                .setVersion("1.0")
                .setSchemaUrl("http://url")
                .build())
        .build();
  }

  /** Generate a fake {@link LogRecordData}. */
  public static LogRecordData generateFakeLogRecordData() {
    return TestLogRecordData.builder()
        .setResource(Resource.getDefault())
        .setInstrumentationScopeInfo(
            InstrumentationScopeInfo.builder("testLib")
                .setVersion("1.0")
                .setSchemaUrl("http://url")
                .build())
        .setBody("log body")
        .setAttributes(Attributes.builder().put("key", "value").build())
        .setSeverity(Severity.INFO)
        .setSeverityText(Severity.INFO.name())
        .setEpoch(Instant.now())
        .build();
  }

  private FakeTelemetryUtil() {}
}
