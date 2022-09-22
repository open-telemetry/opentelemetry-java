/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.export.LogExporter;
import java.util.Collection;
import java.util.stream.Collectors;

public class LogExporterCustomizer implements AutoConfigurationCustomizerProvider {
  @Override
  public void customize(AutoConfigurationCustomizer autoConfiguration) {
    autoConfiguration.addLogExporterCustomizer(
        (delegate, config) ->
            new LogExporter() {
              @Override
              public CompletableResultCode export(Collection<LogData> logs) {
                Collection<LogData> filtered =
                    logs.stream()
                        .filter(
                            log ->
                                log.getSeverity().getSeverityNumber()
                                    >= Severity.INFO.getSeverityNumber())
                        .collect(Collectors.toList());
                return delegate.export(filtered);
              }

              @Override
              public CompletableResultCode flush() {
                return delegate.flush();
              }

              @Override
              public CompletableResultCode shutdown() {
                return delegate.shutdown();
              }
            });
  }
}
