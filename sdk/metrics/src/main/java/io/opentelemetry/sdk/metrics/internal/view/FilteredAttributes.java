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

/**
 * Filtered attributes is a filtered view of a {@link ImmutableKeyValuePairs} backed {@link
 * Attributes} instance. Rather than creating an entirely new attributes instance, it keeps track of
 * which source attributes are excluded while implementing the {@link Attributes} interface.
 *
 * <p>Notably, the {@link FilteredAttributes#equals(Object)} and {@link
 * FilteredAttributes#hashCode()} depend on comparison against other {@link FilteredAttributes}
 * instances. This means that where {@link FilteredAttributes} is used for things like map keys, it
 * must be used for all keys in that map. You cannot mix {@link Attributes} implementations. This is
 * also true for the default attributes implementation.
 */
@SuppressWarnings("unchecked") // lots of generic casting of AttributeKey<?>
abstract class FilteredAttributes implements Attributes {

  // Backing source data from ImmutableKeyValuePairs.data. This array MUST NOT be mutated.
  private final Object[] sourceData;
  private final int hashcode;
  private final int size;

  private FilteredAttributes(Object[] sourceData, int hashcode, int size) {
    this.sourceData = sourceData;
    this.hashcode = hashcode;
    this.size = size;
  }

  /**
   * Create a {@link FilteredAttributes} instance.
   *
   * @param source the source attributes, which SHOULD be based on the standard {@link
   *     ImmutableKeyValuePairs}. If not, the source will first be converted to the standard
   *     implementation.
   * @param includedKeys the set of attribute keys to include in the output.
   */
  @SuppressWarnings("NullAway")
  static Attributes create(Attributes source, Set<AttributeKey<?>> includedKeys) {
    // Convert alternative implementations of Attributes to standard implementation.
    // This is required for proper functioning of equals and hashcode.
    if (!(source instanceof ImmutableKeyValuePairs)) {
      source = convertToStandardImplementation(source);
    }
    if (!(source instanceof ImmutableKeyValuePairs)) {
      throw new IllegalStateException(
          "Expected ImmutableKeyValuePairs based implementation of Attributes. This is a programming error.");
    }
    // Compute filteredIndices (and filteredIndicesBitSet if needed) during initialization. Compute
    // hashcode at the same time to avoid iteration later.
    Object[] sourceData = ((ImmutableKeyValuePairs<?, ?>) source).getData();
    int filteredIndices = 0;
    BitSet filteredIndicesBitSet =
        source.size() > SmallFilteredAttributes.BITS_PER_INTEGER ? new BitSet(source.size()) : null;
    int hashcode = 1;
    int size = 0;
    for (int i = 0; i < sourceData.length; i += 2) {
      int filterIndex = i / 2;
      // If the sourceData key isn't present in includedKeys, record the exclusion in
      // filteredIndices or filteredIndicesBitSet (depending on size)
      if (!includedKeys.contains(sourceData[i])) {
        // Record
        if (filteredIndicesBitSet != null) {
          filteredIndicesBitSet.set(filterIndex);
        } else {
          filteredIndices = filteredIndices | (1 << filterIndex);
        }
      } else { // The key-value is included in the output, record in the hashcode and size.
        hashcode = 31 * hashcode + sourceData[i].hashCode();
        hashcode = 31 * hashcode + sourceData[i + 1].hashCode();
        size++;
      }
    }
    // If size is 0, short circuit and return Attributes.empty()
    if (size == 0) {
      return Attributes.empty();
    }
    return filteredIndicesBitSet != null
        ? new RegularFilteredAttributes(sourceData, hashcode, size, filteredIndicesBitSet)
        : new SmallFilteredAttributes(sourceData, hashcode, size, filteredIndices);
  }

  /**
   * Implementation that relies on the source having less than {@link #BITS_PER_INTEGER} attributes,
   * and storing entry filter status in the bits of an integer.
   */
  private static class SmallFilteredAttributes extends FilteredAttributes {

    private static final int BITS_PER_INTEGER = 32;

    private final int filteredIndices;

