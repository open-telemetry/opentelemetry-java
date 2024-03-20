/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;

@AutoValue
@Immutable
public abstract class MeterConfig {

  private static final MeterConfig DEFAULT_CONFIG = new AutoValue_MeterConfig(/* enabled= */ true);

  public static MeterConfig disabled() {
    return new AutoValue_MeterConfig(/* enabled= */ false);
  }

  public static MeterConfig enabled() {
    return DEFAULT_CONFIG;
  }

  public static MeterConfig defaultConfig() {
    return DEFAULT_CONFIG;
  }

  MeterConfig() {}

  /** Returns {@code true} if this meter is enabled. Defaults to {@code true}. */
  public abstract boolean isEnabled();
}
