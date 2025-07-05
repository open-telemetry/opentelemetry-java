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
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporterBuilder;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertificateEncodingException;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.api.Assertions;
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
class OtlpSpanExporterProviderTest {

  @RegisterExtension
  static final SelfSignedCertificateExtension serverTls = new SelfSignedCertificateExtension();

  @RegisterExtension
  static final SelfSignedCertificateExtension clientTls = new SelfSignedCertificateExtension();

  @Spy private OtlpSpanExporterProvider provider;

  @Spy private OtlpHttpSpanExporterBuilder httpBuilder;

  @Spy private OtlpGrpcSpanExporterBuilder grpcBuilder;

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
                        Collections.singletonMap("otel.exporter.otlp.protocol", "foo"))))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Unsupported OTLP traces protocol: foo");
  }

  @Test
  void createExporter_NoMocks() {
    // Verifies createExporter after resetting the spy overrides
    Mockito.reset(provider);
    try (SpanExporter exporter =
        provider.createExporter(DefaultConfigProperties.createFromMap(Collections.emptyMap()))) {
      assertThat(exporter).isInstanceOf(OtlpGrpcSpanExporter.class);
    }
    try (SpanExporter exporter =
        provider.createExporter(
            DefaultConfigProperties.createFromMap(
                Collections.singletonMap("otel.exporter.otlp.protocol", "http/protobuf")))) {
      assertThat(exporter).isInstanceOf(OtlpHttpSpanExporter.class);
    }
  }

  @Test
  void createExporter_GrpcDefaults() {
    try (SpanExporter exporter =
        provider.createExporter(DefaultConfigProperties.createFromMap(Collections.emptyMap()))) {
      assertThat(exporter).isInstanceOf(OtlpGrpcSpanExporter.class);
      verify(grpcBuilder, times(1)).build();
      verify(grpcBuilder).setComponentLoader(any());
      verify(grpcBuilder, never()).setEndpoint(any());
      verify(grpcBuilder, never()).addHeader(any(), any());
      verify(grpcBuilder, never()).setCompression(any());
      verify(grpcBuilder, never()).setTimeout(any());
      verify(grpcBuilder, never()).setTrustedCertificates(any());
      verify(grpcBuilder, never()).setClientTls(any(), any());
      assertThat(grpcBuilder).extracting("delegate").extracting("retryPolicy").isNotNull();
      getMemoryMode(exporter).isEqualTo(MemoryMode.REUSABLE_DATA);
    }
    Mockito.verifyNoInteractions(httpBuilder);
  }

  private static AbstractObjectAssert<?, ?> getMemoryMode(SpanExporter exporter) {
    return assertThat(exporter).extracting("marshaler").extracting("memoryMode");
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

    try (SpanExporter exporter =
        provider.createExporter(DefaultConfigProperties.createFromMap(config))) {
      assertThat(exporter).isInstanceOf(OtlpGrpcSpanExporter.class);
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
    config.put("otel.exporter.otlp.traces.endpoint", "https://localhost:443/");
    config.put("otel.exporter.otlp.certificate", "dummy.cert");
    config.put("otel.exporter.otlp.traces.certificate", certificatePath);
    config.put("otel.exporter.otlp.client.key", "dummy.key");
    config.put("otel.exporter.otlp.traces.client.key", clientKeyPath);
    config.put("otel.exporter.otlp.client.certificate", "dummy-client.cert");
    config.put("otel.exporter.otlp.traces.client.certificate", clientCertificatePath);
    config.put("otel.exporter.otlp.headers", "dummy=value");
    config.put("otel.exporter.otlp.traces.headers", "header-key=header-value");
    config.put("otel.exporter.otlp.compression", "none");
    config.put("otel.exporter.otlp.traces.compression", "gzip");
    config.put("otel.exporter.otlp.timeout", "1s");
    config.put("otel.exporter.otlp.traces.timeout", "15s");
    config.put("otel.java.exporter.memory_mode", "immutable_data");

    try (SpanExporter exporter =
        provider.createExporter(DefaultConfigProperties.createFromMap(config))) {
      assertThat(exporter).isInstanceOf(OtlpGrpcSpanExporter.class);
      verify(grpcBuilder, times(1)).build();
      verify(grpcBuilder).setComponentLoader(any());
      verify(grpcBuilder).setEndpoint("https://localhost:443/");
      verify(grpcBuilder).addHeader("header-key", "header-value");
      verify(grpcBuilder).setCompression("gzip");
      verify(grpcBuilder).setTimeout(Duration.ofSeconds(15));
      verify(grpcBuilder).setTrustedCertificates(serverTls.certificate().getEncoded());
      verify(grpcBuilder)
          .setClientTls(clientTls.privateKey().getEncoded(), clientTls.certificate().getEncoded());
      getMemoryMode(exporter).isEqualTo(MemoryMode.IMMUTABLE_DATA);
    }
    Mockito.verifyNoInteractions(httpBuilder);
  }

  @Test
  void createExporter_HttpDefaults() {
    try (SpanExporter exporter =
        provider.createExporter(
            DefaultConfigProperties.createFromMap(
                Collections.singletonMap("otel.exporter.otlp.traces.protocol", "http/protobuf")))) {
      assertThat(exporter).isInstanceOf(OtlpHttpSpanExporter.class);
      verify(httpBuilder, times(1)).build();
      verify(httpBuilder).setComponentLoader(any());
      verify(httpBuilder, never()).setEndpoint(any());
      verify(httpBuilder, never()).addHeader(any(), any());
      verify(httpBuilder, never()).setCompression(any());
      verify(httpBuilder, never()).setTimeout(any());
      verify(httpBuilder, never()).setTrustedCertificates(any());
      verify(httpBuilder, never()).setClientTls(any(), any());
      assertThat(httpBuilder).extracting("delegate").extracting("retryPolicy").isNotNull();
      getMemoryMode(exporter).isEqualTo(MemoryMode.REUSABLE_DATA);
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
    config.put(
        "otel.exporter.otlp.headers", "header-key1=header%20value1,header-key2=header value2");
    config.put("otel.exporter.otlp.compression", "gzip");
    config.put("otel.exporter.otlp.timeout", "15s");
    config.put("otel.java.exporter.otlp.retry.disabled", "true");

    try (SpanExporter exporter =
        provider.createExporter(DefaultConfigProperties.createFromMap(config))) {
      assertThat(exporter).isInstanceOf(OtlpHttpSpanExporter.class);
      verify(httpBuilder, times(1)).build();
      verify(httpBuilder).setComponentLoader(any());
      verify(httpBuilder).setEndpoint("https://localhost:443/v1/traces");
      verify(httpBuilder).addHeader("header-key1", "header value1");
      verify(httpBuilder).addHeader("header-key2", "header value2");
      verify(httpBuilder).setCompression("gzip");
      verify(httpBuilder).setTimeout(Duration.ofSeconds(15));
      verify(httpBuilder).setTrustedCertificates(serverTls.certificate().getEncoded());
      verify(httpBuilder)
          .setClientTls(clientTls.privateKey().getEncoded(), clientTls.certificate().getEncoded());
      assertThat(httpBuilder).extracting("delegate").extracting("retryPolicy").isNull();
      getMemoryMode(exporter).isEqualTo(MemoryMode.REUSABLE_DATA);
    }
    Mockito.verifyNoInteractions(grpcBuilder);
  }

  @Test
  void createExporter_HttpWithSignalConfiguration() throws CertificateEncodingException {
    Map<String, String> config = new HashMap<>();
    config.put("otel.exporter.otlp.protocol", "grpc");
    config.put("otel.exporter.otlp.traces.protocol", "http/protobuf");
    config.put("otel.exporter.otlp.endpoint", "https://dummy:443/");
    config.put("otel.exporter.otlp.traces.endpoint", "https://localhost:443/v1/traces");
    config.put("otel.exporter.otlp.certificate", "dummy.cert");
    config.put("otel.exporter.otlp.traces.certificate", certificatePath);
    config.put("otel.exporter.otlp.client.key", "dummy.key");
    config.put("otel.exporter.otlp.traces.client.key", clientKeyPath);
    config.put("otel.exporter.otlp.client.certificate", "dummy-client.cert");
    config.put("otel.exporter.otlp.traces.client.certificate", clientCertificatePath);
    config.put("otel.exporter.otlp.headers", "dummy=value");
    config.put("otel.exporter.otlp.traces.headers", "header-key=header-value");
    config.put("otel.exporter.otlp.compression", "none");
    config.put("otel.exporter.otlp.traces.compression", "gzip");
    config.put("otel.exporter.otlp.timeout", "1s");
    config.put("otel.exporter.otlp.traces.timeout", "15s");
    config.put("otel.java.exporter.memory_mode", "immutable_data");

    try (SpanExporter exporter =
        provider.createExporter(DefaultConfigProperties.createFromMap(config))) {
      assertThat(exporter).isInstanceOf(OtlpHttpSpanExporter.class);
      verify(httpBuilder, times(1)).build();
      verify(httpBuilder).setComponentLoader(any());
      verify(httpBuilder).setEndpoint("https://localhost:443/v1/traces");
      verify(httpBuilder).addHeader("header-key", "header-value");
      verify(httpBuilder).setCompression("gzip");
      verify(httpBuilder).setTimeout(Duration.ofSeconds(15));
      verify(httpBuilder).setTrustedCertificates(serverTls.certificate().getEncoded());
      verify(httpBuilder)
          .setClientTls(clientTls.privateKey().getEncoded(), clientTls.certificate().getEncoded());
      getMemoryMode(exporter).isEqualTo(MemoryMode.IMMUTABLE_DATA);
    }
    Mockito.verifyNoInteractions(grpcBuilder);
  }

  @Test
  void createExporter_decodingError() {
    Assertions.assertThatThrownBy(
            () -> {
              provider.createExporter(
                  DefaultConfigProperties.createFromMap(
                      Collections.singletonMap("otel.exporter.otlp.headers", "header-key=%-1")));
            })
        .isInstanceOf(ConfigurationException.class)
        .hasMessage("Cannot decode header value: %-1");
  }
}
