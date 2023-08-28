/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeLimits;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordLimits;
import io.opentelemetry.sdk.logs.LogLimits;
import io.opentelemetry.sdk.logs.LogLimitsBuilder;
import java.io.Closeable;
import java.util.List;
import javax.annotation.Nullable;

final class LogLimitsFactory implements Factory<LogRecordLimitsAndAttributeLimits, LogLimits> {

  private static final LogLimitsFactory INSTANCE = new LogLimitsFactory();

  private LogLimitsFactory() {}

  static LogLimitsFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public LogLimits create(
      @Nullable LogRecordLimitsAndAttributeLimits model,
      SpiHelper spiHelper,
      List<Closeable> closeables) {
    if (model == null) {
      return LogLimits.getDefault();
    }
    LogLimitsBuilder builder = LogLimits.builder();

    AttributeLimits attributeLimitsModel = model.getAttributeLimits();
    if (attributeLimitsModel != null) {
      if (attributeLimitsModel.getAttributeCountLimit() != null) {
        builder.setMaxNumberOfAttributes(attributeLimitsModel.getAttributeCountLimit());
      }
      if (attributeLimitsModel.getAttributeValueLengthLimit() != null) {
        builder.setMaxAttributeValueLength(attributeLimitsModel.getAttributeValueLengthLimit());
      }
    }

    LogRecordLimits logRecordLimitsModel = model.getLogRecordLimits();
    if (logRecordLimitsModel != null) {
      if (logRecordLimitsModel.getAttributeCountLimit() != null) {
        builder.setMaxNumberOfAttributes(logRecordLimitsModel.getAttributeCountLimit());
      }
      if (logRecordLimitsModel.getAttributeValueLengthLimit() != null) {
        builder.setMaxAttributeValueLength(logRecordLimitsModel.getAttributeValueLengthLimit());
      }
    }

    return builder.build();
  }
}
