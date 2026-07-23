/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;

/**
 * Limits enforced by an {@link AttributesBuilder} created via {@link
 * Attributes#builder(AttributeLimits)}.
 *
 * <p>A builder configured with limits applies last-value-wins semantics on {@link
 * AttributesBuilder#put put} (by {@link AttributeKey#getKey() key name}, regardless of {@link
 * AttributeType}), truncates over-length string and byte values, replaces over-nested array and map
 * values with empty containers, and drops entries added beyond the configured count limit. This
 * differs from the default builder ({@link Attributes#builder()}) which defers de-duplication to
 * {@link AttributesBuilder#build()} and applies no truncation, depth, or count limits.
 *
 * <p>The three parameters correspond to the {@code AttributeCountLimit}, {@code
 * AttributeValueLengthLimit}, and {@code AttributeValueDepthLimit} configurable parameters in the
 * OpenTelemetry <a
 * href="https://github.com/open-telemetry/opentelemetry-specification/tree/main/specification/common#attribute-limits">common
 * attribute-limits</a> specification.
 *
 * <p>Use {@link #noLimits()} to represent an unbounded configuration and {@link #builder()} to
 * construct a bounded one.
 *
 * @since 1.64.0
 */
public abstract class AttributeLimits {

  private static final AttributeLimits NO_LIMITS = new AttributeLimitsBuilder().build();

  /** Returns an {@link AttributeLimits} that imposes no count, length, or depth limits. */
  public static AttributeLimits noLimits() {
    return NO_LIMITS;
  }

  /** Returns a new {@link AttributeLimitsBuilder} initialized to {@link #noLimits()}. */
  public static AttributeLimitsBuilder builder() {
    return new AttributeLimitsBuilder();
  }

  static AttributeLimits create(int countLimit, int valueLengthLimit, int valueDepthLimit) {
    return new AutoValue_AttributeLimits_AttributeLimitsValue(
        countLimit, valueLengthLimit, valueDepthLimit);
  }

  /** Package-private constructor to prevent subclassing. */
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

  /**
   * Returns an {@link AttributeLimitsBuilder} initialized to the same property values as this
   * instance.
   */
  public AttributeLimitsBuilder toBuilder() {
    return builder()
        .setCountLimit(getCountLimit())
        .setValueLengthLimit(getValueLengthLimit())
        .setValueDepthLimit(getValueDepthLimit());
  }

  @AutoValue
  @Immutable
  abstract static class AttributeLimitsValue extends AttributeLimits {}
}
