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

import com.google.common.collect.EvictingQueue;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.Status.CanonicalCode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
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
   * A class of boundaries for the latency buckets. The completed spans with a status of {@link
   * io.opentelemetry.trace.Status#OK} are categorized into one of these buckets om the traceZ
   * zPage.
   */
  enum LatencyBoundaries {
    /** Stores finished successful requests of duration within the interval [0, 10us). */
    ZERO_MICROSx10(0, TimeUnit.MICROSECONDS.toNanos(10)),

    /** Stores finished successful requests of duration within the interval [10us, 100us). */
    MICROSx10_MICROSx100(TimeUnit.MICROSECONDS.toNanos(10), TimeUnit.MICROSECONDS.toNanos(100)),

    /** Stores finished successful requests of duration within the interval [100us, 1ms). */
    MICROSx100_MILLIx1(TimeUnit.MICROSECONDS.toNanos(100), TimeUnit.MILLISECONDS.toNanos(1)),

    /** Stores finished successful requests of duration within the interval [1ms, 10ms). */
    MILLIx1_MILLIx10(TimeUnit.MILLISECONDS.toNanos(1), TimeUnit.MILLISECONDS.toNanos(10)),

    /** Stores finished successful requests of duration within the interval [10ms, 100ms). */
    MILLIx10_MILLIx100(TimeUnit.MILLISECONDS.toNanos(10), TimeUnit.MILLISECONDS.toNanos(100)),

    /** Stores finished successful requests of duration within the interval [100ms, 1sec). */
    MILLIx100_SECONDx1(TimeUnit.MILLISECONDS.toNanos(100), TimeUnit.SECONDS.toNanos(1)),

    /** Stores finished successful requests of duration within the interval [1sec, 10sec). */
    SECONDx1_SECONDx10(TimeUnit.SECONDS.toNanos(1), TimeUnit.SECONDS.toNanos(10)),

    /** Stores finished successful requests of duration within the interval [10sec, 100sec). */
    SECONDx10_SECONDx100(TimeUnit.SECONDS.toNanos(10), TimeUnit.SECONDS.toNanos(100)),

    /** Stores finished successful requests of duration greater than or equal to 100sec. */
    SECONDx100_MAX(TimeUnit.SECONDS.toNanos(100), Long.MAX_VALUE);

    private final long latencyLowerBound;
    private final long latencyUpperBound;

    /**
     * Constructs a {@code LatencyBoundaries} with the given boundaries and label.
     *
     * @param latencyLowerBound the latency lower bound of the bucket.
     * @param latencyUpperBound the latency upper bound of the bucket.
     */
    LatencyBoundaries(long latencyLowerBound, long latencyUpperBound) {
      this.latencyLowerBound = latencyLowerBound;
      this.latencyUpperBound = latencyUpperBound;
    }

    /**
     * Returns the latency lower bound of the bucket.
     *
     * @return the latency lower bound of the bucket.
     */
    long getLatencyLowerBound() {
      return latencyLowerBound;
    }

    /**
     * Returns the latency upper bound of the bucket.
     *
     * @return the latency upper bound of the bucket.
     */
    long getLatencyUpperBound() {
      return latencyUpperBound;
    }
  }

  static final class SpanBuckets {
    private static final int NUM_SAMPLES_PER_LATENCY_BUCKET = 10;
    private static final int NUM_SAMPLES_PER_ERROR_BUCKET = 5;

    private final Map<LatencyBoundaries, EvictingQueue<ReadableSpan>> latencyBuckets =
        new HashMap<>();
    private final Map<CanonicalCode, EvictingQueue<ReadableSpan>> errorBuckets = new HashMap<>();

    SpanBuckets() {
      for (LatencyBoundaries bucket : LatencyBoundaries.values()) {
        latencyBuckets.put(
            bucket, EvictingQueue.<ReadableSpan>create(NUM_SAMPLES_PER_LATENCY_BUCKET));
      }
      for (CanonicalCode code : CanonicalCode.values()) {
        if (!code.toStatus().isOk()) {
          errorBuckets.put(code, EvictingQueue.<ReadableSpan>create(NUM_SAMPLES_PER_ERROR_BUCKET));
        }
      }
    }

    void addToBucket(ReadableSpan span) {
      Status status = span.toSpanData().getStatus();
      if (status.isOk()) {
        long latency = span.getLatencyNanos();
        for (LatencyBoundaries bucket : LatencyBoundaries.values()) {
          if (latency >= bucket.getLatencyLowerBound() && latency < bucket.getLatencyUpperBound()) {
            latencyBuckets.get(bucket).add(span);
            return;
          }
        }
      }
      errorBuckets.get(status.getCanonicalCode()).add(span);
    }

    Map<LatencyBoundaries, Integer> getLatencyBoundariesToCountMap() {
      Map<LatencyBoundaries, Integer> latencyCounts = new EnumMap<>(LatencyBoundaries.class);
      for (LatencyBoundaries bucket : LatencyBoundaries.values()) {
        latencyCounts.put(bucket, latencyBuckets.get(bucket).size());
      }
      return latencyCounts;
    }

    Map<CanonicalCode, Integer> getErrorCanonicalCodeToCountMap() {
      Map<CanonicalCode, Integer> errorCounts = new EnumMap<>(CanonicalCode.class);
      for (CanonicalCode code : CanonicalCode.values()) {
        if (!code.toStatus().isOk()) {
          errorCounts.put(code, errorBuckets.get(code).size());
        }
      }
      return errorCounts;
    }

    Collection<ReadableSpan> getOkSpans() {
      Collection<ReadableSpan> okSpans = new ArrayList<>();
      for (EvictingQueue<ReadableSpan> latencyBucket : latencyBuckets.values()) {
        okSpans.addAll(new ArrayList<>(latencyBucket));
      }
      return okSpans;
    }

    Collection<ReadableSpan> getErrorSpans() {
      Collection<ReadableSpan> errorSpans = new ArrayList<>();
      for (EvictingQueue<ReadableSpan> errorBucket : errorBuckets.values()) {
        errorSpans.addAll(new ArrayList<>(errorBucket));
      }
      return errorSpans;
    }

    Collection<ReadableSpan> getSpans() {
      Collection<ReadableSpan> spans = new ArrayList<>();
      spans.addAll(getOkSpans());
      spans.addAll(getErrorSpans());
      return spans;
    }
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
    Map<String, SpanBuckets> completedSpanCache = spanProcessor.getCompletedSpanCache();
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
    Map<String, SpanBuckets> completedSpanCache = spanProcessor.getCompletedSpanCache();
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
    Map<String, SpanBuckets> completedSpanCache = spanProcessor.getCompletedSpanCache();
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
    Map<String, SpanBuckets> completedSpanCache = spanProcessor.getCompletedSpanCache();
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
