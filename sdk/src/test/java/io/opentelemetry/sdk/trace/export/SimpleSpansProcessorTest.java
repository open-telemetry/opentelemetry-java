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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.Samplers;
import io.opentelemetry.sdk.trace.TestUtils;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.BatchSpansProcessorTest.WaitingSpanExporter;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceState;
import io.opentelemetry.trace.Tracer;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link SimpleSpansProcessor}. */
@RunWith(JUnit4.class)
public class SimpleSpansProcessorTest {
  private static final long MAX_SCHEDULE_DELAY_MILLIS = 500;
  private static final String SPAN_NAME = "MySpanName";
  @Mock private ReadableSpan readableSpan;
  @Mock private SpanExporter spanExporter;
  private final TracerSdkProvider tracerSdkFactory = TracerSdkProvider.builder().build();
  private final Tracer tracer = tracerSdkFactory.get("SimpleSpansProcessor");
  private static final SpanContext SAMPLED_SPAN_CONTEXT =
      SpanContext.create(
          TraceId.getInvalid(),
          SpanId.getInvalid(),
          TraceFlags.builder().setIsSampled(true).build(),
          TraceState.builder().build());
  private static final SpanContext NOT_SAMPLED_SPAN_CONTEXT = SpanContext.getInvalid();

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
    SpanData spanData = TestUtils.makeBasicSpan();
    when(readableSpan.getSpanContext()).thenReturn(SAMPLED_SPAN_CONTEXT);
    when(readableSpan.toSpanData()).thenReturn(spanData);
    simpleSampledSpansProcessor.onEnd(readableSpan);
    verify(spanExporter).export(Collections.singletonList(spanData));
  }

  @Test
  public void onEndSync_NotSampledSpan() {
    when(readableSpan.getSpanContext()).thenReturn(NOT_SAMPLED_SPAN_CONTEXT);
    simpleSampledSpansProcessor.onEnd(readableSpan);
    verifyZeroInteractions(spanExporter);
  }

  @Test
  public void onEndSync_OnlySampled_NotSampledSpan() {
    when(readableSpan.getSpanContext()).thenReturn(NOT_SAMPLED_SPAN_CONTEXT);
    when(readableSpan.toSpanData())
        .thenReturn(TestUtils.makeBasicSpan())
        .thenThrow(new RuntimeException());
    SimpleSpansProcessor simpleSpansProcessor =
        SimpleSpansProcessor.newBuilder(spanExporter).reportOnlySampled(true).build();
    simpleSpansProcessor.onEnd(readableSpan);
    verifyZeroInteractions(spanExporter);
  }

  @Test
  public void onEndSync_OnlySampled_SampledSpan() {
    when(readableSpan.getSpanContext()).thenReturn(SAMPLED_SPAN_CONTEXT);
    when(readableSpan.toSpanData())
        .thenReturn(TestUtils.makeBasicSpan())
        .thenThrow(new RuntimeException());
    SimpleSpansProcessor simpleSpansProcessor =
        SimpleSpansProcessor.newBuilder(spanExporter).reportOnlySampled(true).build();
    simpleSpansProcessor.onEnd(readableSpan);
    verify(spanExporter).export(Collections.singletonList(TestUtils.makeBasicSpan()));
  }

  @Test
  public void tracerSdk_NotSampled_Span() {
    WaitingSpanExporter waitingSpanExporter = new WaitingSpanExporter(1);

    BatchSpansProcessor.Config config =
        BatchSpansProcessor.Config.newBuilder()
            .setScheduleDelayMillis(MAX_SCHEDULE_DELAY_MILLIS)
            .build();
    tracerSdkFactory.addSpanProcessor(BatchSpansProcessor.create(waitingSpanExporter, config));

    TestUtils.startSpanWithSampler(tracerSdkFactory, tracer, SPAN_NAME, Samplers.alwaysOff())
        .startSpan()
        .end();
    TestUtils.startSpanWithSampler(tracerSdkFactory, tracer, SPAN_NAME, Samplers.alwaysOff())
        .startSpan()
        .end();

    io.opentelemetry.trace.Span span = tracer.spanBuilder(SPAN_NAME).startSpan();
    span.end();

    // Spans are recorded and exported in the same order as they are ended, we test that a non
    // sampled span is not exported by creating and ending a sampled span after a non sampled span
    // and checking that the first exported span is the sampled span (the non sampled did not get
    // exported).
    List<SpanData> exported = waitingSpanExporter.waitForExport();
    // Need to check this because otherwise the variable span1 is unused, other option is to not
    // have a span1 variable.
    assertThat(exported).containsExactly(((ReadableSpan) span).toSpanData());
  }

  @Test
  public void tracerSdk_NotSampled_RecordingEventsSpan() {
    // TODO(bdrutu): Fix this when Sampler return RECORD option.
    /*
    tracer.addSpanProcessor(
        BatchSpansProcessor.newBuilder(waitingSpanExporter)
            .setScheduleDelayMillis(MAX_SCHEDULE_DELAY_MILLIS)
            .reportOnlySampled(false)
            .build());

    io.opentelemetry.trace.Span span =
        tracer
            .spanBuilder("FOO")
            .setSampler(Samplers.neverSample())
            .startSpanWithSampler();
    span.end();

    List<SpanData> exported = waitingSpanExporter.waitForExport(1);
    assertThat(exported).containsExactly(((ReadableSpan) span).toSpanData());
    */
  }

  @Test
  public void onEndSync_ExporterReturnError() {
    SpanData spanData = TestUtils.makeBasicSpan();
    when(readableSpan.getSpanContext()).thenReturn(SAMPLED_SPAN_CONTEXT);
    when(readableSpan.toSpanData()).thenReturn(spanData);
    simpleSampledSpansProcessor.onEnd(readableSpan);
    // Try again, now will no longer return error.
    simpleSampledSpansProcessor.onEnd(readableSpan);
    verify(spanExporter, times(2)).export(Collections.singletonList(spanData));
  }

  @Test
  public void shutdown() {
    simpleSampledSpansProcessor.shutdown();
    verify(spanExporter).shutdown();
  }
}
