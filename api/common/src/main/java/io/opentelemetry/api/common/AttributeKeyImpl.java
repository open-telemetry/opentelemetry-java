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
}
