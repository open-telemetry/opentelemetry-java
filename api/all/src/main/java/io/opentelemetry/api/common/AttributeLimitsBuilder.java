/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import static io.opentelemetry.api.internal.Utils.checkArgument;

/** Builder for {@link AttributeLimits}. */
public final class AttributeLimitsBuilder {

  private int countLimit = Integer.MAX_VALUE;
  private int valueLengthLimit = Integer.MAX_VALUE;

  // TODO(jack-berg): before merging, decide whether to default this to 64 (spec-recommended,
  // matches System.Text.Json, provides stack safety when callers set a length limit but forget
  // depth). Since depth is net new we can pick a non-infinite default without breaking anyone;
  // count and length must stay at Integer.MAX_VALUE for back-compat. Would diverge builder
  // defaults from AttributeLimits.noLimits().
  private int valueDepthLimit = Integer.MAX_VALUE;

  AttributeLimitsBuilder() {}

  /**
   * Sets the maximum number of unique attribute keys ({@code AttributeCountLimit}). Additional
   * entries with new key names are dropped once the limit is reached. Overwrites of existing keys
   * do not consume against the limit.
   *
   * @param countLimit non-negative maximum, or {@link Integer#MAX_VALUE} for no limit
   * @throws IllegalArgumentException if {@code countLimit} is negative
   */
  public AttributeLimitsBuilder setCountLimit(int countLimit) {
    checkArgument(countLimit >= 0, "countLimit must be non-negative");
    this.countLimit = countLimit;
    return this;
  }

  /**
   * Sets the maximum length for string and byte-array attribute values ({@code
   * AttributeValueLengthLimit}). Applies recursively to string and byte-array values within {@link
   * Value}-typed and array attributes.
   *
   * @param valueLengthLimit non-negative maximum, or {@link Integer#MAX_VALUE} for no limit
   * @throws IllegalArgumentException if {@code valueLengthLimit} is negative
   */
  public AttributeLimitsBuilder setValueLengthLimit(int valueLengthLimit) {
    checkArgument(valueLengthLimit >= 0, "valueLengthLimit must be non-negative");
    this.valueLengthLimit = valueLengthLimit;
    return this;
  }

  /**
   * Sets the maximum nesting depth for array and map attribute values ({@code
   * AttributeValueDepthLimit}). Depth is 1-indexed (top-level attribute value = depth 1); the limit
   * must therefore be at least 1. Arrays and maps at a depth greater than the limit are replaced
   * with an empty container of the same shape.
   *
   * @param valueDepthLimit maximum nesting depth, minimum 1, or {@link Integer#MAX_VALUE} for no
   *     limit
   * @throws IllegalArgumentException if {@code valueDepthLimit} is less than 1
   */
  public AttributeLimitsBuilder setValueDepthLimit(int valueDepthLimit) {
    checkArgument(valueDepthLimit >= 1, "valueDepthLimit must be at least 1");
    this.valueDepthLimit = valueDepthLimit;
    return this;
  }

  /** Builds the {@link AttributeLimits}. */
  public AttributeLimits build() {
    return AttributeLimits.create(countLimit, valueLengthLimit, valueDepthLimit);
  }
}
