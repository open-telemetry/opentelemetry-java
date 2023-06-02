/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporterBuilder;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertificateEncodingException;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
class OtlpLogRecordExporterProviderTest {

  @RegisterExtension
  static final SelfSignedCertificateExtension serverTls = new SelfSignedCertificateExtension();

  @RegisterExtension
  static final SelfSignedCertificateExtension clientTls = new SelfSignedCertificateExtension();

  @Spy private OtlpLogRecordExporterProvider provider;

  @Spy private OtlpHttpLogRecordExporterBuilder httpBuilder;

  @Spy private OtlpGrpcLogRecordExporterBuilder grpcBuilder;

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
                    DefaultConfigProperties.createForTest(
                        Collections.singletonMap("otel.exporter.otlp.protocol", "foo"))))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Unsupported OTLP logs protocol: foo");
  }

  @Test
  void createExporter_NoMocks() {
    // Verifies createExporter after resetting the spy overrides
    Mockito.reset(provider);
    try (LogRecordExporter exporter =
        provider.createExporter(DefaultConfigProperties.createForTest(Collections.emptyMap()))) {
      assertThat(exporter).isInstanceOf(OtlpGrpcLogRecordExporter.class);
    }
    try (LogRecordExporter exporter =
        provider.createExporter(
            DefaultConfigProperties.createForTest(
                Collections.singletonMap("otel.exporter.otlp.protocol", "http/protobuf")))) {
      assertThat(exporter).isInstanceOf(OtlpHttpLogRecordExporter.class);
    }
  }

  @Test
  void createExporter_GrpcDefaults() {
    try (LogRecordExporter exporter =
        provider.createExporter(DefaultConfigProperties.createForTest(Collections.emptyMap()))) {
      assertThat(exporter).isInstanceOf(OtlpGrpcLogRecordExporter.class);
      verify(grpcBuilder, times(1)).build();
      verify(grpcBuilder, never()).setEndpoint(any());
      verify(grpcBuilder, never()).addHeader(any(), any());
      verify(grpcBuilder, never()).setCompression(any());
      verify(grpcBuilder, never()).setTimeout(any());
      verify(grpcBuilder, never()).setTrustedCertificates(any());
      verify(grpcBuilder, never()).setClientTls(any(), any());
      assertThat(grpcBuilder).extracting("delegate").extracting("retryPolicy").isNull();
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
    config.put("otel.experimental.exporter.otlp.retry.enabled", "true");

    try (LogRecordExporter exporter =
        provider.createExporter(DefaultConfigProperties.createForTest(config))) {
      assertThat(exporter).isInstanceOf(OtlpGrpcLogRecordExporter.class);
      verify(grpcBuilder, times(1)).build();
      verify(grpcBuilder).setEndpoint("https://localhost:443/");
      verify(grpcBuilder).addHeader("header-key", "header-value");
      verify(grpcBuilder).setCompression("gzip");
      verify(grpcBuilder).setTimeout(Duration.ofSeconds(15));
      verify(grpcBuilder).setTrustedCertificates(serverTls.certificate().getEncoded());
      verify(grpcBuilder)
          .setClientTls(clientTls.privateKey().getEncoded(), clientTls.certificate().getEncoded());
      assertThat(grpcBuilder).extracting("delegate").extracting("retryPolicy").isNotNull();
    }
    Mockito.verifyNoInteractions(httpBuilder);
  }

  @Test
  void createExporter_GrpcWithSignalConfiguration() throws CertificateEncodingException {
    Map<String, String> config = new HashMap<>();
    config.put("otel.exporter.otlp.endpoint", "https://dummy:443/");
    config.put("otel.exporter.otlp.logs.endpoint", "https://localhost:443/");
    config.put("otel.exporter.otlp.certificate", "dummy.cert");
    config.put("otel.exporter.otlp.logs.certificate", certificatePath);
    config.put("otel.exporter.otlp.client.key", "dummy.key");
    config.put("otel.exporter.otlp.logs.client.key", clientKeyPath);
    config.put("otel.exporter.otlp.client.certificate", "dummy-client.cert");
    config.put("otel.exporter.otlp.logs.client.certificate", clientCertificatePath);
    config.put("otel.exporter.otlp.headers", "dummy=value");
    config.put("otel.exporter.otlp.logs.headers", "header-key=header-value");
    config.put("otel.exporter.otlp.compression", "none");
    config.put("otel.exporter.otlp.logs.compression", "gzip");
    config.put("otel.exporter.otlp.timeout", "1s");
    config.put("otel.exporter.otlp.logs.timeout", "15s");

    try (LogRecordExporter exporter =
        provider.createExporter(DefaultConfigProperties.createForTest(config))) {
      assertThat(exporter).isInstanceOf(OtlpGrpcLogRecordExporter.class);
      verify(grpcBuilder, times(1)).build();
      verify(grpcBuilder).setEndpoint("https://localhost:443/");
      verify(grpcBuilder).addHeader("header-key", "header-value");
      verify(grpcBuilder).setCompression("gzip");
      verify(grpcBuilder).setTimeout(Duration.ofSeconds(15));
      verify(grpcBuilder).setTrustedCertificates(serverTls.certificate().getEncoded());
      verify(grpcBuilder)
          .setClientTls(clientTls.privateKey().getEncoded(), clientTls.certificate().getEncoded());
    }
    Mockito.verifyNoInteractions(httpBuilder);
  }

  @Test
  void createExporter_HttpDefaults() {
    try (LogRecordExporter exporter =
        provider.createExporter(
            DefaultConfigProperties.createForTest(
                Collections.singletonMap("otel.exporter.otlp.logs.protocol", "http/protobuf")))) {
      assertThat(exporter).isInstanceOf(OtlpHttpLogRecordExporter.class);
      verify(httpBuilder, times(1)).build();
      verify(httpBuilder, never()).setEndpoint(any());
      verify(httpBuilder, never()).addHeader(any(), any());
      verify(httpBuilder, never()).setCompression(any());
      verify(httpBuilder, never()).setTimeout(any());
      verify(httpBuilder, never()).setTrustedCertificates(any());
      verify(httpBuilder, never()).setClientTls(any(), any());
      assertThat(httpBuilder).extracting("delegate").extracting("retryPolicy").isNull();
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
    config.put("otel.experimental.exporter.otlp.retry.enabled", "true");

    try (LogRecordExporter exporter =
        provider.createExporter(DefaultConfigProperties.createForTest(config))) {
      assertThat(exporter).isInstanceOf(OtlpHttpLogRecordExporter.class);
      verify(httpBuilder, times(1)).build();
      verify(httpBuilder).setEndpoint("https://localhost:443/v1/logs");
      verify(httpBuilder).addHeader("header-key", "header-value");
      verify(httpBuilder).setCompression("gzip");
      verify(httpBuilder).setTimeout(Duration.ofSeconds(15));
      verify(httpBuilder).setTrustedCertificates(serverTls.certificate().getEncoded());
      verify(httpBuilder)
          .setClientTls(clientTls.privateKey().getEncoded(), clientTls.certificate().getEncoded());
      assertThat(httpBuilder).extracting("delegate").extracting("retryPolicy").isNotNull();
    }
    Mockito.verifyNoInteractions(grpcBuilder);
  }

  @Test
  void createExporter_HttpWithSignalConfiguration() throws CertificateEncodingException {
    Map<String, String> config = new HashMap<>();
    config.put("otel.exporter.otlp.protocol", "grpc");
    config.put("otel.exporter.otlp.logs.protocol", "http/protobuf");
    config.put("otel.exporter.otlp.endpoint", "https://dummy:443/");
    config.put("otel.exporter.otlp.logs.endpoint", "https://localhost:443/v1/logs");
    config.put("otel.exporter.otlp.certificate", "dummy.cert");
    config.put("otel.exporter.otlp.logs.certificate", certificatePath);
    config.put("otel.exporter.otlp.client.key", "dummy.key");
    config.put("otel.exporter.otlp.logs.client.key", clientKeyPath);
    config.put("otel.exporter.otlp.client.certificate", "dummy-client.cert");
    config.put("otel.exporter.otlp.logs.client.certificate", clientCertificatePath);
    config.put("otel.exporter.otlp.headers", "dummy=value");
    config.put("otel.exporter.otlp.logs.headers", "header-key=header-value");
    config.put("otel.exporter.otlp.compression", "none");
    config.put("otel.exporter.otlp.logs.compression", "gzip");
    config.put("otel.exporter.otlp.timeout", "1s");
    config.put("otel.exporter.otlp.logs.timeout", "15s");

    try (LogRecordExporter exporter =
        provider.createExporter(DefaultConfigProperties.createForTest(config))) {
      assertThat(exporter).isInstanceOf(OtlpHttpLogRecordExporter.class);
      verify(httpBuilder, times(1)).build();
      verify(httpBuilder).setEndpoint("https://localhost:443/v1/logs");
      verify(httpBuilder).addHeader("header-key", "header-value");
      verify(httpBuilder).setCompression("gzip");
      verify(httpBuilder).setTimeout(Duration.ofSeconds(15));
      verify(httpBuilder).setTrustedCertificates(serverTls.certificate().getEncoded());
      verify(httpBuilder)
          .setClientTls(clientTls.privateKey().getEncoded(), clientTls.certificate().getEncoded());
    }
    Mockito.verifyNoInteractions(grpcBuilder);
  }
}
