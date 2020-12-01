/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import static java.util.Objects.requireNonNull;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.internal.ImmutableKeyValuePairs;
import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.context.Context;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
class ImmutableBaggage extends ImmutableKeyValuePairs<String, Entry> implements Baggage {

  private static final Baggage EMPTY = new ImmutableBaggage.Builder().build();

  static Baggage empty() {
    return EMPTY;
  }

  static BaggageBuilder builder() {
    return new Builder();
  }

  @AutoValue
  @Immutable
  abstract static class ArrayBackedBaggage extends ImmutableBaggage {
    ArrayBackedBaggage() {}

    @Override
    protected abstract List<Object> data();

    @Override
    public BaggageBuilder toBuilder() {
      return new ImmutableBaggage.Builder(new ArrayList<>(data()));
    }
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
  public BaggageBuilder toBuilder() {
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
  static class Builder implements BaggageBuilder {

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
    public BaggageBuilder setParent(Context context) {
      requireNonNull(context, "context");
      parent = Baggage.fromContext(context);
      return this;
    }

    @Override
    public BaggageBuilder setNoParent() {
      this.parent = null;
      noImplicitParent = true;
      return this;
    }

    @Override
    public BaggageBuilder put(String key, String value, EntryMetadata entryMetadata) {
      if (!isKeyValid(key) || !isValueValid(value) || entryMetadata == null) {
        return this;
      }
      data.add(key);
      data.add(Entry.create(key, value, entryMetadata));

      return this;
    }

    @Override
    public BaggageBuilder put(String key, String value) {
      return put(key, value, EntryMetadata.EMPTY);
    }

    @Override
    public BaggageBuilder remove(String key) {
      if (key == null) {
        return this;
      }
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
