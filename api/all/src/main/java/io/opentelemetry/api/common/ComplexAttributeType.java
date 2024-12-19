/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

/**
 * An enum that represents all the possible value types for an {@code ComplexAttributeKey} and hence
 * the types of values that are allowed for {@link ComplexAttributes}.
 */
public enum ComplexAttributeType {
  STRING,
  BOOLEAN,
  LONG,
  DOUBLE,
  STRING_ARRAY,
  BOOLEAN_ARRAY,
  LONG_ARRAY,
  DOUBLE_ARRAY,
  COMPLEX
}
