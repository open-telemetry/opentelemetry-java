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

package io.opentelemetry.sdk.distributedcontext;

import static io.opentelemetry.internal.Utils.checkNotNull;

import io.opentelemetry.distributedcontext.DistributedContext;
import io.opentelemetry.distributedcontext.Entry;
import io.opentelemetry.distributedcontext.EntryKey;
import io.opentelemetry.distributedcontext.EntryMetadata;
import io.opentelemetry.distributedcontext.EntryValue;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
// TODO: Migrate to AutoValue
// @AutoValue
class DistributedContextSdk implements DistributedContext {

  // The types of the EntryKey and Entry must match for each entry.
  private final Map<EntryKey, Entry> entries;
  @Nullable private final DistributedContext parent;

  /**
   * Creates a new {@link DistributedContextSdk} with the given entries.
   *
   * @param entries the initial entries for this {@code DistributedContextSdk}.
   * @param parent providing a default set of entries
   */
  private DistributedContextSdk(
      Map<? extends EntryKey, ? extends Entry> entries, DistributedContext parent) {
    this.entries = Collections.unmodifiableMap(new HashMap<>(checkNotNull(entries, "entries")));
    this.parent = parent;
  }

  @Override
  public Iterator<Entry> getIterator() {
    Map<EntryKey, Entry> combined = new HashMap<>(entries);
    if (parent != null) {
      for (Iterator<Entry> it = parent.getIterator(); it.hasNext(); ) {
        Entry entry = it.next();
        if (!combined.containsKey(entry.getKey())) {
          combined.put(entry.getKey(), entry);
        }
      }
    }
    // Clean out any null values that may have been added by Builder.remove.
    for (Iterator<Entry> it = combined.values().iterator(); it.hasNext(); ) {
      if (it.next() == null) {
        it.remove();
      }
    }

    return Collections.unmodifiableCollection(combined.values()).iterator();
  }

  @Nullable
  @Override
  public EntryValue getEntryValue(EntryKey entryKey) {
    Entry entry = entries.get(entryKey);
    if (entry != null) {
      return entry.getValue();
    } else {
      return parent == null ? null : parent.getEntryValue(entryKey);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || !(o instanceof DistributedContextSdk)) {
      return false;
    }

    DistributedContextSdk distContextSdk = (DistributedContextSdk) o;

    if (!entries.equals(distContextSdk.entries)) {
      return false;
    }
    return parent != null ? parent.equals(distContextSdk.parent) : distContextSdk.parent == null;
  }

  @Override
  public int hashCode() {
    int result = entries.hashCode();
    result = 31 * result + (parent != null ? parent.hashCode() : 0);
    return result;
  }

  // TODO: Migrate to AutoValue.Builder
  // @AutoValue.Builder
  static class Builder implements DistributedContext.Builder {
    @Nullable private DistributedContext parent;
    private final Map<EntryKey, Entry> entries;

    /** Create a new empty DistributedContext builder. */
    Builder() {
      this.entries = new HashMap<>();
    }

    @Override
    public DistributedContext.Builder setParent(DistributedContext parent) {
      this.parent = parent;
      return this;
    }

    @Override
    public DistributedContext.Builder setNoParent() {
      parent = null;
      return this;
    }

    @Override
    public DistributedContext.Builder put(
        EntryKey key, EntryValue value, EntryMetadata entryMetadata) {
      entries.put(
          checkNotNull(key, "key"),
          Entry.create(
              key, checkNotNull(value, "value"), checkNotNull(entryMetadata, "entryMetadata")));
      return this;
    }

    @Override
    public DistributedContext.Builder remove(EntryKey key) {
      entries.remove(checkNotNull(key, "key"));
      if (parent != null && parent.getEntryValue(key) != null) {
        entries.put(key, null);
      }
      return this;
    }

    @Override
    public DistributedContextSdk build() {
      // TODO if (parent == null) parent = DistributedContextManager.getCurrentContext();
      return new DistributedContextSdk(entries, parent);
    }
  }
}
