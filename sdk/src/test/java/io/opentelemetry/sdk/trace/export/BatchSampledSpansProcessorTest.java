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

import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.ReadableSpanAdapter;
import io.opentelemetry.sdk.trace.TracerSdk;
import io.opentelemetry.trace.util.Samplers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link BatchSampledSpansProcessor}. */
@RunWith(JUnit4.class)
public class BatchSampledSpansProcessorTest {
  private static final String SPAN_NAME_1 = "MySpanName/1";
  private static final String SPAN_NAME_2 = "MySpanName/2";
  private static final long MAX_SCHEDULE_DELAY_MILLIS = 500;
  private final TracerSdk tracerSdk = new TracerSdk();
  private final WaitingSpanExporter waitingSpanExporter = new WaitingSpanExporter();
  private final BlockingSpanExporter blockingSpanExporter = new BlockingSpanExporter();
  private final ReadableSpanAdapter adapter = new ReadableSpanAdapter();
  @Mock private SpanExporter mockServiceHandler;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void cleanup() {
    tracerSdk.shutdown();
  }

  private ReadableSpan createSampledEndedSpan(String spanName) {
    io.opentelemetry.trace.Span span =
        tracerSdk.spanBuilder(spanName).setSampler(Samplers.alwaysSample()).startSpan();
    span.end();
    return (ReadableSpan) span;
  }

  private void createNotSampledEndedSpan(String spanName) {
    io.opentelemetry.trace.Span span =
        tracerSdk.spanBuilder(spanName).setSampler(Samplers.neverSample()).startSpan();
    span.end();
  }

  @Test
  public void exportDifferentSampledSpans() {
    tracerSdk.addSpanProcessor(
        BatchSampledSpansProcessor.newBuilder(waitingSpanExporter)
            .setScheduleDelayMillis(MAX_SCHEDULE_DELAY_MILLIS)
            .build());

    ReadableSpan span1 = createSampledEndedSpan(SPAN_NAME_1);
    ReadableSpan span2 = createSampledEndedSpan(SPAN_NAME_2);
    List<SpanData> exported = waitingSpanExporter.waitForExport(2);
    assertThat(exported).containsExactly(adapter.adapt(span1), adapter.adapt(span2));
  }

  @Test
  public void exportMoreSpansThanTheBufferSize() {
    tracerSdk.addSpanProcessor(
        BatchSampledSpansProcessor.newBuilder(waitingSpanExporter)
            .setMaxQueueSize(6)
            .setMaxExportBatchSize(2)
            .setScheduleDelayMillis(MAX_SCHEDULE_DELAY_MILLIS)
            .build());

    ReadableSpan span1 = createSampledEndedSpan(SPAN_NAME_1);
    ReadableSpan span2 = createSampledEndedSpan(SPAN_NAME_1);
    ReadableSpan span3 = createSampledEndedSpan(SPAN_NAME_1);
    ReadableSpan span4 = createSampledEndedSpan(SPAN_NAME_1);
    ReadableSpan span5 = createSampledEndedSpan(SPAN_NAME_1);
    ReadableSpan span6 = createSampledEndedSpan(SPAN_NAME_1);
    List<SpanData> exported = waitingSpanExporter.waitForExport(6);
    assertThat(exported)
        .containsExactly(
            adapter.adapt(span1),
            adapter.adapt(span2),
            adapter.adapt(span3),
            adapter.adapt(span4),
            adapter.adapt(span5),
            adapter.adapt(span6));
  }

  @Test
  public void exportSpansToMultipleServices() {
    WaitingSpanExporter waitingSpanExporter2 = new WaitingSpanExporter();
    tracerSdk.addSpanProcessor(
        BatchSampledSpansProcessor.newBuilder(
                MultiSpanExporter.create(
                    Arrays.<SpanExporter>asList(waitingSpanExporter, waitingSpanExporter2)))
            .setScheduleDelayMillis(MAX_SCHEDULE_DELAY_MILLIS)
            .build());

    ReadableSpan span1 = createSampledEndedSpan(SPAN_NAME_1);
    ReadableSpan span2 = createSampledEndedSpan(SPAN_NAME_2);
    List<SpanData> exported1 = waitingSpanExporter.waitForExport(2);
    List<SpanData> exported2 = waitingSpanExporter2.waitForExport(2);
    assertThat(exported1).containsExactly(adapter.adapt(span1), adapter.adapt(span2));
    assertThat(exported2).containsExactly(adapter.adapt(span1), adapter.adapt(span2));
  }

