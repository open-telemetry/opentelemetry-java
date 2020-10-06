/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.baggage;

import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.baggage.Baggage;
import io.opentelemetry.baggage.Entry;
import io.opentelemetry.baggage.EntryMetadata;
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

  /**
   * Creates a new {@link BaggageSdk} with the given entries.
   *
   * @param entries the initial entries for this {@code BaggageSdk}.
   */
  private BaggageSdk(Map<String, ? extends Entry> entries) {
    this.entries =
        Collections.unmodifiableMap(new HashMap<>(Objects.requireNonNull(entries, "entries")));
  }

  @Override
  public Collection<Entry> getEntries() {
    return Collections.unmodifiableCollection(entries.values());
  }

  @Nullable
  @Override
  public String getEntryValue(String entryKey) {
    Entry entry = entries.get(entryKey);
    return entry != null ? entry.getValue() : null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BaggageSdk)) {
      return false;
    }
    BaggageSdk that = (BaggageSdk) o;
    return Objects.equals(entries, that.entries);
  }

  @Override
  public int hashCode() {
    return Objects.hash(entries);
  }

  // TODO: Migrate to AutoValue.Builder
  // @AutoValue.Builder
  static class Builder implements Baggage.Builder {

    private final Map<String, Entry> entries;

    /** Create a new empty Baggage builder. */
    Builder() {
      this.entries = new HashMap<>();
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
      return this;
    }

    @Override
    public BaggageSdk build() {
      // todo: is this implicit parenting part of the baggage spec?
      Baggage parent = OpenTelemetry.getBaggageManager().getCurrentBaggage();
      parent
          .getEntries()
          .forEach(
              entry -> {
                if (!entries.containsKey(entry.getKey())) {
                  put(entry.getKey(), entry.getValue(), entry.getEntryMetadata());
                }
              });
      return new BaggageSdk(entries);
    }
  }
}
