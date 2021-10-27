/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.concurrent.Immutable;

/** SDK implementation of {@link LogData}. */
@AutoValue
@Immutable
abstract class SdkLogData implements LogData {

  SdkLogData() {}

  static SdkLogData create(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      LogRecord logRecord) {
    return new AutoValue_SdkLogData(
        logRecord.getEpochNanos(),
        logRecord.getTraceId(),
        logRecord.getSpanId(),
        logRecord.getFlags(),
        logRecord.getSeverity(),
        logRecord.getSeverityText(),
        logRecord.getName(),
        logRecord.getBody(),
        logRecord.getAttributes(),
        resource,
        instrumentationLibraryInfo);
  }
}
