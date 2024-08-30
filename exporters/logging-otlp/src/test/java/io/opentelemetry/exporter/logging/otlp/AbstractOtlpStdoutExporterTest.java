/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import java.util.Map;
import org.assertj.core.api.AbstractObjectAssert;
import org.junit.jupiter.api.Test;

public abstract class AbstractOtlpStdoutExporterTest<T>
    extends AbstractOtlpJsonLoggingExporterTest<T> {

  private final Class<?> componentProviderType;

  public AbstractOtlpStdoutExporterTest(
      String type,
      TestDataExporter<? super T> testDataExporter,
      Class<?> exporterClass,
      Class<?> providerClass,
      Class<?> componentProviderType,
      String expectedFileNoWrapper,
      String expectedFileWrapper,
      String defaultConfigString) {
    super(
        type,
        testDataExporter,
        exporterClass,
        providerClass,
        expectedFileNoWrapper,
        expectedFileWrapper,
        defaultConfigString);
    this.componentProviderType = componentProviderType;
  }

  @SuppressWarnings("unchecked")
  protected T exporterFromComponentProvider(StructuredConfigProperties properties) {
    return (T)
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

  private void assertStdoutProperties(T exporter, Map<String, String> expected) {
    AbstractObjectAssert<?, ?> assertThat = assertThat(exporter);

        expected.forEach(
            (key, value) ->
     assertThat.extracting(key).extracting(Object::toString).isEqualTo(value));
  }
}
