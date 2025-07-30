/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig.internal;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.DeclarativeConfiguration;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.TraceBasedLogRecordProcessor;
import java.util.ArrayList;
import java.util.List;

/**
 * ComponentProvider for TraceBasedLogRecordProcessor to support declarative configuration.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class TraceBasedLogRecordProcessorComponentProvider
    implements ComponentProvider<LogRecordProcessor> {

  @Override
  public Class<LogRecordProcessor> getType() {
    return LogRecordProcessor.class;
  }

  @Override
  public String getName() {
    return "trace_based";
  }

  @Override
  public LogRecordProcessor create(DeclarativeConfigProperties config) {
    List<DeclarativeConfigProperties> processorConfigs = config.getStructuredList("processors");
    if (processorConfigs == null || processorConfigs.isEmpty()) {
      throw new IllegalArgumentException(
          "At least one processor is required for trace_based log processors");
    }

    List<LogRecordProcessor> processors = new ArrayList<>();
    for (DeclarativeConfigProperties processorConfig : processorConfigs) {
      LogRecordProcessor processor =
          DeclarativeConfiguration.createLogRecordProcessor(processorConfig);
      processors.add(processor);
    }

    return TraceBasedLogRecordProcessor.builder().addProcessors(processors).build();
  }
}
