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
 * AttributeType}), truncates over-length string values, and drops entries added beyond the
 * configured capacity. This differs from the default builder ({@link Attributes#builder()}) which
 * defers de-duplication to {@link AttributesBuilder#build()} and applies no truncation or capacity.
 *
 * <p>Use {@link #noLimits()} to represent an unbounded configuration and {@link #builder()} to
 * construct a bounded one.
 *
 * @since 1.64.0
 */
public abstract class AttributeLimits {

  private static final AttributeLimits NO_LIMITS = new AttributeLimitsBuilder().build();

  /** Returns an {@link AttributeLimits} that imposes no capacity or length limits. */
  public static AttributeLimits noLimits() {
    return NO_LIMITS;
  }

  /** Returns a new {@link AttributeLimitsBuilder} initialized to {@link #noLimits()}. */
  public static AttributeLimitsBuilder builder() {
    return new AttributeLimitsBuilder();
  }

  static AttributeLimits create(int capacity, int lengthLimit) {
    return new AutoValue_AttributeLimits_AttributeLimitsValue(capacity, lengthLimit);
  }

  /** Package-private constructor to prevent subclassing. */
  AttributeLimits() {}

  /**
   * Returns the maximum number of unique attribute keys. Additional entries with new key names are
   * dropped. Overwrites of existing keys do not consume capacity.
   *
   * <p>{@link Integer#MAX_VALUE} means no capacity limit.
   */
  public abstract int getCapacity();

  /**
   * Returns the maximum length for string and string-array attribute values. Longer values are
   * truncated to this length. Applies recursively to nested {@link Value}-typed attributes.
   *
   * <p>{@link Integer#MAX_VALUE} means no length limit.
   */
  public abstract int getLengthLimit();

  /**
   * Returns an {@link AttributeLimitsBuilder} initialized to the same property values as this
   * instance.
   */
  public AttributeLimitsBuilder toBuilder() {
    return builder().setCapacity(getCapacity()).setLengthLimit(getLengthLimit());
  }

  @AutoValue
  @Immutable
  abstract static class AttributeLimitsValue extends AttributeLimits {}
}
