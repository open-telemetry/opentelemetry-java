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
import com.google.common.collect.ImmutableMap;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.Status.CanonicalCode;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

final class TracezSpanBuckets {
  private static final int NUM_SAMPLES_PER_LATENCY_BUCKET = 10;
  private static final int NUM_SAMPLES_PER_ERROR_BUCKET = 5;

  private final ImmutableMap<LatencyBoundary, EvictingQueue<ReadableSpan>> latencyBuckets;
  private final ImmutableMap<CanonicalCode, EvictingQueue<ReadableSpan>> errorBuckets;

  TracezSpanBuckets() {
    ImmutableMap.Builder<LatencyBoundary, EvictingQueue<ReadableSpan>> latencyBucketsBuilder =
        ImmutableMap.builder();
    for (LatencyBoundary bucket : LatencyBoundary.values()) {
      latencyBucketsBuilder.put(
          bucket, EvictingQueue.<ReadableSpan>create(NUM_SAMPLES_PER_LATENCY_BUCKET));
    }
    latencyBuckets = latencyBucketsBuilder.build();
    ImmutableMap.Builder<CanonicalCode, EvictingQueue<ReadableSpan>> errorBucketsBuilder =
        ImmutableMap.builder();
    for (CanonicalCode code : CanonicalCode.values()) {
      if (!code.toStatus().isOk()) {
        errorBucketsBuilder.put(
            code, EvictingQueue.<ReadableSpan>create(NUM_SAMPLES_PER_ERROR_BUCKET));
      }
    }
    errorBuckets = errorBucketsBuilder.build();
  }

  public void addToBucket(ReadableSpan span) {
    Status status = span.toSpanData().getStatus();
    if (status.isOk()) {
      synchronized (latencyBuckets) {
        latencyBuckets.get(LatencyBoundary.getBoundary(span.getLatencyNanos())).add(span);
      }
      return;
    }
    synchronized (errorBuckets) {
      errorBuckets.get(status.getCanonicalCode()).add(span);
    }
  }

  public Map<LatencyBoundary, Integer> getLatencyBoundaryToCountMap() {
    Map<LatencyBoundary, Integer> latencyCounts = new EnumMap<>(LatencyBoundary.class);
    for (LatencyBoundary bucket : LatencyBoundary.values()) {
      latencyCounts.put(bucket, latencyBuckets.get(bucket).size());
    }
    return latencyCounts;
  }

  public List<ReadableSpan> getOkSpans() {
    List<ReadableSpan> okSpans = new ArrayList<>();
    for (EvictingQueue<ReadableSpan> latencyBucket : latencyBuckets.values()) {
      synchronized (latencyBucket) {
        okSpans.addAll(new ArrayList<>(latencyBucket));
      }
    }
    return okSpans;
  }

  public List<ReadableSpan> getErrorSpans() {
    List<ReadableSpan> errorSpans = new ArrayList<>();
    for (EvictingQueue<ReadableSpan> errorBucket : errorBuckets.values()) {
      errorSpans.addAll(new ArrayList<>(errorBucket));
    }
    return errorSpans;
  }

  public List<ReadableSpan> getSpans() {
    List<ReadableSpan> spans = new ArrayList<>();
    spans.addAll(getOkSpans());
    spans.addAll(getErrorSpans());
    return spans;
  }
}
