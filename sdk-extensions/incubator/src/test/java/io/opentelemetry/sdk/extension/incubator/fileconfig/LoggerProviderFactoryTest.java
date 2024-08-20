/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AttributeLimits;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.BatchLogRecordProcessor;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordExporter;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordLimits;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordProcessor;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LoggerProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Otlp;
import io.opentelemetry.sdk.logs.LogLimits;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LoggerProviderFactoryTest {

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  private final SpiHelper spiHelper =
      SpiHelper.create(LoggerProviderFactoryTest.class.getClassLoader());

  @ParameterizedTest
  @MethodSource("createArguments")
  void create(LoggerProviderAndAttributeLimits model, SdkLoggerProvider expectedProvider) {
    List<Closeable> closeables = new ArrayList<>();
    cleanup.addCloseable(expectedProvider);

    SdkLoggerProvider provider =
        LoggerProviderFactory.getInstance().create(model, spiHelper, closeables).build();
    cleanup.addCloseable(provider);
    cleanup.addCloseables(closeables);

    assertThat(provider.toString()).isEqualTo(expectedProvider.toString());
  }

  private static Stream<Arguments> createArguments() {
    return Stream.of(
        Arguments.of(
            LoggerProviderAndAttributeLimits.create(null, null),
            SdkLoggerProvider.builder().build()),
        Arguments.of(
            LoggerProviderAndAttributeLimits.create(new AttributeLimits(), new LoggerProvider()),
            SdkLoggerProvider.builder().build()),
        Arguments.of(
            LoggerProviderAndAttributeLimits.create(
                new AttributeLimits(),
                new LoggerProvider()
                    .withLimits(
                        new LogRecordLimits()
                            .withAttributeCountLimit(1)
                            .withAttributeValueLengthLimit(2))
                    .withProcessors(
                        Collections.singletonList(
                            new LogRecordProcessor()
                                .withBatch(
                                    new BatchLogRecordProcessor()
                                        .withExporter(
                                            new LogRecordExporter().withOtlp(new Otlp())))))),
            SdkLoggerProvider.builder()
                .setLogLimits(
                    () ->
                        LogLimits.builder()
                            .setMaxNumberOfAttributes(1)
                            .setMaxAttributeValueLength(2)
                            .build())
                .addLogRecordProcessor(
                    io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor.builder(
                            OtlpGrpcLogRecordExporter.getDefault())
                        .build())
                .build()));
  }
}
