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
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Console;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Headers;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Otlp;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.Zipkin;
import io.opentelemetry.sdk.trace.export.SpanExporter;
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
    OtlpGrpcSpanExporter expectedExporter = OtlpGrpcSpanExporter.getDefault();
    cleanup.addCloseable(expectedExporter);

    SpanExporter exporter =
        SpanExporterFactory.getInstance()
            .create(
                new io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model
                        .SpanExporter()
                    .withOtlp(new Otlp()),
                spiHelper,
                closeables);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());

    ArgumentCaptor<ConfigProperties> configCaptor = ArgumentCaptor.forClass(ConfigProperties.class);
    verify(spiHelper)
        .loadConfigurable(
            eq(ConfigurableSpanExporterProvider.class), any(), any(), configCaptor.capture());
    ConfigProperties configProperties = configCaptor.getValue();
    assertThat(configProperties.getString("otel.exporter.otlp.traces.protocol")).isNull();
    assertThat(configProperties.getString("otel.exporter.otlp.endpoint")).isNull();
    assertThat(configProperties.getMap("otel.exporter.otlp.traces.headers")).isEmpty();
    assertThat(configProperties.getString("otel.exporter.otlp.traces.compression")).isNull();
    assertThat(configProperties.getDuration("otel.exporter.otlp.traces.timeout")).isNull();
    assertThat(configProperties.getString("otel.exporter.otlp.traces.certificate")).isNull();
    assertThat(configProperties.getString("otel.exporter.otlp.traces.client.key")).isNull();
    assertThat(configProperties.getString("otel.exporter.otlp.traces.client.certificate")).isNull();
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
                        .SpanExporter()
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
            eq(ConfigurableSpanExporterProvider.class), any(), any(), configCaptor.capture());
    ConfigProperties configProperties = configCaptor.getValue();
    assertThat(configProperties.getString("otel.exporter.otlp.traces.protocol"))
        .isEqualTo("http/protobuf");
    assertThat(configProperties.getString("otel.exporter.otlp.endpoint"))
        .isEqualTo("http://example:4318");
    assertThat(configProperties.getMap("otel.exporter.otlp.traces.headers"))
        .isEqualTo(ImmutableMap.of("key1", "value1", "key2", "value2"));
    assertThat(configProperties.getString("otel.exporter.otlp.traces.compression"))
        .isEqualTo("gzip");
    assertThat(configProperties.getDuration("otel.exporter.otlp.traces.timeout"))
        .isEqualTo(Duration.ofSeconds(15));
    assertThat(configProperties.getString("otel.exporter.otlp.traces.certificate"))
        .isEqualTo(certificatePath);
    assertThat(configProperties.getString("otel.exporter.otlp.traces.client.key"))
        .isEqualTo(clientKeyPath);
    assertThat(configProperties.getString("otel.exporter.otlp.traces.client.certificate"))
        .isEqualTo(clientCertificatePath);
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
                        .SpanExporter()
                    .withConsole(new Console()),
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
                        .SpanExporter()
                    .withZipkin(new Zipkin()),
                spiHelper,
                closeables);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());

    ArgumentCaptor<ConfigProperties> configCaptor = ArgumentCaptor.forClass(ConfigProperties.class);
    verify(spiHelper)
        .loadConfigurable(
            eq(ConfigurableSpanExporterProvider.class), any(), any(), configCaptor.capture());
    ConfigProperties configProperties = configCaptor.getValue();
    assertThat(configProperties.getString("otel.exporter.zipkin.endpoint")).isNull();
    assertThat(configProperties.getDuration("otel.exporter.zipkin.timeout")).isNull();
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
                        .SpanExporter()
                    .withZipkin(
                        new Zipkin()
                            .withEndpoint("http://zipkin:9411/v1/v2/spans")
                            .withTimeout(15_000)),
                spiHelper,
                closeables);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());

    ArgumentCaptor<ConfigProperties> configCaptor = ArgumentCaptor.forClass(ConfigProperties.class);
    verify(spiHelper)
        .loadConfigurable(
            eq(ConfigurableSpanExporterProvider.class), any(), any(), configCaptor.capture());
    ConfigProperties configProperties = configCaptor.getValue();
    assertThat(configProperties.getString("otel.exporter.zipkin.endpoint"))
        .isEqualTo("http://zipkin:9411/v1/v2/spans");
    assertThat(configProperties.getDuration("otel.exporter.zipkin.timeout"))
        .isEqualTo(Duration.ofSeconds(15));
  }

  @Test
  void create_SpiExporter() {
    List<Closeable> closeables = new ArrayList<>();

    assertThatThrownBy(
            () ->
                SpanExporterFactory.getInstance()
                    .create(
                        new io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model
                                .SpanExporter()
                            .withAdditionalProperty("test", ImmutableMap.of("key1", "value1")),
                        spiHelper,
                        new ArrayList<>()))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage("Unrecognized span exporter(s): [test]");
    cleanup.addCloseables(closeables);
  }
}
