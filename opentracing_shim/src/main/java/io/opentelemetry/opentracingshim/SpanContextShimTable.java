/*
 * Copyright 2019, OpenTelemetry Authors
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

package io.opentelemetry.opentracingshim;

import io.opentelemetry.correlationcontext.CorrelationContext;
import io.opentelemetry.trace.Span;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nullable;

/*
 * SpanContextShimTable stores and manages OpenTracing SpanContext instances,
 * which are expected to a unmodfiable union of SpanContext and Baggage
 * (CorrelationContext/TagMap under OpenTelemetry).
 *
 * This requires that changes on a given Span and its (new) SpanContext
 * are visible in all threads at *any* moment. The current approach uses
 * a weak map synchronized through a read-write lock to get and set both
 * SpanContext and any Baggage content related to its owner Span.
 *
 * Observe that, because of this design, a global read or write lock
 * will be taken for ALL operations involving OT SpanContext/Baggage.
 * When/if performance becomes an issue in the OT Shim layer, consider
 * adding an extra slot in io.opentelemetry.trace.Span, so:
 * 1) The current SpanContextShim is directly stored in Span.
 * 2) The lock for this operation can be on a per-Span basis.
 *
 * For more information, see:
 * https://github.com/opentracing/specification/blob/master/specification.md#set-a-baggage-item
 */
final class SpanContextShimTable {
  private final Map<Span, SpanContextShim> shimsMap = new WeakHashMap<>();
  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  public void setBaggageItem(SpanShim spanShim, String key, String value) {
    lock.writeLock().lock();
    try {
      SpanContextShim contextShim = shimsMap.get(spanShim.getSpan());
      if (contextShim == null) {
        contextShim = new SpanContextShim(spanShim);
      }

      contextShim = contextShim.newWithKeyValue(key, value);
      shimsMap.put(spanShim.getSpan(), contextShim);
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Nullable
  public String getBaggageItem(SpanShim spanShim, String key) {
    lock.readLock().lock();
    try {
      SpanContextShim contextShim = shimsMap.get(spanShim.getSpan());
      return contextShim == null ? null : contextShim.getBaggageItem(key);
    } finally {
      lock.readLock().unlock();
    }
  }

  @Nullable
  public SpanContextShim get(SpanShim spanShim) {
    lock.readLock().lock();
    try {
      return shimsMap.get(spanShim.getSpan());
    } finally {
      lock.readLock().unlock();
    }
  }

  public SpanContextShim create(SpanShim spanShim) {
    return create(spanShim, spanShim.telemetryInfo().emptyCorrelationContext());
  }

  public SpanContextShim create(SpanShim spanShim, CorrelationContext distContext) {
    lock.writeLock().lock();
    try {
      SpanContextShim contextShim = shimsMap.get(spanShim.getSpan());
      if (contextShim != null) {
        return contextShim;
      }

      contextShim =
          new SpanContextShim(
              spanShim.telemetryInfo(), spanShim.getSpan().getContext(), distContext);
      shimsMap.put(spanShim.getSpan(), contextShim);
      return contextShim;

    } finally {
      lock.writeLock().unlock();
    }
  }
}
