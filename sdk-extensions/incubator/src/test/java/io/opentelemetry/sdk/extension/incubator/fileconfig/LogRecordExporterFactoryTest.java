/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.extension.incubator.fileconfig.FileConfigTestUtil.createTempFileWithContent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableMap;
import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import io.opentelemetry.sdk.extension.incubator.fileconfig.component.LogRecordExporterComponentProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.NameStringValuePairModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpModel;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.security.cert.CertificateEncodingException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LogRecordExporterFactoryTest {

  @RegisterExtension
  static final SelfSignedCertificateExtension serverTls = new SelfSignedCertificateExtension();

  @RegisterExtension
  static final SelfSignedCertificateExtension clientTls = new SelfSignedCertificateExtension();

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  private SpiHelper spiHelper =
      SpiHelper.create(LogRecordExporterFactoryTest.class.getClassLoader());

  @Test
  void create_OtlpDefaults() {
    spiHelper = spy(spiHelper);
    List<Closeable> closeables = new ArrayList<>();
    OtlpGrpcLogRecordExporter expectedExporter = OtlpGrpcLogRecordExporter.getDefault();
    cleanup.addCloseable(expectedExporter);

    LogRecordExporter exporter =
        LogRecordExporterFactory.getInstance()
            .create(
                new io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model
                        .LogRecordExporterModel()
                    .withOtlp(new OtlpModel()),
                spiHelper,
                closeables);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());

    ArgumentCaptor<StructuredConfigProperties> configCaptor =
        ArgumentCaptor.forClass(StructuredConfigProperties.class);
    verify(spiHelper)
        .loadComponent(eq(LogRecordExporter.class), eq("otlp"), configCaptor.capture());
    StructuredConfigProperties configProperties = configCaptor.getValue();
    assertThat(configProperties.getString("protocol")).isNull();
    assertThat(configProperties.getString("endpoint")).isNull();
    assertThat(configProperties.getStructured("headers")).isNull();
    assertThat(configProperties.getString("compression")).isNull();
    assertThat(configProperties.getInt("timeout")).isNull();
    assertThat(configProperties.getString("certificate")).isNull();
    assertThat(configProperties.getString("client_key")).isNull();
    assertThat(configProperties.getString("client_certificate")).isNull();
  }

  @Test
  void create_OtlpConfigured(@TempDir Path tempDir)
      throws CertificateEncodingException, IOException {
    spiHelper = spy(spiHelper);
    List<Closeable> closeables = new ArrayList<>();
    OtlpHttpLogRecordExporter expectedExporter =
        OtlpHttpLogRecordExporter.builder()
            .setEndpoint("http://example:4318/v1/logs")
            .addHeader("key1", "value1")
            .addHeader("key2", "value2")
            .setTimeout(Duration.ofSeconds(15))
            .setCompression("gzip")
            .build();
    cleanup.addCloseable(expectedExporter);

    // Write certificates to temp files
    String certificatePath =
        createTempFileWithContent(
            tempDir, "certificate.cert", serverTls.certificate().getEncoded());
    String clientKeyPath =
        createTempFileWithContent(tempDir, "clientKey.key", clientTls.privateKey().getEncoded());
    String clientCertificatePath =
        createTempFileWithContent(
            tempDir, "clientCertificate.cert", clientTls.certificate().getEncoded());

    LogRecordExporter exporter =
        LogRecordExporterFactory.getInstance()
            .create(
                new io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model
                        .LogRecordExporterModel()
                    .withOtlp(
                        new OtlpModel()
                            .withProtocol("http/protobuf")
                            .withEndpoint("http://example:4318/v1/logs")
                            .withHeaders(
                                Arrays.asList(
                                    new NameStringValuePairModel()
                                        .withName("key1")
                                        .withValue("value1"),
                                    new NameStringValuePairModel()
                                        .withName("key2")
                                        .withValue("value2")))
                            .withCompression("gzip")
                            .withTimeout(15_000)
                            .withCertificate(certificatePath)
                            .withClientKey(clientKeyPath)
                            .withClientCertificate(clientCertificatePath)),
                spiHelper,
                closeables);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());

    ArgumentCaptor<StructuredConfigProperties> configCaptor =
        ArgumentCaptor.forClass(StructuredConfigProperties.class);
    verify(spiHelper)
        .loadComponent(eq(LogRecordExporter.class), eq("otlp"), configCaptor.capture());
    StructuredConfigProperties configProperties = configCaptor.getValue();
    assertThat(configProperties.getString("protocol")).isEqualTo("http/protobuf");
    assertThat(configProperties.getString("endpoint")).isEqualTo("http://example:4318/v1/logs");
    List<StructuredConfigProperties> headers = configProperties.getStructuredList("headers");
    assertThat(headers)
        .isNotNull()
        .satisfiesExactly(
            header -> {
              assertThat(header.getString("name")).isEqualTo("key1");
              assertThat(header.getString("value")).isEqualTo("value1");
            },
            header -> {
              assertThat(header.getString("name")).isEqualTo("key2");
              assertThat(header.getString("value")).isEqualTo("value2");
            });
    assertThat(configProperties.getString("compression")).isEqualTo("gzip");
    assertThat(configProperties.getInt("timeout")).isEqualTo(Duration.ofSeconds(15).toMillis());
    assertThat(configProperties.getString("certificate")).isEqualTo(certificatePath);
    assertThat(configProperties.getString("client_key")).isEqualTo(clientKeyPath);
    assertThat(configProperties.getString("client_certificate")).isEqualTo(clientCertificatePath);
  }

  @Test
  void create_SpiExporter_Unknown() {
    List<Closeable> closeables = new ArrayList<>();

    assertThatThrownBy(
            () ->
                LogRecordExporterFactory.getInstance()
                    .create(
                        new io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model
                                .LogRecordExporterModel()
                            .withAdditionalProperty(
                                "unknown_key", ImmutableMap.of("key1", "value1")),
                        spiHelper,
                        new ArrayList<>()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage(
            "No component provider detected for io.opentelemetry.sdk.logs.export.LogRecordExporter with name \"unknown_key\".");
    cleanup.addCloseables(closeables);
  }

  @Test
  void create_SpiExporter_Valid() {
    LogRecordExporter logRecordExporter =
        LogRecordExporterFactory.getInstance()
            .create(
                new LogRecordExporterModel()
                    .withAdditionalProperty("test", ImmutableMap.of("key1", "value1")),
                spiHelper,
                new ArrayList<>());
    assertThat(logRecordExporter)
        .isInstanceOf(LogRecordExporterComponentProvider.TestLogRecordExporter.class);
    assertThat(
            ((LogRecordExporterComponentProvider.TestLogRecordExporter) logRecordExporter)
                .config.getString("key1"))
        .isEqualTo("value1");
  }
}
