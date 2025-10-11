/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.extension.incubator.fileconfig.FileConfigTestUtil.createTempFileWithContent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.exporter.logging.otlp.internal.logs.OtlpStdoutLogRecordExporter;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.component.LogRecordExporterComponentProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalOtlpFileExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.LogRecordExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.NameStringValuePairModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpGrpcExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpHttpExporterModel;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.security.cert.CertificateEncodingException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

class LogRecordExporterFactoryTest {

  @RegisterExtension
  static final SelfSignedCertificateExtension serverTls = new SelfSignedCertificateExtension();

  @RegisterExtension
  static final SelfSignedCertificateExtension clientTls = new SelfSignedCertificateExtension();

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  private CapturingComponentLoader capturingComponentLoader;
  private SpiHelper spiHelper;
  private DeclarativeConfigContext context;

  @BeforeEach
  void setup() {
    capturingComponentLoader = new CapturingComponentLoader();
    spiHelper = SpiHelper.create(capturingComponentLoader);
    context = new DeclarativeConfigContext(spiHelper);
  }

  @Test
  void create_OtlpHttpDefaults() {
    List<Closeable> closeables = new ArrayList<>();
    OtlpHttpLogRecordExporter expectedExporter =
        OtlpHttpLogRecordExporter.getDefault().toBuilder()
            .setComponentLoader(capturingComponentLoader) // needed for the toString() check to pass
            .build();
    cleanup.addCloseable(expectedExporter);

    LogRecordExporter exporter =
        LogRecordExporterFactory.getInstance()
            .create(
                new LogRecordExporterModel().withOtlpHttp(new OtlpHttpExporterModel()), context);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());