    private SmallFilteredAttributes(
        Object[] sourceData, int hashcode, int size, int filteredIndices) {
      super(sourceData, hashcode, size);
      this.filteredIndices = filteredIndices;
    }

    @Override
    boolean includeIndexInOutput(int sourceIndex) {
      return (filteredIndices & (1 << (sourceIndex / 2))) == 0;
    }
  }

  /**
   * Implementation that can handle attributes of arbitrary size by storing filter status in a
   * {@link BitSet}.
   */
  private static class RegularFilteredAttributes extends FilteredAttributes {

    private final BitSet bitSet;

    private RegularFilteredAttributes(Object[] sourceData, int hashcode, int size, BitSet bitSet) {
      super(sourceData, hashcode, size);
      this.bitSet = bitSet;
    }

    @Override
    boolean includeIndexInOutput(int sourceIndex) {
      return !bitSet.get(sourceIndex / 2);
    }
  }

  private static Attributes convertToStandardImplementation(Attributes source) {
    AttributesBuilder builder = Attributes.builder();
    source.forEach(
        (key, value) -> putInBuilder(builder, (AttributeKey<? super Object>) key, value));
    return builder.build();
  }

  @Nullable
  @Override
  public <T> T get(AttributeKey<T> key) {
    if (key == null) {
      return null;
    }
    for (int i = 0; i < sourceData.length; i += 2) {
      if (key.equals(sourceData[i]) && includeIndexInOutput(i)) {
        return (T) sourceData[i + 1];
      }
    }
    return null;
  }

  @Override
  public void forEach(BiConsumer<? super AttributeKey<?>, ? super Object> consumer) {
    for (int i = 0; i < sourceData.length; i += 2) {
      if (includeIndexInOutput(i)) {
        consumer.accept((AttributeKey<?>) sourceData[i], sourceData[i + 1]);
      }
    }
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean isEmpty() {
    // #create short circuits and returns Attributes.empty() if empty, so FilteredAttributes is
    // never empty
    return false;
  }

  @Override
  public Map<AttributeKey<?>, Object> asMap() {
    Map<AttributeKey<?>, Object> result = new LinkedHashMap<>(size);
    for (int i = 0; i < sourceData.length; i += 2) {
      if (includeIndexInOutput(i)) {
        result.put((AttributeKey<?>) sourceData[i], sourceData[i + 1]);
      }
    }
    return Collections.unmodifiableMap(result);
  }

  @Override
  public AttributesBuilder toBuilder() {
    AttributesBuilder builder = Attributes.builder();
    for (int i = 0; i < sourceData.length; i += 2) {
      if (includeIndexInOutput(i)) {
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
    // We require other object to also be instances of FilteredAttributes. In other words, where one
    // FilteredAttributes is used for a key in a map, it must be used for all the keys. Note, this
    // same requirement exists for the default Attributes implementation - you can not mix
    // implementations.
    if (object == null || !(object instanceof FilteredAttributes)) {
      return false;
    }

    FilteredAttributes that = (FilteredAttributes) object;
    // exit early if sizes are not equal
    if (size() != that.size()) {
      return false;
    }
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
      if (!thisDone && !this.includeIndexInOutput(thisIndex)) {
        thisIndex += 2;
        continue;
      }
      if (!thatDone && !that.includeIndexInOutput(thatIndex)) {
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
      // if we make it here, both thisIndex and thatIndex within bounds and are included in the
      // output. the current
      // key and value and this and that must be equal for this and that to be equal.
      if (!Objects.equals(this.sourceData[thisIndex], that.sourceData[thatIndex])
          || !Objects.equals(this.sourceData[thisIndex + 1], that.sourceData[thatIndex + 1])) {
        return false;
      }
      thisIndex += 2;
      thatIndex += 2;
    } while (true);
    // if we make it here without exiting early, all elements of this and that are equal
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
      if (includeIndexInOutput(i)) {
        joiner.add(((AttributeKey<?>) sourceData[i]).getKey() + "=" + sourceData[i + 1]);
      }
    }
    return joiner.toString();
  }

  abstract boolean includeIndexInOutput(int sourceIndex);
}
