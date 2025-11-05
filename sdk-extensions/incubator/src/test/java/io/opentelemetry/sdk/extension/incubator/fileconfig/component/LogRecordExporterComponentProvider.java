/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig.component;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.util.Collection;

public class LogRecordExporterComponentProvider implements ComponentProvider {
  @Override
  public Class<LogRecordExporter> getType() {
    return LogRecordExporter.class;
  }

  @Override
  public String getName() {
    return "test";
  }

  @Override
  public LogRecordExporter create(DeclarativeConfigProperties config) {
    return new TestLogRecordExporter(config);
  }

  public static class TestLogRecordExporter implements LogRecordExporter {

    public final DeclarativeConfigProperties config;

    private TestLogRecordExporter(DeclarativeConfigProperties config) {
      this.config = config;
    }

    @Override
    public CompletableResultCode export(Collection<LogRecordData> logs) {
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
      return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
      return CompletableResultCode.ofSuccess();
    }
  }
}
