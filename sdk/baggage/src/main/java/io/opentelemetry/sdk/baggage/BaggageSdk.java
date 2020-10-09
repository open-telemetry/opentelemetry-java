/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.baggage;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.baggage.Baggage;
import io.opentelemetry.baggage.BaggageUtils;
import io.opentelemetry.baggage.Entry;
import io.opentelemetry.baggage.EntryMetadata;
import io.opentelemetry.context.Context;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
// TODO: Migrate to AutoValue
// @AutoValue
class BaggageSdk implements Baggage {

  // The types of the EntryKey and Entry must match for each entry.
  private final Map<String, Entry> entries;
  @Nullable private final Baggage parent;

  /**
   * Creates a new {@link BaggageSdk} with the given entries.
   *
   * @param entries the initial entries for this {@code BaggageSdk}.
   * @param parent providing a default set of entries
   */
  private BaggageSdk(Map<String, ? extends Entry> entries, Baggage parent) {
    this.entries =
        Collections.unmodifiableMap(new HashMap<>(Objects.requireNonNull(entries, "entries")));
    this.parent = parent;
  }

  @Override
  public Collection<Entry> getEntries() {
    Map<String, Entry> combined = new HashMap<>(entries);
    if (parent != null) {
      for (Entry entry : parent.getEntries()) {
        if (!combined.containsKey(entry.getKey())) {
          combined.put(entry.getKey(), entry);
        }
      }
    }
    // Clean out any null values that may have been added by Builder.remove.
    combined.values().removeIf(Objects::isNull);

    return Collections.unmodifiableCollection(combined.values());
  }

  @Nullable
  @Override
  public String getEntryValue(String entryKey) {
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
    if (!(o instanceof BaggageSdk)) {
      return false;
    }

    BaggageSdk baggageSdk = (BaggageSdk) o;

    if (!entries.equals(baggageSdk.entries)) {
      return false;
    }
    return parent != null ? parent.equals(baggageSdk.parent) : baggageSdk.parent == null;
  }

  @Override
  public int hashCode() {
    int result = entries.hashCode();
    result = 31 * result + (parent != null ? parent.hashCode() : 0);
    return result;
  }

  // TODO: Migrate to AutoValue.Builder
  // @AutoValue.Builder
  static class Builder implements Baggage.Builder {
    @Nullable private Baggage parent;
    private boolean noImplicitParent;
    private final Map<String, Entry> entries;

    /** Create a new empty Baggage builder. */
    Builder() {
      this.entries = new HashMap<>();
    }

    @Override
    public Baggage.Builder setParent(Context context) {
      Objects.requireNonNull(context, "context");
      parent = BaggageUtils.getBaggage(context);
      return this;
    }

    @Override
    public Baggage.Builder setNoParent() {
      this.parent = null;
      noImplicitParent = true;
      return this;
    }

    @Override
    public Baggage.Builder put(String key, String value, EntryMetadata entryMetadata) {
      entries.put(
          Objects.requireNonNull(key, "key"),
          Entry.create(
              key,
              Objects.requireNonNull(value, "value"),
              Objects.requireNonNull(entryMetadata, "entryMetadata")));
      return this;
    }

    @Override
    public Baggage.Builder remove(String key) {
      entries.remove(Objects.requireNonNull(key, "key"));
      if (parent != null && parent.getEntryValue(key) != null) {
        entries.put(key, null);
      }
      return this;
    }

    @Override
    public BaggageSdk build() {
      if (parent == null && !noImplicitParent) {
        parent = OpenTelemetry.getBaggageManager().getCurrentBaggage();
      }
      return new BaggageSdk(entries, parent);
    }
  }
}
