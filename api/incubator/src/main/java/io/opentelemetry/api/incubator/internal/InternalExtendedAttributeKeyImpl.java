/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.internal;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributeType;
import io.opentelemetry.api.incubator.common.ExtendedAttributeKey;
import io.opentelemetry.api.incubator.common.ExtendedAttributeType;
import io.opentelemetry.api.internal.InternalAttributeKeyImpl;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class InternalExtendedAttributeKeyImpl<T> implements ExtendedAttributeKey<T> {

  private final ExtendedAttributeType type;
  private final String key;
  private final int hashCode;

  @Nullable private byte[] keyUtf8;
  @Nullable private AttributeKey<T> attributeKey;

  private InternalExtendedAttributeKeyImpl(ExtendedAttributeType type, String key) {
    if (type == null) {
      throw new NullPointerException("Null type");
    }
    this.type = type;
    if (key == null) {
      throw new NullPointerException("Null key");
    }
    this.key = key;
    this.hashCode = buildHashCode(type, key);
  }

  public static <T> ExtendedAttributeKey<T> create(
      @Nullable String key, ExtendedAttributeType type) {
    return new InternalExtendedAttributeKeyImpl<>(type, key != null ? key : "");
  }

  @Override
  public ExtendedAttributeType getType() {
    return type;
  }

  @Override
  public String getKey() {
    return key;
  }

  @Nullable
  @Override
  public AttributeKey<T> asAttributeKey() {
    if (attributeKey == null) {
      attributeKey = toAttributeKey(this);
    }
    return attributeKey;
  }

  /** Returns the key, encoded as UTF-8 bytes. */
  public byte[] getKeyUtf8() {
    byte[] keyUtf8 = this.keyUtf8;
    if (keyUtf8 == null) {
      keyUtf8 = key.getBytes(StandardCharsets.UTF_8);
      this.keyUtf8 = keyUtf8;
    }
    return keyUtf8;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof InternalExtendedAttributeKeyImpl) {
      InternalExtendedAttributeKeyImpl<?> that = (InternalExtendedAttributeKeyImpl<?>) o;
      return this.type.equals(that.getType()) && this.key.equals(that.getKey());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return hashCode;
  }

  @Override
  public String toString() {
    return key;
  }

  // this method exists to make EqualsVerifier happy
  @SuppressWarnings("unused")
  private int buildHashCode() {
    return buildHashCode(type, key);
  }

  private static int buildHashCode(ExtendedAttributeType type, String key) {
    int result = 1;
    result *= 1000003;
    result ^= type.hashCode();
    result *= 1000003;
    result ^= key.hashCode();
    return result;
  }

  /**
   * Return the equivalent {@link AttributeKey} for the {@link ExtendedAttributeKey}, or {@code
   * null} if the {@link #getType()} has no equivalent {@link
   * io.opentelemetry.api.common.AttributeType}.
   */
  @Nullable
  @SuppressWarnings("deprecation") // Supporting deprecated EXTENDED_ATTRIBUTES until removed
  public static <T> AttributeKey<T> toAttributeKey(ExtendedAttributeKey<T> extendedAttributeKey) {
    switch (extendedAttributeKey.getType()) {
      case STRING:
        return InternalAttributeKeyImpl.create(extendedAttributeKey.getKey(), AttributeType.STRING);
      case BOOLEAN:
        return InternalAttributeKeyImpl.create(
            extendedAttributeKey.getKey(), AttributeType.BOOLEAN);
      case LONG:
        return InternalAttributeKeyImpl.create(extendedAttributeKey.getKey(), AttributeType.LONG);
      case DOUBLE:
        return InternalAttributeKeyImpl.create(extendedAttributeKey.getKey(), AttributeType.DOUBLE);
      case STRING_ARRAY:
        return InternalAttributeKeyImpl.create(
            extendedAttributeKey.getKey(), AttributeType.STRING_ARRAY);
      case BOOLEAN_ARRAY:
        return InternalAttributeKeyImpl.create(
            extendedAttributeKey.getKey(), AttributeType.BOOLEAN_ARRAY);
      case LONG_ARRAY:
        return InternalAttributeKeyImpl.create(
            extendedAttributeKey.getKey(), AttributeType.LONG_ARRAY);
      case DOUBLE_ARRAY:
        return InternalAttributeKeyImpl.create(
            extendedAttributeKey.getKey(), AttributeType.DOUBLE_ARRAY);
      case VALUE:
        return InternalAttributeKeyImpl.create(extendedAttributeKey.getKey(), AttributeType.VALUE);
      case EXTENDED_ATTRIBUTES:
        return null;
    }
    throw new IllegalArgumentException(
        "Unrecognized extendedAttributeKey type: " + extendedAttributeKey.getType());
  }

  /** Return the equivalent {@link ExtendedAttributeKey} for the {@link AttributeKey}. */
  public static <T> ExtendedAttributeKey<T> toExtendedAttributeKey(AttributeKey<T> attributeKey) {
    switch (attributeKey.getType()) {
      case STRING:
        return InternalExtendedAttributeKeyImpl.create(
            attributeKey.getKey(), ExtendedAttributeType.STRING);
      case BOOLEAN:
        return InternalExtendedAttributeKeyImpl.create(
            attributeKey.getKey(), ExtendedAttributeType.BOOLEAN);
      case LONG:
        return InternalExtendedAttributeKeyImpl.create(
            attributeKey.getKey(), ExtendedAttributeType.LONG);
      case DOUBLE:
        return InternalExtendedAttributeKeyImpl.create(
            attributeKey.getKey(), ExtendedAttributeType.DOUBLE);
      case STRING_ARRAY:
        return InternalExtendedAttributeKeyImpl.create(
            attributeKey.getKey(), ExtendedAttributeType.STRING_ARRAY);
      case BOOLEAN_ARRAY:
        return InternalExtendedAttributeKeyImpl.create(
            attributeKey.getKey(), ExtendedAttributeType.BOOLEAN_ARRAY);
      case LONG_ARRAY:
        return InternalExtendedAttributeKeyImpl.create(
            attributeKey.getKey(), ExtendedAttributeType.LONG_ARRAY);
      case DOUBLE_ARRAY:
        return InternalExtendedAttributeKeyImpl.create(
            attributeKey.getKey(), ExtendedAttributeType.DOUBLE_ARRAY);
      case VALUE:
        return InternalExtendedAttributeKeyImpl.create(
            attributeKey.getKey(), ExtendedAttributeType.VALUE);
    }
    throw new IllegalArgumentException("Unrecognized attributeKey type: " + attributeKey.getType());
  }
}
