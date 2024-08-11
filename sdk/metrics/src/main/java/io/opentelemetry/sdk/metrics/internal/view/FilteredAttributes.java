/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.internal.ImmutableKeyValuePairs;
import java.util.BitSet;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
final class FilteredAttributes implements Attributes {

  private static final int BITS_PER_INTEGER = 32;

  private final int filteredIndices;
  @Nullable private final BitSet overflowFilteredIndices;
  private final Object[] sourceData;
  private final int hashcode;

  @SuppressWarnings("NullAway")
  private FilteredAttributes(
      ImmutableKeyValuePairs<AttributeKey<?>, Object> source, Set<AttributeKey<?>> keys) {
    this.sourceData = source.getData();
    int filteredIndices = 0;
    this.overflowFilteredIndices =
        source.size() > BITS_PER_INTEGER ? new BitSet(source.size() - BITS_PER_INTEGER) : null;
    // Record the indices to filter.
    // Compute hashcode inline to avoid iteration later
    int hashcode = 1;
    for (int i = 0; i < sourceData.length; i += 2) {
      int filterIndex = i / 2;
      if (!keys.contains(sourceData[i])) {
        if (filterIndex < BITS_PER_INTEGER) {
          filteredIndices = filteredIndices | (1 << filterIndex);
        } else {
          overflowFilteredIndices.set(filterIndex - BITS_PER_INTEGER);
        }
      } else {
        hashcode = 31 * hashcode + sourceData[i].hashCode();
        hashcode = 31 * hashcode + sourceData[i + 1].hashCode();
      }
    }
    this.filteredIndices = filteredIndices;
    this.hashcode = hashcode;
  }

  static Attributes create(Attributes source, Set<AttributeKey<?>> keys) {
    if (keys.isEmpty()) {
      return source;
    }
    if (!(source instanceof ImmutableKeyValuePairs)) {
      // convert alternative implementations of attributes to standard implementation and wrap with
      // FilteredAttributes
      // this is required for proper funcitoning of equals and hashcode
      AttributesBuilder builder = Attributes.builder();
      source.forEach(
          (key, value) -> putInBuilder(builder, (AttributeKey<? super Object>) key, value));
      source = builder.build();
    }
    if (!(source instanceof ImmutableKeyValuePairs)) {
      throw new IllegalStateException(
          "Expected ImmutableKeyValuePairs based implementation of Attributes. This is a programming error.");
    }
    return new FilteredAttributes((ImmutableKeyValuePairs<AttributeKey<?>, Object>) source, keys);
  }

  @Nullable
  @Override
  public <T> T get(AttributeKey<T> key) {
    if (key == null) {
      return null;
    }
    for (int i = 0; i < sourceData.length; i += 2) {
      if (key.equals(sourceData[i]) && !get(i / 2)) {
        return (T) sourceData[i + 1];
      }
    }
    return null;
  }

  @Override
  public void forEach(BiConsumer<? super AttributeKey<?>, ? super Object> consumer) {
    for (int i = 0; i < sourceData.length; i += 2) {
      if (!get(i / 2)) {
        consumer.accept((AttributeKey<?>) sourceData[i], sourceData[i + 1]);
      }
    }
  }

  @Override
  public int size() {
    return sourceData.length / 2 - cardinality();
  }

  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  @Override
  public Map<AttributeKey<?>, Object> asMap() {
    int size = size();
    if (size == 0) {
      return Collections.emptyMap();
    }
    Map<AttributeKey<?>, Object> result = new LinkedHashMap<>(size);
    for (int i = 0; i < sourceData.length; i += 2) {
      if (!get(i / 2)) {
        result.put((AttributeKey<?>) sourceData[i], sourceData[i + 1]);
      }
    }
    return Collections.unmodifiableMap(result);
  }

  @Override
  public AttributesBuilder toBuilder() {
    AttributesBuilder builder = Attributes.builder();
    int size = size();
    if (size == 0) {
      return builder;
    }
    for (int i = 0; i < sourceData.length; i += 2) {
      if (!get(i / 2)) {
        putInBuilder(builder, (AttributeKey<? super Object>) sourceData[i], sourceData[i + 1]);
      }
    }
    return builder;
  }

  private static <T> void putInBuilder(AttributesBuilder builder, AttributeKey<T> key, T value) {
    builder.put(key, value);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    }
    // We require other object to also be instance of FilteredAttributes. In other words, where one
    // FilteredAttributes is used for a key in a map, it must be used for all the keys. Note, this
    // same requirement exists for the default Attributes implementation - you can not mix
    // implementations.
    if (object == null || getClass() != object.getClass()) {
      return false;
    }

    FilteredAttributes that = (FilteredAttributes) object;
    // Compare each non-filtered key / value pair from this to that.
    // Depends on the entries from the backing ImmutableKeyValuePairs being sorted.
    int thisIndex = 0;
    int thatIndex = 0;
    boolean thisDone;
    boolean thatDone;
    do {
      thisDone = thisIndex >= this.sourceData.length;
      thatDone = thatIndex >= that.sourceData.length;
      // advance to next unfiltered key value pair for this and that
      if (!thisDone && this.get(thisIndex / 2)) {
        thisIndex += 2;
        continue;
      }
      if (!thatDone && that.get(thatIndex / 2)) {
        thatIndex += 2;
        continue;
      }
      // if we're done iterating both this and that, we exit and return true since these are equal
      if (thisDone && thatDone) {
        break;
      }
      // if either this or that is done iterating, but not both, these are not equal
      if (thisDone != thatDone) {
        return false;
      }
      // if we make it here, both thisIndex and thatIndex are unfiltered and are within the bounds
      // of their respective sourceData. the current key and value and this and that must be equal
      // for this and that to be equal.
      if (!Objects.equals(this.sourceData[thisIndex], that.sourceData[thatIndex])
          || !Objects.equals(this.sourceData[thisIndex + 1], that.sourceData[thatIndex + 1])) {
        return false;
      }
      thisIndex += 2;
      thatIndex += 2;
    } while (true);

    return true;
  }

  @Override
  public int hashCode() {
    return hashcode;
  }

  @Override
  public String toString() {
    StringJoiner joiner = new StringJoiner(",", "FilteredAttributes{", "}");
    for (int i = 0; i < sourceData.length; i += 2) {
      if (!get(i / 2)) {
        joiner.add(((AttributeKey<?>) sourceData[i]).getKey() + "=" + sourceData[i + 1]);
      }
    }
    return joiner.toString();
  }

  @SuppressWarnings("NullAway")
  private boolean get(int bitIndex) {
    if (bitIndex < BITS_PER_INTEGER) {
      return (filteredIndices & (1 << bitIndex)) != 0;
    }
    return overflowFilteredIndices.get(bitIndex - BITS_PER_INTEGER);
  }

  private int cardinality() {
    return Integer.bitCount(filteredIndices)
        + (overflowFilteredIndices == null ? 0 : overflowFilteredIndices.cardinality());
  }
}
