/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import io.opentelemetry.correlationcontext.CorrelationContext;
import io.opentelemetry.correlationcontext.Entry;
import io.opentelemetry.correlationcontext.EntryMetadata;
import io.opentracing.SpanContext;
import java.util.Iterator;
import java.util.Map;

final class SpanContextShim extends BaseShimObject implements SpanContext {
  static final EntryMetadata DEFAULT_ENTRY_METADATA =
      EntryMetadata.create(EntryMetadata.EntryTtl.UNLIMITED_PROPAGATION);

  private final io.opentelemetry.trace.SpanContext context;
  private final CorrelationContext distContext;

  public SpanContextShim(SpanShim spanShim) {
    this(
        spanShim.telemetryInfo(),
        spanShim.getSpan().getContext(),
        spanShim.telemetryInfo().emptyCorrelationContext());
  }

  public SpanContextShim(TelemetryInfo telemetryInfo, io.opentelemetry.trace.SpanContext context) {
    this(telemetryInfo, context, telemetryInfo.emptyCorrelationContext());
  }

  public SpanContextShim(
      TelemetryInfo telemetryInfo,
      io.opentelemetry.trace.SpanContext context,
      CorrelationContext distContext) {
    super(telemetryInfo);
    this.context = context;
    this.distContext = distContext;
  }

  SpanContextShim newWithKeyValue(String key, String value) {
    CorrelationContext.Builder builder = contextManager().contextBuilder().setParent(distContext);
    builder.put(key, value, DEFAULT_ENTRY_METADATA);

    return new SpanContextShim(telemetryInfo(), context, builder.build());
  }

  io.opentelemetry.trace.SpanContext getSpanContext() {
    return context;
  }

  CorrelationContext getCorrelationContext() {
    return distContext;
  }

  @Override
  public String toTraceId() {
    return context.getTraceIdAsHexString();
  }

  @Override
  public String toSpanId() {
    return context.getSpanIdAsHexString().toString();
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
