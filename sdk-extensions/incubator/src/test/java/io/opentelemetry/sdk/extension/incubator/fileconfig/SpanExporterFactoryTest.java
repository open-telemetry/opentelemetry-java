/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.sdk.extension.incubator.fileconfig.FileConfigTestUtil.createTempFileWithContent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import io.opentelemetry.api.incubator.authenticator.ExporterAuthenticator;
import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.logging.otlp.internal.traces.OtlpStdoutSpanExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.component.SpanExporterComponentProvider;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.AuthenticatorModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ConsoleExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ExperimentalOtlpFileExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.NameStringValuePairModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpGrpcExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OtlpHttpExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.SpanExporterModel;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.ZipkinSpanExporterModel;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.security.cert.CertificateEncodingException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
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

  private final SpiHelper spiHelper =
      spy(SpiHelper.create(SpanExporterFactoryTest.class.getClassLoader()));
  private final DeclarativeConfigContext context = new DeclarativeConfigContext(spiHelper);

  private static final ComponentProvider<ExporterAuthenticator> authenticatorComponentProvider =
     new ComponentProvider<ExporterAuthenticator>() {
      @Override
      public Class<ExporterAuthenticator> getType() {
        return ExporterAuthenticator.class;
      }

      @Override
      public String getName() {
        return "test_auth";
      }

      @Override
      public ExporterAuthenticator create(
          DeclarativeConfigProperties config,
          ComponentProviderLoader componentProviderLoader) {
        return new ExporterAuthenticator() {
          ;

          @Override
          public String getName() {
            return "test_auth";
          }

          @Override
          public Map<String, String> getAuthenticationHeaders() {
            return Collections.singletonMap("auth_provider_key1", "value1");
          }
        };
      }
    };

  private List<ComponentProvider<?>> loadedComponentProviders = Collections.emptyList();

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setup() {
    when(spiHelper.load(ComponentProvider.class))
        .thenAnswer(
            invocation -> {
              List<ComponentProvider<?>> result =
                  (List<ComponentProvider<?>>) invocation.callRealMethod();
              loadedComponentProviders =
                  result.stream().map(Mockito::spy).collect(Collectors.toList());
              loadedComponentProviders.add(authenticatorComponentProvider);
              return loadedComponentProviders;
            });
  }

  private ComponentProvider<?> getComponentProvider(String name, Class<?> type) {
    return loadedComponentProviders.stream()
        .filter(
            componentProvider ->
                componentProvider.getName().equals(name)
                    && componentProvider.getType().equals(type))
        .findFirst()
        .orElseThrow(IllegalStateException::new);
  }

  @Test
  void create_OtlpHttpDefaults() {
    List<Closeable> closeables = new ArrayList<>();
    OtlpHttpSpanExporter expectedExporter = OtlpHttpSpanExporter.getDefault();
    cleanup.addCloseable(expectedExporter);

    SpanExporter exporter =
        SpanExporterFactory.getInstance()
            .create(new SpanExporterModel().withOtlpHttp(new OtlpHttpExporterModel()), context);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());

    ArgumentCaptor<DeclarativeConfigProperties> configCaptor =
        ArgumentCaptor.forClass(DeclarativeConfigProperties.class);
    ComponentProvider<?> componentProvider = getComponentProvider("otlp_http", SpanExporter.class);
    verify(componentProvider).create(configCaptor.capture(), any());
    DeclarativeConfigProperties configProperties = configCaptor.getValue();
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
                new SpanExporterModel()
                    .withOtlpHttp(
                        new OtlpHttpExporterModel()
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
                            .withCertificateFile(certificatePath)
                            .withClientKeyFile(clientKeyPath)
                            .withClientCertificateFile(clientCertificatePath)),
                context);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());

    ArgumentCaptor<DeclarativeConfigProperties> configCaptor =
        ArgumentCaptor.forClass(DeclarativeConfigProperties.class);
    ComponentProvider<?> componentProvider = getComponentProvider("otlp_http", SpanExporter.class);
    verify(componentProvider).create(configCaptor.capture(), any());
    DeclarativeConfigProperties configProperties = configCaptor.getValue();
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
    assertThat(configProperties.getString("certificate_file")).isEqualTo(certificatePath);
    assertThat(configProperties.getString("client_key_file")).isEqualTo(clientKeyPath);
    assertThat(configProperties.getString("client_certificate_file"))
        .isEqualTo(clientCertificatePath);
  }

  @Test
  void create_OtlpGrpcDefaults() {
    List<Closeable> closeables = new ArrayList<>();
    OtlpGrpcSpanExporter expectedExporter = OtlpGrpcSpanExporter.getDefault();
    cleanup.addCloseable(expectedExporter);

    SpanExporter exporter =
        SpanExporterFactory.getInstance()
            .create(new SpanExporterModel().withOtlpGrpc(new OtlpGrpcExporterModel()), context);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());

    ArgumentCaptor<DeclarativeConfigProperties> configCaptor =
        ArgumentCaptor.forClass(DeclarativeConfigProperties.class);
    ComponentProvider<?> componentProvider = getComponentProvider("otlp_grpc", SpanExporter.class);
    verify(componentProvider).create(configCaptor.capture(), any());
    DeclarativeConfigProperties configProperties = configCaptor.getValue();
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
    OtlpGrpcSpanExporter expectedExporter =
        OtlpGrpcSpanExporter.builder()
            .setEndpoint("http://example:4317")
            .addHeader("key1", "value1")
            .addHeader("key2", "value2")
            .setHeaders(
                () -> Collections.singletonMap("auth_provider_key1", "value1")
            )
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
                new SpanExporterModel()
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
                            .withAuthenticator(
                                new AuthenticatorModel().withAdditionalProperty("test_auth", null))
                            .withCompression("gzip")
                            .withTimeout(15_000)
                            .withCertificateFile(certificatePath)
                            .withClientKeyFile(clientKeyPath)
                            .withClientCertificateFile(clientCertificatePath)),
                context);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());

    ArgumentCaptor<DeclarativeConfigProperties> configCaptor =
        ArgumentCaptor.forClass(DeclarativeConfigProperties.class);
    ComponentProvider<?> componentProvider = getComponentProvider("otlp_grpc", SpanExporter.class);
    verify(componentProvider).create(configCaptor.capture(), any());
    DeclarativeConfigProperties configProperties = configCaptor.getValue();
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
  void create_Console() {
    List<Closeable> closeables = new ArrayList<>();
    LoggingSpanExporter expectedExporter = LoggingSpanExporter.create();
    cleanup.addCloseable(expectedExporter);

    SpanExporter exporter =
        SpanExporterFactory.getInstance()
            .create(new SpanExporterModel().withConsole(new ConsoleExporterModel()), context);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());
  }

  @Test
  void create_ZipkinDefaults() {
    List<Closeable> closeables = new ArrayList<>();
    ZipkinSpanExporter expectedExporter = ZipkinSpanExporter.builder().build();

    cleanup.addCloseable(expectedExporter);

    SpanExporter exporter =
        SpanExporterFactory.getInstance()
            .create(new SpanExporterModel().withZipkin(new ZipkinSpanExporterModel()), context);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());

    ArgumentCaptor<DeclarativeConfigProperties> configCaptor =
        ArgumentCaptor.forClass(DeclarativeConfigProperties.class);
    ComponentProvider<?> componentProvider = getComponentProvider("zipkin", SpanExporter.class);
    verify(componentProvider).create(configCaptor.capture(), any());
    DeclarativeConfigProperties configProperties = configCaptor.getValue();
    assertThat(configProperties.getString("endpoint")).isNull();
    assertThat(configProperties.getLong("timeout")).isNull();
  }

  @Test
  void create_ZipkinConfigured() {
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
                new SpanExporterModel()
                    .withZipkin(
                        new ZipkinSpanExporterModel()
                            .withEndpoint("http://zipkin:9411/v1/v2/spans")
                            .withTimeout(15_000)),
                context);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());

    ArgumentCaptor<DeclarativeConfigProperties> configCaptor =
        ArgumentCaptor.forClass(DeclarativeConfigProperties.class);
    ComponentProvider<?> componentProvider = getComponentProvider("zipkin", SpanExporter.class);
    verify(componentProvider).create(configCaptor.capture(), any());
    DeclarativeConfigProperties configProperties = configCaptor.getValue();
    assertThat(configProperties.getString("endpoint")).isEqualTo("http://zipkin:9411/v1/v2/spans");
    assertThat(configProperties.getLong("timeout")).isEqualTo(15_000);
  }

  @Test
  void create_OtlpFile() {
    List<Closeable> closeables = new ArrayList<>();
    OtlpStdoutSpanExporter expectedExporter = OtlpStdoutSpanExporter.builder().build();
    cleanup.addCloseable(expectedExporter);

    SpanExporter exporter =
        SpanExporterFactory.getInstance()
            .create(
                new SpanExporterModel()
                    .withOtlpFileDevelopment(new ExperimentalOtlpFileExporterModel()),
                context);
    cleanup.addCloseable(exporter);
    cleanup.addCloseables(closeables);

    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());

    ArgumentCaptor<DeclarativeConfigProperties> configCaptor =
        ArgumentCaptor.forClass(DeclarativeConfigProperties.class);
    ComponentProvider<?> componentProvider =
        getComponentProvider("otlp_file/development", SpanExporter.class);
    verify(componentProvider).create(configCaptor.capture(), any());
  }

  @Test
  void create_SpiExporter_Unknown() {
    List<Closeable> closeables = new ArrayList<>();

    assertThatThrownBy(
            () ->
                SpanExporterFactory.getInstance()
                    .create(
                        new SpanExporterModel()
                            .withAdditionalProperty(
                                "unknown_key", ImmutableMap.of("key1", "value1")),
                        context))
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
                new SpanExporterModel()
                    .withAdditionalProperty("test", ImmutableMap.of("key1", "value1")),
                context);
    assertThat(spanExporter).isInstanceOf(SpanExporterComponentProvider.TestSpanExporter.class);
    assertThat(
            ((SpanExporterComponentProvider.TestSpanExporter) spanExporter)
                .config.getString("key1"))
        .isEqualTo("value1");
  }
}
