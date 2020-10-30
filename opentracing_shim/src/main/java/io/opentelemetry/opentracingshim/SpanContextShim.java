/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.Entry;
import io.opentelemetry.api.baggage.EntryMetadata;
import io.opentelemetry.context.Context;
import io.opentracing.SpanContext;
import java.util.Iterator;
import java.util.Map;

final class SpanContextShim extends BaseShimObject implements SpanContext {

  private final io.opentelemetry.api.trace.SpanContext context;
  private final Baggage baggage;

  public SpanContextShim(SpanShim spanShim) {
    this(
        spanShim.telemetryInfo(),
        spanShim.getSpan().getSpanContext(),
        spanShim.telemetryInfo().emptyBaggage());
  }

  public SpanContextShim(
      TelemetryInfo telemetryInfo, io.opentelemetry.api.trace.SpanContext context) {
    this(telemetryInfo, context, telemetryInfo.emptyBaggage());
  }

  public SpanContextShim(
      TelemetryInfo telemetryInfo,
      io.opentelemetry.api.trace.SpanContext context,
      Baggage baggage) {
    super(telemetryInfo);
    this.context = context;
    this.baggage = baggage;
  }

  SpanContextShim newWithKeyValue(String key, String value) {
    Context parentContext = Context.current().with(baggage);

    Baggage.Builder builder = Baggage.builder().setParent(parentContext);
    builder.put(key, value, EntryMetadata.empty());

    return new SpanContextShim(telemetryInfo(), context, builder.build());
  }

  io.opentelemetry.api.trace.SpanContext getSpanContext() {
    return context;
  }

  Baggage getBaggage() {
    return baggage;
  }

  @Override
  public String toTraceId() {
    return context.getTraceIdAsHexString();
  }

  @Override
  public String toSpanId() {
    return context.getSpanIdAsHexString();
  }

  @Override
  public Iterable<Map.Entry<String, String>> baggageItems() {
    final Iterator<Entry> iterator = baggage.getEntries().iterator();
    return new BaggageIterable(iterator);
  }

  @SuppressWarnings("ReturnMissingNullable")
  String getBaggageItem(String key) {
    return baggage.getEntryValue(key);
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
