/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeLimitsModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordLimitsModel;
import io.opentelemetry.sdk.logs.LogLimits;
import io.opentelemetry.sdk.logs.LogLimitsBuilder;

final class LogLimitsFactory implements Factory<LogRecordLimitsAndAttributeLimits, LogLimits> {

  private static final LogLimitsFactory INSTANCE = new LogLimitsFactory();

  private LogLimitsFactory() {}

  static LogLimitsFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public LogLimits create(
      LogRecordLimitsAndAttributeLimits model, DeclarativeConfigContext context) {
    LogLimitsBuilder builder = LogLimits.builder();

    AttributeLimitsModel attributeLimitsModel = model.getAttributeLimits();
    if (attributeLimitsModel != null) {
      if (attributeLimitsModel.getAttributeCountLimit() != null) {
        builder.setMaxNumberOfAttributes(attributeLimitsModel.getAttributeCountLimit());
      }
      if (attributeLimitsModel.getAttributeValueLengthLimit() != null) {
        builder.setMaxAttributeValueLength(attributeLimitsModel.getAttributeValueLengthLimit());
      }
    }

    LogRecordLimitsModel logRecordLimitsModel = model.getLogRecordLimits();
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
