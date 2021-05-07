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
  static <T> AttributeKey<T> create(@Nullable String key, AttributeType type) {
    return new AutoValue_AttributeKeyImpl<>(type, key != null ? key : "");
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
