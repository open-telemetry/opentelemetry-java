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
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.Status.CanonicalCode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

final class TracezSpanBuckets {
  private static final int NUM_SAMPLES_PER_LATENCY_BUCKET = 10;
  private static final int NUM_SAMPLES_PER_ERROR_BUCKET = 5;

  private final Map<LatencyBoundaries, EvictingQueue<ReadableSpan>> latencyBuckets =
      new HashMap<>();
  private final Map<CanonicalCode, EvictingQueue<ReadableSpan>> errorBuckets = new HashMap<>();

  public TracezSpanBuckets() {
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

  public void addToBucket(ReadableSpan span) {
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

  public Map<LatencyBoundaries, Integer> getLatencyBoundariesToCountMap() {
    Map<LatencyBoundaries, Integer> latencyCounts = new EnumMap<>(LatencyBoundaries.class);
    for (LatencyBoundaries bucket : LatencyBoundaries.values()) {
      latencyCounts.put(bucket, latencyBuckets.get(bucket).size());
    }
    return latencyCounts;
  }

  public Collection<ReadableSpan> getOkSpans() {
    Collection<ReadableSpan> okSpans = new ArrayList<>();
    for (EvictingQueue<ReadableSpan> latencyBucket : latencyBuckets.values()) {
      okSpans.addAll(new ArrayList<>(latencyBucket));
    }
    return okSpans;
  }

  public Collection<ReadableSpan> getErrorSpans() {
    Collection<ReadableSpan> errorSpans = new ArrayList<>();
    for (EvictingQueue<ReadableSpan> errorBucket : errorBuckets.values()) {
      errorSpans.addAll(new ArrayList<>(errorBucket));
    }
    return errorSpans;
  }

  public Collection<ReadableSpan> getSpans() {
    Collection<ReadableSpan> spans = new ArrayList<>();
    spans.addAll(getOkSpans());
    spans.addAll(getErrorSpans());
    return spans;
  }
}
