/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.common;

import static io.opentelemetry.api.internal.Utils.checkArgument;

/**
 * Builder for {@link AttributeLimits}.
 *
 * @since 1.64.0
 */
public final class AttributeLimitsBuilder {

  private int capacity = Integer.MAX_VALUE;
  private int lengthLimit = Integer.MAX_VALUE;

  AttributeLimitsBuilder() {}

  /**
   * Sets the maximum number of unique attribute keys. Additional entries with new key names are
   * dropped once the capacity is reached. Overwrites of existing keys do not consume capacity.
   *
   * @param capacity the maximum number of unique attribute keys; must be non-negative. Use {@link
   *     Integer#MAX_VALUE} for no capacity limit.
   */
  public AttributeLimitsBuilder setCapacity(int capacity) {
    checkArgument(capacity >= 0, "capacity must be non-negative");
    this.capacity = capacity;
    return this;
  }

  /**
   * Sets the maximum length for string and string-array attribute values. Longer values are
   * truncated. Applies recursively to nested {@link Value}-typed attributes.
   *
   * @param lengthLimit the maximum length; must be non-negative. Use {@link Integer#MAX_VALUE} for
   *     no length limit.
   */
  public AttributeLimitsBuilder setLengthLimit(int lengthLimit) {
    checkArgument(lengthLimit >= 0, "lengthLimit must be non-negative");
    this.lengthLimit = lengthLimit;
    return this;
  }

  /** Builds the {@link AttributeLimits}. */
  public AttributeLimits build() {
    return AttributeLimits.create(capacity, lengthLimit);
  }
}
