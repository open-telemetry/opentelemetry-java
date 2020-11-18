/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation of the {@code SpanProcessor} that simply forwards all received events to a list of
 * {@code SpanProcessor}s.
 *
 * @deprecated Use {@link SpanProcessor#delegating(SpanProcessor...)}
 */
@Deprecated
public final class MultiSpanProcessor implements SpanProcessor {
  private final List<SpanProcessor> spanProcessorsStart;
  private final List<SpanProcessor> spanProcessorsEnd;
  private final List<SpanProcessor> spanProcessorsAll;
  private final AtomicBoolean isShutdown = new AtomicBoolean(false);

  /**
   * Creates a new {@code MultiSpanProcessor}.
   *
   * @param spanProcessorList the {@code List} of {@code SpanProcessor}s.
   * @return a new {@code MultiSpanProcessor}.
   * @throws NullPointerException if the {@code spanProcessorList} is {@code null}.
   * @deprecated Use {@link SpanProcessor#delegating(SpanProcessor...)}
   */
  @Deprecated
  public static SpanProcessor create(List<SpanProcessor> spanProcessorList) {
    return new MultiSpanProcessor(
        new ArrayList<>(Objects.requireNonNull(spanProcessorList, "spanProcessorList")));
  }

  @Override
  public void onStart(Context parentContext, ReadWriteSpan readableSpan) {
    for (SpanProcessor spanProcessor : spanProcessorsStart) {
      spanProcessor.onStart(parentContext, readableSpan);
    }
  }

  @Override
  public boolean isStartRequired() {
    return !spanProcessorsStart.isEmpty();
  }

  @Override
  public void onEnd(ReadableSpan readableSpan) {
    for (SpanProcessor spanProcessor : spanProcessorsEnd) {
      spanProcessor.onEnd(readableSpan);
    }
  }

  @Override
  public boolean isEndRequired() {
    return !spanProcessorsEnd.isEmpty();
  }

  @Override
  public CompletableResultCode shutdown() {
    if (isShutdown.getAndSet(true)) {
      return CompletableResultCode.ofSuccess();
    }
    List<CompletableResultCode> results = new ArrayList<>(spanProcessorsAll.size());
    for (SpanProcessor spanProcessor : spanProcessorsAll) {
      results.add(spanProcessor.shutdown());
    }
    return CompletableResultCode.ofAll(results);
  }

  @Override
  public CompletableResultCode forceFlush() {
    List<CompletableResultCode> results = new ArrayList<>(spanProcessorsAll.size());
    for (SpanProcessor spanProcessor : spanProcessorsAll) {
      results.add(spanProcessor.forceFlush());
    }
    return CompletableResultCode.ofAll(results);
  }

  private MultiSpanProcessor(List<SpanProcessor> spanProcessors) {
    this.spanProcessorsAll = spanProcessors;
    this.spanProcessorsStart = new ArrayList<>(spanProcessorsAll.size());
    this.spanProcessorsEnd = new ArrayList<>(spanProcessorsAll.size());
    for (SpanProcessor spanProcessor : spanProcessorsAll) {
      if (spanProcessor.isStartRequired()) {
        spanProcessorsStart.add(spanProcessor);
      }
      if (spanProcessor.isEndRequired()) {
        spanProcessorsEnd.add(spanProcessor);
      }
    }
  }
}
