/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.InstrumentationScopeUtil;
import io.opentelemetry.sdk.logs.data.LogDataBuilder;

/** SDK implementation of {@link LogEmitter}. */
final class SdkLogEmitter implements LogEmitter {

  private final LogEmitterSharedState logEmitterSharedState;
  private final InstrumentationScopeInfo instrumentationScopeInfo;
  private final InstrumentationLibraryInfo instrumentationLibraryInfo;

  SdkLogEmitter(
      LogEmitterSharedState logEmitterSharedState,
      InstrumentationScopeInfo instrumentationScopeInfo) {
    this.logEmitterSharedState = logEmitterSharedState;
    this.instrumentationScopeInfo = instrumentationScopeInfo;
    this.instrumentationLibraryInfo =
        InstrumentationScopeUtil.toInstrumentationLibraryInfo(instrumentationScopeInfo);
  }

  @Override
  public LogBuilder logBuilder() {
    LogDataBuilder logDataBuilder =
        LogDataBuilder.create(
            logEmitterSharedState.getResource(),
            instrumentationLibraryInfo,
            logEmitterSharedState.getClock());
    return new SdkLogBuilder(logEmitterSharedState, logDataBuilder);
  }

  // VisibleForTesting
  InstrumentationScopeInfo getInstrumentationScopeInfo() {
    return instrumentationScopeInfo;
  }
}
