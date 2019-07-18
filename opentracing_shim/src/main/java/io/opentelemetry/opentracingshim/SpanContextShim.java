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

import io.opentelemetry.distributedcontext.DistributedContext;
import io.opentelemetry.distributedcontext.EmptyDistributedContext;
import io.opentelemetry.distributedcontext.Entry;
import io.opentelemetry.distributedcontext.EntryKey;
import io.opentelemetry.distributedcontext.EntryMetadata;
import io.opentelemetry.distributedcontext.EntryValue;
import io.opentracing.SpanContext;
import java.util.Iterator;
import java.util.Map;

final class SpanContextShim implements SpanContext {
  static final EntryMetadata DEFAULT_ENTRY_METADATA =
      EntryMetadata.create(EntryMetadata.EntryTtl.UNLIMITED_PROPAGATION);

  private final TelemetryInfo telemetryInfo;
  private final io.opentelemetry.trace.SpanContext context;
  private final DistributedContext distContext;

  public SpanContextShim(TelemetryInfo telemetryInfo, io.opentelemetry.trace.SpanContext context) {
    this(telemetryInfo, context, EmptyDistributedContext.getInstance());
  }

  public SpanContextShim(
      TelemetryInfo telemetryInfo,
      io.opentelemetry.trace.SpanContext context,
      DistributedContext distContext) {
    this.telemetryInfo = telemetryInfo;
    this.context = context;
    this.distContext = distContext;
  }

  SpanContextShim newWithKeyValue(String key, String value) {
    DistributedContext.Builder builder =
        telemetryInfo.contextManager().contextBuilder().setParent(distContext);
    builder.put(EntryKey.create(key), EntryValue.create(value), DEFAULT_ENTRY_METADATA);
    return new SpanContextShim(telemetryInfo, context, builder.build());
  }

  io.opentelemetry.trace.SpanContext getSpanContext() {
    return context;
  }

  DistributedContext getDistributedContext() {
    return distContext;
  }

  @Override
  public String toTraceId() {
    return context.getTraceId().toString();
  }

  @Override
  public String toSpanId() {
    return context.getSpanId().toString();
  }

  @Override
  public Iterable<Map.Entry<String, String>> baggageItems() {
    final Iterator<Entry> iterator = distContext.getEntries().iterator();
    return new BaggageIterable(iterator);
  }

  @SuppressWarnings("ReturnMissingNullable")
  String getBaggageItem(String key) {
    EntryValue value = distContext.getEntryValue(EntryKey.create(key));
    return value == null ? null : value.asString();
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
      return entry.getKey().getName();
    }

    @Override
    public String getValue() {
      return entry.getValue().asString();
    }

    @Override
    public String setValue(String value) {
      return getValue();
    }
  }
}
