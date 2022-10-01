/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import java.util.function.Supplier;
import javax.annotation.concurrent.Immutable;

/**
 * Class that holds limits enforced during log recording.
 *
 * <p>Note: To allow dynamic updates of {@link LogLimits} you should register a {@link
 * java.util.function.Supplier} with {@link SdkLoggerProviderBuilder#setLogLimits(Supplier)} which
 * supplies dynamic configs when queried.
 */
@AutoValue
@Immutable
public abstract class LogLimits {

  private static final LogLimits DEFAULT = new LogLimitsBuilder().build();

  /** Returns the default {@link LogLimits}. */
  public static LogLimits getDefault() {
    return DEFAULT;
  }

  /** Returns a new {@link LogLimitsBuilder} to construct a {@link LogLimits}. */
  public static LogLimitsBuilder builder() {
    return new LogLimitsBuilder();
  }

  static LogLimits create(int maxNumAttributes, int maxAttributeLength) {
    return new AutoValue_LogLimits(maxNumAttributes, maxAttributeLength);
  }

  LogLimits() {}

  /**
   * Returns the max number of attributes per {@link LogRecordData}.
   *
   * @return the max number of attributes per {@link LogRecordData}.
   */
  public abstract int getMaxNumberOfAttributes();

  /**
   * Returns the max number of characters for string attribute values. For string array attribute
   * values, applies to each entry individually.
   *
   * @return the max number of characters for attribute strings.
   */
  public abstract int getMaxAttributeValueLength();

  /**
   * Returns a {@link LogLimitsBuilder} initialized to the same property values as the current
   * instance.
   *
   * @return a {@link LogLimitsBuilder} initialized to the same property values as the current
   *     instance.
   */
  public LogLimitsBuilder toBuilder() {
    return new LogLimitsBuilder()
        .setMaxNumberOfAttributes(getMaxNumberOfAttributes())
        .setMaxAttributeValueLength(getMaxAttributeValueLength());
  }
}
