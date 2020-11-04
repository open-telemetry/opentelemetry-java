/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.zpages;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.TracerSdkProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class SpanBucketTest {
  private static final String SPAN_NAME = "span";
  private static final int LATENCY_BUCKET_SIZE = 16;
  private static final int ERROR_BUCKET_SIZE = 8;
  private final TracerSdkProvider tracerSdkProvider = TracerSdkProvider.builder().build();
  private final Tracer tracer = tracerSdkProvider.get("SpanBucketTest");

  @Test
  void verifyLatencyBucketSizeLimit() {
    SpanBucket latencyBucket = new SpanBucket(/* isLatencyBucket= */ true);
    Span[] spans = new Span[LATENCY_BUCKET_SIZE + 1];
    for (int i = 0; i < LATENCY_BUCKET_SIZE + 1; i++) {
      spans[i] = tracer.spanBuilder(SPAN_NAME).startSpan();
      latencyBucket.add((ReadableSpan) spans[i]);
      spans[i].end();
    }
    List<ReadableSpan> bucketSpans = new ArrayList<>();
    latencyBucket.addTo(bucketSpans);
    /* The latency SpanBucket should have the most recent LATENCY_BUCKET_SIZE spans */
    assertThat(latencyBucket.size()).isEqualTo(LATENCY_BUCKET_SIZE);
    assertThat(bucketSpans.size()).isEqualTo(LATENCY_BUCKET_SIZE);
    assertThat(bucketSpans).doesNotContain((ReadableSpan) spans[0]);
    for (int i = 1; i < LATENCY_BUCKET_SIZE + 1; i++) {
      assertThat(bucketSpans).contains((ReadableSpan) spans[i]);
    }
  }

  @Test
  void verifyErrorBucketSizeLimit() {
    SpanBucket errorBucket = new SpanBucket(/* isLatencyBucket= */ false);
    Span[] spans = new Span[ERROR_BUCKET_SIZE + 1];
    for (int i = 0; i < ERROR_BUCKET_SIZE + 1; i++) {
      spans[i] = tracer.spanBuilder(SPAN_NAME).startSpan();
      errorBucket.add((ReadableSpan) spans[i]);
      spans[i].end();
    }
    List<ReadableSpan> bucketSpans = new ArrayList<>();
    errorBucket.addTo(bucketSpans);
    /* The error SpanBucket should have the most recent ERROR_BUCKET_SIZE spans */
    assertThat(errorBucket.size()).isEqualTo(ERROR_BUCKET_SIZE);
    assertThat(bucketSpans.size()).isEqualTo(ERROR_BUCKET_SIZE);
    assertThat(bucketSpans).doesNotContain((ReadableSpan) spans[0]);
    for (int i = 1; i < ERROR_BUCKET_SIZE + 1; i++) {
      assertThat(bucketSpans).contains((ReadableSpan) spans[i]);
    }
  }

  @Timeout(value = 1)
  public void verifyThreadSafety() throws InterruptedException {
    int numberOfThreads = 4;
    int numberOfSpans = 4;
    SpanBucket spanBucket = new SpanBucket(/* isLatencyBucket= */ true);
    final CountDownLatch startSignal = new CountDownLatch(1);
    final CountDownLatch endSignal = new CountDownLatch(numberOfThreads);
    for (int i = 0; i < numberOfThreads; i++) {
      new Thread(
              () -> {
                try {
                  startSignal.await();
                  for (int j = 0; j < numberOfSpans; j++) {
                    Span span = tracer.spanBuilder(SPAN_NAME).startSpan();
                    spanBucket.add((ReadableSpan) span);
                    span.end();
                  }
                  endSignal.countDown();
                } catch (InterruptedException e) {
                  return;
                }
              })
          .start();
    }
    startSignal.countDown();
    endSignal.await();
    /* The SpanBucket should have exactly 16 spans */
    assertThat(spanBucket.size()).isEqualTo(numberOfThreads * numberOfSpans);
  }
}
