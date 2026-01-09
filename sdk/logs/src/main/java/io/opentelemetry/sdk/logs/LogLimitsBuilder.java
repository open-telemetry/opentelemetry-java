/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.internal.Utils;
import io.opentelemetry.sdk.logs.data.LogRecordData;

/**
 * Builder for {@link LogLimits}.
 *
 * @since 1.27.0
 */
public final class LogLimitsBuilder {

  private static final int DEFAULT_LOG_MAX_NUM_ATTRIBUTES = 128;
  private static final int DEFAULT_LOG_MAX_ATTRIBUTE_LENGTH = Integer.MAX_VALUE;

  private int maxNumAttributes = DEFAULT_LOG_MAX_NUM_ATTRIBUTES;
  private int maxAttributeValueLength = DEFAULT_LOG_MAX_ATTRIBUTE_LENGTH;

  LogLimitsBuilder() {}

  /**
   * Sets the max number of attributes per {@link LogRecordData}.
   *
   * @param maxNumberOfAttributes the max number of attributes per {@link LogRecordData}. Must be
   *     positive.
   * @return this.
   * @throws IllegalArgumentException if {@code maxNumberOfAttributes} is not positive.
   */
  public LogLimitsBuilder setMaxNumberOfAttributes(int maxNumberOfAttributes) {
    Utils.checkArgument(maxNumberOfAttributes >= 0, "maxNumberOfAttributes must be non-negative");
    this.maxNumAttributes = maxNumberOfAttributes;
    return this;
  }

  /**
   * Sets the max number of characters for string attribute values. For string array attribute
   * values, applies to each entry individually.
   *
   * @param maxAttributeValueLength the max number of characters for attribute strings. Must not be
   *     negative.
   * @return this.
   * @throws IllegalArgumentException if {@code maxAttributeValueLength} is negative.
   */
  public LogLimitsBuilder setMaxAttributeValueLength(int maxAttributeValueLength) {
    Utils.checkArgument(
        maxAttributeValueLength >= 0, "maxAttributeValueLength must be non-negative");
    this.maxAttributeValueLength = maxAttributeValueLength;
    return this;
  }

  /** Builds and returns a {@link LogLimits} with the values of this builder. */
  public LogLimits build() {
    return LogLimits.create(maxNumAttributes, maxAttributeValueLength);
  }
}
