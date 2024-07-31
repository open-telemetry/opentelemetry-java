/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.config;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A registry for accessing configuration.
 *
 * <p>The name <i>Provider</i> is for consistency with other languages and it is <b>NOT</b> loaded
 * using reflection.
 *
 * <p>See {@link InstrumentationConfigUtil} for convenience methods for extracting config from
 * {@link ConfigProvider}.
 */
@ThreadSafe
public interface ConfigProvider {

  /**
   * Returns the {@link StructuredConfigProperties} corresponding to <a
   * href="https://github.com/open-telemetry/opentelemetry-configuration/blob/main/schema/instrumentation.json">instrumentation
   * config</a>, or {@code null} if unavailable.
   *
   * @return the instrumentation {@link StructuredConfigProperties}
   */
  @Nullable
  StructuredConfigProperties getInstrumentationConfig();

  /** Returns a no-op {@link ConfigProvider}. */
  static ConfigProvider noop() {
    return () -> null;
  }
}
