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
  DOUBLE_ARRAY,
  /**
   * Simple attributes ({@link AttributeType#STRING}, {@link AttributeType#LONG}, {@link
   * AttributeType#DOUBLE}, {@link AttributeType#BOOLEAN}, {@link AttributeType#STRING_ARRAY},
   * {@link AttributeType#LONG_ARRAY}, {@link AttributeType#DOUBLE_ARRAY}, {@link
   * AttributeType#BOOLEAN_ARRAY}) SHOULD be used whenever possible. Instrumentations SHOULD assume
   * that backends do not index individual properties of complex attributes, that querying or
   * aggregating on such properties is inefficient and complicated, and that reporting complex
   * attributes carries higher performance overhead.
   */
  VALUE
}
