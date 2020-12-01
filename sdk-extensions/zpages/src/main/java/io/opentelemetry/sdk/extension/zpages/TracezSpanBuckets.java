/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.zpages;

import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class TracezSpanBuckets {
  private final Map<LatencyBoundary, SpanBucket> latencyBuckets = new HashMap<>();
  private final Map<StatusCode, SpanBucket> errorBuckets = new HashMap<>();

  TracezSpanBuckets() {
    for (LatencyBoundary bucket : LatencyBoundary.values()) {
      latencyBuckets.put(bucket, new SpanBucket(/* isLatencyBucket= */ true));
    }
    for (StatusCode code : StatusCode.values()) {
      if (code == StatusCode.ERROR) {
        errorBuckets.put(code, new SpanBucket(/* isLatencyBucket= */ false));
      }
    }
  }

  void addToBucket(ReadableSpan span) {
    SpanData.Status status = span.toSpanData().getStatus();
    if (status.isOk()) {
      latencyBuckets.get(LatencyBoundary.getBoundary(span.getLatencyNanos())).add(span);
      return;
    }
    errorBuckets.get(status.getStatusCode()).add(span);
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
