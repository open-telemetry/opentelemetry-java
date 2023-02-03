/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.provider;

import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.util.Collection;
import java.util.stream.Collectors;

public class LogRecordExporterCustomizer implements AutoConfigurationCustomizerProvider {
  @Override
  public void customize(AutoConfigurationCustomizer autoConfiguration) {
    autoConfiguration.addLogRecordExporterCustomizer(
        (delegate, config) ->
            new LogRecordExporter() {
              @Override
              public CompletableResultCode export(Collection<LogRecordData> logs) {
                Collection<LogRecordData> filtered =
                    logs.stream()
                        .filter(
                            log ->
                                log.getSeverity() == Severity.UNDEFINED_SEVERITY_NUMBER
                                    || log.getSeverity().getSeverityNumber()
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
