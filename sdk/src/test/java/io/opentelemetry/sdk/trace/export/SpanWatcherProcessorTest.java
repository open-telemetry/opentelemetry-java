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

import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.Samplers;
import io.opentelemetry.sdk.trace.SpanData;
import io.opentelemetry.sdk.trace.TestUtils;
import io.opentelemetry.sdk.trace.TracerSdkFactory;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link BatchSpansProcessor}. */
@RunWith(JUnit4.class)
public class SpanWatcherProcessorTest {
  private static final String SPAN_NAME_1 = "MySpanName/1";
  private static final String SPAN_NAME_2 = "MySpanName/2";
  private static final long MAX_SCHEDULE_DELAY_MILLIS = 500;
  private final TracerSdkFactory tracerSdkFactory = TracerSdkFactory.create();
  private final Tracer tracer = tracerSdkFactory.get("BatchSpansProcessorTest");
  private final WaitingSpanExporter waitingSpanExporter = new WaitingSpanExporter();
  @Mock private SpanExporter mockServiceHandler;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void cleanup() {
    tracerSdkFactory.shutdown();
  }

  private Span createSampledActiveSpan(String spanName) {
    return TestUtils.startSpanWithSampler(tracerSdkFactory, tracer, spanName, Samplers.alwaysOn())
        .startSpan();
  }

  private Span createNotSampledActiveSpan(String spanName) {
    return TestUtils.startSpanWithSampler(tracerSdkFactory, tracer, spanName, Samplers.alwaysOff())
        .startSpan();
  }

  @Test
  public void testSpanWatcherBasic() {
    tracerSdkFactory.addSpanProcessor(
        SpanWatcherProcessor.newBuilder(waitingSpanExporter)
            .setScheduleDelayMillis(MAX_SCHEDULE_DELAY_MILLIS)
            .build());

    ReadableSpan span1 = (ReadableSpan) createSampledActiveSpan(SPAN_NAME_1);
    ReadableSpan span2 = (ReadableSpan) createSampledActiveSpan(SPAN_NAME_2);
    List<SpanData> exported = waitingSpanExporter.waitForExport(2); // Active spans are reported
    assertThat(exported).containsExactly(span1.toSpanData(), span2.toSpanData());
    ((Span) span1).setAttribute("foo", "bar");
    exported = waitingSpanExporter.waitForExport(2); // Attribute changes get reflected
    assertThat(exported).containsExactly(span1.toSpanData(), span2.toSpanData());
    ((Span) span1).end();
    exported = waitingSpanExporter.waitForExport(2); // Inactive spans are not reported
    assertThat(exported).containsExactly(span2.toSpanData(), span2.toSpanData());
  }

  @Test
  public void testUnreferencedSpansAreNotReported() {
    tracerSdkFactory.addSpanProcessor(
        SpanWatcherProcessor.newBuilder(waitingSpanExporter)
            .setScheduleDelayMillis(MAX_SCHEDULE_DELAY_MILLIS)
            .build());

    @SuppressWarnings("unused")
    ReadableSpan span1 = (ReadableSpan) createSampledActiveSpan(SPAN_NAME_1);
    ReadableSpan span2 = (ReadableSpan) createSampledActiveSpan(SPAN_NAME_2);
    List<SpanData> exported = waitingSpanExporter.waitForExport(2); // Active spans are reported
    assertThat(exported).containsExactly(span1.toSpanData(), span2.toSpanData());

    //noinspection UnusedAssignment
    span1 = null;

    // Make sure the span is GCd, so the weakref goes stale.
    System.gc();
    System.gc();
    exported = waitingSpanExporter.waitForExport(2);
    assertThat(exported).containsExactly(span2.toSpanData(), span2.toSpanData());
  }

