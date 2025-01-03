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
   * IMPORTANT: complex valued attributes are only supported by Logs. Spans and Metrics do not
   * support complex valued attributes.
   */
  COMPLEX
}
