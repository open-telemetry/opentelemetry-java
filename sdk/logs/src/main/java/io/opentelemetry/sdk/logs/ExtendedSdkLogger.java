/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.incubator.logs.ExtendedLogRecordBuilder;
import io.opentelemetry.api.incubator.logs.ExtendedLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.internal.LoggerConfig;

/** SDK implementation of {@link ExtendedLogger}. */
final class ExtendedSdkLogger extends SdkLogger implements ExtendedLogger {

  ExtendedSdkLogger(
      LoggerSharedState loggerSharedState,
      InstrumentationScopeInfo instrumentationScopeInfo,
      LoggerConfig loggerConfig) {
    super(loggerSharedState, instrumentationScopeInfo, loggerConfig);
  }

  @Override
  public boolean isEnabled() {
    return super.isEnabled();
  }

  @Override
  public ExtendedLogRecordBuilder logRecordBuilder() {
    return (ExtendedLogRecordBuilder) super.logRecordBuilder();
  }
}
