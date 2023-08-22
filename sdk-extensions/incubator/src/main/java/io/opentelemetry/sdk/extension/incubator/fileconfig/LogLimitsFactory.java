/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordLimits;
import io.opentelemetry.sdk.logs.LogLimits;
import io.opentelemetry.sdk.logs.LogLimitsBuilder;
import java.io.Closeable;
import java.util.List;
import javax.annotation.Nullable;

final class LogLimitsFactory implements Factory<LogRecordLimits, LogLimits> {

  private static final LogLimitsFactory INSTANCE = new LogLimitsFactory();

  private LogLimitsFactory() {}

  static LogLimitsFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public LogLimits create(
      @Nullable LogRecordLimits model, SpiHelper spiHelper, List<Closeable> closeables) {
    if (model == null) {
      return LogLimits.getDefault();
    }

    LogLimitsBuilder builder = LogLimits.builder();
    if (model.getAttributeCountLimit() != null) {
      builder.setMaxNumberOfAttributes(model.getAttributeCountLimit());
    }
    if (model.getAttributeValueLengthLimit() != null) {
      builder.setMaxAttributeValueLength(model.getAttributeValueLengthLimit());
    }
    return builder.build();
  }
}
