/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeLimits;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordLimits;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LoggerProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MeterProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpentelemetryConfiguration;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanLimits;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TracerProvider;
import java.io.FileInputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class ConfigurationReaderTest {

  @Test
  void read_ExampleFile() throws IOException {
    OpentelemetryConfiguration expected = new OpentelemetryConfiguration();

    expected.setFileFormat("0.1");

    AttributeLimits attributeLimits = new AttributeLimits();
    expected.setAttributeLimits(attributeLimits);
    attributeLimits.setAttributeValueLengthLimit(4096);
    attributeLimits.setAttributeCountLimit(128);

    TracerProvider tracerProvider = new TracerProvider();
    expected.setTracerProvider(tracerProvider);

    SpanLimits spanLimits = new SpanLimits();
    tracerProvider.setSpanLimits(spanLimits);
    spanLimits.setAttributeValueLengthLimit(4096);
    spanLimits.setAttributeCountLimit(128);
    spanLimits.setEventCountLimit(128);
    spanLimits.setLinkCountLimit(128);
    spanLimits.setEventAttributeCountLimit(128);
    spanLimits.setLinkAttributeCountLimit(128);

    expected.setMeterProvider(new MeterProvider());

    LoggerProvider loggerProvider = new LoggerProvider();
    expected.setLoggerProvider(loggerProvider);

    LogRecordLimits logRecordLimits = new LogRecordLimits();
    loggerProvider.setLogRecordLimits(logRecordLimits);
    logRecordLimits.setAttributeValueLengthLimit(4096);
    logRecordLimits.setAttributeCountLimit(128);

    try (FileInputStream configExampleFile =
        new FileInputStream(System.getenv("CONFIG_EXAMPLE_FILE"))) {
      OpentelemetryConfiguration configuration = ConfigurationReader.parse(configExampleFile);

      assertThat(configuration).isEqualTo(expected);
    }
  }
}
