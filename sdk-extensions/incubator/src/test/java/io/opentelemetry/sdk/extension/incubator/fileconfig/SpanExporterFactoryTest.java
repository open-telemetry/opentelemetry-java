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
import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.extension.incubator.fileconfig.component.SpanExporterComponentProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ConsoleModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.NameStringValuePairModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ZipkinModel;
import io.opentelemetry.sdk.trace.export.SpanExporter;
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
class SpanExporterFactoryTest {

  @RegisterExtension
  static final SelfSignedCertificateExtension serverTls = new SelfSignedCertificateExtension();

  @RegisterExtension
  static final SelfSignedCertificateExtension clientTls = new SelfSignedCertificateExtension();

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  private SpiHelper spiHelper = SpiHelper.create(SpanExporterFactoryTest.class.getClassLoader());

  @Test
  void create_OtlpDefaults() {
    spiHelper = spy(spiHelper);
    List<Closeable> closeables = new ArrayList<>();
    OtlpHttpSpanExporter expectedExporter = OtlpHttpSpanExporter.getDefault();
    cleanup.addCloseable(expectedExporter);

    SpanExporter exporter =
        SpanExporterFactory.getInstance()
            .create(
                new io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model
                        .SpanExporterModel()
                    .withOtlp(new OtlpModel()),
                spiHelper,
                closeables);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());

    ArgumentCaptor<DeclarativeConfigProperties> configCaptor =
        ArgumentCaptor.forClass(DeclarativeConfigProperties.class);
    verify(spiHelper).loadComponent(eq(SpanExporter.class), eq("otlp"), configCaptor.capture());
    DeclarativeConfigProperties configProperties = configCaptor.getValue();
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
    OtlpHttpSpanExporter expectedExporter =
        OtlpHttpSpanExporter.builder()
            .setEndpoint("http://example:4318/v1/traces")
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

    SpanExporter exporter =
        SpanExporterFactory.getInstance()
            .create(
                new io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model
                        .SpanExporterModel()
                    .withOtlp(
                        new OtlpModel()
                            .withProtocol("http/protobuf")
                            .withEndpoint("http://example:4318/v1/traces")
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

    ArgumentCaptor<DeclarativeConfigProperties> configCaptor =
        ArgumentCaptor.forClass(DeclarativeConfigProperties.class);
    verify(spiHelper).loadComponent(eq(SpanExporter.class), eq("otlp"), configCaptor.capture());
    DeclarativeConfigProperties configProperties = configCaptor.getValue();
    assertThat(configProperties.getString("protocol")).isEqualTo("http/protobuf");
    assertThat(configProperties.getString("endpoint")).isEqualTo("http://example:4318/v1/traces");
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
    assertThat(configProperties.getString("certificate")).isEqualTo(certificatePath);
    assertThat(configProperties.getString("client_key")).isEqualTo(clientKeyPath);
    assertThat(configProperties.getString("client_certificate")).isEqualTo(clientCertificatePath);
  }

  @Test
  void create_Console() {
    spiHelper = spy(spiHelper);
    List<Closeable> closeables = new ArrayList<>();
    LoggingSpanExporter expectedExporter = LoggingSpanExporter.create();
    cleanup.addCloseable(expectedExporter);

    SpanExporter exporter =
        SpanExporterFactory.getInstance()
            .create(
                new io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model
                        .SpanExporterModel()
                    .withConsole(new ConsoleModel()),
                spiHelper,
                closeables);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());
  }

  @Test
  void create_ZipkinDefaults() {
    spiHelper = spy(spiHelper);
    List<Closeable> closeables = new ArrayList<>();
    ZipkinSpanExporter expectedExporter = ZipkinSpanExporter.builder().build();

    cleanup.addCloseable(expectedExporter);

    SpanExporter exporter =
        SpanExporterFactory.getInstance()
            .create(
                new io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model
                        .SpanExporterModel()
                    .withZipkin(new ZipkinModel()),
                spiHelper,
                closeables);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());

    ArgumentCaptor<DeclarativeConfigProperties> configCaptor =
        ArgumentCaptor.forClass(DeclarativeConfigProperties.class);
    verify(spiHelper).loadComponent(eq(SpanExporter.class), eq("zipkin"), configCaptor.capture());
    DeclarativeConfigProperties configProperties = configCaptor.getValue();
    assertThat(configProperties.getString("endpoint")).isNull();
    assertThat(configProperties.getLong("timeout")).isNull();
  }

  @Test
  void create_ZipkinConfigured() {
    spiHelper = spy(spiHelper);
    List<Closeable> closeables = new ArrayList<>();
    ZipkinSpanExporter expectedExporter =
        ZipkinSpanExporter.builder()
            .setEndpoint("http://zipkin:9411/v1/v2/spans")
            .setReadTimeout(Duration.ofSeconds(15))
            .build();
    cleanup.addCloseable(expectedExporter);

    SpanExporter exporter =
        SpanExporterFactory.getInstance()
            .create(
                new io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model
                        .SpanExporterModel()
                    .withZipkin(
                        new ZipkinModel()
                            .withEndpoint("http://zipkin:9411/v1/v2/spans")
                            .withTimeout(15_000)),
                spiHelper,
                closeables);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());

    ArgumentCaptor<DeclarativeConfigProperties> configCaptor =
        ArgumentCaptor.forClass(DeclarativeConfigProperties.class);
    verify(spiHelper).loadComponent(eq(SpanExporter.class), eq("zipkin"), configCaptor.capture());
    DeclarativeConfigProperties configProperties = configCaptor.getValue();
    assertThat(configProperties.getString("endpoint")).isEqualTo("http://zipkin:9411/v1/v2/spans");
    assertThat(configProperties.getLong("timeout")).isEqualTo(15_000);
  }

  @Test
  void create_SpiExporter_Unknown() {
    List<Closeable> closeables = new ArrayList<>();

    assertThatThrownBy(
            () ->
                SpanExporterFactory.getInstance()
                    .create(
                        new io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model
                                .SpanExporterModel()
                            .withAdditionalProperty(
                                "unknown_key", ImmutableMap.of("key1", "value1")),
                        spiHelper,
                        new ArrayList<>()))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessage(
            "No component provider detected for io.opentelemetry.sdk.trace.export.SpanExporter with name \"unknown_key\".");
    cleanup.addCloseables(closeables);
  }

  @Test
  void create_SpiExporter_Valid() {
    SpanExporter spanExporter =
        SpanExporterFactory.getInstance()
            .create(
                new io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model
                        .SpanExporterModel()
                    .withAdditionalProperty("test", ImmutableMap.of("key1", "value1")),
                spiHelper,
                new ArrayList<>());
    assertThat(spanExporter).isInstanceOf(SpanExporterComponentProvider.TestSpanExporter.class);
    assertThat(
            ((SpanExporterComponentProvider.TestSpanExporter) spanExporter)
                .config.getString("key1"))
        .isEqualTo("value1");
  }
}