  @Test
  public void exportMoreSpansThanTheMaximumLimit() {
    final int maxQueuedSpans = 8;
    tracerSdk.addSpanProcessor(
        BatchSampledSpansProcessor.newBuilder(
                MultiSpanExporter.create(Arrays.asList(blockingSpanExporter, waitingSpanExporter)))
            .setScheduleDelayMillis(MAX_SCHEDULE_DELAY_MILLIS)
            .setMaxQueueSize(maxQueuedSpans)
            .setMaxExportBatchSize(maxQueuedSpans / 2)
            .build());

    List<SpanData> spansToExport = new ArrayList<>(maxQueuedSpans + 1);
    // Wait to block the worker thread in the BatchSampledSpansProcessor. This ensures that no items
    // can be removed from the queue. Need to add a span to trigger the export otherwise the
    // pipeline is never called.
    spansToExport.add(adapter.adapt(createSampledEndedSpan("blocking_span")));
    blockingSpanExporter.waitUntilIsBlocked();

    for (int i = 0; i < maxQueuedSpans; i++) {
      // First export maxQueuedSpans, the worker thread is blocked so all items should be queued.
      spansToExport.add(adapter.adapt(createSampledEndedSpan("span_1_" + i)));
    }

    // TODO: assertThat(spanExporter.getReferencedSpans()).isEqualTo(maxQueuedSpans);

    // Now we should start dropping.
    for (int i = 0; i < 7; i++) {
      createSampledEndedSpan("span_2_" + i);
      // TODO: assertThat(getDroppedSpans()).isEqualTo(i + 1);
    }

    // TODO: assertThat(getReferencedSpans()).isEqualTo(maxQueuedSpans);

    // Release the blocking exporter
    blockingSpanExporter.unblock();

    // While we wait for maxQueuedSpans we ensure that the queue is also empty after this.
    List<SpanData> exported = waitingSpanExporter.waitForExport(maxQueuedSpans);
    assertThat(exported).isNotNull();
    assertThat(exported).containsExactlyElementsIn(spansToExport);
    exported.clear();
    spansToExport.clear();

    // We cannot compare with maxReferencedSpans here because the worker thread may get
    // unscheduled immediately after exporting, but before updating the pushed spans, if that is
    // the case at most bufferSize spans will miss.
    // TODO: assertThat(getPushedSpans()).isAtLeast((long) maxQueuedSpans - maxBatchSize);

    for (int i = 0; i < maxQueuedSpans; i++) {
      spansToExport.add(adapter.adapt(createSampledEndedSpan("span_3_" + i)));
      // No more dropped spans.
      // TODO: assertThat(getDroppedSpans()).isEqualTo(7);
    }

    exported = waitingSpanExporter.waitForExport(maxQueuedSpans);
    assertThat(exported).isNotNull();
    assertThat(exported).containsExactlyElementsIn(spansToExport);
  }

  @Test
  public void serviceHandlerThrowsException() {
    doThrow(new IllegalArgumentException("No export for you."))
        .when(mockServiceHandler)
        .export(ArgumentMatchers.<SpanData>anyList());

    tracerSdk.addSpanProcessor(
        BatchSampledSpansProcessor.newBuilder(
                MultiSpanExporter.create(Arrays.asList(mockServiceHandler, waitingSpanExporter)))
            .setScheduleDelayMillis(MAX_SCHEDULE_DELAY_MILLIS)
            .build());
    ReadableSpan span1 = createSampledEndedSpan(SPAN_NAME_1);
    List<SpanData> exported = waitingSpanExporter.waitForExport(1);
    assertThat(exported).containsExactly(adapter.adapt(span1));

    // Continue to export after the exception was received.
    ReadableSpan span2 = createSampledEndedSpan(SPAN_NAME_2);
    exported = waitingSpanExporter.waitForExport(1);
    assertThat(exported).containsExactly(adapter.adapt(span2));
  }

