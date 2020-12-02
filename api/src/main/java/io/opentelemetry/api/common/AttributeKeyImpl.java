/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;

@SuppressWarnings("rawtypes")
@AutoValue
abstract class AttributeKeyImpl<T> implements AttributeKey<T> {

  static <T> AttributeKeyImpl<T> create(String key, AttributeType type) {
    return new AutoValue_AttributeKeyImpl<>(type, key);
  }

  @Override
  public String getKey() {
    return key();
  }

  @Nullable
  abstract String key();

  @Override
  public final String toString() {
    return getKey();
  }

  //////////////////////////////////
  // IMPORTANT: the equals/hashcode/compareTo *only* include the key, and not the type,
  // so that de-duping of attributes is based on the key, and not also based on the type.
  //////////////////////////////////

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AttributeKeyImpl)) {
      return false;
    }

    AttributeKeyImpl<?> that = (AttributeKeyImpl<?>) o;

    return getKey() != null ? getKey().equals(that.getKey()) : that.getKey() == null;
  }

  @Override
  public final int hashCode() {
    return getKey() != null ? getKey().hashCode() : 0;
  }

  @Override
  public int compareTo(AttributeKey o) {
    if (getKey() == null) {
      return o.getKey() == null ? 0 : -1;
    }
    if (o.getKey() == null) {
      return 1;
    }
    return getKey().compareTo(o.getKey());
  }
}
