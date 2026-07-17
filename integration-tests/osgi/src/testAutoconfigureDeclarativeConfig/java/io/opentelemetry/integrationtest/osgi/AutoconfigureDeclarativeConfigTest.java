/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.integrationtest.osgi;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.extension.trace.propagation.B3Propagator;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.ServiceResourceDetector;
import io.opentelemetry.sdk.internal.OpenTelemetrySdkBuilderUtil;
import io.opentelemetry.sdk.internal.SdkConfigProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.test.junit5.context.BundleContextExtension;

/** Verifies autoconfigure with declarative config works in OSGi. */
@ExtendWith(BundleContextExtension.class)
public class AutoconfigureDeclarativeConfigTest {

  @Test
  void declarativeConfigSdkInitializes(@TempDir Path tempDir) throws IOException {
    String yaml =
        "file_format: \"1.0\"\n"
            + "resource:\n"
            + "  attributes:\n"
            + "    - name: service.name\n"
            + "      value: test-osgi-declarative\n"
            + "  detection/development:\n"
            + "    detectors:\n"
            + "      - service:\n"
            + "propagator:\n"
            + "  composite:\n"
            + "    - tracecontext:\n"
            + "    - b3:\n"
            + "tracer_provider:\n"
            + "  processors:\n"
            + "    - simple:\n"
            + "        exporter:\n"
            + "          otlp_http:\n"
            + "meter_provider:\n"
            + "  readers:\n"
            + "    - periodic:\n"
            + "        exporter:\n"
            + "          otlp_http:\n"
            + "logger_provider:\n"
            + "  processors:\n"
            + "    - simple:\n"
            + "        exporter:\n"
            + "          otlp_http:\n";
    Path configFile = tempDir.resolve("otel-config.yaml");
    Files.write(configFile, yaml.getBytes(StandardCharsets.UTF_8));

    // System.setProperty (not addPropertiesSupplier) is required because property suppliers are
    // not resolved until after the otel.config.file check.
    System.setProperty("otel.config.file", configFile.toString());
    AutoConfiguredOpenTelemetrySdk autoConfigured;
    try {
      autoConfigured = AutoConfiguredOpenTelemetrySdk.builder().build();
    } catch (RuntimeException e) {
      System.clearProperty("otel.config.file");
      throw e;
    }

    ComponentLoader delegateLoader =
        ComponentLoader.forClassLoader(AutoConfiguredOpenTelemetrySdk.class.getClassLoader());
    ComponentLoader autoConfigureLoader =
        componentLoaderWithToString(
            delegateLoader, "DeclarativeConfigContext{componentLoader=" + delegateLoader + "}");

    // ServiceResourceDetector.RANDOM_SERVICE_INSTANCE_ID is a static field — same value within
    // a JVM run, so the expected and actual resources match deterministically.
    Resource detectedResource =
        new ServiceResourceDetector().create(DeclarativeConfigProperties.empty());
    Resource resource =
        Resource.getDefault().toBuilder()
            .putAll(detectedResource.getAttributes())
            .put(AttributeKey.stringKey("service.name"), "test-osgi-declarative")
            .build();

    OpenTelemetrySdk expected =
        OpenTelemetrySdkBuilderUtil.setConfigProvider(
                OpenTelemetrySdk.builder()
                    .setTracerProvider(
                        SdkTracerProvider.builder()
                            .setResource(resource)
                            .addSpanProcessor(
                                SimpleSpanProcessor.create(
                                    // TestDeclarativeConfigurationCustomizerProvider wraps
                                    // OtlpHttpSpanExporter — proves the SPI was invoked.
                                    new TestDeclarativeConfigurationCustomizerProvider
                                        .TestCustomizedSpanExporter(
                                        OtlpHttpSpanExporter.builder()
                                            .setComponentLoader(autoConfigureLoader)
                                            .build())))
                            .build())
                    .setMeterProvider(
                        SdkMeterProvider.builder()
                            .setResource(resource)
                            .registerMetricReader(
                                PeriodicMetricReader.create(
                                    OtlpHttpMetricExporter.builder()
                                        .setComponentLoader(autoConfigureLoader)
                                        .build()))
                            .build())
                    .setLoggerProvider(
                        SdkLoggerProvider.builder()
                            .setResource(resource)
                            .addLogRecordProcessor(
                                SimpleLogRecordProcessor.create(
                                    OtlpHttpLogRecordExporter.builder()
                                        .setComponentLoader(autoConfigureLoader)
                                        .build()))
                            .build())
                    .setPropagators(
                        ContextPropagators.create(
                            TextMapPropagator.composite(
                                W3CTraceContextPropagator.getInstance(),
                                B3Propagator.injectingSingleHeader()))),
                SdkConfigProvider.create(DeclarativeConfigProperties.empty()))
            .build();
    try {
      assertThat(autoConfigured.getOpenTelemetrySdk().toString()).isEqualTo(expected.toString());
    } finally {
      autoConfigured.getOpenTelemetrySdk().close();
      expected.close();
      System.clearProperty("otel.config.file");
    }
  }

  private static ComponentLoader componentLoaderWithToString(
      ComponentLoader componentLoader, String toString) {
    return new ComponentLoader() {
      @Override
      public <T> Iterable<T> load(Class<T> spiClass) {
        return componentLoader.load(spiClass);
      }

      @Override
      public String toString() {
        return toString;
      }
    };
  }
}