    // Verify the configuration passed to the component provider
    DeclarativeConfigProperties configProperties =
        capturingComponentLoader.getCapturedConfig("otlp_http");
    assertThat(configProperties).isNotNull();
    assertThat(configProperties.getString("protocol")).isNull();
    assertThat(configProperties.getString("endpoint")).isNull();
    assertThat(configProperties.getStructured("headers")).isNull();
    assertThat(configProperties.getString("compression")).isNull();
    assertThat(configProperties.getInt("timeout")).isNull();
    assertThat(configProperties.getString("certificate_file")).isNull();
    assertThat(configProperties.getString("client_key_file")).isNull();
    assertThat(configProperties.getString("client_certificate_file")).isNull();
  }

  @Test
  void create_OtlpHttpConfigured(@TempDir Path tempDir)
      throws CertificateEncodingException, IOException {
    List<Closeable> closeables = new ArrayList<>();
    OtlpHttpLogRecordExporter expectedExporter =
        OtlpHttpLogRecordExporter.builder()
            .setEndpoint("http://example:4318/v1/logs")
            .addHeader("key1", "value1")
            .addHeader("key2", "value2")
            .setTimeout(Duration.ofSeconds(15))
            .setCompression("gzip")
            .setComponentLoader(capturingComponentLoader) // needed for the toString() check to pass
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
                new LogRecordExporterModel()
                    .withOtlpHttp(
                        new OtlpHttpExporterModel()
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
                            .withCertificateFile(certificatePath)
                            .withClientKeyFile(clientKeyPath)
                            .withClientCertificateFile(clientCertificatePath)),
                context);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());

    // Verify the configuration passed to the component provider
    DeclarativeConfigProperties configProperties =
        capturingComponentLoader.getCapturedConfig("otlp_http");
    assertThat(configProperties).isNotNull();
    assertThat(configProperties.getString("endpoint")).isEqualTo("http://example:4318/v1/logs");
    List<DeclarativeConfigProperties> headers = configProperties.getStructuredList("headers");
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
    assertThat(configProperties.getString("certificate_file")).isEqualTo(certificatePath);
    assertThat(configProperties.getString("client_key_file")).isEqualTo(clientKeyPath);
    assertThat(configProperties.getString("client_certificate_file"))
        .isEqualTo(clientCertificatePath);
  }

  @Test
  void create_OtlpGrpcDefaults() {
    List<Closeable> closeables = new ArrayList<>();
    OtlpGrpcLogRecordExporter expectedExporter =
        OtlpGrpcLogRecordExporter.getDefault().toBuilder()
            .setComponentLoader(capturingComponentLoader) // needed for the toString() check to pass
            .build();
    cleanup.addCloseable(expectedExporter);

    LogRecordExporter exporter =
        LogRecordExporterFactory.getInstance()
            .create(
                new LogRecordExporterModel().withOtlpGrpc(new OtlpGrpcExporterModel()), context);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());

    // Verify the configuration passed to the component provider
    DeclarativeConfigProperties configProperties =
        capturingComponentLoader.getCapturedConfig("otlp_grpc");
    assertThat(configProperties).isNotNull();
    assertThat(configProperties.getString("endpoint")).isNull();
    assertThat(configProperties.getStructured("headers")).isNull();
    assertThat(configProperties.getString("compression")).isNull();
    assertThat(configProperties.getInt("timeout")).isNull();
    assertThat(configProperties.getString("certificate_file")).isNull();
    assertThat(configProperties.getString("client_key_file")).isNull();
    assertThat(configProperties.getString("client_certificate_file")).isNull();
  }

  @Test
  void create_OtlpGrpcConfigured(@TempDir Path tempDir)
      throws CertificateEncodingException, IOException {
    List<Closeable> closeables = new ArrayList<>();
    OtlpGrpcLogRecordExporter expectedExporter =
        OtlpGrpcLogRecordExporter.builder()
            .setEndpoint("http://example:4317")
            .addHeader("key1", "value1")
            .addHeader("key2", "value2")
            .setTimeout(Duration.ofSeconds(15))
            .setCompression("gzip")
            .setComponentLoader(capturingComponentLoader) // needed for the toString() check to pass
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
                new LogRecordExporterModel()
                    .withOtlpGrpc(
                        new OtlpGrpcExporterModel()
                            .withEndpoint("http://example:4317")
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
                            .withCertificateFile(certificatePath)
                            .withClientKeyFile(clientKeyPath)
                            .withClientCertificateFile(clientCertificatePath)),
                context);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());

    // Verify the configuration passed to the component provider
    DeclarativeConfigProperties configProperties =
        capturingComponentLoader.getCapturedConfig("otlp_grpc");
    assertThat(configProperties).isNotNull();
    assertThat(configProperties.getString("endpoint")).isEqualTo("http://example:4317");
    List<DeclarativeConfigProperties> headers = configProperties.getStructuredList("headers");
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
    assertThat(configProperties.getString("certificate_file")).isEqualTo(certificatePath);
    assertThat(configProperties.getString("client_key_file")).isEqualTo(clientKeyPath);
    assertThat(configProperties.getString("client_certificate_file"))
        .isEqualTo(clientCertificatePath);
  }

  @Test
  void create_OtlpFile() {
    List<Closeable> closeables = new ArrayList<>();
    OtlpStdoutLogRecordExporter expectedExporter = OtlpStdoutLogRecordExporter.builder().build();
    cleanup.addCloseable(expectedExporter);

    LogRecordExporter exporter =
        LogRecordExporterFactory.getInstance()
            .create(
                new LogRecordExporterModel()
                    .withOtlpFileDevelopment(new ExperimentalOtlpFileExporterModel()),
                context);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());

    // Verify the configuration passed to the component provider
    DeclarativeConfigProperties configProperties =
        capturingComponentLoader.getCapturedConfig("otlp_file/development");
    assertThat(configProperties).isNotNull();
  }

  @Test
  void create_SpiExporter_Unknown() {
    List<Closeable> closeables = new ArrayList<>();

    assertThatThrownBy(
            () ->
                LogRecordExporterFactory.getInstance()
                    .create(
                        new LogRecordExporterModel()
                            .withAdditionalProperty(
                                "unknown_key", ImmutableMap.of("key1", "value1")),
                        context))
        .isInstanceOf(DeclarativeConfigException.class)
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
                context);
    assertThat(logRecordExporter)
        .isInstanceOf(LogRecordExporterComponentProvider.TestLogRecordExporter.class);
    assertThat(
            ((LogRecordExporterComponentProvider.TestLogRecordExporter) logRecordExporter)
                .config.getString("key1"))
        .isEqualTo("value1");
  }
}
