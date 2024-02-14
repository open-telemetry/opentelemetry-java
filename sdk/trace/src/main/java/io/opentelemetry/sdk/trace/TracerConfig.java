/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;

@AutoValue
@Immutable
public abstract class TracerConfig {

  private static final TracerConfig DEFAULT_CONFIG =
      new AutoValue_TracerConfig(/* enabled= */ true);

  public static TracerConfig disabled() {
    return new AutoValue_TracerConfig(/* enabled= */ false);
  }

  public static TracerConfig enabled() {
    return DEFAULT_CONFIG;
  }

  public static TracerConfig defaultConfig() {
    return DEFAULT_CONFIG;
  }

  TracerConfig() {}

  /** Returns {@code true} if this tracer is enabled. Defaults to {@code true}. */
  public abstract boolean isEnabled();
}
