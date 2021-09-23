/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.zpages;

import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

final class TracezSpanBuckets {
  private final LatencyBuckets latencyBuckets = new LatencyBuckets();
  private final SpanBucket errors = new SpanBucket(/* isLatencyBucket= */ false);

  void addToBucket(ReadableSpan span) {
    StatusData status = span.toSpanData().getStatus();
    if (status.getStatusCode() != StatusCode.ERROR) {
      latencyBuckets.get(LatencyBoundary.getBoundary(span.getLatencyNanos())).add(span);
      return;
    }
    errors.add(span);
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
    latencyBuckets.addTo(okSpans);
    return okSpans;
  }

  List<ReadableSpan> getErrorSpans() {
    List<ReadableSpan> errorSpans = new ArrayList<>();
    errors.addTo(errorSpans);
    return errorSpans;
  }

  List<ReadableSpan> getSpans() {
    List<ReadableSpan> spans = new ArrayList<>();
    spans.addAll(getOkSpans());
    spans.addAll(getErrorSpans());
    return spans;
  }

  private static class LatencyBuckets {
    final SpanBucket zeroToTenMicros = new SpanBucket(/* isLatencyBucket= */ true);
    final SpanBucket tenMicrosToHundredMicros = new SpanBucket(/* isLatencyBucket= */ true);
    final SpanBucket hundredMicrosToOneMilli = new SpanBucket(/* isLatencyBucket= */ true);
    final SpanBucket oneMilliToTenMillis = new SpanBucket(/* isLatencyBucket= */ true);
    final SpanBucket tenMillisToHundredMillis = new SpanBucket(/* isLatencyBucket= */ true);
    final SpanBucket hundredMillisToOneSecond = new SpanBucket(/* isLatencyBucket= */ true);
    final SpanBucket oneSecondToTenSeconds = new SpanBucket(/* isLatencyBucket= */ true);
    final SpanBucket tenSecondsToHundredSeconds = new SpanBucket(/* isLatencyBucket= */ true);
    final SpanBucket hundredSecondsToMax = new SpanBucket(/* isLatencyBucket= */ true);

    void addTo(List<ReadableSpan> spans) {
      zeroToTenMicros.addTo(spans);
      tenMicrosToHundredMicros.addTo(spans);
      hundredMicrosToOneMilli.addTo(spans);
      oneMilliToTenMillis.addTo(spans);
      tenMillisToHundredMillis.addTo(spans);
      hundredMillisToOneSecond.addTo(spans);
      oneSecondToTenSeconds.addTo(spans);
      tenSecondsToHundredSeconds.addTo(spans);
      hundredSecondsToMax.addTo(spans);
    }

    SpanBucket get(LatencyBoundary boundary) {
      switch (boundary) {
        case ZERO_MICROSx10:
          return zeroToTenMicros;
        case MICROSx10_MICROSx100:
          return tenMicrosToHundredMicros;
        case MICROSx100_MILLIx1:
          return hundredMicrosToOneMilli;
        case MILLIx1_MILLIx10:
          return oneMilliToTenMillis;
        case MILLIx10_MILLIx100:
          return tenMillisToHundredMillis;
        case MILLIx100_SECONDx1:
          return hundredMillisToOneSecond;
        case SECONDx1_SECONDx10:
          return oneSecondToTenSeconds;
        case SECONDx10_SECONDx100:
          return tenSecondsToHundredSeconds;
        case SECONDx100_MAX:
          return hundredSecondsToMax;
      }
      // Can't happen with aligned versions, just pick an arbitrary one to compile.
      return hundredSecondsToMax;
    }
  }
}