  @Test
  public void exportMoreSpansThanTheBufferSize() {
    tracerSdkFactory.addSpanProcessor(
        SpanWatcherProcessor.newBuilder(waitingSpanExporter)
            .setMaxQueueSize(6)
            .setMaxExportBatchSize(2)
            .setScheduleDelayMillis(MAX_SCHEDULE_DELAY_MILLIS)
            .build());

    ReadableSpan span1 = (ReadableSpan) createSampledActiveSpan(SPAN_NAME_1);
    ReadableSpan span2 = (ReadableSpan) createSampledActiveSpan(SPAN_NAME_2);
    ReadableSpan span3 = (ReadableSpan) createSampledActiveSpan(SPAN_NAME_1);
    ReadableSpan span4 = (ReadableSpan) createSampledActiveSpan(SPAN_NAME_1);
    ReadableSpan span5 = (ReadableSpan) createSampledActiveSpan(SPAN_NAME_1);
    ReadableSpan span6 = (ReadableSpan) createSampledActiveSpan(SPAN_NAME_1);
    List<SpanData> exported = waitingSpanExporter.waitForExport(6);
    assertThat(exported)
        .containsExactly(
            span1.toSpanData(),
            span2.toSpanData(),
            span3.toSpanData(),
            span4.toSpanData(),
            span5.toSpanData(),
            span6.toSpanData());
  }

  @Test
  public void exportSpansToMultipleServices() {
    io.opentelemetry.sdk.trace.export.WaitingSpanExporter waitingSpanExporter2 =
        new io.opentelemetry.sdk.trace.export.WaitingSpanExporter();
    tracerSdkFactory.addSpanProcessor(
        SpanWatcherProcessor.newBuilder(
                MultiSpanExporter.create(
                    Arrays.<SpanExporter>asList(waitingSpanExporter, waitingSpanExporter2)))
            .setScheduleDelayMillis(MAX_SCHEDULE_DELAY_MILLIS)
            .build());

    ReadableSpan span1 = (ReadableSpan) createSampledActiveSpan(SPAN_NAME_1);
    ReadableSpan span2 = (ReadableSpan) createSampledActiveSpan(SPAN_NAME_2);
    List<SpanData> exported1 = waitingSpanExporter.waitForExport(2);
    List<SpanData> exported2 = waitingSpanExporter2.waitForExport(2);
    assertThat(exported1).containsExactly(span1.toSpanData(), span2.toSpanData());
    assertThat(exported2).containsExactly(span1.toSpanData(), span2.toSpanData());
  }

