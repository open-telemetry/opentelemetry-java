/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import io.opentelemetry.context.Context;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
  public int size() {
    // TODO(anuraaga): Optimize this
    BaggageCounter counter = new BaggageCounter();
    forEach(counter);
    return counter.count;
  }

  private static class BaggageCounter implements BaggageConsumer {
    private int count = 0;

    @Override
    public void accept(String key, String value, EntryMetadata metadata) {
      count++;
    }
  }

  @Override
  public void forEach(BaggageConsumer consumer) {
    Set<String> consumedKeys = new HashSet<>(entries.size());
    entries.forEach(
        (key, entry) -> {
          consumedKeys.add(key);
          // Skip any null entries that may have been added by Builder.remove. We already added to
          // consumed keys, so even if a parent has it it won't be consumed.
          if (entry == null) {
            return;
          }
          consumer.accept(key, entry.getValue(), entry.getEntryMetadata());
        });
    if (parent != null) {
      parent.forEach(
          (key, value, metadata) -> {
            if (consumedKeys.add(key)) {
              consumer.accept(key, value, metadata);
            }
          });
    }
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
      entries.put(
          Objects.requireNonNull(key, "key"),
          Entry.create(
              key,
              Objects.requireNonNull(value, "value"),
              Objects.requireNonNull(entryMetadata, "entryMetadata")));
      return this;
    }

    @Override
    public Baggage.Builder put(String key, String value) {
      entries.put(
          Objects.requireNonNull(key, "key"),
          Entry.create(key, Objects.requireNonNull(value, "value"), EntryMetadata.EMPTY));
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
    public ImmutableBaggage build() {
      if (parent == null && !noImplicitParent) {
        parent = Baggage.current();
      }
      return new ImmutableBaggage(entries, parent);
    }
  }
}
