/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LoggerProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.MeterProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpentelemetryConfiguration;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.TracerProvider;
import java.io.FileInputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class ConfigurationReaderTest {

  @Test
  void read_ExampleFile() throws IOException {
    OpentelemetryConfiguration expected = new OpentelemetryConfiguration();
    expected.setFileFormat("0.1");
    expected.setTracerProvider(new TracerProvider());
    expected.setMeterProvider(new MeterProvider());
    expected.setLoggerProvider(new LoggerProvider());

    try (FileInputStream configExampleFile =
        new FileInputStream(System.getenv("CONFIG_EXAMPLE_FILE"))) {
      OpentelemetryConfiguration configuration = ConfigurationReader.parse(configExampleFile);

      assertThat(configuration).isEqualTo(expected);
    }
  }
}
