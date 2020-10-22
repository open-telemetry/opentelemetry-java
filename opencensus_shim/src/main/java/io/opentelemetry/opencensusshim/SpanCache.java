/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.opencensus.implcore.trace.RecordEventsSpanImpl;
import io.opencensus.trace.Span;
import java.util.concurrent.TimeUnit;

public class SpanCache {

  private static final SpanCache SPAN_CACHE = new SpanCache();

  private static final int MAXIMUM_CACHE_SIZE = 10000;
  private static final int CACHE_EXPIRE_TIME_MINUTES = 10;
  private static final TimeUnit CACHE_EXPIRE_UNIT = TimeUnit.MINUTES;

  private static final Cache<io.opentelemetry.trace.Span, Span> OT_TO_OC =
      CacheBuilder.newBuilder()
          .maximumSize(MAXIMUM_CACHE_SIZE)
          .expireAfterAccess(CACHE_EXPIRE_TIME_MINUTES, CACHE_EXPIRE_UNIT)
          .build();

  private static final Cache<Span, io.opentelemetry.trace.Span> OC_TO_OT =
      CacheBuilder.newBuilder()
          .maximumSize(MAXIMUM_CACHE_SIZE)
          .expireAfterAccess(CACHE_EXPIRE_TIME_MINUTES, CACHE_EXPIRE_UNIT)
          .build();

  public static SpanCache getInstance() {
    return SPAN_CACHE;
  }

  private SpanCache() {}

  io.opentelemetry.trace.Span toOtelSpan(Span ocSpan) {
    io.opentelemetry.trace.Span otSpan = OC_TO_OT.getIfPresent(ocSpan);
    if (otSpan == null) {
      otSpan = SpanConverter.toOtelSpan(ocSpan);
      OC_TO_OT.put(ocSpan, otSpan);
      OT_TO_OC.put(otSpan, ocSpan);
    }
    return otSpan;
  }

  Span fromOtelSpan(io.opentelemetry.trace.Span otSpan) {
    Span span = OT_TO_OC.getIfPresent(otSpan);
    if (span == null) {
      span = SpanConverter.fromOtelSpan(otSpan);
      OT_TO_OC.put(otSpan, span);
      OC_TO_OT.put(span, otSpan);
    }
    return span;
  }

  void removeFromCache(RecordEventsSpanImpl ocSpan) {
    io.opentelemetry.trace.Span otSpan = OC_TO_OT.getIfPresent(ocSpan);
    if (otSpan != null) {
      OC_TO_OT.invalidate(ocSpan);
      OT_TO_OC.invalidate(otSpan);
    }
  }
}
