/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

/**
 * An enum that represents all the possible value types for an {@code AttributeKey} and hence the
 * types of values that are allowed for {@link Attributes}.
 */
public enum AttributeType {
  STRING,
  BOOLEAN,
  LONG,
  DOUBLE,
  STRING_ARRAY,
  BOOLEAN_ARRAY,
  LONG_ARRAY,
  DOUBLE_ARRAY;

  /**
   * Returns whether {@code this} is a primitive type or not.
   *
   * @return {@code true} if primitive, otherwise {@code false}
   */
  public boolean isPrimitive() {
    if (AttributeType.STRING.equals(this)) {
      return true;
    } else if (AttributeType.BOOLEAN.equals(this)) {
      return true;
    } else if (AttributeType.LONG.equals(this)) {
      return true;
    } else if (AttributeType.DOUBLE.equals(this)) {
      return true;
    } else if (AttributeType.STRING_ARRAY.equals(this)) {
      return false;
    } else if (AttributeType.BOOLEAN_ARRAY.equals(this)) {
      return false;
    } else if (AttributeType.LONG_ARRAY.equals(this)) {
      return false;
    } else if (AttributeType.DOUBLE_ARRAY.equals(this)) {
      return false;
    } else {
      throw new IllegalStateException(("Unrecognized attribute type: " + this));
    }
  }
}
