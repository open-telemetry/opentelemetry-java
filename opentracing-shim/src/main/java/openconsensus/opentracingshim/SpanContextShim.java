/*
 * Copyright 2019, OpenConsensus Authors
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

package openconsensus.opentracingshim;

import io.opentracing.SpanContext;
import java.util.Iterator;
import java.util.Map;
import openconsensus.tags.EmptyTagMap;
import openconsensus.tags.Tag;
import openconsensus.tags.TagKey;
import openconsensus.tags.TagMap;
import openconsensus.tags.TagMapBuilder;
import openconsensus.tags.TagMetadata;
import openconsensus.tags.TagValue;

final class SpanContextShim implements SpanContext {
  static final TagMetadata DEFAULT_TAG_METADATA =
      TagMetadata.create(TagMetadata.TagTtl.UNLIMITED_PROPAGATION);

  private final TracerShim tracerShim;
  private final openconsensus.trace.SpanContext context;
  private TagMap tagMap;

  public SpanContextShim(TracerShim tracerShim, openconsensus.trace.SpanContext context) {
    this(tracerShim, context, EmptyTagMap.INSTANCE);
  }

  public SpanContextShim(
      TracerShim tracerShim, openconsensus.trace.SpanContext context, TagMap tagMap) {
    this.tracerShim = tracerShim;
    this.context = context;
    this.tagMap = tagMap;
  }

  openconsensus.trace.SpanContext getSpanContext() {
    return context;
  }

  TagMap getTagMap() {
    return tagMap;
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
    synchronized (this) {
      final Iterator<Tag> iterator = tagMap.getIterator();
      return new BaggageIterable(iterator);
    }
  }

  @SuppressWarnings("ReturnMissingNullable")
  String getBaggageItem(String key) {
    synchronized (this) {
      TagValue value = tagMap.getTagValue(TagKey.create(key));
      return value == null ? null : value.asString();
    }
  }

  void setBaggageItem(String key, String value) {
    synchronized (this) {
      TagMapBuilder builder = tracerShim.getTagger().toBuilder(tagMap);
      builder.put(TagKey.create(key), TagValue.create(value), DEFAULT_TAG_METADATA);
      tagMap = builder.build();
    }
  }

  static class BaggageIterable implements Iterable<Map.Entry<String, String>> {
    final Iterator<Tag> iterator;

    BaggageIterable(Iterator<Tag> iterator) {
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
    final Tag tag;

    BaggageEntry(Tag tag) {
      this.tag = tag;
    }

    @Override
    public String getKey() {
      return tag.getKey().getName();
    }

    @Override
    public String getValue() {
      return tag.getValue().asString();
    }

    @Override
    public String setValue(String value) {
      return getValue();
    }
  }
}
