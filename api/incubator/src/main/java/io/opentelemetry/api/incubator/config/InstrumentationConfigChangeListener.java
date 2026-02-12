/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.config;

/** Listener notified when instrumentation configuration changes. */
@FunctionalInterface
public interface InstrumentationConfigChangeListener {

  /**
   * Called when instrumentation configuration changes.
   *
   * @param instrumentationConfig the updated instrumentation configuration
   */
  void onChange(DeclarativeConfigProperties instrumentationConfig);
}
