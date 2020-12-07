/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.internal.ImmutableKeyValuePairs;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import javax.annotation.concurrent.Immutable;

@AutoValue
@Immutable
abstract class ArrayBackedAttributes extends ImmutableKeyValuePairs<AttributeKey<?>, Object>
    implements Attributes {

  static final Attributes EMPTY = Attributes.builder().build();

  ArrayBackedAttributes() {}

  @Override
  protected abstract List<Object> data();

  @Override
  public AttributesBuilder toBuilder() {
    return new ArrayBackedAttributesBuilder(new ArrayList<>(data()));
  }

  @SuppressWarnings("unchecked")
  @Override
  public void forEach(BiConsumer<AttributeKey<?>, Object> consumer) {
    List<Object> data = data();
    for (int i = 0; i < data.size(); i += 2) {
      consumer.accept((AttributeKey<?>) data.get(i), data.get(i + 1));
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T get(AttributeKey<T> key) {
    return (T) super.get(key);
  }

  static Attributes sortAndFilterToAttributes(Object... data) {
    // null out any empty keys or keys with null values
    // so they will then be removed by the sortAndFilter method.
    for (int i = 0; i < data.length; i += 2) {
      AttributeKey<?> key = (AttributeKey<?>) data[i];
      if (key != null && (key.getKey() == null || "".equals(key.getKey()))) {
        data[i] = null;
      }
    }
    return new AutoValue_ArrayBackedAttributes(sortAndFilter(data, /* filterNullValues= */ true));
  }
}
