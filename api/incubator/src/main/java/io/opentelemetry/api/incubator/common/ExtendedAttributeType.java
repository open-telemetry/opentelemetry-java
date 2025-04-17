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
  EXTENDED_ATTRIBUTES;
}
