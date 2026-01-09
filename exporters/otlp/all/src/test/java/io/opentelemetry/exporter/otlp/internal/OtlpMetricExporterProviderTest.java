/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporterBuilder;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporterBuilder;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertificateEncodingException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OtlpMetricExporterProviderTest {

  @RegisterExtension
  static final SelfSignedCertificateExtension serverTls = new SelfSignedCertificateExtension();

  @RegisterExtension
  static final SelfSignedCertificateExtension clientTls = new SelfSignedCertificateExtension();

  @Spy private OtlpMetricExporterProvider provider;

  @Spy private OtlpHttpMetricExporterBuilder httpBuilder;

  @Spy private OtlpGrpcMetricExporterBuilder grpcBuilder;

  private String certificatePath;
  private String clientKeyPath;
  private String clientCertificatePath;

  @BeforeEach
  void setup(@TempDir Path tempDir) throws IOException, CertificateEncodingException {
    doReturn(httpBuilder).when(provider).httpBuilder();
    doReturn(grpcBuilder).when(provider).grpcBuilder();

    certificatePath =
        createTempFileWithContent(
            tempDir, "certificate.cert", serverTls.certificate().getEncoded());
    clientKeyPath =
        createTempFileWithContent(tempDir, "clientKey.key", clientTls.privateKey().getEncoded());
    clientCertificatePath =
        createTempFileWithContent(
            tempDir, "clientCertificate.cert", clientTls.certificate().getEncoded());
  }

  private static String createTempFileWithContent(Path dir, String filename, byte[] content)
      throws IOException {
    Path path = dir.resolve(filename);
    Files.write(path, content);
    return path.toString();
  }

  @Test
  void getName() {
    assertThat(provider.getName()).isEqualTo("otlp");
  }

  @Test
  void createExporter_UnsupportedProtocol() {
    assertThatThrownBy(
            () ->
                provider.createExporter(
                    DefaultConfigProperties.createFromMap(
                        singletonMap("otel.exporter.otlp.protocol", "foo"))))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Unsupported OTLP metrics protocol: foo");
  }

  @Test
  void createExporter_NoMocks() {
    // Verifies createExporter after resetting the spy overrides
    Mockito.reset(provider);
    try (MetricExporter exporter =
        provider.createExporter(DefaultConfigProperties.createFromMap(emptyMap()))) {
      assertThat(exporter).isInstanceOf(OtlpGrpcMetricExporter.class);
    }
    try (MetricExporter exporter =
        provider.createExporter(
            DefaultConfigProperties.createFromMap(
                singletonMap("otel.exporter.otlp.protocol", "http/protobuf")))) {
      assertThat(exporter).isInstanceOf(OtlpHttpMetricExporter.class);
    }
  }

  @Test
  void createExporter_GrpcDefaults() {
    try (MetricExporter exporter =
        provider.createExporter(DefaultConfigProperties.createFromMap(emptyMap()))) {
      assertThat(exporter).isInstanceOf(OtlpGrpcMetricExporter.class);
      verify(grpcBuilder, times(1)).build();
      verify(grpcBuilder).setComponentLoader(any());
      verify(grpcBuilder, never()).setEndpoint(any());
      verify(grpcBuilder, never()).addHeader(any(), any());
      verify(grpcBuilder, never()).setCompression(any());
      verify(grpcBuilder, never()).setTimeout(any());
      verify(grpcBuilder, never()).setTrustedCertificates(any());
      verify(grpcBuilder, never()).setClientTls(any(), any());
      assertThat(grpcBuilder).extracting("delegate").extracting("retryPolicy").isNotNull();
      assertThat(exporter.getMemoryMode()).isEqualTo(MemoryMode.REUSABLE_DATA);
    }
    Mockito.verifyNoInteractions(httpBuilder);
  }

  @Test
  void createExporter_GrpcWithGeneralConfiguration() throws CertificateEncodingException {
    Map<String, String> config = new HashMap<>();
    config.put("otel.exporter.otlp.endpoint", "https://localhost:443/");
    config.put("otel.exporter.otlp.certificate", certificatePath);
    config.put("otel.exporter.otlp.client.key", clientKeyPath);
    config.put("otel.exporter.otlp.client.certificate", clientCertificatePath);
    config.put("otel.exporter.otlp.headers", "header-key=header-value");
    config.put("otel.exporter.otlp.compression", "gzip");
    config.put("otel.exporter.otlp.timeout", "15s");
    config.put("otel.java.exporter.otlp.retry.disabled", "true");

    try (MetricExporter exporter =
        provider.createExporter(DefaultConfigProperties.createFromMap(config))) {
      assertThat(exporter).isInstanceOf(OtlpGrpcMetricExporter.class);
      verify(grpcBuilder, times(1)).build();
      verify(grpcBuilder).setComponentLoader(any());
      verify(grpcBuilder).setEndpoint("https://localhost:443/");
      verify(grpcBuilder).addHeader("header-key", "header-value");
      verify(grpcBuilder).setCompression("gzip");
      verify(grpcBuilder).setTimeout(Duration.ofSeconds(15));
      verify(grpcBuilder).setTrustedCertificates(serverTls.certificate().getEncoded());
      verify(grpcBuilder)
          .setClientTls(clientTls.privateKey().getEncoded(), clientTls.certificate().getEncoded());
      assertThat(grpcBuilder).extracting("delegate").extracting("retryPolicy").isNull();
    }
    Mockito.verifyNoInteractions(httpBuilder);
  }

  @Test
  void createExporter_GrpcWithSignalConfiguration() throws CertificateEncodingException {
    Map<String, String> config = new HashMap<>();
    config.put("otel.exporter.otlp.endpoint", "https://dummy:443/");
    config.put("otel.exporter.otlp.metrics.endpoint", "https://localhost:443/");
    config.put("otel.exporter.otlp.certificate", "dummy.cert");
    config.put("otel.exporter.otlp.metrics.certificate", certificatePath);
    config.put("otel.exporter.otlp.client.key", "dummy.key");
    config.put("otel.exporter.otlp.metrics.client.key", clientKeyPath);
    config.put("otel.exporter.otlp.client.certificate", "dummy-client.cert");
    config.put("otel.exporter.otlp.metrics.client.certificate", clientCertificatePath);
    config.put("otel.exporter.otlp.headers", "dummy=value");
    config.put("otel.exporter.otlp.metrics.headers", "header-key=header-value");
    config.put("otel.exporter.otlp.compression", "none");
    config.put("otel.exporter.otlp.metrics.compression", "gzip");
    config.put("otel.exporter.otlp.timeout", "1s");
    config.put("otel.exporter.otlp.metrics.timeout", "15s");
    config.put("otel.java.exporter.memory_mode", "immutable_data");

    try (MetricExporter exporter =
        provider.createExporter(DefaultConfigProperties.createFromMap(config))) {
      assertThat(exporter).isInstanceOf(OtlpGrpcMetricExporter.class);
      verify(grpcBuilder, times(1)).build();
      verify(grpcBuilder).setComponentLoader(any());
      verify(grpcBuilder).setEndpoint("https://localhost:443/");
      verify(grpcBuilder).addHeader("header-key", "header-value");
      verify(grpcBuilder).setCompression("gzip");
      verify(grpcBuilder).setTimeout(Duration.ofSeconds(15));
      verify(grpcBuilder).setTrustedCertificates(serverTls.certificate().getEncoded());
      verify(grpcBuilder)
          .setClientTls(clientTls.privateKey().getEncoded(), clientTls.certificate().getEncoded());
      assertThat(exporter.getMemoryMode()).isEqualTo(MemoryMode.IMMUTABLE_DATA);
    }
    Mockito.verifyNoInteractions(httpBuilder);
  }

  @Test
  void createExporter_HttpDefaults() {
    try (MetricExporter exporter =
        provider.createExporter(
            DefaultConfigProperties.createFromMap(
                singletonMap("otel.exporter.otlp.metrics.protocol", "http/protobuf")))) {
      assertThat(exporter).isInstanceOf(OtlpHttpMetricExporter.class);
      verify(httpBuilder, times(1)).build();
      verify(httpBuilder).setComponentLoader(any());
      verify(httpBuilder, never()).setEndpoint(any());
      verify(httpBuilder, never()).addHeader(any(), any());
      verify(httpBuilder, never()).setCompression(any());
      verify(httpBuilder, never()).setTimeout(any());
      verify(httpBuilder, never()).setTrustedCertificates(any());
      verify(httpBuilder, never()).setClientTls(any(), any());
      assertThat(httpBuilder).extracting("delegate").extracting("retryPolicy").isNotNull();
      assertThat(exporter.getMemoryMode()).isEqualTo(MemoryMode.REUSABLE_DATA);
    }
    Mockito.verifyNoInteractions(grpcBuilder);
  }

  @Test
  void createExporter_HttpWithGeneralConfiguration() throws CertificateEncodingException {
    Map<String, String> config = new HashMap<>();
    config.put("otel.exporter.otlp.protocol", "http/protobuf");
    config.put("otel.exporter.otlp.endpoint", "https://localhost:443/");
    config.put("otel.exporter.otlp.certificate", certificatePath);
    config.put("otel.exporter.otlp.client.key", clientKeyPath);
    config.put("otel.exporter.otlp.client.certificate", clientCertificatePath);
    config.put("otel.exporter.otlp.headers", "header-key=header-value");
    config.put("otel.exporter.otlp.compression", "gzip");
    config.put("otel.exporter.otlp.timeout", "15s");
    config.put("otel.java.exporter.otlp.retry.disabled", "true");

    try (MetricExporter exporter =
        provider.createExporter(DefaultConfigProperties.createFromMap(config))) {
      assertThat(exporter).isInstanceOf(OtlpHttpMetricExporter.class);
      verify(httpBuilder, times(1)).build();
      verify(httpBuilder).setComponentLoader(any());
      verify(httpBuilder).setEndpoint("https://localhost:443/v1/metrics");
      verify(httpBuilder).addHeader("header-key", "header-value");
      verify(httpBuilder).setCompression("gzip");
      verify(httpBuilder).setTimeout(Duration.ofSeconds(15));
      verify(httpBuilder).setTrustedCertificates(serverTls.certificate().getEncoded());
      verify(httpBuilder)
          .setClientTls(clientTls.privateKey().getEncoded(), clientTls.certificate().getEncoded());
      assertThat(httpBuilder).extracting("delegate").extracting("retryPolicy").isNull();
    }
    Mockito.verifyNoInteractions(grpcBuilder);
  }

  @Test
  void createExporter_HttpWithSignalConfiguration() throws CertificateEncodingException {
    Map<String, String> config = new HashMap<>();
    config.put("otel.exporter.otlp.protocol", "grpc");
    config.put("otel.exporter.otlp.metrics.protocol", "http/protobuf");
    config.put("otel.exporter.otlp.endpoint", "https://dummy:443/");
    config.put("otel.exporter.otlp.metrics.endpoint", "https://localhost:443/v1/metrics");
    config.put("otel.exporter.otlp.certificate", "dummy.cert");
    config.put("otel.exporter.otlp.metrics.certificate", certificatePath);
    config.put("otel.exporter.otlp.client.key", "dummy.key");
    config.put("otel.exporter.otlp.metrics.client.key", clientKeyPath);
    config.put("otel.exporter.otlp.client.certificate", "dummy-client.cert");
    config.put("otel.exporter.otlp.metrics.client.certificate", clientCertificatePath);
    config.put("otel.exporter.otlp.headers", "dummy=value");
    config.put("otel.exporter.otlp.metrics.headers", "header-key=header-value");
    config.put("otel.exporter.otlp.compression", "none");
    config.put("otel.exporter.otlp.metrics.compression", "gzip");
    config.put("otel.exporter.otlp.timeout", "1s");
    config.put("otel.exporter.otlp.metrics.timeout", "15s");
    config.put("otel.java.exporter.memory_mode", "immutable_data");

    try (MetricExporter exporter =
        provider.createExporter(DefaultConfigProperties.createFromMap(config))) {
      assertThat(exporter).isInstanceOf(OtlpHttpMetricExporter.class);
      verify(httpBuilder, times(1)).build();
      verify(httpBuilder).setComponentLoader(any());
      verify(httpBuilder).setEndpoint("https://localhost:443/v1/metrics");
      verify(httpBuilder).addHeader("header-key", "header-value");
      verify(httpBuilder).setCompression("gzip");
      verify(httpBuilder).setTimeout(Duration.ofSeconds(15));
      verify(httpBuilder).setTrustedCertificates(serverTls.certificate().getEncoded());
      verify(httpBuilder)
          .setClientTls(clientTls.privateKey().getEncoded(), clientTls.certificate().getEncoded());
      assertThat(exporter.getMemoryMode()).isEqualTo(MemoryMode.IMMUTABLE_DATA);
    }
    Mockito.verifyNoInteractions(grpcBuilder);
  }

  @Test
  void meterProviderRef_InitializedWithNoop() throws Exception {
    AtomicReference<MeterProvider> meterProviderRef = getMeterProviderRef(provider);

    assertThat(meterProviderRef.get()).isSameAs(MeterProvider.noop());
  }

  @Test
  void afterAutoConfigure_UpdatesMeterProviderRef() throws Exception {
    OpenTelemetrySdk sdk = OpenTelemetrySdk.builder().build();

    AtomicReference<MeterProvider> meterProviderRef = getMeterProviderRef(provider);
    assertThat(meterProviderRef.get()).isSameAs(MeterProvider.noop());

    provider.afterAutoConfigure(sdk);

    assertThat(meterProviderRef.get()).isSameAs(sdk.getMeterProvider());
  }

  @Test
  @SuppressWarnings("unchecked")
  void createExporter_GrpcSetsMeterProviderSupplier() {
    AtomicReference<Supplier<MeterProvider>> capturedSupplier = new AtomicReference<>();

    OpenTelemetrySdk sdk = OpenTelemetrySdk.builder().build();
    provider.afterAutoConfigure(sdk);

    doAnswer(
            invocation -> {
              capturedSupplier.set(invocation.getArgument(0));
              return grpcBuilder;
            })
        .when(grpcBuilder)
        .setMeterProvider(any(Supplier.class));

    try (MetricExporter exporter =
        provider.createExporter(DefaultConfigProperties.createFromMap(emptyMap()))) {

      assertThat(exporter).isInstanceOf(OtlpGrpcMetricExporter.class);
      assertThat(capturedSupplier.get()).isNotNull();
      assertThat(capturedSupplier.get().get()).isSameAs(sdk.getMeterProvider());
    }
    Mockito.verifyNoInteractions(httpBuilder);
  }

  @Test
  @SuppressWarnings("unchecked")
  void createExporter_HttpSetsMeterProviderSupplier() {
    AtomicReference<Supplier<MeterProvider>> capturedSupplier = new AtomicReference<>();

    OpenTelemetrySdk sdk = OpenTelemetrySdk.builder().build();
    provider.afterAutoConfigure(sdk);

    doAnswer(
            invocation -> {
              capturedSupplier.set(invocation.getArgument(0));
              return httpBuilder;
            })
        .when(httpBuilder)
        .setMeterProvider(any(Supplier.class));

    try (MetricExporter exporter =
        provider.createExporter(
            DefaultConfigProperties.createFromMap(
                singletonMap("otel.exporter.otlp.metrics.protocol", "http/protobuf")))) {

      assertThat(exporter).isInstanceOf(OtlpHttpMetricExporter.class);
      assertThat(capturedSupplier.get()).isNotNull();
      assertThat(capturedSupplier.get().get()).isSameAs(sdk.getMeterProvider());
    }
    Mockito.verifyNoInteractions(grpcBuilder);
  }

  @Test
  @SuppressWarnings("unchecked")
  void meterProviderSupplier_ReturnsNoopBeforeAutoConfigure() {
    AtomicReference<Supplier<MeterProvider>> capturedSupplier = new AtomicReference<>();

    doAnswer(
            invocation -> {
              capturedSupplier.set(invocation.getArgument(0));
              return grpcBuilder;
            })
        .when(grpcBuilder)
        .setMeterProvider(any(Supplier.class));

    try (MetricExporter exporter =
        provider.createExporter(DefaultConfigProperties.createFromMap(emptyMap()))) {

      assertThat(exporter).isInstanceOf(OtlpGrpcMetricExporter.class);
      assertThat(capturedSupplier.get()).isNotNull();
      assertThat(capturedSupplier.get().get()).isSameAs(MeterProvider.noop());
    }
  }

  @SuppressWarnings("unchecked")
  private static AtomicReference<MeterProvider> getMeterProviderRef(
      OtlpMetricExporterProvider provider) throws Exception {
    Field field = OtlpMetricExporterProvider.class.getDeclaredField("meterProviderRef");
    field.setAccessible(true);
    return (AtomicReference<MeterProvider>) field.get(provider);
  }
}
