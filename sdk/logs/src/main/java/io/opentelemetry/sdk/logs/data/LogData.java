/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.concurrent.Immutable;

/**
 * A {@link LogRecord} with an associated {@link Resource} and {@link InstrumentationLibraryInfo}.
 */
@Immutable
public interface LogData extends LogRecord {

  /** Create a log data instance. */
  static LogData create(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      LogRecord logRecord) {
    return SdkLogData.create(resource, instrumentationLibraryInfo, logRecord);
  }

  /** Returns the resource of this log. */
  Resource getResource();

  /** Returns the instrumentation library that generated this log. */
  InstrumentationLibraryInfo getInstrumentationLibraryInfo();
}
