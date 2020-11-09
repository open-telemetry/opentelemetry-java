/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import static java.util.Objects.requireNonNull;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.internal.ImmutableKeyValuePairs;
import io.opentelemetry.context.Context;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
class ImmutableBaggage extends ImmutableKeyValuePairs<String, Entry> implements Baggage {

  static final Baggage EMPTY = new ImmutableBaggage.Builder().setNoParent().build();

  @AutoValue
  @Immutable
  abstract static class ArrayBackedBaggage extends ImmutableBaggage {
    ArrayBackedBaggage() {}

    @Override
    protected abstract List<Object> data();

    @Override
    public Baggage.Builder toBuilder() {
      return new ImmutableBaggage.Builder(new ArrayList<>(data()));
    }
  }

  public static Baggage.Builder builder() {
    return new Builder();
  }

  @Override
  public void forEach(BaggageConsumer consumer) {
    for (int i = 0; i < data().size(); i += 2) {
      Entry entry = (Entry) data().get(i + 1);
      consumer.accept((String) data().get(i), entry.getValue(), entry.getEntryMetadata());
    }
  }

  @Nullable
  @Override
  public String getEntryValue(String entryKey) {
    Entry entry = get(entryKey);
    return entry != null ? entry.getValue() : null;
  }

  @Override
  public Baggage.Builder toBuilder() {
    Builder builder = new Builder(data());
    builder.noImplicitParent = true;
    return builder;
  }

  private static Baggage sortAndFilterToBaggage(Object[] data) {
    return new AutoValue_ImmutableBaggage_ArrayBackedBaggage(
        sortAndFilter(data, /* filterNullValues= */ true));
  }

  // TODO: Migrate to AutoValue.Builder
  // @AutoValue.Builder
  static class Builder implements Baggage.Builder {

    @Nullable private Baggage parent;
    private boolean noImplicitParent;
    private final List<Object> data;

    Builder() {
      this.data = new ArrayList<>();
    }

    Builder(List<Object> data) {
      this.data = new ArrayList<>(data);
    }

    @Override
    public Baggage.Builder setParent(Context context) {
      requireNonNull(context, "context");
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
      requireNonNull(key, "key");
      requireNonNull(value, "value");
      requireNonNull(entryMetadata, "entryMetadata");

      data.add(key);
      data.add(Entry.create(key, value, entryMetadata));
      return this;
    }

    @Override
    public Baggage.Builder put(String key, String value) {
      requireNonNull(key, "key");
      requireNonNull(value, "value");
      return put(key, value, EntryMetadata.EMPTY);
    }

    @Override
    public Baggage.Builder remove(String key) {
      requireNonNull(key, "key");
      data.add(key);
      data.add(null);
      return this;
    }

    @Override
    public Baggage build() {
      if (parent == null && !noImplicitParent) {
        parent = Baggage.current();
      }

      List<Object> data = this.data;
      if (parent != null && !parent.isEmpty()) {
        List<Object> merged = new ArrayList<>(parent.size() * 2 + data.size());
        parent.forEach(
            (key, value, metadata) -> {
              merged.add(key);
              merged.add(Entry.create(key, value, metadata));
            });
        merged.addAll(data);
        data = merged;
      }
      return sortAndFilterToBaggage(data.toArray());
    }
  }
}
