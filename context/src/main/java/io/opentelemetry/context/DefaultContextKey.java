/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

final class DefaultContextKey<T> implements ContextKey<T> {

  private final String name;

  DefaultContextKey(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
