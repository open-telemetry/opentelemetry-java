/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.sdk.extensions.zpages;

import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.trace.Status.CanonicalCode;
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
   * Constructor for {@link io.opentelemetry.sdk.extensions.zpages.TracezDataAggregator}.
   *
   * @param spanProcessor collects span data.
   */
  public TracezDataAggregator(TracezSpanProcessor spanProcessor) {
    this.spanProcessor = spanProcessor;
  }

  /**
   * Returns a Set of running and completed span names for {@link
   * io.opentelemetry.sdk.extensions.zpages.TracezDataAggregator}.
   *
   * @return a Set of {@link String}.
   */
  public Set<String> getSpanNames() {
    Set<String> spanNames = new TreeSet<>();
    Collection<ReadableSpan> allRunningSpans = spanProcessor.getRunningSpans();
    for (ReadableSpan span : allRunningSpans) {
      spanNames.add(span.getName());
    }
    spanNames.addAll(spanProcessor.getCompletedSpanCache().keySet());
    return spanNames;
  }

  /**
   * Returns a Map of the running span counts for {@link
   * io.opentelemetry.sdk.extensions.zpages.TracezDataAggregator}.
   *
   * @return a Map of span counts for each span name.
   */
  public Map<String, Integer> getRunningSpanCounts() {
    Collection<ReadableSpan> allRunningSpans = spanProcessor.getRunningSpans();
    Map<String, Integer> numSpansPerName = new HashMap<>();
    for (ReadableSpan span : allRunningSpans) {
      Integer prevValue = numSpansPerName.get(span.getName());
      numSpansPerName.put(span.getName(), prevValue != null ? prevValue + 1 : 1);
    }
    return numSpansPerName;
  }

  /**
   * Returns a List of all running spans with a given span name for {@link
   * io.opentelemetry.sdk.extensions.zpages.TracezDataAggregator}.
   *
   * @param spanName name to filter returned spans.
   * @return a List of {@link io.opentelemetry.sdk.trace.data.SpanData}.
   */
  public List<SpanData> getRunningSpans(String spanName) {
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
   * Returns a Map of counts for the {@link io.opentelemetry.trace.Status#OK} spans within
   * [lowerBound, upperBound) {@link io.opentelemetry.sdk.extensions.zpages.TracezDataAggregator}.
   *
   * @param lowerBound latency lower bound (inclusive)
   * @param upperBound latency upper bound (exclusive)
   * @return a Map of span counts for each span name within the bounds.
   */
  public Map<String, Integer> getSpanLatencyCounts(long lowerBound, long upperBound) {
    Collection<ReadableSpan> allCompletedSpans = spanProcessor.getCompletedSpans();
    Map<String, Integer> numSpansPerName = new HashMap<>();
    for (ReadableSpan span : allCompletedSpans) {
      if (span.toSpanData().getStatus().isOk()
          && span.getLatencyNanos() >= lowerBound
          && span.getLatencyNanos() < upperBound) {
        Integer prevValue = numSpansPerName.get(span.getName());
        numSpansPerName.put(span.getName(), prevValue != null ? prevValue + 1 : 1);
      }
    }
    return numSpansPerName;
  }

  /**
   * Returns a Map of span names to counts for all {@link io.opentelemetry.trace.Status#OK} spans in
   * {@link io.opentelemetry.sdk.extensions.zpages.TracezDataAggregator}.
   *
   * @return a Map of span names to counts, where the counts are further indexed by the latency
   *     boundaries.
   */
  public Map<String, Map<LatencyBoundaries, Integer>> getSpanLatencyCounts() {
    Map<String, TracezSpanBuckets> completedSpanCache = spanProcessor.getCompletedSpanCache();
    Map<String, Map<LatencyBoundaries, Integer>> numSpansPerName = new HashMap<>();
    for (String name : completedSpanCache.keySet()) {
      numSpansPerName.put(name, completedSpanCache.get(name).getLatencyBoundariesToCountMap());
    }
    return numSpansPerName;
  }

  /**
   * Returns a List of all {@link io.opentelemetry.trace.Status#OK} spans with a given span name
   * between [lowerBound, upperBound) for {@link
   * io.opentelemetry.sdk.extensions.zpages.TracezDataAggregator}.
   *
   * @param spanName name to filter returned spans.
   * @param lowerBound latency lower bound (inclusive)
   * @param upperBound latency upper bound (exclusive)
   * @return a List of {@link io.opentelemetry.sdk.trace.data.SpanData}.
   */
  public List<SpanData> getOkSpans(String spanName, long lowerBound, long upperBound) {
    Map<String, TracezSpanBuckets> completedSpanCache = spanProcessor.getCompletedSpanCache();
    Collection<ReadableSpan> allCompletedSpans =
        completedSpanCache.containsKey(spanName)
            ? completedSpanCache.get(spanName).getOkSpans()
            : Collections.<ReadableSpan>emptyList();
    List<SpanData> filteredSpans = new ArrayList<>();
    for (ReadableSpan span : allCompletedSpans) {
      if (span.getLatencyNanos() >= lowerBound && span.getLatencyNanos() < upperBound) {
        filteredSpans.add(span.toSpanData());
      }
    }
    return filteredSpans;
  }

  /**
   * Returns a Map of error span counts for {@link
   * io.opentelemetry.sdk.extensions.zpages.TracezDataAggregator}.
   *
   * @return a Map of error span counts for each span name.
   */
  public Map<String, Integer> getErrorSpanCounts() {
    Map<String, TracezSpanBuckets> completedSpanCache = spanProcessor.getCompletedSpanCache();
    Map<String, Integer> numErrorsPerName = new HashMap<>();
    for (String name : completedSpanCache.keySet()) {
      numErrorsPerName.put(name, completedSpanCache.get(name).getErrorSpans().size());
    }
    return numErrorsPerName;
  }

  /**
   * Returns a List of error spans with a given span name for {@link
   * io.opentelemetry.sdk.extensions.zpages.TracezDataAggregator}.
   *
   * @param spanName name to filter returned spans.
   * @return a List of {@link io.opentelemetry.sdk.trace.data.SpanData}.
   */
  public List<SpanData> getErrorSpans(String spanName) {
    Map<String, TracezSpanBuckets> completedSpanCache = spanProcessor.getCompletedSpanCache();
    Collection<ReadableSpan> allCompletedSpans =
        completedSpanCache.containsKey(spanName)
            ? completedSpanCache.get(spanName).getErrorSpans()
            : Collections.<ReadableSpan>emptyList();
    List<SpanData> errorSpans = new ArrayList<>();
    for (ReadableSpan span : allCompletedSpans) {
      errorSpans.add(span.toSpanData());
    }
    return errorSpans;
  }

  /**
   * Returns a List of error spans with a given span name and canonical code for {@link
   * io.opentelemetry.sdk.extensions.zpages.TracezDataAggregator}.
   *
   * @param spanName name to filter returned spans.
   * @param errorCode canonical error code to filter returned spans.
   * @return a List of {@link io.opentelemetry.sdk.trace.data.SpanData}.
   */
  public List<SpanData> getErrorSpans(String spanName, CanonicalCode errorCode) {
    List<SpanData> allErrorSpans = getErrorSpans(spanName);
    List<SpanData> filteredSpans = new ArrayList<>();
    for (SpanData span : allErrorSpans) {
      if (span.getStatus().getCanonicalCode().equals(errorCode)) {
        filteredSpans.add(span);
      }
    }
    return filteredSpans;
  }
}
