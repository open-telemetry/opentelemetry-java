/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig.internal;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.DeclarativeConfiguration;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.SeverityBasedLogRecordProcessor;

/**
 * ComponentProvider for SeverityBasedLogRecordProcessor to support declarative configuration.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class SeverityBasedLogRecordProcessorComponentProvider
    implements ComponentProvider<LogRecordProcessor> {

  @Override
  public Class<LogRecordProcessor> getType() {
    return LogRecordProcessor.class;
  }

  @Override
  public String getName() {
    return "severity_based";
  }

  @Override
  public LogRecordProcessor create(DeclarativeConfigProperties config) {
    String minimumSeverityStr = config.getString("minimum_severity");
    if (minimumSeverityStr == null) {
      throw new IllegalArgumentException(
          "minimum_severity is required for severity_based log processors");
    }

    Severity minimumSeverity;
    try {
      minimumSeverity = Severity.valueOf(minimumSeverityStr);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid severity value: " + minimumSeverityStr, e);
    }

    DeclarativeConfigProperties delegateConfig = config.getStructured("delegate");
    if (delegateConfig == null) {
      throw new IllegalArgumentException("delegate is required for severity_based log processors");
    }

    LogRecordProcessor delegate = DeclarativeConfiguration.createLogRecordProcessor(delegateConfig);

    return SeverityBasedLogRecordProcessor.builder(minimumSeverity, delegate).build();
  }
}
