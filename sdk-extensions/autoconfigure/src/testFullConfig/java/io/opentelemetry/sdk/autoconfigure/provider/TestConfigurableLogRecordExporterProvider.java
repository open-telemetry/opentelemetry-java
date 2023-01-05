/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.provider;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.logs.ConfigurableLogRecordExporterProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.util.Collection;

public class TestConfigurableLogRecordExporterProvider
    implements ConfigurableLogRecordExporterProvider {

  @Override
  public LogRecordExporter createExporter(ConfigProperties config) {
    return new TestLogRecordExporter(config);
  }

  @Override
  public String getName() {
    return "testExporter";
  }

  public static class TestLogRecordExporter implements LogRecordExporter {
    private final ConfigProperties config;

    public TestLogRecordExporter(ConfigProperties config) {
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

    public ConfigProperties getConfig() {
      return config;
    }
  }
}