  @Test
  public void exportMoreSpansThanTheMaximumLimit() {
    final int maxQueuedSpans = 8;
    tracerSdkFactory.addSpanProcessor(
        SpanWatcherProcessor.newBuilder(waitingSpanExporter)
            .setScheduleDelayMillis(MAX_SCHEDULE_DELAY_MILLIS)
            .setMaxQueueSize(maxQueuedSpans)
            .setMaxExportBatchSize(maxQueuedSpans / 2)
            .build());

    List<ReadableSpan> spansToExport = new ArrayList<>(maxQueuedSpans);
    // Wait to block the worker thread in the BatchSampledSpansProcessor. This ensures that no items
    // can be removed from the queue. Need to add a span to trigger the export otherwise the
    // pipeline is never called.
    spansToExport.add(((ReadableSpan) createSampledActiveSpan("blocking_span")));

    for (int i = 0; i < maxQueuedSpans - 1; i++) {
      // First export maxQueuedSpans, the worker thread is blocked so all items should be queued.
      spansToExport.add((ReadableSpan) createSampledActiveSpan("span_1_" + i));
    }

    // TODO: assertThat(spanExporter.getReferencedSpans()).isEqualTo(maxQueuedSpans);

    @SuppressWarnings("ModifiedButNotUsed")
    List<Span> notIncludedSpans = new ArrayList<>();
    // Now we should start dropping.
    for (int i = 0; i < 7; i++) {
      // Keep a strong reference to these spans.
      notIncludedSpans.add(createSampledActiveSpan("span_2_" + i));
      // TODO: assertThat(getDroppedSpans()).isEqualTo(i + 1);
    }

    // TODO: assertThat(getReferencedSpans()).isEqualTo(maxQueuedSpans);

    // While we wait for maxQueuedSpans we ensure that the queue is also empty after this.
    List<SpanData> exported = waitingSpanExporter.waitForExport(maxQueuedSpans);
    assertThat(exported).isNotNull();
    List<SpanData> expected = new ArrayList<>(spansToExport.size());
    for (ReadableSpan readableSpan : spansToExport) {
      expected.add(readableSpan.toSpanData());
    }
    assertThat(exported).containsExactlyElementsIn(expected);
    exported.clear();

    // We cannot compare with maxReferencedSpans here because the worker thread may get
    // unscheduled immediately after exporting, but before updating the pushed spans, if that is
    // the case at most bufferSize spans will miss.
    // TODO: assertThat(getPushedSpans()).isAtLeast((long) maxQueuedSpans - maxBatchSize);

    for (int i = 0; i < spansToExport.size() - 1; ++i) {
      ((Span) spansToExport.get(i)).end();
    }

    assertThat(waitingSpanExporter.waitForExport(1))
        .containsExactly(expected.get(expected.size() - 1));

    expected.clear();

    for (int i = 0; i < maxQueuedSpans - 1; i++) {
      spansToExport.set(i, ((ReadableSpan) createSampledActiveSpan("span_3_" + i)));
      // No more dropped spans.
      // TODO: assertThat(getDroppedSpans()).isEqualTo(7);
    }

    for (ReadableSpan readableSpan : spansToExport) {
      expected.add(readableSpan.toSpanData());
    }
    exported = waitingSpanExporter.waitForExport(maxQueuedSpans);
    assertThat(exported).isNotNull();
    assertThat(exported).containsExactlyElementsIn(expected);
  }

  @Test
  public void serviceHandlerThrowsException() {
    doThrow(new IllegalArgumentException("No export for you."))
        .when(mockServiceHandler)
        .export(ArgumentMatchers.<SpanData>anyList());

    tracerSdkFactory.addSpanProcessor(
        SpanWatcherProcessor.newBuilder(
                MultiSpanExporter.create(Arrays.asList(mockServiceHandler, waitingSpanExporter)))
            .setScheduleDelayMillis(MAX_SCHEDULE_DELAY_MILLIS)
            .build());
    ReadableSpan span1 = (ReadableSpan) createSampledActiveSpan(SPAN_NAME_1);
    List<SpanData> exported = waitingSpanExporter.waitForExport(1);
    assertThat(exported).containsExactly(span1.toSpanData());
    ((Span) span1).end();

    // Continue to export after the exception was received.
    ReadableSpan span2 = (ReadableSpan) createSampledActiveSpan(SPAN_NAME_2);
    exported = waitingSpanExporter.waitForExport(1);
    assertThat(exported).containsExactly(span2.toSpanData());
  }

  @Test
  public void exportNotSampledSpans() {
    tracerSdkFactory.addSpanProcessor(
        SpanWatcherProcessor.newBuilder(waitingSpanExporter)
            .setScheduleDelayMillis(MAX_SCHEDULE_DELAY_MILLIS)
            .build());

    @SuppressWarnings("unused")
    Span s1 = createNotSampledActiveSpan(SPAN_NAME_1);

    @SuppressWarnings("unused")
    Span s2 = createNotSampledActiveSpan(SPAN_NAME_2);
    ReadableSpan span3 = (ReadableSpan) createSampledActiveSpan(SPAN_NAME_2);
    // Spans are recorded and exported in the same order as they are ended, we test that a non
    // sampled span is not exported by creating and ending a sampled span after a non sampled span
    // and checking that the first exported span is the sampled span (the non sampled did not get
    // exported).
    List<SpanData> exported = waitingSpanExporter.waitForExport(2);
    assertThat(exported).containsExactly(span3.toSpanData(), span3.toSpanData());
  }
}
