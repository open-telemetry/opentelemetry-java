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
import io.opentelemetry.sdk.trace.TestUtils;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.trace.Tracer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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

/** Unit tests for {@link BatchSpansProcessor}. */
@RunWith(JUnit4.class)
public class BatchSpansProcessorTest {

  private static final String SPAN_NAME_1 = "MySpanName/1";
  private static final String SPAN_NAME_2 = "MySpanName/2";
  private static final long MAX_SCHEDULE_DELAY_MILLIS = 500;
  private final TracerSdkProvider tracerSdkFactory = TracerSdkProvider.builder().build();
  private final Tracer tracer = tracerSdkFactory.get("BatchSpansProcessorTest");
  private final BlockingSpanExporter blockingSpanExporter = new BlockingSpanExporter();
  @Mock private SpanExporter mockServiceHandler;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void cleanup() {
    tracerSdkFactory.shutdown();
  }

  private ReadableSpan createSampledEndedSpan(final String spanName) {
    final io.opentelemetry.trace.Span span =
        TestUtils.startSpanWithSampler(tracerSdkFactory, tracer, spanName, Samplers.alwaysOn())
            .startSpan();
    span.end();
    return (ReadableSpan) span;
  }

  // TODO(bdrutu): Fix this when Sampler return RECORD option.
  /*
  private ReadableSpan createNotSampledRecordingEventsEndedSpan(String spanName) {
    io.opentelemetry.trace.Span span =
        tracer.spanBuilder(spanName).setSampler(Samplers.neverSample()).startSpanWithSampler();
    span.end();
    return (ReadableSpan) span;
  }
  */

  private void createNotSampledEndedSpan(final String spanName) {
    TestUtils.startSpanWithSampler(tracerSdkFactory, tracer, spanName, Samplers.alwaysOff())
        .startSpan()
        .end();
  }

  @Test
  public void exportDifferentSampledSpans() {
    final WaitingSpanExporter waitingSpanExporter = new WaitingSpanExporter(2);
    tracerSdkFactory.addSpanProcessor(
        BatchSpansProcessor.newBuilder(waitingSpanExporter)
            .setScheduleDelayMillis(MAX_SCHEDULE_DELAY_MILLIS)
            .build());

    final ReadableSpan span1 = createSampledEndedSpan(SPAN_NAME_1);
    final ReadableSpan span2 = createSampledEndedSpan(SPAN_NAME_2);
    final List<SpanData> exported = waitingSpanExporter.waitForExport();
    assertThat(exported).containsExactly(span1.toSpanData(), span2.toSpanData());
  }

