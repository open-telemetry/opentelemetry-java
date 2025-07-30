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
import java.util.ArrayList;
import java.util.List;

/**
 * ComponentProvider for SeverityBasedLogRecordProcessor to support declarative configuration.
 *
 * <p>This provider creates a {@link SeverityBasedLogRecordProcessor} that filters log records
 * based on minimum severity level. Only log records with a severity level greater than or
 * equal to the configured minimum are forwarded to the configured downstream processors.
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

    List<DeclarativeConfigProperties> processorConfigs = config.getStructuredList("processors");
    if (processorConfigs == null || processorConfigs.isEmpty()) {
      throw new IllegalArgumentException(
          "At least one processor is required for severity_based log processors");
    }

    List<LogRecordProcessor> processors = new ArrayList<>();
    for (DeclarativeConfigProperties processorConfig : processorConfigs) {
      LogRecordProcessor processor =
          DeclarativeConfiguration.createLogRecordProcessor(processorConfig);
      processors.add(processor);
    }

    return SeverityBasedLogRecordProcessor.builder(minimumSeverity)
        .addProcessors(processors)
        .build();
  }
}
