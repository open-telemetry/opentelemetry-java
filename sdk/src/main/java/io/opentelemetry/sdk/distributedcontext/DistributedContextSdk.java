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

import com.google.auto.value.AutoValue;
import io.opentelemetry.OpenTelemetry;
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
@AutoValue
abstract class DistributedContextSdk implements DistributedContext {

  DistributedContextSdk() {}

  // The types of the EntryKey and Entry must match for each entry.
  abstract Map<EntryKey, Entry> getEntries();

  @Nullable
  abstract DistributedContext getParent();

  /**
   * Returns a new {@link Builder}.
   *
   * @return a {@code Builder}.
   */
  static Builder builder() {
    return new AutoValue_DistributedContextSdk.Builder().setEntries(new HashMap<EntryKey, Entry>());
  }

  @Override
  public Iterator<Entry> getIterator() {
    Map<EntryKey, Entry> combined = new HashMap<>(getEntries());
    if (getParent() != null) {
      for (Iterator<Entry> it = getParent().getIterator(); it.hasNext(); ) {
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
    Entry entry = getEntries().get(entryKey);
    if (entry != null) {
      return entry.getValue();
    } else {
      return getParent() == null ? null : getParent().getEntryValue(entryKey);
    }
  }

  @AutoValue.Builder
  abstract static class Builder implements DistributedContext.Builder {

    Builder() {}

    abstract DistributedContextSdk autoBuild();

    abstract DistributedContextSdk.Builder setEntries(Map<EntryKey, Entry> entries);

    abstract Map<EntryKey, Entry> getEntries();

    @Nullable
    abstract DistributedContext getParent();

    @Override
    public abstract DistributedContextSdk.Builder setParent(@Nullable DistributedContext parent);

    @Override
    public DistributedContextSdk.Builder setNoParent() {
      return setParent(null);
    }

    @Override
    public DistributedContextSdk.Builder put(
        EntryKey key, EntryValue value, EntryMetadata entryMetadata) {
      getEntries()
          .put(
              checkNotNull(key, "key"),
              Entry.create(
                  key, checkNotNull(value, "value"), checkNotNull(entryMetadata, "entryMetadata")));
      return this;
    }

    @Override
    public DistributedContextSdk.Builder remove(EntryKey key) {
      getEntries().remove(checkNotNull(key, "key"));
      if (getParent() != null && getParent().getEntryValue(key) != null) {
        getEntries().put(key, null);
      }
      return this;
    }

    @Override
    public DistributedContextSdk build() {
      if (getParent() == null) {
        setParent(OpenTelemetry.getDistributedContextManager().getCurrentContext());
      }
      setEntries(Collections.unmodifiableMap(new HashMap<EntryKey, Entry>(getEntries())));
      return autoBuild();
    }
  }
}
