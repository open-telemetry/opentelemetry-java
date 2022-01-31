/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import javax.annotation.Nullable;

class NamedSpiManager<T> {

  private final Map<String, Supplier<T>> nameToProvider;
  private final ConcurrentMap<String, Optional<T>> nameToImplementation = new ConcurrentHashMap<>();

  private NamedSpiManager(Map<String, Supplier<T>> nameToProvider) {
    this.nameToProvider = nameToProvider;
  }

  static <T> NamedSpiManager<T> create(Map<String, Supplier<T>> nameToProvider) {
    return new NamedSpiManager<>(nameToProvider);
  }

  static <T> NamedSpiManager<T> createEmpty() {
    return create(Collections.emptyMap());
  }

  @Nullable
  T getByName(String name) {
    return nameToImplementation
        .computeIfAbsent(name, this::tryLoadImplementationForName)
        .orElse(null);
  }

  private Optional<T> tryLoadImplementationForName(String name) {
    Supplier<T> provider = nameToProvider.get(name);
    if (provider == null) {
      return Optional.empty();
    }

    return Optional.ofNullable(provider.get());
  }
}
