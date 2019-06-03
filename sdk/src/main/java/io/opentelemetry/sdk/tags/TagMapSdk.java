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
class TagMapSdk implements TagMap {

  // The types of the TagKey and Tag must match for each entry.
  private final Map<TagKey, Tag> tags;
  @Nullable private final TagMap parent;

  /**
   * Creates a new {@link TagMapSdk} with the given tags.
   *
   * @param tags the initial tags for this {@code TagMapSdk}.
   * @param parent providing a default set of tags
   */
  private TagMapSdk(Map<? extends TagKey, ? extends Tag> tags, TagMap parent) {
    this.tags = Collections.unmodifiableMap(new HashMap<>(checkNotNull(tags, "tags")));
    this.parent = parent;
  }

  @Override
  public Iterator<Tag> getIterator() {
    Map<TagKey, Tag> combined = new HashMap<>(tags);
    if (parent != null) {
      for (Iterator<Tag> it = parent.getIterator(); it.hasNext(); ) {
        Tag tag = it.next();
        if (!combined.containsKey(tag.getKey())) {
          combined.put(tag.getKey(), tag);
        }
      }
    }
    // Clean out any null values that may have been added by Builder.remove.
    for (Iterator<Tag> it = combined.values().iterator(); it.hasNext(); ) {
      if (it.next() == null) {
        it.remove();
      }
    }

    return Collections.unmodifiableCollection(combined.values()).iterator();
  }

  @Nullable
  @Override
  public TagValue getTagValue(TagKey tagKey) {
    Tag tag = tags.get(tagKey);
    if (tag != null) {
      return tag.getValue();
    } else {
      return parent == null ? null : parent.getTagValue(tagKey);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || !(o instanceof TagMapSdk)) {
      return false;
    }

    TagMapSdk tagMapSdk = (TagMapSdk) o;

    if (!tags.equals(tagMapSdk.tags)) {
      return false;
    }
    return parent != null ? parent.equals(tagMapSdk.parent) : tagMapSdk.parent == null;
  }

  @Override
  public int hashCode() {
    int result = tags.hashCode();
    result = 31 * result + (parent != null ? parent.hashCode() : 0);
    return result;
  }

  static class Builder implements TagMap.Builder {
    @Nullable private TagMap parent;
    private final Map<TagKey, Tag> tags;

    /** Create a new empty TagMap builder. */
    Builder() {
      this.tags = new HashMap<>();
    }

    @Override
    public TagMap.Builder setParent(TagMap parent) {
      this.parent = parent;
      return this;
    }

    @Override
    public TagMap.Builder setNoParent() {
      parent = null;
      return this;
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
      if (parent != null && parent.getTagValue(key) != null) {
        tags.put(key, null);
      }
      return this;
    }

    @Override
    public TagMapSdk build() {
      return new TagMapSdk(tags, parent);
    }

    @Override
    public Scope buildScoped() {
      return ContextUtils.withTagMap(build());
    }
  }
}
