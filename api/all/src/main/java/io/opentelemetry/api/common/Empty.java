/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

/**
 * A singleton class representing an empty value, used as the generic type parameter for {@link
 * Value} when representing empty values.
 */
public final class Empty {

  private static final Empty INSTANCE = new Empty();

  private Empty() {}

  public static Empty getInstance() {
    return INSTANCE;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Empty;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public String toString() {
    return "Empty";
  }
}
