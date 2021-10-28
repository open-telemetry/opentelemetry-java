/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.LogRecord;

/** SDK implementation of {@link LogEmitter}. */
final class SdkLogEmitter implements LogEmitter {

  private final LogEmitterSharedState logEmitterSharedState;
  private final InstrumentationLibraryInfo instrumentationLibraryInfo;

  SdkLogEmitter(
      LogEmitterSharedState logEmitterSharedState,
      InstrumentationLibraryInfo instrumentationLibraryInfo) {
    this.logEmitterSharedState = logEmitterSharedState;
    this.instrumentationLibraryInfo = instrumentationLibraryInfo;
  }

  @Override
  public void emit(LogRecord logRecord) {
    logEmitterSharedState
        .getLogProcessor()
        .emit(
            LogData.create(
                logEmitterSharedState.getResource(), instrumentationLibraryInfo, logRecord));
  }

  // VisibleForTesting
  InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
    return instrumentationLibraryInfo;
  }
}
