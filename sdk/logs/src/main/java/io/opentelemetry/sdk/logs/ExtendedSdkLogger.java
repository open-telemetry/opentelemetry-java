/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.incubator.logs.ExtendedLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.internal.LoggerConfig;

/** SDK implementation of {@link ExtendedLogger}. */
final class ExtendedSdkLogger extends SdkLogger implements ExtendedLogger {

  private final boolean loggerEnabled;

  ExtendedSdkLogger(
      LoggerSharedState loggerSharedState,
      InstrumentationScopeInfo instrumentationScopeInfo,
      LoggerConfig loggerConfig) {
    super(loggerSharedState, instrumentationScopeInfo, loggerConfig);
    this.loggerEnabled = loggerConfig.isEnabled();
  }

  @Override
  public boolean isEnabled() {
    return loggerEnabled;
  }
}
