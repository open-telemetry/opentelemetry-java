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
    DeclarativeConfigProperties delegateConfig = config.getStructured("delegate");
    if (delegateConfig == null) {
      throw new IllegalArgumentException("delegate is required for trace_based log processors");
    }

    LogRecordProcessor delegate = DeclarativeConfiguration.createLogRecordProcessor(delegateConfig);

    return TraceBasedLogRecordProcessor.builder(delegate).build();
  }
}
