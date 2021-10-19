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
    STRING
  }

  /**
   * Returns an {@code Body} with a string value.
   *
   * @param stringValue The new value.
   * @return a {@code Body} with a string value.
   */
  static Body stringBody(String stringValue) {
    return StringBody.create(stringValue);
  }

  /** Returns the String value of this {@code Body}. */
  String asString();

  Type getType();
}
