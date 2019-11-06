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

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.distributedcontext.CorrelationContext;
import io.opentelemetry.distributedcontext.Label;
import io.opentelemetry.distributedcontext.LabelKey;
import io.opentelemetry.distributedcontext.LabelMetadata;
import io.opentelemetry.distributedcontext.LabelValue;
import io.opentelemetry.internal.Utils;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
// TODO: Migrate to AutoValue
// @AutoValue
class CorrelationContextSdk implements CorrelationContext {

  // The types of the EntryKey and Entry must match for each entry.
  private final Map<LabelKey, Label> entries;
  @Nullable private final CorrelationContext parent;

  /**
   * Creates a new {@link CorrelationContextSdk} with the given entries.
   *
   * @param entries the initial entries for this {@code DistributedContextSdk}.
   * @param parent providing a default set of entries
   */
  private CorrelationContextSdk(
      Map<? extends LabelKey, ? extends Label> entries, CorrelationContext parent) {
    this.entries = Collections.unmodifiableMap(new HashMap<>(checkNotNull(entries, "entries")));
    this.parent = parent;
  }

  @Override
  public Collection<Label> getEntries() {
    Map<LabelKey, Label> combined = new HashMap<>(entries);
    if (parent != null) {
      for (Label entry : parent.getEntries()) {
        if (!combined.containsKey(entry.getKey())) {
          combined.put(entry.getKey(), entry);
        }
      }
    }
    // Clean out any null values that may have been added by Builder.remove.
    for (Iterator<Label> it = combined.values().iterator(); it.hasNext(); ) {
      if (it.next() == null) {
        it.remove();
      }
    }

    return Collections.unmodifiableCollection(combined.values());
  }

  @Nullable
  @Override
  public LabelValue getEntryValue(LabelKey entryKey) {
    Label entry = entries.get(entryKey);
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
    private final Map<LabelKey, Label> entries;

    /** Create a new empty DistributedContext builder. */
    Builder() {
      this.entries = new HashMap<>();
    }

    @Override
    public CorrelationContext.Builder setParent(CorrelationContext parent) {
      this.parent = Utils.checkNotNull(parent, "parent");
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
        LabelKey key, LabelValue value, LabelMetadata entryMetadata) {
      entries.put(
          checkNotNull(key, "key"),
          Label.create(
              key, checkNotNull(value, "value"), checkNotNull(entryMetadata, "entryMetadata")));
      return this;
    }

    @Override
    public CorrelationContext.Builder remove(LabelKey key) {
      entries.remove(checkNotNull(key, "key"));
      if (parent != null && parent.getEntryValue(key) != null) {
        entries.put(key, null);
      }
      return this;
    }

    @Override
    public CorrelationContextSdk build() {
      if (parent == null && !noImplicitParent) {
        parent = OpenTelemetry.getDistributedContextManager().getCurrentContext();
      }
      return new CorrelationContextSdk(entries, parent);
    }
  }
}
