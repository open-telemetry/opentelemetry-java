/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.trace.export;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.TracerSdk;
import io.opentelemetry.sdk.trace.export.BatchSpansProcessorTest.WaitingSpanExporter;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.Tracestate;
import io.opentelemetry.trace.util.Samplers;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link SimpleSpansProcessor}. */
@RunWith(JUnit4.class)
public class SimpleSpansProcessorTest {
  private static final long MAX_SCHEDULE_DELAY_MILLIS = 500;
  private static final String SPAN_NAME = "MySpanName";
  @Mock private ReadableSpan readableSpan;
  @Mock private SpanExporter spanExporter;
  private final TracerSdk tracerSdk = new TracerSdk();
  private final WaitingSpanExporter waitingSpanExporter = new WaitingSpanExporter();
  private static final SpanContext SAMPLED_SPAN_CONTEXT =
      SpanContext.create(
          TraceId.getInvalid(),
          SpanId.getInvalid(),
          TraceFlags.builder().setIsSampled(true).build(),
          Tracestate.builder().build());
  private static final SpanContext NOT_SAMPLED_SPAN_CONTEXT =
      SpanContext.create(
          TraceId.getInvalid(),
          SpanId.getInvalid(),
          TraceFlags.builder().build(),
          Tracestate.builder().build());

  private SimpleSpansProcessor simpleSampledSpansProcessor;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    simpleSampledSpansProcessor = SimpleSpansProcessor.newBuilder(spanExporter).build();
  }

  @Test
  public void onStartSync() {
    simpleSampledSpansProcessor.onStart(readableSpan);
    verifyZeroInteractions(spanExporter);
  }

  @Test
  public void onEndSync_SampledSpan() {
    when(readableSpan.getSpanContext()).thenReturn(SAMPLED_SPAN_CONTEXT);
    when(readableSpan.toSpanProto())
        .thenReturn(Span.getDefaultInstance())
        .thenThrow(new RuntimeException());
    simpleSampledSpansProcessor.onEnd(readableSpan);
    verify(spanExporter).export(Collections.singletonList(Span.getDefaultInstance()));
  }

  @Test
  public void onEndSync_NotSampledSpan() {
    when(readableSpan.getSpanContext()).thenReturn(NOT_SAMPLED_SPAN_CONTEXT);
    when(readableSpan.toSpanProto())
        .thenReturn(Span.getDefaultInstance())
        .thenThrow(new RuntimeException());
    simpleSampledSpansProcessor.onEnd(readableSpan);
    verifyZeroInteractions(spanExporter);
  }

  @Test
  public void onEndSync_OnlySampled_NotSampledSpan() {
    when(readableSpan.getSpanContext()).thenReturn(NOT_SAMPLED_SPAN_CONTEXT);
    when(readableSpan.toSpanProto())
        .thenReturn(Span.getDefaultInstance())
        .thenThrow(new RuntimeException());
    SimpleSpansProcessor simpleSpansProcessor =
        SimpleSpansProcessor.newBuilder(spanExporter).reportOnlySampled(true).build();
    simpleSpansProcessor.onEnd(readableSpan);
    verifyZeroInteractions(spanExporter);
  }

  @Test
  public void onEndSync_OnlySampled_SampledSpan() {
    when(readableSpan.getSpanContext()).thenReturn(SAMPLED_SPAN_CONTEXT);
    when(readableSpan.toSpanProto())
        .thenReturn(Span.getDefaultInstance())
        .thenThrow(new RuntimeException());
    SimpleSpansProcessor simpleSpansProcessor =
        SimpleSpansProcessor.newBuilder(spanExporter).reportOnlySampled(true).build();
    simpleSpansProcessor.onEnd(readableSpan);
    verify(spanExporter).export(Collections.singletonList(Span.getDefaultInstance()));
  }

  @Test
  public void tracerSdk_NotSampled_Span() {
    tracerSdk.addSpanProcessor(
        BatchSpansProcessor.newBuilder(waitingSpanExporter)
            .setScheduleDelayMillis(MAX_SCHEDULE_DELAY_MILLIS)
            .build());

    tracerSdk.spanBuilder(SPAN_NAME).setSampler(Samplers.neverSample()).startSpan().end();
    tracerSdk.spanBuilder(SPAN_NAME).setSampler(Samplers.neverSample()).startSpan().end();

    io.opentelemetry.trace.Span span =
        tracerSdk.spanBuilder(SPAN_NAME).setSampler(Samplers.alwaysSample()).startSpan();
    span.end();

    // Spans are recorded and exported in the same order as they are ended, we test that a non
    // sampled span is not exported by creating and ending a sampled span after a non sampled span
    // and checking that the first exported span is the sampled span (the non sampled did not get
    // exported).
    List<Span> exported = waitingSpanExporter.waitForExport(1);
    // Need to check this because otherwise the variable span1 is unused, other option is to not
    // have a span1 variable.
    assertThat(exported).containsExactly(((ReadableSpan) span).toSpanProto());
  }

  @Test
  public void tracerSdk_NotSampled_RecordingEventsSpan() {
    tracerSdk.addSpanProcessor(
        BatchSpansProcessor.newBuilder(waitingSpanExporter)
            .setScheduleDelayMillis(MAX_SCHEDULE_DELAY_MILLIS)
            .reportOnlySampled(false)
            .build());

    io.opentelemetry.trace.Span span =
        tracerSdk
            .spanBuilder("FOO")
            .setSampler(Samplers.neverSample())
            .setRecordEvents(true)
            .startSpan();
    span.end();

    List<Span> exported = waitingSpanExporter.waitForExport(1);
    assertThat(exported).containsExactly(((ReadableSpan) span).toSpanProto());
  }

  @Test
  public void onEndSync_ExporterReturnError() {
    when(readableSpan.getSpanContext()).thenReturn(SAMPLED_SPAN_CONTEXT);
    when(readableSpan.toSpanProto())
        .thenReturn(Span.getDefaultInstance())
        .thenReturn(Span.getDefaultInstance())
        .thenThrow(new RuntimeException());
    doThrow(new RuntimeException()).when(spanExporter).export(ArgumentMatchers.<Span>anyList());
    simpleSampledSpansProcessor.onEnd(readableSpan);
    // Try again, now will no longer return error.
    simpleSampledSpansProcessor.onEnd(readableSpan);
    verify(spanExporter, times(2)).export(Collections.singletonList(Span.getDefaultInstance()));
  }

  @Test
  public void shutdown() {
    simpleSampledSpansProcessor.shutdown();
    verify(spanExporter).shutdown();
  }
}
