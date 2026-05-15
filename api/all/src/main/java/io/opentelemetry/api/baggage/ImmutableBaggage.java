/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.baggage;

import io.opentelemetry.api.internal.ImmutableKeyValuePairs;
import io.opentelemetry.common.impl.ApiUsageLogger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
final class ImmutableBaggage extends ImmutableKeyValuePairs<String, BaggageEntry>
    implements Baggage {

  private static final Baggage EMPTY = new ImmutableBaggage.Builder().build();

  private ImmutableBaggage(Object[] data) {
    super(data, Comparator.naturalOrder());
  }

  static Baggage empty() {
    return EMPTY;
  }

  static BaggageBuilder builder() {
    return new Builder();
  }

  @Nullable
  @Override
  public String getEntryValue(String entryKey) {
    if (entryKey == null) {
      ApiUsageLogger.logNullParam(Baggage.class, "getEntryValue", "entryKey");
      return null;
    }
    BaggageEntry entry = get(entryKey);
    return entry != null ? entry.getValue() : null;
  }

  // Overrides the default implementation to provide a more performant implementation.
  @Nullable
  @Override
  public BaggageEntry getEntry(String entryKey) {
    if (entryKey == null) {
      ApiUsageLogger.logNullParam(Baggage.class, "getEntry", "entryKey");
      return null;
    }
    return get(entryKey);
  }

  @Override
  public BaggageBuilder toBuilder() {
    return new Builder(new ArrayList<>(data()));
  }

  private static Baggage sortAndFilterToBaggage(Object[] data) {
    return new ImmutableBaggage(data);
  }

  // TODO: Migrate to AutoValue.Builder
  // @AutoValue.Builder
  static class Builder implements BaggageBuilder {

    private final List<Object> data;

    Builder() {
      this.data = new ArrayList<>();
    }

    Builder(List<Object> data) {
      this.data = data;
    }

    @Override
    public BaggageBuilder put(String key, String value, BaggageEntryMetadata entryMetadata) {
      if (key == null) {
        ApiUsageLogger.logNullParam(BaggageBuilder.class, "put", "key");
        return this;
      }
      if (value == null) {
        ApiUsageLogger.logNullParam(BaggageBuilder.class, "put", "value");
        return this;
      }
      if (entryMetadata == null) {
        ApiUsageLogger.logNullParam(BaggageBuilder.class, "put", "entryMetadata");
        return this;
      }
      data.add(key);
      data.add(ImmutableEntry.create(value, entryMetadata));

      return this;
    }

    @Override
    public BaggageBuilder remove(String key) {
      if (key == null) {
        ApiUsageLogger.logNullParam(BaggageBuilder.class, "remove", "key");
        return this;
      }
      data.add(key);
      data.add(null);
      return this;
    }

    @Override
    public Baggage build() {
      return sortAndFilterToBaggage(data.toArray());
    }
  }
}
