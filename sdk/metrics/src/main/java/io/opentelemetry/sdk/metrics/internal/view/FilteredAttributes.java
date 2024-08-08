/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.internal.ImmutableKeyValuePairs;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
final class FilteredAttributes implements Attributes {

  private final Object[] sourceData;
  private final List<Integer> filteredIndices;
  private final Set<AttributeKey<?>> keys;
  private int hashcode;

  private FilteredAttributes(
      ImmutableKeyValuePairs<AttributeKey<?>, Object> source, Set<AttributeKey<?>> keys) {
    this.keys = keys;
    this.sourceData = source.getData();
    filteredIndices = new ArrayList<>(source.size() - keys.size());
    for (int i = 0; i < sourceData.length; i += 2) {
      if (!keys.contains(sourceData[i])) {
        filteredIndices.add(i);
      }
    }
  }

  static Attributes create(Attributes source, Set<AttributeKey<?>> keys) {
    if (keys.isEmpty()) {
      return source;
    }
    if (!hasExtraKeys(source, keys)) {
      return source;
    }
    if (source instanceof ImmutableKeyValuePairs) {
      return new FilteredAttributes((ImmutableKeyValuePairs<AttributeKey<?>, Object>) source, keys);
    }
    AttributesBuilder builder = source.toBuilder();
    builder.removeIf(key -> !keys.contains(key));
    return builder.build();
  }

  /** Returns true if {@code attributes} has keys not contained in {@code keys}. */
  private static boolean hasExtraKeys(Attributes attributes, Set<AttributeKey<?>> keys) {
    if (attributes.size() > keys.size()) {
      return true;
    }
    boolean[] result = {false};
    attributes.forEach(
        (key, value) -> {
          if (!result[0] && !keys.contains(key)) {
            result[0] = true;
          }
        });
    return result[0];
  }

  @Nullable
  @Override
  public <T> T get(AttributeKey<T> key) {
    if (key == null) {
      return null;
    }
    for (int i = 0; i < sourceData.length; i += 2) {
      if (key.equals(sourceData[i]) && keys.contains(sourceData[i])) {
        return (T) sourceData[i + 1];
      }
    }
    return null;
  }

  @Override
  public void forEach(BiConsumer<? super AttributeKey<?>, ? super Object> consumer) {
    for (int i = 0; i < sourceData.length; i += 2) {
      if (keys.contains(sourceData[i])) {
        consumer.accept((AttributeKey<?>) sourceData[i], sourceData[i + 1]);
      }
    }
  }

  @Override
  public int size() {
    return sourceData.length / 2 - filteredIndices.size();
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
    Map<AttributeKey<?>, Object> result = new HashMap<>(size);
    for (int i = 0; i < sourceData.length; i += 2) {
      if (keys.contains(sourceData[i])) {
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
      if (keys.contains(sourceData[i])) {
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
    if (object == null || getClass() != object.getClass()) {
      return false;
    }

    // TODO: valid equals implementation when object isn't FilteredAttributes, or ensure no short
    // circuiting to other types in FilteredAttributes.create
    FilteredAttributes that = (FilteredAttributes) object;
    int size = size();
    if (size != that.size()) {
      return false;
    } else if (size == 0) {
      return true;
    }

    // compare each non-filtered key / value pair from this to that.
    // depends on the entries from the backing ImmutableKeyValuePairs being sorted.
    // sacrifice readability for performance because this is on the hotpath
    int thisIndex = 0;
    int thatIndex = 0;
    int thisFilterIndex = 0;
    int thisFilterIndexValue = nextFilteredIndex(thisFilterIndex, this);
    int thatFilterIndex = 0;
    int thatFilterIndexValue = nextFilteredIndex(thatFilterIndex, that);
    boolean thisDone;
    boolean thatDone;
    do {
      thisDone = thisIndex >= this.sourceData.length;
      thatDone = thatIndex >= that.sourceData.length;
      // advance to next unfiltered key value pair for this and that
      if (!thisDone && thisFilterIndexValue != -1 && thisIndex == thisFilterIndexValue) {
        thisFilterIndexValue = nextFilteredIndex(thisFilterIndex++, this);
        thisIndex += 2;
        continue;
      }
      if (!thatDone && thatFilterIndexValue != -1 && thatIndex == thatFilterIndexValue) {
        thisFilterIndexValue = nextFilteredIndex(thatFilterIndex++, this);
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

  private static int nextFilteredIndex(int index, FilteredAttributes filteredAttributes) {
    return filteredAttributes.filteredIndices.size() > index
        ? filteredAttributes.filteredIndices.get(index)
        : -1;
  }

  @Override
  public int hashCode() {
    // memoize the hashcode to avoid comparatively expensive recompute
    int result = hashcode;
    if (result == 0) {
      result = 1;
      for (int i = 0; i < sourceData.length; i += 2) {
        if (keys.contains(sourceData[i])) {
          result = 31 * result + sourceData[i].hashCode();
          result = 31 * result + sourceData[i + 1].hashCode();
        }
      }
      hashcode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringJoiner joiner = new StringJoiner(",", "FilteredAttributes{", "}");
    for (int i = 0; i < sourceData.length; i += 2) {
      if (keys.contains(sourceData[i])) {
        joiner.add(((AttributeKey<?>) sourceData[i]).getKey() + "=" + sourceData[i + 1]);
      }
    }
    return joiner.toString();
  }
}
