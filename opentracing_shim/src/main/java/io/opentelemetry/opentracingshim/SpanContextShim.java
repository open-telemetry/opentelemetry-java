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
import io.opentelemetry.correlationcontext.Entry;
import io.opentelemetry.correlationcontext.EntryMetadata;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.TraceState;
import io.opentracing.SpanContext;
import java.util.Iterator;
import java.util.Map;

final class SpanContextShim extends BaseShimObject implements SpanContext {
  static final EntryMetadata DEFAULT_ENTRY_METADATA =
      EntryMetadata.create(EntryMetadata.EntryTtl.UNLIMITED_PROPAGATION);

  private final String traceId;
  private final String spanId;
  private final byte traceFlags;
  private final TraceState traceState;

  private final CorrelationContext distContext;

  public SpanContextShim(SpanShim spanShim) {
    this(
        spanShim.telemetryInfo(),
        spanShim.getSpan(),
        spanShim.telemetryInfo().emptyCorrelationContext());
  }

  public SpanContextShim(TelemetryInfo telemetryInfo, io.opentelemetry.trace.Span span) {
    this(telemetryInfo, span, telemetryInfo.emptyCorrelationContext());
  }

  public SpanContextShim(
      TelemetryInfo telemetryInfo,
      io.opentelemetry.trace.Span span,
      CorrelationContext distContext) {
    this(
        telemetryInfo,
        span.getTraceIdAsHexString(),
        span.getSpanIdAsHexString(),
        span.getTraceFlags(),
        span.getTraceState(),
        distContext);
  }

  private SpanContextShim(
      TelemetryInfo telemetryInfo,
      String traceId,
      String spanId,
      byte traceFlags,
      TraceState traceState,
      CorrelationContext distContext) {
    super(telemetryInfo);
    this.traceId = traceId;
    this.spanId = spanId;
    this.traceFlags = traceFlags;
    this.traceState = traceState;
    this.distContext = distContext;
  }

  SpanContextShim newWithKeyValue(String key, String value) {
    CorrelationContext.Builder builder = contextManager().contextBuilder().setParent(distContext);
    builder.put(key, value, DEFAULT_ENTRY_METADATA);

    return new SpanContextShim(
        telemetryInfo(), traceId, spanId, traceFlags, traceState, builder.build());
  }

  io.opentelemetry.trace.Span getPropagatedSpan() {
    return Span.getPropagated(traceId, spanId, traceFlags, traceState);
  }

  CorrelationContext getCorrelationContext() {
    return distContext;
  }

  @Override
  public String toTraceId() {
    return traceId;
  }

  @Override
  public String toSpanId() {
    return spanId;
  }

  @Override
  public Iterable<Map.Entry<String, String>> baggageItems() {
    final Iterator<Entry> iterator = distContext.getEntries().iterator();
    return new BaggageIterable(iterator);
  }

  @SuppressWarnings("ReturnMissingNullable")
  String getBaggageItem(String key) {
    return distContext.getEntryValue(key);
  }

  static class BaggageIterable implements Iterable<Map.Entry<String, String>> {
    final Iterator<Entry> iterator;

    BaggageIterable(Iterator<Entry> iterator) {
      this.iterator = iterator;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
      return new Iterator<Map.Entry<String, String>>() {
        @Override
        public boolean hasNext() {
          return iterator.hasNext();
        }

        @Override
        public Map.Entry<String, String> next() {
          return new BaggageEntry(iterator.next());
        }

        @Override
        public void remove() {}
      };
    }
  }

  static class BaggageEntry implements Map.Entry<String, String> {
    final Entry entry;

    BaggageEntry(Entry entry) {
      this.entry = entry;
    }

    @Override
    public String getKey() {
      return entry.getKey();
    }

    @Override
    public String getValue() {
      return entry.getValue();
    }

    @Override
    public String setValue(String value) {
      return getValue();
    }
  }
}
