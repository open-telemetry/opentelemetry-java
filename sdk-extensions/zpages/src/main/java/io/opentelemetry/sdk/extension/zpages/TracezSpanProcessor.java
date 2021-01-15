/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.zpages;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.concurrent.ThreadSafe;

/** A {@link SpanProcessor} implementation for the traceZ zPage. */
@ThreadSafe
final class TracezSpanProcessor implements SpanProcessor {
  private final ConcurrentMap<String, ReadableSpan> runningSpanCache;
  private final ConcurrentMap<String, TracezSpanBuckets> completedSpanCache;
  private final boolean sampled;

  /**
   * Constructor for {@link TracezSpanProcessor}.
   *
   * @param sampled report only sampled spans.
   */
  TracezSpanProcessor(boolean sampled) {
    runningSpanCache = new ConcurrentHashMap<>();
    completedSpanCache = new ConcurrentHashMap<>();
    this.sampled = sampled;
  }

  @Override
  public void onStart(Context parentContext, ReadWriteSpan span) {
    runningSpanCache.put(span.getSpanContext().getSpanIdAsHexString(), span);
  }

  @Override
  public boolean isStartRequired() {
    return true;
  }

  @Override
  public void onEnd(ReadableSpan span) {
    runningSpanCache.remove(span.getSpanContext().getSpanIdAsHexString());
    if (!sampled || span.getSpanContext().isSampled()) {
      completedSpanCache.putIfAbsent(span.getName(), new TracezSpanBuckets());
      completedSpanCache.get(span.getName()).addToBucket(span);
    }
  }

  @Override
  public boolean isEndRequired() {
    return true;
  }

  @Override
  public CompletableResultCode shutdown() {
    // Do nothing.
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode forceFlush() {
    // Do nothing.
    return CompletableResultCode.ofSuccess();
  }

  /**
   * Returns a Collection of all running spans for {@link TracezSpanProcessor}.
   *
   * @return a Collection of {@link ReadableSpan}.
   */
  Collection<ReadableSpan> getRunningSpans() {
    return runningSpanCache.values();
  }

  /**
   * Returns a Collection of all completed spans for {@link TracezSpanProcessor}.
   *
   * @return a Collection of {@link ReadableSpan}.
   */
  Collection<ReadableSpan> getCompletedSpans() {
    Collection<ReadableSpan> completedSpans = new ArrayList<>();
    for (TracezSpanBuckets buckets : completedSpanCache.values()) {
      completedSpans.addAll(buckets.getSpans());
    }
    return completedSpans;
  }

  /**
   * Returns the completed span cache for {@link TracezSpanProcessor}.
   *
   * @return a Map of String to {@link TracezSpanBuckets}.
   */
  Map<String, TracezSpanBuckets> getCompletedSpanCache() {
    return completedSpanCache;
  }

  /**
   * Returns a new Builder for {@link TracezSpanProcessor}.
   *
   * @return a new {@link TracezSpanProcessor}.
   */
  public static Builder builder() {
    return new Builder();
  }

  /** Builder class for {@link TracezSpanProcessor}. */
  public static final class Builder {

    private static final boolean DEFAULT_EXPORT_ONLY_SAMPLED = true;
    private boolean sampled = DEFAULT_EXPORT_ONLY_SAMPLED;

    private Builder() {}

    /**
     * Sets whether only sampled spans should be exported.
     *
     * <p>Default value is {@code true}.
     *
     * @see Builder#DEFAULT_EXPORT_ONLY_SAMPLED
     * @param sampled report only sampled spans.
     * @return this.
     */
    public Builder setExportOnlySampled(boolean sampled) {
      this.sampled = sampled;
      return this;
    }

    /**
     * Returns a new {@link TracezSpanProcessor}.
     *
     * @return a new {@link TracezSpanProcessor}.
     */
    public TracezSpanProcessor build() {
      return new TracezSpanProcessor(sampled);
    }
  }
}
