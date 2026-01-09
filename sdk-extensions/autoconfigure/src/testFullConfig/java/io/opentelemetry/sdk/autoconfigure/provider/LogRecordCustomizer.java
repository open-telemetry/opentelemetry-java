/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.provider;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.ReadWriteLogRecord;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.util.Collection;
import java.util.stream.Collectors;

/** Behavior asserted in {@link io.opentelemetry.sdk.autoconfigure.FullConfigTest}. */
public class LogRecordCustomizer implements AutoConfigurationCustomizerProvider {
  @Override
  public void customize(AutoConfigurationCustomizer autoConfiguration) {
    autoConfiguration.addLogRecordProcessorCustomizer(LogRecordCustomizer::processorCustomizer);
    autoConfiguration.addLogRecordExporterCustomizer(LogRecordCustomizer::exporterCustomizer);
  }

  private static LogRecordProcessor processorCustomizer(
      LogRecordProcessor delegate, ConfigProperties config) {
    return new LogRecordProcessor() {
      @Override
      public void onEmit(Context context, ReadWriteLogRecord logRecord) {
        logRecord.setAttribute(AttributeKey.stringKey("extra-key"), "extra-value");
        delegate.onEmit(context, logRecord);
      }

      @Override
      public CompletableResultCode shutdown() {
        return delegate.shutdown();
      }

      @Override
      public CompletableResultCode forceFlush() {
        return delegate.forceFlush();
      }
    };
  }

  private static LogRecordExporter exporterCustomizer(
      LogRecordExporter delegate, ConfigProperties config) {
    return new LogRecordExporter() {
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
    };
  }
}
