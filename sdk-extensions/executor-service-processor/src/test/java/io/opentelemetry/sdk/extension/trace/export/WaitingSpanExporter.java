/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.export;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;

public class WaitingSpanExporter implements SpanExporter {

  private final ConcurrentLinkedQueue<SpanData> spanDataList = new ConcurrentLinkedQueue<>();
  private final int numberToWaitFor;
  private final CompletableResultCode exportResultCode;
  private CountDownLatch countDownLatch;
  private int timeout = 10;
  public final AtomicBoolean shutDownCalled = new AtomicBoolean(false);

  WaitingSpanExporter(int numberToWaitFor, CompletableResultCode exportResultCode) {
    countDownLatch = new CountDownLatch(numberToWaitFor);
    this.numberToWaitFor = numberToWaitFor;
    this.exportResultCode = exportResultCode;
  }

  WaitingSpanExporter(int numberToWaitFor, CompletableResultCode exportResultCode, int timeout) {
    this(numberToWaitFor, exportResultCode);
    this.timeout = timeout;
  }

  List<SpanData> getExported() {
    List<SpanData> result = new ArrayList<>(spanDataList);
    spanDataList.clear();
    return result;
  }

  /**
   * Waits until we received numberOfSpans spans to export. Returns the list of exported {@link
   * SpanData} objects, otherwise {@code null} if the current thread is interrupted.
   *
   * @return the list of exported {@link SpanData} objects, otherwise {@code null} if the current
   *     thread is interrupted.
   */
  @Nullable
  List<SpanData> waitForExport() {
    try {
      countDownLatch.await(timeout, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      // Preserve the interruption status as per guidance.
      Thread.currentThread().interrupt();
      return null;
    }
    return getExported();
  }

  @Override
  public CompletableResultCode export(Collection<SpanData> spans) {
    this.spanDataList.addAll(spans);
    for (int i = 0; i < spans.size(); i++) {
      countDownLatch.countDown();
    }
    return exportResultCode;
  }

  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode shutdown() {
    shutDownCalled.set(true);
    return CompletableResultCode.ofSuccess();
  }

  public void reset() {
    this.countDownLatch = new CountDownLatch(numberToWaitFor);
  }
}
