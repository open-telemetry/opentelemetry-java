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

package io.opentelemetry.sdk.correlationcontext;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.correlationcontext.CorrelationContext;
import io.opentelemetry.correlationcontext.Entry;
import io.opentelemetry.correlationcontext.EntryKey;
import io.opentelemetry.correlationcontext.EntryMetadata;
import io.opentelemetry.correlationcontext.EntryValue;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
// TODO: Migrate to AutoValue
// @AutoValue
class CorrelationContextSdk implements CorrelationContext {

  // The types of the EntryKey and Entry must match for each entry.
  private final Map<EntryKey, Entry> entries;
  @Nullable private final CorrelationContext parent;

  /**
   * Creates a new {@link CorrelationContextSdk} with the given entries.
   *
   * @param entries the initial entries for this {@code CorrelationContextSdk}.
   * @param parent providing a default set of entries
   */
  private CorrelationContextSdk(
      Map<? extends EntryKey, ? extends Entry> entries, CorrelationContext parent) {
    this.entries =
        Collections.unmodifiableMap(new HashMap<>(Objects.requireNonNull(entries, "entries")));
    this.parent = parent;
  }

  @Override
  public Collection<Entry> getEntries() {
    Map<EntryKey, Entry> combined = new HashMap<>(entries);
    if (parent != null) {
      for (Entry entry : parent.getEntries()) {
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

    return Collections.unmodifiableCollection(combined.values());
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
    if (o == null || !(o instanceof CorrelationContextSdk)) {
      return false;
    }

    CorrelationContextSdk distContextSdk = (CorrelationContextSdk) o;

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
  static class Builder implements CorrelationContext.Builder {
    @Nullable private CorrelationContext parent;
    private boolean noImplicitParent;
    private final Map<EntryKey, Entry> entries;

    /** Create a new empty CorrelationContext builder. */
    Builder() {
      this.entries = new HashMap<>();
    }

    @Override
    public CorrelationContext.Builder setParent(CorrelationContext parent) {
      this.parent = Objects.requireNonNull(parent, "parent");
      return this;
    }

    @Override
    public CorrelationContext.Builder setNoParent() {
      this.parent = null;
      noImplicitParent = true;
      return this;
    }

    @Override
    public CorrelationContext.Builder put(
        EntryKey key, EntryValue value, EntryMetadata entryMetadata) {
      entries.put(
          Objects.requireNonNull(key, "key"),
          Entry.create(
              key,
              Objects.requireNonNull(value, "value"),
              Objects.requireNonNull(entryMetadata, "entryMetadata")));
      return this;
    }

    @Override
    public CorrelationContext.Builder remove(EntryKey key) {
      entries.remove(Objects.requireNonNull(key, "key"));
      if (parent != null && parent.getEntryValue(key) != null) {
        entries.put(key, null);
      }
      return this;
    }

    @Override
    public CorrelationContextSdk build() {
      if (parent == null && !noImplicitParent) {
        parent = OpenTelemetry.getCorrelationContextManager().getCurrentContext();
      }
      return new CorrelationContextSdk(entries, parent);
    }
  }
}
