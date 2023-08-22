/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BatchLogRecordProcessor;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordExporter;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordLimits;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordProcessor;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LoggerProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfiguration;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Otlp;
import io.opentelemetry.sdk.logs.LogLimits;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class OpenTelemetryConfigurationFactoryTest {

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  private final SpiHelper spiHelper =
      SpiHelper.create(OpenTelemetryConfigurationFactoryTest.class.getClassLoader());

  @Test
  void create_Null() {
    List<Closeable> closeables = new ArrayList<>();
    OpenTelemetrySdk expectedSdk = OpenTelemetrySdk.builder().build();
    cleanup.addCloseable(expectedSdk);

    OpenTelemetrySdk sdk =
        OpenTelemetryConfigurationFactory.getInstance().create(null, spiHelper, closeables);
    cleanup.addCloseable(sdk);
    cleanup.addCloseables(closeables);

    assertThat(sdk.toString()).isEqualTo(expectedSdk.toString());
  }

  @Test
  void create_InvalidFileFormat() {
    List<OpenTelemetryConfiguration> testCases =
        Arrays.asList(
            new OpenTelemetryConfiguration(), new OpenTelemetryConfiguration().withFileFormat("1"));

    List<Closeable> closeables = new ArrayList<>();
    for (OpenTelemetryConfiguration testCase : testCases) {
      assertThatThrownBy(
              () ->
                  OpenTelemetryConfigurationFactory.getInstance()
                      .create(testCase, spiHelper, closeables))
          .isInstanceOf(ConfigurationException.class)
          .hasMessage("Unsupported file format. Supported formats include: 0.1");
      cleanup.addCloseables(closeables);
    }
  }

  @Test
  void create_Defaults() {
    List<Closeable> closeables = new ArrayList<>();
    OpenTelemetrySdk expectedSdk = OpenTelemetrySdk.builder().build();
    cleanup.addCloseable(expectedSdk);

    OpenTelemetrySdk sdk =
        OpenTelemetryConfigurationFactory.getInstance()
            .create(new OpenTelemetryConfiguration().withFileFormat("0.1"), spiHelper, closeables);
    cleanup.addCloseable(sdk);
    cleanup.addCloseables(closeables);

    assertThat(sdk.toString()).isEqualTo(expectedSdk.toString());
  }

  @Test
  void create_Configured() {
    List<Closeable> closeables = new ArrayList<>();
    OpenTelemetrySdk expectedSdk =
        OpenTelemetrySdk.builder()
            .setLoggerProvider(
                SdkLoggerProvider.builder()
                    .setLogLimits(
                        () ->
                            LogLimits.builder()
                                .setMaxAttributeValueLength(1)
                                .setMaxNumberOfAttributes(2)
                                .build())
                    .addLogRecordProcessor(
                        io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor.builder(
                                OtlpGrpcLogRecordExporter.getDefault())
                            .build())
                    .build())
            .build();
    cleanup.addCloseable(expectedSdk);

    OpenTelemetrySdk sdk =
        OpenTelemetryConfigurationFactory.getInstance()
            .create(
                new OpenTelemetryConfiguration()
                    .withFileFormat("0.1")
                    .withLoggerProvider(
                        new LoggerProvider()
                            .withLimits(
                                new LogRecordLimits()
                                    .withAttributeValueLengthLimit(1)
                                    .withAttributeCountLimit(2))
                            .withProcessors(
                                Collections.singletonList(
                                    new LogRecordProcessor()
                                        .withBatch(
                                            new BatchLogRecordProcessor()
                                                .withExporter(
                                                    new LogRecordExporter()
                                                        .withOtlp(new Otlp())))))),
                spiHelper,
                closeables);
    cleanup.addCloseable(sdk);
    cleanup.addCloseables(closeables);

    assertThat(sdk.toString()).isEqualTo(expectedSdk.toString());
  }
}
