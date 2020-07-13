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

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.Status.CanonicalCode;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

final class TracezSpanBuckets {
  private final ImmutableMap<LatencyBoundary, SpanBucket> latencyBuckets;
  private final ImmutableMap<CanonicalCode, SpanBucket> errorBuckets;

  TracezSpanBuckets() {
    ImmutableMap.Builder<LatencyBoundary, SpanBucket> latencyBucketsBuilder =
        ImmutableMap.builder();
    for (LatencyBoundary bucket : LatencyBoundary.values()) {
      latencyBucketsBuilder.put(bucket, new SpanBucket(/* isLatencyBucket= */ true));
    }
    latencyBuckets = latencyBucketsBuilder.build();
    ImmutableMap.Builder<CanonicalCode, SpanBucket> errorBucketsBuilder = ImmutableMap.builder();
    for (CanonicalCode code : CanonicalCode.values()) {
      if (!code.toStatus().isOk()) {
        errorBucketsBuilder.put(code, new SpanBucket(/* isLatencyBucket= */ false));
      }
    }
    errorBuckets = errorBucketsBuilder.build();
  }

  void addToBucket(ReadableSpan span) {
    Status status = span.toSpanData().getStatus();
    if (status.isOk()) {
      latencyBuckets.get(LatencyBoundary.getBoundary(span.getLatencyNanos())).add(span);
      return;
    }
    errorBuckets.get(status.getCanonicalCode()).add(span);
  }

  Map<LatencyBoundary, Integer> getLatencyBoundaryToCountMap() {
    Map<LatencyBoundary, Integer> latencyCounts = new EnumMap<>(LatencyBoundary.class);
    for (LatencyBoundary bucket : LatencyBoundary.values()) {
      latencyCounts.put(bucket, latencyBuckets.get(bucket).size());
    }
    return latencyCounts;
  }

  List<ReadableSpan> getOkSpans() {
    List<ReadableSpan> okSpans = new ArrayList<>();
    for (SpanBucket latencyBucket : latencyBuckets.values()) {
      latencyBucket.addTo(okSpans);
    }
    return okSpans;
  }

  List<ReadableSpan> getErrorSpans() {
    List<ReadableSpan> errorSpans = new ArrayList<>();
    for (SpanBucket errorBucket : errorBuckets.values()) {
      errorBucket.addTo(errorSpans);
    }
    return errorSpans;
  }

  List<ReadableSpan> getSpans() {
    List<ReadableSpan> spans = new ArrayList<>();
    spans.addAll(getOkSpans());
    spans.addAll(getErrorSpans());
    return spans;
  }
}