  @Test
  public void exportNotSampledSpans() {
    tracerSdk.addSpanProcessor(
        BatchSampledSpansProcessor.newBuilder(waitingSpanExporter)
            .setScheduleDelayMillis(MAX_SCHEDULE_DELAY_MILLIS)
            .build());

    createNotSampledEndedSpan(SPAN_NAME_1);
    createNotSampledEndedSpan(SPAN_NAME_2);
    ReadableSpan span2 = createSampledEndedSpan(SPAN_NAME_2);
    // Spans are recorded and exported in the same order as they are ended, we test that a non
    // sampled span is not exported by creating and ending a sampled span after a non sampled span
    // and checking that the first exported span is the sampled span (the non sampled did not get
    // exported).
    List<SpanData> exported = waitingSpanExporter.waitForExport(1);
    // Need to check this because otherwise the variable span1 is unused, other option is to not
    // have a span1 variable.
    assertThat(exported).containsExactly(adapter.adapt(span2));
  }

  @Test(timeout = 10000L)
  public void shutdownFlushes() {
    // Set the export delay to zero, for no timeout, in order to confirm the #flush() below works
    tracerSdk.addSpanProcessor(
        BatchSampledSpansProcessor.newBuilder(waitingSpanExporter)
            .setScheduleDelayMillis(0)
            .build());

    ReadableSpan span2 = createSampledEndedSpan(SPAN_NAME_2);

    // Force a shutdown, without this, the #waitForExport() call below would block indefinitely.
    tracerSdk.shutdown();

    List<SpanData> exported = waitingSpanExporter.waitForExport(1);
    assertThat(exported).containsExactly(adapter.adapt(span2));
  }

  private static final class BlockingSpanExporter implements SpanExporter {
    final Object monitor = new Object();

    private enum State {
      WAIT_TO_BLOCK,
      BLOCKED,
      UNBLOCKED
    }

    @GuardedBy("monitor")
    State state = State.WAIT_TO_BLOCK;

    @Override
    public ResultCode export(List<SpanData> spanDataList) {
      synchronized (monitor) {
        while (state != State.UNBLOCKED) {
          try {
            state = State.BLOCKED;
            // Some threads may wait for Blocked State.
            monitor.notifyAll();
            monitor.wait();
          } catch (InterruptedException e) {
            // Do nothing
          }
        }
      }
      return ResultCode.SUCCESS;
    }

    private void waitUntilIsBlocked() {
      synchronized (monitor) {
        while (state != State.BLOCKED) {
          try {
            monitor.wait();
          } catch (InterruptedException e) {
            // Do nothing
          }
        }
      }
    }

    @Override
    public void shutdown() {
      // Do nothing;
    }

    private void unblock() {
      synchronized (monitor) {
        state = State.UNBLOCKED;
        monitor.notifyAll();
      }
    }
  }

  private static final class WaitingSpanExporter implements SpanExporter {
    private final Object monitor = new Object();

    @GuardedBy("monitor")
    private final List<SpanData> spanDataList = new ArrayList<>();

    /**
     * Waits until we received numberOfSpans spans to export. Returns the list of exported {@link
     * Span} objects, otherwise {@code null} if the current thread is interrupted.
     *
     * @param numberOfSpans the number of minimum spans to be collected.
     * @return the list of exported {@link Span} objects, otherwise {@code null} if the current
     *     thread is interrupted.
     */
    @Nullable
    private List<SpanData> waitForExport(int numberOfSpans) {
      List<SpanData> ret;
      synchronized (monitor) {
        while (spanDataList.size() < numberOfSpans) {
          try {
            monitor.wait();
          } catch (InterruptedException e) {
            // Preserve the interruption status as per guidance.
            Thread.currentThread().interrupt();
            return null;
          }
        }
        ret = new ArrayList<>(spanDataList);
        spanDataList.clear();
      }
      return ret;
    }

    @Override
    public ResultCode export(List<SpanData> spans) {
      synchronized (monitor) {
        this.spanDataList.addAll(spans);
        monitor.notifyAll();
      }
      return ResultCode.SUCCESS;
    }

    @Override
    public void shutdown() {
      // Do nothing;
    }
  }
}
