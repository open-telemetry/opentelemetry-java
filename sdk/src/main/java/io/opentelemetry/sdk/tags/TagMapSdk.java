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

package io.opentelemetry.sdk.tags;

import static io.opentelemetry.internal.Utils.checkNotNull;

import io.opentelemetry.context.Scope;
import io.opentelemetry.tags.Tag;
import io.opentelemetry.tags.TagKey;
import io.opentelemetry.tags.TagMap;
import io.opentelemetry.tags.TagMetadata;
import io.opentelemetry.tags.TagValue;
import io.opentelemetry.tags.unsafe.ContextUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
public class TagMapSdk implements TagMap {

  // The types of the TagKey and Tag must match for each entry.
  private final Map<TagKey, Tag> tags;

  /**
   * Creates a new {@link TagMapSdk} with the given tags.
   *
   * @param tags the initial tags for this {@code TagMapSdk}.
   */
  private TagMapSdk(Map<? extends TagKey, ? extends Tag> tags) {
    this.tags = Collections.unmodifiableMap(new HashMap<>(checkNotNull(tags, "tags")));
  }

  @Override
  public Iterator<Tag> getIterator() {
    return tags.values().iterator();
  }

  @Nullable
  @Override
  public TagValue getTagValue(TagKey tagKey) {
    Tag tag = tags.get(tagKey);
    return tag == null ? null : tag.getValue();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TagMapSdk tagMapSdk = (TagMapSdk) o;

    return tags.equals(tagMapSdk.tags);
  }

  @Override
  public int hashCode() {
    return tags.hashCode();
  }

  public static class Builder implements TagMap.Builder {
    private final Map<TagKey, Tag> tags;

    /**
     * Create a new {@link TagMap.Builder} with the provided {@link TagMap}.
     *
     * @param tagMap starting set of tags.
     */
    public Builder(TagMap tagMap) {
      this.tags = new HashMap<>();
      for (Iterator<Tag> it = checkNotNull(tagMap, "tagMap").getIterator(); it.hasNext(); ) {
        Tag tag = it.next();
        this.tags.put(tag.getKey(), tag);
      }
    }

    /**
     * Create a new {@link TagMap.Builder} with the provided tags.
     *
     * @param tags starting set of tags.
     */
    public Builder(Map<TagKey, Tag> tags) {
      this.tags = new HashMap<>(checkNotNull(tags, "tags"));
    }

    /** Create a new empty TagMap builder. */
    public Builder() {
      this.tags = new HashMap<>();
    }

    @Override
    public TagMap.Builder put(TagKey key, TagValue value, TagMetadata tagMetadata) {
      tags.put(
          checkNotNull(key, "key"),
          Tag.create(key, checkNotNull(value, "value"), checkNotNull(tagMetadata, "tagMetadata")));
      return this;
    }

    @Override
    public TagMap.Builder remove(TagKey key) {
      tags.remove(checkNotNull(key, "key"));
      return this;
    }

    @Override
    public TagMapSdk build() {
      return new TagMapSdk(tags);
    }

    @Override
    public Scope buildScoped() {
      return ContextUtils.withTagMap(build());
    }
  }
}
