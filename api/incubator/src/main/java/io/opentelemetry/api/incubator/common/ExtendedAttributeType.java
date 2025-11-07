/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.common;

/**
 * An enum that represents all the possible value types for an {@link ExtendedAttributeKey} and
 * hence the types of values that are allowed for {@link ExtendedAttributes}.
 *
 * <p>This is a superset of {@link io.opentelemetry.api.common.AttributeType},
 */
public enum ExtendedAttributeType {
  // Types copied AttributeType
  STRING,
  BOOLEAN,
  LONG,
  DOUBLE,
  STRING_ARRAY,
  BOOLEAN_ARRAY,
  LONG_ARRAY,
  DOUBLE_ARRAY,
  // Extended types unique to ExtendedAttributes
  /**
   * Complex attribute type for {@link io.opentelemetry.api.common.Value}-based maps.
   *
   * @deprecated Use {@link #VALUE} with {@link io.opentelemetry.api.common.Value}-based maps
   *     instead.
   */
  @Deprecated
  EXTENDED_ATTRIBUTES,
  /**
   * Simple attributes ({@link AttributeType#STRING}, {@link AttributeType#LONG}, {@link
   * AttributeType#DOUBLE}, {@link AttributeType#BOOLEAN}, {@link AttributeType#STRING_ARRAY},
   * {@link AttributeType#LONG_ARRAY}, {@link AttributeType#DOUBLE_ARRAY}, {@link
   * AttributeType#BOOLEAN_ARRAY}) SHOULD be used whenever possible. Instrumentations SHOULD assume
   * that backends do not index individual properties of complex attributes, that querying or
   * aggregating on such properties is inefficient and complicated, and that reporting complex
   * attributes carries higher performance overhead.
   */
  VALUE;
}