  @Test
  public void exportMoreSpansThanTheBufferSize() {
    final WaitingSpanExporter waitingSpanExporter = new WaitingSpanExporter(6);
    tracerSdkFactory.addSpanProcessor(
        BatchSpansProcessor.newBuilder(waitingSpanExporter)
            .setMaxQueueSize(6)
            .setMaxExportBatchSize(2)
            .setScheduleDelayMillis(MAX_SCHEDULE_DELAY_MILLIS)
            .build());

    final ReadableSpan span1 = createSampledEndedSpan(SPAN_NAME_1);
    final ReadableSpan span2 = createSampledEndedSpan(SPAN_NAME_1);
    final ReadableSpan span3 = createSampledEndedSpan(SPAN_NAME_1);
    final ReadableSpan span4 = createSampledEndedSpan(SPAN_NAME_1);
    final ReadableSpan span5 = createSampledEndedSpan(SPAN_NAME_1);
    final ReadableSpan span6 = createSampledEndedSpan(SPAN_NAME_1);
    final List<SpanData> exported = waitingSpanExporter.waitForExport();
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
  public void forceExport() {
    final WaitingSpanExporter waitingSpanExporter = new WaitingSpanExporter(1, 1);
    final BatchSpansProcessor batchSpansProcessor =
        BatchSpansProcessor.newBuilder(waitingSpanExporter)
            .setMaxQueueSize(10_000)
            .setMaxExportBatchSize(2_000)
            .setScheduleDelayMillis(10_000) // 10s
            .build();
    tracerSdkFactory.addSpanProcessor(batchSpansProcessor);
    for (int i = 0; i < 100; i++) {
      createSampledEndedSpan("notExported");
    }
    List<SpanData> exported = waitingSpanExporter.waitForExport();
    assertThat(exported).isNotNull();
    assertThat(exported.size()).isEqualTo(0);
    batchSpansProcessor.forceFlush();
    exported = waitingSpanExporter.waitForExport();
    assertThat(exported).isNotNull();
    assertThat(exported.size()).isEqualTo(100);
  }

  @Test
  public void exportSpansToMultipleServices() {
    final WaitingSpanExporter waitingSpanExporter = new WaitingSpanExporter(2);
    final WaitingSpanExporter waitingSpanExporter2 = new WaitingSpanExporter(2);
    tracerSdkFactory.addSpanProcessor(
        BatchSpansProcessor.newBuilder(
                MultiSpanExporter.create(
                    Arrays.<SpanExporter>asList(waitingSpanExporter, waitingSpanExporter2)))
            .setScheduleDelayMillis(MAX_SCHEDULE_DELAY_MILLIS)
            .build());

    final ReadableSpan span1 = createSampledEndedSpan(SPAN_NAME_1);
    final ReadableSpan span2 = createSampledEndedSpan(SPAN_NAME_2);
    final List<SpanData> exported1 = waitingSpanExporter.waitForExport();
    final List<SpanData> exported2 = waitingSpanExporter2.waitForExport();
    assertThat(exported1).containsExactly(span1.toSpanData(), span2.toSpanData());
    assertThat(exported2).containsExactly(span1.toSpanData(), span2.toSpanData());
  }

  @Test
  public void exportMoreSpansThanTheMaximumLimit() {
    final int maxQueuedSpans = 8;
    final WaitingSpanExporter waitingSpanExporter = new WaitingSpanExporter(maxQueuedSpans);
    tracerSdkFactory.addSpanProcessor(
        BatchSpansProcessor.newBuilder(
                MultiSpanExporter.create(Arrays.asList(blockingSpanExporter, waitingSpanExporter)))
            .setScheduleDelayMillis(MAX_SCHEDULE_DELAY_MILLIS)
            .setMaxQueueSize(maxQueuedSpans)
            .setMaxExportBatchSize(maxQueuedSpans / 2)
            .build());

    final List<SpanData> spansToExport = new ArrayList<>(maxQueuedSpans + 1);
    // Wait to block the worker thread in the BatchSampledSpansProcessor. This ensures that no items
    // can be removed from the queue. Need to add a span to trigger the config otherwise the
    // pipeline is never called.
    spansToExport.add(createSampledEndedSpan("blocking_span").toSpanData());
    blockingSpanExporter.waitUntilIsBlocked();

    for (int i = 0; i < maxQueuedSpans; i++) {
      // First config maxQueuedSpans, the worker thread is blocked so all items should be queued.
      spansToExport.add(createSampledEndedSpan("span_1_" + i).toSpanData());
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
    List<SpanData> exported = waitingSpanExporter.waitForExport();
    assertThat(exported).isNotNull();
    assertThat(exported).containsExactlyElementsIn(spansToExport);
    exported.clear();
    spansToExport.clear();

    waitingSpanExporter.reset();
    // We cannot compare with maxReferencedSpans here because the worker thread may get
    // unscheduled immediately after exporting, but before updating the pushed spans, if that is
    // the case at most bufferSize spans will miss.
    // TODO: assertThat(getPushedSpans()).isAtLeast((long) maxQueuedSpans - maxBatchSize);

    for (int i = 0; i < maxQueuedSpans; i++) {
      spansToExport.add(createSampledEndedSpan("span_3_" + i).toSpanData());
      // No more dropped spans.
      // TODO: assertThat(getDroppedSpans()).isEqualTo(7);
    }

    exported = waitingSpanExporter.waitForExport();
    assertThat(exported).isNotNull();
    assertThat(exported).containsExactlyElementsIn(spansToExport);
  }

  @Test
  public void serviceHandlerThrowsException() {
    final WaitingSpanExporter waitingSpanExporter = new WaitingSpanExporter(1);
    doThrow(new IllegalArgumentException("No config for you."))
        .when(mockServiceHandler)
        .export(ArgumentMatchers.<SpanData>anyList());

    tracerSdkFactory.addSpanProcessor(
        BatchSpansProcessor.newBuilder(
                MultiSpanExporter.create(Arrays.asList(mockServiceHandler, waitingSpanExporter)))
            .setScheduleDelayMillis(MAX_SCHEDULE_DELAY_MILLIS)
            .build());
    final ReadableSpan span1 = createSampledEndedSpan(SPAN_NAME_1);
    List<SpanData> exported = waitingSpanExporter.waitForExport();
    assertThat(exported).containsExactly(span1.toSpanData());
    waitingSpanExporter.reset();
    // Continue to config after the exception was received.
    final ReadableSpan span2 = createSampledEndedSpan(SPAN_NAME_2);
    exported = waitingSpanExporter.waitForExport();
    assertThat(exported).containsExactly(span2.toSpanData());
  }

  @Test(timeout = 5000)
  public void exporterTimesOut() throws Exception {
    final CountDownLatch interruptMarker = new CountDownLatch(1);
    final WaitingSpanExporter waitingSpanExporter =
        new WaitingSpanExporter(1) {
          @Override
          public ResultCode export(final List<SpanData> spans) {
            final ResultCode result = super.export(spans);
            try {
              // sleep longer than the configured timout of 100ms
              Thread.sleep(1000);
            } catch (final InterruptedException e) {
              interruptMarker.countDown();
            }
            return result;
          }
        };

    final int exporterTimeoutMillis = 100;
    tracerSdkFactory.addSpanProcessor(
        BatchSpansProcessor.newBuilder(waitingSpanExporter)
            .setExporterTimeoutMillis(exporterTimeoutMillis)
            .setScheduleDelayMillis(1)
            .setMaxQueueSize(1)
            .build());

    final ReadableSpan span = createSampledEndedSpan(SPAN_NAME_1);
    final List<SpanData> exported = waitingSpanExporter.waitForExport();
    assertThat(exported).containsExactly(span.toSpanData());

    // since the interrupt happens outside the execution of the test method, we'll block to make
    // sure that the thread was actually interrupted due to the timeout.
    interruptMarker.await();
  }

  @Test
  public void exportNotSampledSpans() {
    final WaitingSpanExporter waitingSpanExporter = new WaitingSpanExporter(1);
    tracerSdkFactory.addSpanProcessor(
        BatchSpansProcessor.newBuilder(waitingSpanExporter)
            .setScheduleDelayMillis(MAX_SCHEDULE_DELAY_MILLIS)
            .build());

    createNotSampledEndedSpan(SPAN_NAME_1);
    createNotSampledEndedSpan(SPAN_NAME_2);
    final ReadableSpan span2 = createSampledEndedSpan(SPAN_NAME_2);
    // Spans are recorded and exported in the same order as they are ended, we test that a non
    // sampled span is not exported by creating and ending a sampled span after a non sampled span
    // and checking that the first exported span is the sampled span (the non sampled did not get
    // exported).
    final List<SpanData> exported = waitingSpanExporter.waitForExport();
    // Need to check this because otherwise the variable span1 is unused, other option is to not
    // have a span1 variable.
    assertThat(exported).containsExactly(span2.toSpanData());
  }

  @Test
  public void exportNotSampledSpans_recordingEvents() {
    // TODO(bdrutu): Fix this when Sampler return RECORD option.
    /*
    tracerSdkFactory.addSpanProcessor(
        BatchSpansProcessor.newBuilder(waitingSpanExporter)
            .setScheduleDelayMillis(MAX_SCHEDULE_DELAY_MILLIS)
            .reportOnlySampled(false)
            .build());

    ReadableSpan span = createNotSampledRecordingEventsEndedSpan(SPAN_NAME_1);
    List<SpanData> exported = waitingSpanExporter.waitForExport(1);
    assertThat(exported).containsExactly(span.toSpanData());
    */
  }

  @Test
  public void exportNotSampledSpans_reportOnlySampled() {
    // TODO(bdrutu): Fix this when Sampler return RECORD option.
    /*
    tracerSdkFactory.addSpanProcessor(
        BatchSpansProcessor.newBuilder(waitingSpanExporter)
            .reportOnlySampled(true)
            .setScheduleDelayMillis(MAX_SCHEDULE_DELAY_MILLIS)
            .build());

    createNotSampledRecordingEventsEndedSpan(SPAN_NAME_1);
    ReadableSpan sampledSpan = createSampledEndedSpan(SPAN_NAME_2);
    List<SpanData> exported = waitingSpanExporter.waitForExport(1);
    assertThat(exported).containsExactly(sampledSpan.toSpanData());
    */
  }

  @Test(timeout = 10000L)
  public void shutdownFlushes() {
    final WaitingSpanExporter waitingSpanExporter = new WaitingSpanExporter(1);
    // Set the config delay to zero, for no timeout, in order to confirm the #flush() below works
    tracerSdkFactory.addSpanProcessor(
        BatchSpansProcessor.newBuilder(waitingSpanExporter).setScheduleDelayMillis(0).build());

    final ReadableSpan span2 = createSampledEndedSpan(SPAN_NAME_2);

    // Force a shutdown, without this, the #waitForExport() call below would block indefinitely.
    tracerSdkFactory.shutdown();

    final List<SpanData> exported = waitingSpanExporter.waitForExport();
    assertThat(exported).containsExactly(span2.toSpanData());
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
    public ResultCode export(final List<SpanData> spanDataList) {
      synchronized (monitor) {
        while (state != State.UNBLOCKED) {
          try {
            state = State.BLOCKED;
            // Some threads may wait for Blocked State.
            monitor.notifyAll();
            monitor.wait();
          } catch (final InterruptedException e) {
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
          } catch (final InterruptedException e) {
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

  static class WaitingSpanExporter implements SpanExporter {

    private final List<SpanData> spanDataList = new ArrayList<>();
    private final int numberToWaitFor;
    private CountDownLatch countDownLatch;
    private int timeout = 10;

    WaitingSpanExporter(final int numberToWaitFor) {
      countDownLatch = new CountDownLatch(numberToWaitFor);
      this.numberToWaitFor = numberToWaitFor;
    }

    WaitingSpanExporter(final int numberToWaitFor, final int timeout) {
      this(numberToWaitFor);
      this.timeout = timeout;
    }

    /**
     * Waits until we received numberOfSpans spans to config. Returns the list of exported {@link
     * SpanData} objects, otherwise {@code null} if the current thread is interrupted.
     *
     * @return the list of exported {@link SpanData} objects, otherwise {@code null} if the current
     *     thread is interrupted.
     */
    @Nullable
    List<SpanData> waitForExport() {
      try {
        countDownLatch.await(timeout, TimeUnit.SECONDS);
      } catch (final InterruptedException e) {
        // Preserve the interruption status as per guidance.
        Thread.currentThread().interrupt();
        return null;
      }
      final List<SpanData> result = new ArrayList<>(spanDataList);
      spanDataList.clear();
      return result;
    }

    @Override
    public ResultCode export(final List<SpanData> spans) {
      spanDataList.addAll(spans);
      for (int i = 0; i < spans.size(); i++) {
        countDownLatch.countDown();
      }
      return ResultCode.SUCCESS;
    }

    @Override
    public void shutdown() {
      // Do nothing;
    }

    public void reset() {
      countDownLatch = new CountDownLatch(numberToWaitFor);
    }
  }
}
