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
import io.opentelemetry.sdk.trace.TracerSdkRegistry;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
  private static final long TEST_REPORT_INTERVAL = 5;
  private final TracerSdkRegistry tracerSdkRegistry = TracerSdkRegistry.create();
  private final Tracer tracer = tracerSdkRegistry.get("SpanWatcherProcessorTest");
  private final WaitingSpanExporter waitingSpanExporter = new WaitingSpanExporter();
  @Mock private SpanExporter throwingExporter;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void cleanup() {
    tracerSdkRegistry.shutdown();
  }

  private Span createSampledActiveSpan(String spanName) {
    return TestUtils.startSpanWithSampler(tracerSdkRegistry, tracer, spanName, Samplers.alwaysOn())
        .startSpan();
  }

  private Span createNotSampledActiveSpan(String spanName) {
    return TestUtils.startSpanWithSampler(tracerSdkRegistry, tracer, spanName, Samplers.alwaysOff())
        .startSpan();
  }

  private void waitForSpans(List<SpanData> expected) {
    waitForSpans(expected, expected, waitingSpanExporter);
  }

  private void waitForSpans(List<SpanData> expected, List<SpanData> allowed) {
    waitForSpans(expected, allowed, waitingSpanExporter);
  }

  // Wait for all the expected Spans and no more to be in the watchlist of exporter.
  private static void waitForSpans(
      List<SpanData> expected, List<SpanData> allowed, WaitingSpanExporter exporter) {
    Set<SpanData> actual = new HashSet<>(expected.size());
    do {
      if (!actual.addAll(exporter.waitForExport(1))) {
        actual.clear();
      }
      assertThat(allowed).containsAtLeastElementsIn(actual);
    } while (!actual.containsAll(expected) || actual.size() != expected.size());
    assertThat(actual).containsExactlyElementsIn(expected);

    // No other spans should be in the watchlist.
    assertThat(expected).containsAtLeastElementsIn(new HashSet<>(exporter.waitForExport(1)));
  }

  @Test
  public void testSpanWatcherBasic() {
    tracerSdkRegistry.addSpanProcessor(
        SpanWatcherProcessor.newBuilder(waitingSpanExporter)
            .setReportIntervalMillis(TEST_REPORT_INTERVAL)
            .build());

    ReadableSpan span1 = (ReadableSpan) createSampledActiveSpan(SPAN_NAME_1);
    ReadableSpan span2 = (ReadableSpan) createSampledActiveSpan(SPAN_NAME_2);
    waitForSpans(Arrays.asList(span1.toSpanData(), span2.toSpanData()));
    ((Span) span1).setAttribute("foo", "bar");
    List<SpanData> initialExpected = Arrays.asList(span1.toSpanData(), span2.toSpanData());
    waitForSpans(initialExpected);
    ((Span) span1).end();

    waitForSpans(Arrays.asList(span2.toSpanData()), initialExpected);
  }

  @Test
  public void testUnreferencedSpansAreNotReported() {
    tracerSdkRegistry.addSpanProcessor(
        SpanWatcherProcessor.newBuilder(waitingSpanExporter)
            .setReportIntervalMillis(TEST_REPORT_INTERVAL)
            .build());

    @SuppressWarnings("unused")
    ReadableSpan span1 = (ReadableSpan) createSampledActiveSpan(SPAN_NAME_1);
    ReadableSpan span2 = (ReadableSpan) createSampledActiveSpan(SPAN_NAME_2);
    waitForSpans(Arrays.asList(span1.toSpanData(), span2.toSpanData()));

    //noinspection UnusedAssignment
    span1 = null;

    // Make sure the span is GCd, so the weakref goes stale.
    System.gc();
    System.gc();
    waitForSpans(Arrays.asList(span2.toSpanData()));
  }

  @Test
  public void exportMoreSpansThanTheBufferSize() {
    tracerSdkRegistry.addSpanProcessor(
        SpanWatcherProcessor.newBuilder(waitingSpanExporter)
            .setMaxWatchlistSize(6)
            .setMaxExportBatchSize(2)
            .setReportIntervalMillis(TEST_REPORT_INTERVAL)
            .build());

    ReadableSpan span1 = (ReadableSpan) createSampledActiveSpan("s*1");
    ReadableSpan span2 = (ReadableSpan) createSampledActiveSpan("s*2");
    ReadableSpan span3 = (ReadableSpan) createSampledActiveSpan("s*3");
    ReadableSpan span4 = (ReadableSpan) createSampledActiveSpan("s*4");
    ReadableSpan span5 = (ReadableSpan) createSampledActiveSpan("s*5");
    ReadableSpan span6 = (ReadableSpan) createSampledActiveSpan("s*6");
    waitForSpans(
        Arrays.asList(
            span1.toSpanData(),
            span2.toSpanData(),
            span3.toSpanData(),
            span4.toSpanData(),
            span5.toSpanData(),
            span6.toSpanData()));
  }

  @Test
  public void exportSpansToMultipleServices() {
    io.opentelemetry.sdk.trace.export.WaitingSpanExporter waitingSpanExporter2 =
        new io.opentelemetry.sdk.trace.export.WaitingSpanExporter();
    tracerSdkRegistry.addSpanProcessor(
        SpanWatcherProcessor.newBuilder(
                MultiSpanExporter.create(
                    Arrays.<SpanExporter>asList(waitingSpanExporter, waitingSpanExporter2)))
            .setReportIntervalMillis(TEST_REPORT_INTERVAL)
            .build());

    ReadableSpan span1 = (ReadableSpan) createSampledActiveSpan(SPAN_NAME_1);
    ReadableSpan span2 = (ReadableSpan) createSampledActiveSpan(SPAN_NAME_2);
    List<SpanData> expected = Arrays.asList(span1.toSpanData(), span2.toSpanData());
    waitForSpans(expected, expected, waitingSpanExporter);
    waitForSpans(expected, expected, waitingSpanExporter2);
  }

  @Test
  public void exportMoreSpansThanTheMaximumLimit() {
    final int maxQueuedSpans = 8;
    final SpanWatcherProcessor processor =
        SpanWatcherProcessor.newBuilder(waitingSpanExporter)
            .setReportIntervalMillis(TEST_REPORT_INTERVAL)
            .setMaxWatchlistSize(maxQueuedSpans)
            .setMaxExportBatchSize(maxQueuedSpans / 2)
            .build();
    tracerSdkRegistry.addSpanProcessor(processor);

    List<ReadableSpan> spansToExport = new ArrayList<>(maxQueuedSpans);
    // Wait to block the worker thread in the BatchSampledSpansProcessor. This ensures that no items
    // can be removed from the queue. Need to add a span to trigger the export otherwise the
    // pipeline is never called.
    spansToExport.add(((ReadableSpan) createSampledActiveSpan("blocking_span")));

    for (int i = 0; i < maxQueuedSpans - 1; i++) {
      // First export maxQueuedSpans, the worker thread is blocked so all items should be queued.
      spansToExport.add((ReadableSpan) createSampledActiveSpan("span_ini_" + i));
    }

    assertThat(spansToExport).hasSize(maxQueuedSpans); // INTERNAL test assertion

    // TODO: assertThat(spanExporter.getReferencedSpans()).isEqualTo(maxQueuedSpans);

    // We keep strong references to these spans because we want to them to not be included
    // even if any weak reference to them stays valid.
    @SuppressWarnings("ModifiedButNotUsed")
    List<Span> notIncludedSpans = new ArrayList<>();

    // Now we should start dropping.
    for (int i = 0; i < 7; i++) {
      // Keep a strong reference to these spans.
      notIncludedSpans.add(createSampledActiveSpan("span_not_" + i));
      // TODO: assertThat(getDroppedSpans()).isEqualTo(i + 1);
    }

    // TODO: assertThat(getReferencedSpans()).isEqualTo(maxQueuedSpans);

    // While we wait for maxQueuedSpans we ensure that the queue is also empty after this.

    List<SpanData> expected = new ArrayList<>(spansToExport.size() * 2);
    for (ReadableSpan readableSpan : spansToExport) {
      expected.add(readableSpan.toSpanData());
    }

    waitForSpans(expected);

    // We cannot compare with maxReferencedSpans here because the worker thread may get
    // unscheduled immediately after exporting, but before updating the pushed spans, if that is
    // the case at most bufferSize spans will miss.
    // TODO: assertThat(getPushedSpans()).isAtLeast((long) maxQueuedSpans - maxBatchSize);

    // End each but the last span.
    for (int i = 0; i < spansToExport.size() - 1; ++i) {
      ((Span) spansToExport.get(i)).end();
    }

    waitForSpans(expected.subList(expected.size() - 1, expected.size()), expected);

    expected.clear();

    for (int i = 0; i < maxQueuedSpans - 1; i++) {
      spansToExport.set(i, ((ReadableSpan) createSampledActiveSpan("span_3_" + i)));
      // No more dropped spans.
      // TODO: assertThat(getDroppedSpans()).isEqualTo(7);
    }

    for (ReadableSpan readableSpan : spansToExport) {
      expected.add(readableSpan.toSpanData());
    }
    waitForSpans(expected);
  }

  @Test
  public void serviceHandlerThrowsException() {
    doThrow(new IllegalArgumentException("No export for you."))
        .when(throwingExporter)
        .export(ArgumentMatchers.<SpanData>anyList());

    tracerSdkRegistry.addSpanProcessor(
        SpanWatcherProcessor.newBuilder(
                MultiSpanExporter.create(Arrays.asList(throwingExporter, waitingSpanExporter)))
            .setReportIntervalMillis(TEST_REPORT_INTERVAL)
            .build());
    ReadableSpan span1 = (ReadableSpan) createSampledActiveSpan(SPAN_NAME_1);
    waitForSpans(Arrays.asList(span1.toSpanData()));
    ((Span) span1).end();

    // Continue to export after the exception was received.
    ReadableSpan span2 = (ReadableSpan) createSampledActiveSpan(SPAN_NAME_2);
    waitForSpans(Arrays.asList(span2.toSpanData()));
  }

  @Test
  public void exportNotSampledSpans() {
    tracerSdkRegistry.addSpanProcessor(
        SpanWatcherProcessor.newBuilder(waitingSpanExporter)
            .setReportIntervalMillis(TEST_REPORT_INTERVAL)
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
    waitForSpans(Arrays.asList(span3.toSpanData()));
  }
}
