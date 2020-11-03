/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import io.opentelemetry.api.internal.StringUtils;
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
class ImmutableBaggage implements Baggage {

  static final Baggage EMPTY = new ImmutableBaggage.Builder().build();

  // The types of the EntryKey and Entry must match for each entry.
  private final Map<String, Entry> entries;
  @Nullable private final Baggage parent;

  /**
   * Creates a new {@link ImmutableBaggage} with the given entries.
   *
   * @param entries the initial entries for this {@code BaggageSdk}.
   * @param parent providing a default set of entries
   */
  private ImmutableBaggage(Map<String, ? extends Entry> entries, Baggage parent) {
    this.entries =
        Collections.unmodifiableMap(new HashMap<>(Objects.requireNonNull(entries, "entries")));
    this.parent = parent;
  }

  public static Baggage.Builder builder() {
    return new Builder();
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
  public Baggage.Builder toBuilder() {
    Builder builder = new Builder();
    builder.entries.putAll(entries);
    builder.parent = parent;
    builder.noImplicitParent = true;
    return builder;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ImmutableBaggage)) {
      return false;
    }

    ImmutableBaggage baggage = (ImmutableBaggage) o;

    if (!entries.equals(baggage.entries)) {
      return false;
    }
    return Objects.equals(parent, baggage.parent);
  }

  @Override
  public int hashCode() {
    int result = entries.hashCode();
    result = 31 * result + (parent != null ? parent.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ImmutableBaggage{" + "entries=" + entries + ", parent=" + parent + '}';
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
      parent = Baggage.fromContext(context);
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
      if (!isKeyValid(key) || !isValueValid(value) || entryMetadata == null) {
        return this;
      }
      entries.put(key, Entry.create(key, value, entryMetadata));
      return this;
    }

    @Override
    public Baggage.Builder put(String key, String value) {
      return put(key, value, EntryMetadata.EMPTY);
    }

    @Override
    public Baggage.Builder remove(String key) {
      if (key == null) {
        return this;
      }
      entries.remove(key);
      if (parent != null && parent.getEntryValue(key) != null) {
        entries.put(key, null);
      }
      return this;
    }

    @Override
    public ImmutableBaggage build() {
      if (parent == null && !noImplicitParent) {
        parent = Baggage.current();
      }
      return new ImmutableBaggage(entries, parent);
    }
  }

  /**
   * Determines whether the given {@code String} is a valid entry key.
   *
   * @param name the entry key name to be validated.
   * @return whether the name is valid.
   */
  private static boolean isKeyValid(String name) {
    return name != null && !name.isEmpty() && StringUtils.isPrintableString(name);
  }

  /**
   * Determines whether the given {@code String} is a valid entry value.
   *
   * @param value the entry value to be validated.
   * @return whether the value is valid.
   */
  private static boolean isValueValid(String value) {
    return value != null && StringUtils.isPrintableString(value);
  }
}
