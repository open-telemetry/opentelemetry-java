/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig.component;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.ReadWriteLogRecord;

public class LogRecordProcessorComponentProvider implements ComponentProvider {
  @Override
  public Class<LogRecordProcessor> getType() {
    return LogRecordProcessor.class;
  }

  @Override
  public String getName() {
    return "test";
  }

  @Override
  public LogRecordProcessor create(DeclarativeConfigProperties config) {
    return new TestLogRecordProcessor(config);
  }

  public static class TestLogRecordProcessor implements LogRecordProcessor {

    public final DeclarativeConfigProperties config;

    private TestLogRecordProcessor(DeclarativeConfigProperties config) {
      this.config = config;
    }

    @Override
    public void onEmit(Context context, ReadWriteLogRecord logRecord) {}

    @Override
    public CompletableResultCode shutdown() {
      return CompletableResultCode.ofSuccess();
    }
  }
}
