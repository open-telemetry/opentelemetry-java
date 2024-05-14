/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
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
import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.incubator.events.GlobalEventLoggerProvider;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import io.opentelemetry.sdk.logs.internal.SdkEventLoggerProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.event.Level;

class FileConfigurationTest {

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
        "file_format: \"0.1\"\n"
            + "resource:\n"
            + "  attributes:\n"
            + "    service.name: test\n"
            + "tracer_provider:\n"
            + "  processors:\n"
            + "    - simple:\n"
            + "        exporter:\n"
            + "          console: {}\n"
            + "other:\n"
            + "  str_key: str_value\n"
            + "  map_key:\n"
            + "    str_key1: str_value1\n";
    configFilePath = tempDir.resolve("otel-config.yaml");
    Files.write(configFilePath, yaml.getBytes(StandardCharsets.UTF_8));
    GlobalOpenTelemetry.resetForTest();
    GlobalEventLoggerProvider.resetForTest();
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
            .setPropagators(
                ContextPropagators.create(
                    TextMapPropagator.composite(
                        W3CTraceContextPropagator.getInstance(),
                        W3CBaggagePropagator.getInstance())))
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
    assertThat(GlobalEventLoggerProvider.get())
        .isNotSameAs(openTelemetrySdk.getSdkLoggerProvider());
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
    assertThat(GlobalEventLoggerProvider.get())
        .isInstanceOf(SdkEventLoggerProvider.class)
        .extracting("delegateLoggerProvider")
        .isSameAs(openTelemetrySdk.getSdkLoggerProvider());
  }

  @Test
  void configFile_Error(@TempDir Path tempDir) throws IOException {
    String yaml =
        "file_format: \"0.1\"\n"
            + "resource:\n"
            + "  attributes:\n"
            + "    service.name: test\n"
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
        .hasMessage("Unrecognized span exporter(s): [foo]");
  }

  @Test
  void configFile_StructuredConfigProperties() {
    ConfigProperties config =
        DefaultConfigProperties.createFromMap(
            Collections.singletonMap("otel.experimental.config.file", configFilePath.toString()));

    AutoConfiguredOpenTelemetrySdk autoConfiguredOpenTelemetrySdk =
        AutoConfiguredOpenTelemetrySdk.builder().setConfig(config).setResultAsGlobal().build();
    OpenTelemetrySdk openTelemetrySdk = autoConfiguredOpenTelemetrySdk.getOpenTelemetrySdk();
    cleanup.addCloseable(openTelemetrySdk);

    // getConfig() should return ExtendedConfigProperties generic representation of the config file
    StructuredConfigProperties structuredConfigProps =
        autoConfiguredOpenTelemetrySdk.getStructuredConfig();
    assertThat(structuredConfigProps).isNotNull();
    StructuredConfigProperties otherProps = structuredConfigProps.getStructured("other");
    assertThat(otherProps).isNotNull();
    assertThat(otherProps.getString("str_key")).isEqualTo("str_value");
    StructuredConfigProperties otherMapKeyProps = otherProps.getStructured("map_key");
    assertThat(otherMapKeyProps).isNotNull();
    assertThat(otherMapKeyProps.getString("str_key1")).isEqualTo("str_value1");
  }
}
