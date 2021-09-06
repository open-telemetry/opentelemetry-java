/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributeType;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;

/** Default AttributeKey implementation which preencodes to UTF8 for OTLP export. */
public final class InternalAttributeKeyImpl<T> implements AttributeKey<T> {

  private final AttributeType type;
  private final String key;
  private final int hashCode;

  private byte[] keyUtf8;

  private InternalAttributeKeyImpl(AttributeType type, String key) {
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

  // Used by auto-instrumentation agent. Check with auto-instrumentation before making changes to
  // this method.
  //
  // In particular, do not change this return type to AttributeKeyImpl because auto-instrumentation
  // hijacks this method and returns a bridged implementation of Context.
  //
  // Ideally auto-instrumentation would hijack the public AttributeKey.*Key() instead of this
  // method, but auto-instrumentation also needs to inject its own implementation of AttributeKey
  // into the class loader at the same time, which causes a problem because injecting a class into
  // the class loader automatically resolves its super classes (interfaces), which in this case is
  // Context, which would be the same class (interface) being instrumented at that time,
  // which would lead to the JVM throwing a LinkageError "attempted duplicate interface definition"
  public static <T> AttributeKey<T> create(@Nullable String key, AttributeType type) {
    return new InternalAttributeKeyImpl<>(type, key != null ? key : "");
  }

  @Override
  public AttributeType getType() {
    return type;
  }

  @Override
  public String getKey() {
    return key;
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
    if (o instanceof InternalAttributeKeyImpl) {
      InternalAttributeKeyImpl<?> that = (InternalAttributeKeyImpl<?>) o;
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

  private static int buildHashCode(AttributeType type, String key) {
    int result = 1;
    result *= 1000003;
    result ^= type.hashCode();
    result *= 1000003;
    result ^= key.hashCode();
    return result;
  }
}
