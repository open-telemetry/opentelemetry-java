/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DelayingSpanExporter implements SpanExporter {

  private final ScheduledExecutorService executor;

  private final int delayMs;

  public DelayingSpanExporter(int delayMs) {
    executor = Executors.newScheduledThreadPool(5);
    this.delayMs = delayMs;
  }

  @SuppressWarnings("FutureReturnValueIgnored")
  @Override
  public CompletableResultCode export(Collection<SpanData> spans) {
    final CompletableResultCode result = new CompletableResultCode();
    executor.schedule((Runnable) result::succeed, delayMs, TimeUnit.MILLISECONDS);
    return result;
  }

  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode shutdown() {
    return CompletableResultCode.ofSuccess();
  }
}
