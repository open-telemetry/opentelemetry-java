/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.exporter.logging.otlp.internal.logs.OtlpStdoutLogRecordExporter;
import io.opentelemetry.exporter.logging.otlp.internal.logs.OtlpStdoutLogRecordExporterBuilder;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.logs.ConfigurableLogRecordExporterProvider;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.io.OutputStream;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import org.assertj.core.api.AbstractObjectAssert;
import org.junit.jupiter.api.Test;

@SuppressLogger(LogRecordExporter.class)
class OtlpStdoutLogRecordExporterTest
    extends AbstractOtlpJsonLoggingExporterTest<OtlpStdoutLogRecordExporter> {

  private final Class<?> componentProviderType;

  public OtlpStdoutLogRecordExporterTest() {
    super(
        "otlp-stdout",
        TestDataExporter.forLogs(),
        OtlpStdoutLogRecordExporter.class,
        ConfigurableLogRecordExporterProvider.class,
        "OtlpStdoutLogRecordExporter{jsonWriter=StreamJsonWriter{outputStream=stdout}, wrapperJsonObject=true}");
    this.componentProviderType = LogRecordExporter.class;
  }

  @Override
  protected OtlpStdoutLogRecordExporter createExporter(
      @Nullable OutputStream outputStream, boolean wrapperJsonObject) {
    OtlpStdoutLogRecordExporterBuilder builder =
        OtlpStdoutLogRecordExporter.builder().setWrapperJsonObject(wrapperJsonObject);
    if (outputStream != null) {
      builder.setOutputStream(outputStream);
    } else {
      builder.setLogger(Logger.getLogger(exporterClass.getName()));
    }
    return builder.build();
  }

  @Override
  protected OtlpStdoutLogRecordExporter createDefaultExporter() {
    return OtlpStdoutLogRecordExporter.builder().build();
  }

  protected OtlpStdoutLogRecordExporter exporterFromComponentProvider(
      StructuredConfigProperties properties) {
    return (OtlpStdoutLogRecordExporter)
        ((ComponentProvider<?>)
                loadSpi(ComponentProvider.class)
                    .filter(
                        p -> {
                          ComponentProvider<?> c = (ComponentProvider<?>) p;
                          return "otlp-stdout".equals(c.getName())
                              && c.getType().equals(componentProviderType);
                        })
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No provider found")))
            .create(properties);
  }

  @Test
  void stdoutComponentProviderConfig() {
    StructuredConfigProperties properties = mock(StructuredConfigProperties.class);

    assertStdoutProperties(
        exporterFromComponentProvider(properties),
        ImmutableMap.of(
            "wrapperJsonObject", "true",
            "jsonWriter", "StreamJsonWriter{outputStream=stdout}"));
  }

  private void assertStdoutProperties(
      OtlpStdoutLogRecordExporter exporter, Map<String, String> expected) {
    AbstractObjectAssert<?, ?> assertThat = assertThat(exporter);

    expected.forEach(
        (key, value) -> assertThat.extracting(key).extracting(Object::toString).isEqualTo(value));
  }
}
