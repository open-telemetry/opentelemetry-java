/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extensions.zpages;

import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A data aggregator for the traceZ zPage.
 *
 * <p>The traceZ data aggregator compiles information about the running spans, span latencies, and
 * error spans for the frontend of the zPage.
 */
@ThreadSafe
final class TracezDataAggregator {
  private final TracezSpanProcessor spanProcessor;

  /**
   * Constructor for {@link TracezDataAggregator}.
   *
   * @param spanProcessor collects span data.
   */
  TracezDataAggregator(TracezSpanProcessor spanProcessor) {
    this.spanProcessor = spanProcessor;
  }

  /**
   * Returns a Set of running and completed span names for {@link TracezDataAggregator}.
   *
   * @return a Set of {@link String}.
   */
  Set<String> getSpanNames() {
    Set<String> spanNames = new TreeSet<>();
    Collection<ReadableSpan> allRunningSpans = spanProcessor.getRunningSpans();
    for (ReadableSpan span : allRunningSpans) {
      spanNames.add(span.getName());
    }
    spanNames.addAll(spanProcessor.getCompletedSpanCache().keySet());
    return spanNames;
  }

  /**
   * Returns a Map of the running span counts for {@link TracezDataAggregator}.
   *
   * @return a Map of span counts for each span name.
   */
  Map<String, Integer> getRunningSpanCounts() {
    Collection<ReadableSpan> allRunningSpans = spanProcessor.getRunningSpans();
    Map<String, Integer> numSpansPerName = new HashMap<>();
    for (ReadableSpan span : allRunningSpans) {
      Integer prevValue = numSpansPerName.get(span.getName());
      numSpansPerName.put(span.getName(), prevValue != null ? prevValue + 1 : 1);
    }
    return numSpansPerName;
  }

  /**
   * Returns a List of all running spans with a given span name for {@link TracezDataAggregator}.
   *
   * @param spanName name to filter returned spans.
   * @return a List of {@link SpanData}.
   */
  List<SpanData> getRunningSpans(String spanName) {
    Collection<ReadableSpan> allRunningSpans = spanProcessor.getRunningSpans();
    List<SpanData> filteredSpans = new ArrayList<>();
    for (ReadableSpan span : allRunningSpans) {
      if (span.getName().equals(spanName)) {
        filteredSpans.add(span.toSpanData());
      }
    }
    return filteredSpans;
  }

  /**
   * Returns a Map of span names to counts for all {@link StatusCode#OK} spans in {@link
   * TracezDataAggregator}.
   *
   * @return a Map of span names to counts, where the counts are further indexed by the latency
   *     boundaries.
   */
  Map<String, Map<LatencyBoundary, Integer>> getSpanLatencyCounts() {
    Map<String, TracezSpanBuckets> completedSpanCache = spanProcessor.getCompletedSpanCache();
    Map<String, Map<LatencyBoundary, Integer>> numSpansPerName = new HashMap<>();
    for (Map.Entry<String, TracezSpanBuckets> cacheEntry : completedSpanCache.entrySet()) {
      numSpansPerName.put(
          cacheEntry.getKey(), cacheEntry.getValue().getLatencyBoundaryToCountMap());
    }
    return numSpansPerName;
  }

  /**
   * Returns a List of all {@link StatusCode#OK} spans with a given span name between [lowerBound,
   * upperBound) for {@link TracezDataAggregator}.
   *
   * @param spanName name to filter returned spans.
   * @param lowerBound latency lower bound (inclusive)
   * @param upperBound latency upper bound (exclusive)
   * @return a List of {@link SpanData}.
   */
  List<SpanData> getOkSpans(String spanName, long lowerBound, long upperBound) {
    Map<String, TracezSpanBuckets> completedSpanCache = spanProcessor.getCompletedSpanCache();
    TracezSpanBuckets buckets = completedSpanCache.get(spanName);
    if (buckets == null) {
      return Collections.emptyList();
    }
    Collection<ReadableSpan> allOkSpans = buckets.getOkSpans();
    List<SpanData> filteredSpans = new ArrayList<>();
    for (ReadableSpan span : allOkSpans) {
      if (span.getLatencyNanos() >= lowerBound && span.getLatencyNanos() < upperBound) {
        filteredSpans.add(span.toSpanData());
      }
    }
    return Collections.unmodifiableList(filteredSpans);
  }

  /**
   * Returns a Map of error span counts for {@link TracezDataAggregator}.
   *
   * @return a Map of error span counts for each span name.
   */
  Map<String, Integer> getErrorSpanCounts() {
    Map<String, TracezSpanBuckets> completedSpanCache = spanProcessor.getCompletedSpanCache();
    Map<String, Integer> numErrorsPerName = new HashMap<>();
    for (Map.Entry<String, TracezSpanBuckets> cacheEntry : completedSpanCache.entrySet()) {
      numErrorsPerName.put(cacheEntry.getKey(), cacheEntry.getValue().getErrorSpans().size());
    }
    return numErrorsPerName;
  }

  /**
   * Returns a List of error spans with a given span name for {@link TracezDataAggregator}.
   *
   * @param spanName name to filter returned spans.
   * @return a List of {@link SpanData}.
   */
  List<SpanData> getErrorSpans(String spanName) {
    Map<String, TracezSpanBuckets> completedSpanCache = spanProcessor.getCompletedSpanCache();
    TracezSpanBuckets buckets = completedSpanCache.get(spanName);
    if (buckets == null) {
      return Collections.emptyList();
    }
    Collection<ReadableSpan> allErrorSpans = buckets.getErrorSpans();
    List<SpanData> errorSpans = new ArrayList<>();
    for (ReadableSpan span : allErrorSpans) {
      errorSpans.add(span.toSpanData());
    }
    return Collections.unmodifiableList(errorSpans);
  }
}
