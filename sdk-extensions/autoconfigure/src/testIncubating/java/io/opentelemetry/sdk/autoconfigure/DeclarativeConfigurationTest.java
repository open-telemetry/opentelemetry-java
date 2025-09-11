/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.incubator.config.GlobalConfigProvider;
import io.opentelemetry.api.incubator.config.InstrumentationConfigUtil;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.internal.AutoConfigureUtil;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.event.Level;

class DeclarativeConfigurationTest {

  @RegisterExtension private static final CleanupExtension cleanup = new CleanupExtension();

  @RegisterExtension
  static final LogCapturer logCapturer =
      LogCapturer.create()
          .captureForLogger(AutoConfiguredOpenTelemetrySdkBuilder.class.getName(), Level.TRACE);

  @TempDir private Path tempDir;
  private Path configFilePath;

  @BeforeEach
  void setup() throws IOException {
    String yaml =
        "file_format: \"1.0-rc.1\"\n"
            + "resource:\n"
            + "  attributes:\n"
            + "    - name: service.name\n"
            + "      value: test\n"
            + "tracer_provider:\n"
            + "  processors:\n"
            + "    - simple:\n"
            + "        exporter:\n"
            + "          console: {}\n"
            + "instrumentation/development:\n"
            + "  general:\n"
            + "    http:\n"
            + "      client:\n"
            + "        request_captured_headers:\n"
            + "          - Content-Type\n"
            + "          - Accept\n"
            + "  java:\n"
            + "    example:\n"
            + "      key: value\n";
    configFilePath = tempDir.resolve("otel-config.yaml");
    Files.write(configFilePath, yaml.getBytes(StandardCharsets.UTF_8));
    GlobalOpenTelemetry.resetForTest();
    GlobalConfigProvider.resetForTest();
  }

  @Test
  @SuppressLogger(AutoConfiguredOpenTelemetrySdkBuilder.class)
  void configFile_fileNotFound() {
    assertThatThrownBy(
            () ->
                AutoConfiguredOpenTelemetrySdk.builder()
                    .setConfig(
                        DefaultConfigProperties.createFromMap(
                            Collections.singletonMap("otel.experimental.config.file", "foo")))
                    .build())
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Configuration file not found");

    assertThatCode(
            () ->
                AutoConfiguredOpenTelemetrySdk.builder()
                    .addPropertiesSupplier(() -> singletonMap("otel.experimental.config.file", ""))
                    .addPropertiesSupplier(() -> singletonMap("otel.sdk.disabled", "true"))
                    .build())
        .doesNotThrowAnyException();
  }

  @Test
  void configFile_Valid() {
    ConfigProperties config =
        DefaultConfigProperties.createFromMap(
            Collections.singletonMap("otel.experimental.config.file", configFilePath.toString()));
    OpenTelemetrySdk expectedSdk =
        OpenTelemetrySdk.builder()
            .setTracerProvider(
                SdkTracerProvider.builder()
                    .setResource(
                        Resource.getDefault().toBuilder().put("service.name", "test").build())
                    .addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
                    .build())
            .build();
    cleanup.addCloseable(expectedSdk);
    AutoConfiguredOpenTelemetrySdkBuilder builder = spy(AutoConfiguredOpenTelemetrySdk.builder());
    Thread thread = new Thread();
    doReturn(thread).when(builder).shutdownHook(any());

    AutoConfiguredOpenTelemetrySdk autoConfiguredOpenTelemetrySdk =
        builder.setConfig(config).build();
    cleanup.addCloseable(autoConfiguredOpenTelemetrySdk.getOpenTelemetrySdk());

    assertThat(autoConfiguredOpenTelemetrySdk.getOpenTelemetrySdk().toString())
        .isEqualTo(expectedSdk.toString());
    // AutoConfiguredOpenTelemetrySdk#getResource() is set to a dummy value when configuring from
    // file
    assertThat(autoConfiguredOpenTelemetrySdk.getResource()).isEqualTo(Resource.getDefault());
    verify(builder, times(1)).shutdownHook(autoConfiguredOpenTelemetrySdk.getOpenTelemetrySdk());
    assertThat(Runtime.getRuntime().removeShutdownHook(thread)).isTrue();
    logCapturer.assertContains("Autoconfiguring from configuration file: " + configFilePath);
  }

  @Test
  void configFile_setComponentLoader() {
    ComponentLoader componentLoader =
        ComponentLoader.forClassLoader(DeclarativeConfigurationTest.class.getClassLoader());
    ConfigProperties config =
        DefaultConfigProperties.createFromMap(
            Collections.singletonMap("otel.experimental.config.file", configFilePath.toString()));

    AutoConfiguredOpenTelemetrySdk autoConfiguredOpenTelemetrySdk =
        AutoConfiguredOpenTelemetrySdk.builder()
            .setConfig(config)
            .setComponentLoader(componentLoader)
            .build();
    cleanup.addCloseable(autoConfiguredOpenTelemetrySdk.getOpenTelemetrySdk());

    assertThat(
            Optional.ofNullable(AutoConfigureUtil.getConfigProvider(autoConfiguredOpenTelemetrySdk))
                .map(ConfigProvider::getInstrumentationConfig)
                .map(DeclarativeConfigProperties::getComponentLoader)
                .orElse(null))
        .isSameAs(componentLoader);
  }

