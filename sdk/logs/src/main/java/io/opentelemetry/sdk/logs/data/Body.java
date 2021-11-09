/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data;

import javax.annotation.concurrent.Immutable;

/**
 * This represents all the possible values for a log message body. A {@code Body} can currently only
 * have 1 type of values: {@code String}, represented through {@code Body.Type}. This class will
 * likely be extended in the future to include additional body types supported by the OpenTelemetry
 * log data model.
 */
@Immutable
public interface Body {

  /** An enum that represents all the possible value types for an {@code Body}. */
  enum Type {
    EMPTY,
    STRING
  }

  /**
   * Returns an {@code Body} with a string value.
   *
   * @param stringValue The new value.
   * @return a {@code Body} with a string value.
   */
  static Body string(String stringValue) {
    return StringBody.create(stringValue);
  }

  /**
   * Return an empty {@code Body}.
   *
   * @return a {@code Body} without a value.
   */
  static Body empty() {
    return EmptyBody.INSTANCE;
  }

  /** Returns the String value of this {@code Body}. */
  String asString();

  /** Returns the type of the {@code Body}. */
  Type getType();
}
