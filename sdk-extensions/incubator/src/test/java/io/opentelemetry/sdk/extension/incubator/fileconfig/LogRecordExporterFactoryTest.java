/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.extension.incubator.fileconfig.FileConfigTestUtil.createTempFileWithContent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableMap;
import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import io.opentelemetry.api.incubator.config.StructuredConfigException;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.logs.ConfigurableLogRecordExporterProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Headers;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Otlp;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.security.cert.CertificateEncodingException;
import java.time.Duration;
import java.util.ArrayList;
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
  void create_Null() {
    LogRecordExporter expectedExporter = LogRecordExporter.composite();

    LogRecordExporter exporter =
        LogRecordExporterFactory.getInstance().create(null, spiHelper, new ArrayList<>());

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());
  }

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
                        .LogRecordExporter()
                    .withOtlp(new Otlp()),
                spiHelper,
                closeables);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());

    ArgumentCaptor<ConfigProperties> configCaptor = ArgumentCaptor.forClass(ConfigProperties.class);
    verify(spiHelper)
        .loadConfigurable(
            eq(ConfigurableLogRecordExporterProvider.class), any(), any(), configCaptor.capture());
    ConfigProperties configProperties = configCaptor.getValue();
    assertThat(configProperties.getString("otel.exporter.otlp.logs.protocol")).isNull();
    assertThat(configProperties.getString("otel.exporter.otlp.endpoint")).isNull();
    assertThat(configProperties.getMap("otel.exporter.otlp.logs.headers")).isEmpty();
    assertThat(configProperties.getString("otel.exporter.otlp.logs.compression")).isNull();
    assertThat(configProperties.getDuration("otel.exporter.otlp.logs.timeout")).isNull();
    assertThat(configProperties.getString("otel.exporter.otlp.logs.certificate")).isNull();
    assertThat(configProperties.getString("otel.exporter.otlp.logs.client.key")).isNull();
    assertThat(configProperties.getString("otel.exporter.otlp.logs.client.certificate")).isNull();
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
                        .LogRecordExporter()
                    .withOtlp(
                        new Otlp()
                            .withProtocol("http/protobuf")
                            .withEndpoint("http://example:4318")
                            .withHeaders(
                                new Headers()
                                    .withAdditionalProperty("key1", "value1")
                                    .withAdditionalProperty("key2", "value2"))
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

    ArgumentCaptor<ConfigProperties> configCaptor = ArgumentCaptor.forClass(ConfigProperties.class);
    verify(spiHelper)
        .loadConfigurable(
            eq(ConfigurableLogRecordExporterProvider.class), any(), any(), configCaptor.capture());
    ConfigProperties configProperties = configCaptor.getValue();
    assertThat(configProperties.getString("otel.exporter.otlp.logs.protocol"))
        .isEqualTo("http/protobuf");
    assertThat(configProperties.getString("otel.exporter.otlp.endpoint"))
        .isEqualTo("http://example:4318");
    assertThat(configProperties.getMap("otel.exporter.otlp.logs.headers"))
        .isEqualTo(ImmutableMap.of("key1", "value1", "key2", "value2"));
    assertThat(configProperties.getString("otel.exporter.otlp.logs.compression")).isEqualTo("gzip");
    assertThat(configProperties.getDuration("otel.exporter.otlp.logs.timeout"))
        .isEqualTo(Duration.ofSeconds(15));
    assertThat(configProperties.getString("otel.exporter.otlp.logs.certificate"))
        .isEqualTo(certificatePath);
    assertThat(configProperties.getString("otel.exporter.otlp.logs.client.key"))
        .isEqualTo(clientKeyPath);
    assertThat(configProperties.getString("otel.exporter.otlp.logs.client.certificate"))
        .isEqualTo(clientCertificatePath);
  }

  @Test
  void create_SpiExporter() {
    List<Closeable> closeables = new ArrayList<>();

    assertThatThrownBy(
            () ->
                LogRecordExporterFactory.getInstance()
                    .create(
                        new io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model
                                .LogRecordExporter()
                            .withAdditionalProperty("test", ImmutableMap.of("key1", "value1")),
                        spiHelper,
                        new ArrayList<>()))
        .isInstanceOf(StructuredConfigException.class)
        .hasMessage("Unrecognized log record exporter(s): [test]");
    cleanup.addCloseables(closeables);
  }
}