  @Test
  void configFile_NoShutdownHook() {
    ConfigProperties config =
        DefaultConfigProperties.createFromMap(
            Collections.singletonMap("otel.experimental.config.file", configFilePath.toString()));
    AutoConfiguredOpenTelemetrySdkBuilder builder = spy(AutoConfiguredOpenTelemetrySdk.builder());

    AutoConfiguredOpenTelemetrySdk autoConfiguredOpenTelemetrySdk =
        builder.setConfig(config).disableShutdownHook().build();
    cleanup.addCloseable(autoConfiguredOpenTelemetrySdk.getOpenTelemetrySdk());

    verify(builder, never()).shutdownHook(any());
  }

  @Test
  void configFile_setResultAsGlobalFalse() {
    GlobalOpenTelemetry.set(OpenTelemetry.noop());
    ConfigProperties config =
        DefaultConfigProperties.createFromMap(
            Collections.singletonMap("otel.experimental.config.file", configFilePath.toString()));

    AutoConfiguredOpenTelemetrySdk autoConfiguredOpenTelemetrySdk =
        AutoConfiguredOpenTelemetrySdk.builder().setConfig(config).build();
    OpenTelemetrySdk openTelemetrySdk = autoConfiguredOpenTelemetrySdk.getOpenTelemetrySdk();
    cleanup.addCloseable(openTelemetrySdk);

    assertThat(GlobalOpenTelemetry.get()).extracting("delegate").isNotSameAs(openTelemetrySdk);
    assertThat(GlobalConfigProvider.get())
        .isNotSameAs(autoConfiguredOpenTelemetrySdk.getConfigProvider());
  }

  @Test
  void configFile_setResultAsGlobalTrue() {
    ConfigProperties config =
        DefaultConfigProperties.createFromMap(
            Collections.singletonMap("otel.experimental.config.file", configFilePath.toString()));

    AutoConfiguredOpenTelemetrySdk autoConfiguredOpenTelemetrySdk =
        AutoConfiguredOpenTelemetrySdk.builder().setConfig(config).setResultAsGlobal().build();
    OpenTelemetrySdk openTelemetrySdk = autoConfiguredOpenTelemetrySdk.getOpenTelemetrySdk();
    cleanup.addCloseable(openTelemetrySdk);

    assertThat(GlobalOpenTelemetry.get()).extracting("delegate").isSameAs(openTelemetrySdk);
    assertThat(GlobalConfigProvider.get())
        .isSameAs(autoConfiguredOpenTelemetrySdk.getConfigProvider());
  }

  @Test
  void configFile_Error(@TempDir Path tempDir) throws IOException {
    String yaml =
        "file_format: \"1.0-rc.1\"\n"
            + "resource:\n"
            + "  attributes:\n"
            + "    - name: service.name\n"
            + "      value: test\n"
            + "tracer_provider:\n"
            + "  processors:\n"
            + "    - simple:\n"
            + "        exporter:\n"
            + "          foo: {}\n";
    Path path = tempDir.resolve("otel-config.yaml");
    Files.write(path, yaml.getBytes(StandardCharsets.UTF_8));
    ConfigProperties config =
        DefaultConfigProperties.createFromMap(
            Collections.singletonMap("otel.experimental.config.file", path.toString()));

    assertThatThrownBy(() -> AutoConfiguredOpenTelemetrySdk.builder().setConfig(config).build())
        .isInstanceOf(ConfigurationException.class)
        .hasMessage(
            "No component provider detected for io.opentelemetry.sdk.trace.export.SpanExporter with name \"foo\".");
  }

  @Test
  void configFile_ConfigProvider() {
    ConfigProperties config =
        DefaultConfigProperties.createFromMap(
            Collections.singletonMap("otel.experimental.config.file", configFilePath.toString()));

    AutoConfiguredOpenTelemetrySdk autoConfiguredOpenTelemetrySdk =
        AutoConfiguredOpenTelemetrySdk.builder().setConfig(config).setResultAsGlobal().build();
    OpenTelemetrySdk openTelemetrySdk = autoConfiguredOpenTelemetrySdk.getOpenTelemetrySdk();
    cleanup.addCloseable(openTelemetrySdk);

    // getConfig() should return ExtendedConfigProperties generic representation of the config file
    ConfigProvider globalConfigProvider = GlobalConfigProvider.get();
    assertThat(globalConfigProvider)
        .isNotNull()
        .isSameAs(AutoConfigureUtil.getConfigProvider(autoConfiguredOpenTelemetrySdk));
    DeclarativeConfigProperties instrumentationConfig =
        globalConfigProvider.getInstrumentationConfig();
    assertThat(instrumentationConfig).isNotNull();

    // Extract instrumentation config from ConfigProvider
    assertThat(InstrumentationConfigUtil.httpClientRequestCapturedHeaders(globalConfigProvider))
        .isEqualTo(Arrays.asList("Content-Type", "Accept"));
    assertThat(InstrumentationConfigUtil.javaInstrumentationConfig(globalConfigProvider, "example"))
        .isNotNull()
        .satisfies(exampleConfig -> assertThat(exampleConfig.getString("key")).isEqualTo("value"));
  }
}
