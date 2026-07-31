/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.common.Value;
import javax.annotation.concurrent.Immutable;

/**
 * Limits enforced by an {@link AttributesBuilder} created via {@link
 * LimitedAttributes#builder(AttributeLimits)}.
 *
 * <p>The three parameters correspond to the {@code AttributeCountLimit}, {@code
 * AttributeValueLengthLimit}, and {@code AttributeValueDepthLimit} configurable parameters in the
 * OpenTelemetry <a
 * href="https://github.com/open-telemetry/opentelemetry-specification/tree/main/specification/common#attribute-limits">common
 * attribute-limits</a> specification.
 */
@AutoValue
@Immutable
public abstract class AttributeLimits {

  /** Returns a new {@link AttributeLimitsBuilder} initialized to spec-recommended defaults. */
  public static AttributeLimitsBuilder builder() {
    return new AttributeLimitsBuilder();
  }

  static AttributeLimits create(int countLimit, int valueLengthLimit, int valueDepthLimit) {
    return new AutoValue_AttributeLimits(countLimit, valueLengthLimit, valueDepthLimit);
  }

  AttributeLimits() {}

  /**
   * Returns the maximum number of unique attribute keys ({@code AttributeCountLimit}). Additional
   * entries with new key names are dropped once the limit is reached. Overwrites of existing keys
   * do not consume against the limit.
   *
   * <p>{@link Integer#MAX_VALUE} means no count limit.
   */
  public abstract int getCountLimit();

  /**
   * Returns the maximum length for string and byte-array attribute values ({@code
   * AttributeValueLengthLimit}). Longer values are truncated to this length. Applies recursively to
   * string and byte-array values within {@link Value}-typed and array attributes.
   *
   * <p>{@link Integer#MAX_VALUE} means no length limit.
   */
  public abstract int getValueLengthLimit();

  /**
   * Returns the maximum nesting depth for array and map attribute values ({@code
   * AttributeValueDepthLimit}). Depth counting starts at 1 for the top-level attribute value and
   * increments when descending into array elements or map values. Arrays and maps at a depth
   * greater than this limit are replaced with an empty container of the same shape.
   *
   * <p>{@link Integer#MAX_VALUE} means no depth limit.
   */
  public abstract int getValueDepthLimit();
}
