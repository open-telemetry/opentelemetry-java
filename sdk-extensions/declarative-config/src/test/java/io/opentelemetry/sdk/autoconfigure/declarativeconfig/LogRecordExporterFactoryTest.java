/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.FileConfigTestUtil.createTempFileWithContent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.exporter.logging.SystemOutLogRecordExporter;
import io.opentelemetry.exporter.logging.otlp.internal.logs.OtlpStdoutLogRecordExporter;
import io.opentelemetry.exporter.otlp.http.logs.OtlpHttpLogRecordExporter;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.component.LogRecordExporterComponentProvider;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ConsoleExporterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ExperimentalOtlpFileExporterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.GrpcTlsModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.HttpTlsModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.LogRecordExporterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.LogRecordExporterPropertyModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.NameStringValuePairModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OtlpGrpcExporterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OtlpHttpExporterModel;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.io.IOException;
import java.nio.file.Path;
import java.security.cert.CertificateEncodingException;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LogRecordExporterFactoryTest {

  @RegisterExtension
  static final SelfSignedCertificateExtension serverTls = new SelfSignedCertificateExtension();

  @RegisterExtension
  static final SelfSignedCertificateExtension clientTls = new SelfSignedCertificateExtension();

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  private static final DeclarativeConfigContext context =
      new DeclarativeConfigContext(
          ComponentLoader.forClassLoader(LogRecordExporterFactoryTest.class.getClassLoader()));

  private String certificatePath;
  private String clientKeyPath;
  private String clientCertificatePath;

  @BeforeAll
  void setupTls(@TempDir Path tempDir) throws CertificateEncodingException, IOException {
    certificatePath =
        createTempFileWithContent(
            tempDir, "certificate.cert", serverTls.certificate().getEncoded());
    clientKeyPath =
        createTempFileWithContent(tempDir, "clientKey.key", clientTls.privateKey().getEncoded());
    clientCertificatePath =
        createTempFileWithContent(
            tempDir, "clientCertificate.cert", clientTls.certificate().getEncoded());
  }

  @BeforeEach
  void setup() {
    context.setBuilder(new DeclarativeConfigurationBuilder());
  }

  @ParameterizedTest
  @MethodSource("createTestCases")
  void create(LogRecordExporterModel model, LogRecordExporter expectedExporter) {
    cleanup.addCloseable(expectedExporter);
    LogRecordExporter exporter = LogRecordExporterFactory.getInstance().create(model, context);
    cleanup.addCloseable(exporter);
    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());
  }

  Stream<Arguments> createTestCases() {
    return Stream.of(
        Arguments.argumentSet(
            "otlp_http default",
            new LogRecordExporterModel().withOtlpHttp(new OtlpHttpExporterModel()),
            OtlpHttpLogRecordExporter.getDefault().toBuilder().setComponentLoader(context).build()),
        Arguments.argumentSet(
            "otlp_http with options",
            new LogRecordExporterModel()
                .withOtlpHttp(
                    new OtlpHttpExporterModel()
                        .withEndpoint("http://example:4318/v1/logs")
                        .withHeaders(
                            Arrays.asList(
                                new NameStringValuePairModel().withName("key1").withValue("value1"),
                                new NameStringValuePairModel()
                                    .withName("key2")
                                    .withValue("value2")))
                        .withCompression("gzip")
                        .withTimeout(15_000)
                        .withTls(
                            new HttpTlsModel()
                                .withCaFile(certificatePath)
                                .withKeyFile(clientKeyPath)
                                .withCertFile(clientCertificatePath))),
            OtlpHttpLogRecordExporter.builder()
                .setEndpoint("http://example:4318/v1/logs")
                .addHeader("key1", "value1")
                .addHeader("key2", "value2")
                .setTimeout(Duration.ofSeconds(15))
                .setCompression("gzip")
                .setComponentLoader(context)
                .build()),
        Arguments.argumentSet(
            "otlp_grpc default",
            new LogRecordExporterModel().withOtlpGrpc(new OtlpGrpcExporterModel()),
            OtlpGrpcLogRecordExporter.getDefault().toBuilder().setComponentLoader(context).build()),
        Arguments.argumentSet(
            "otlp_grpc with options",
            new LogRecordExporterModel()
                .withOtlpGrpc(
                    new OtlpGrpcExporterModel()
                        .withEndpoint("http://example:4317")
                        .withHeaders(
                            Arrays.asList(
                                new NameStringValuePairModel().withName("key1").withValue("value1"),
                                new NameStringValuePairModel()
                                    .withName("key2")
                                    .withValue("value2")))
                        .withCompression("gzip")
                        .withTimeout(15_000)
                        .withTls(
                            new GrpcTlsModel()
                                .withCaFile(certificatePath)
                                .withKeyFile(clientKeyPath)
                                .withCertFile(clientCertificatePath))),
            OtlpGrpcLogRecordExporter.builder()
                .setEndpoint("http://example:4317")
                .addHeader("key1", "value1")
                .addHeader("key2", "value2")
                .setTimeout(Duration.ofSeconds(15))
                .setCompression("gzip")
                .setComponentLoader(context)
                .build()),
        Arguments.argumentSet(
            "otlp_file/development",
            new LogRecordExporterModel()
                .withOtlpFileDevelopment(new ExperimentalOtlpFileExporterModel()),
            OtlpStdoutLogRecordExporter.builder().build()));
  }

  @ParameterizedTest
  @MethodSource("createInvalidTestCases")
  void create_Invalid(LogRecordExporterModel model, String expectedMessage) {
    assertThatThrownBy(() -> LogRecordExporterFactory.getInstance().create(model, context))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessage(expectedMessage);
  }

  Stream<Arguments> createInvalidTestCases() {
    return Stream.of(
        Arguments.argumentSet(
            "unknown component provider",
            new LogRecordExporterModel()
                .withAdditionalProperty(
                    "unknown_key",
                    new LogRecordExporterPropertyModel().withAdditionalProperty("key1", "value1")),
            "No component provider detected for io.opentelemetry.sdk.logs.export.LogRecordExporter with name \"unknown_key\"."));
  }

  @Test
  void create_SpiExporter_Valid() {
    LogRecordExporter logRecordExporter =
        LogRecordExporterFactory.getInstance()
            .create(
                new LogRecordExporterModel()
                    .withAdditionalProperty(
                        "test",
                        new LogRecordExporterPropertyModel()
                            .withAdditionalProperty("key1", "value1")),
                context);
    assertThat(logRecordExporter)
        .isInstanceOf(LogRecordExporterComponentProvider.TestLogRecordExporter.class);
    assertThat(
            ((LogRecordExporterComponentProvider.TestLogRecordExporter) logRecordExporter)
                .config.getString("key1"))
        .isEqualTo("value1");
  }

  @Test
  void create_Customizer() {
    context.setBuilder(new DeclarativeConfigurationBuilder());
    context
        .getBuilder()
        .addLogRecordExporterCustomizer(
            LogRecordExporter.class, (exporter, properties) -> SystemOutLogRecordExporter.create());

    LogRecordExporter result =
        LogRecordExporterFactory.getInstance()
            .create(new LogRecordExporterModel().withConsole(new ConsoleExporterModel()), context);
    cleanup.addCloseable(result);

    assertThat(result).isInstanceOf(SystemOutLogRecordExporter.class);
  }

  @Test
  void create_Customizer_TypeSafe() {
    context.setBuilder(new DeclarativeConfigurationBuilder());
    context
        .getBuilder()
        .addLogRecordExporterCustomizer(
            OtlpGrpcLogRecordExporter.class,
            (exporter, properties) ->
                exporter.toBuilder().setTimeout(Duration.ofSeconds(42)).build());

    LogRecordExporter result =
        LogRecordExporterFactory.getInstance()
            .create(
                new LogRecordExporterModel().withOtlpGrpc(new OtlpGrpcExporterModel()), context);
    cleanup.addCloseable(result);

    assertThat(result).isInstanceOf(OtlpGrpcLogRecordExporter.class);
    assertThat(result.toString()).contains("timeoutNanos=42000000000");
  }

  @Test
  void create_Customizer_TypeMismatch() {
    AtomicInteger callCount = new AtomicInteger(0);
    context.setBuilder(new DeclarativeConfigurationBuilder());
    context
        .getBuilder()
        .addLogRecordExporterCustomizer(
            OtlpGrpcLogRecordExporter.class,
            (exporter, properties) -> {
              callCount.incrementAndGet();
              return exporter;
            });

    LogRecordExporter result =
        LogRecordExporterFactory.getInstance()
            .create(new LogRecordExporterModel().withConsole(new ConsoleExporterModel()), context);
    cleanup.addCloseable(result);

    assertThat(callCount.get()).isEqualTo(0);
  }

  @Test
  void create_Customizer_ReturnsNull() {
    context.setBuilder(new DeclarativeConfigurationBuilder());
    context
        .getBuilder()
        .addLogRecordExporterCustomizer(LogRecordExporter.class, (exporter, properties) -> null);

    assertThatThrownBy(
            () ->
                LogRecordExporterFactory.getInstance()
                    .create(
                        new LogRecordExporterModel().withConsole(new ConsoleExporterModel()),
                        context))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessageContaining("Customizer returned null for LogRecordExporter: console");
  }
}
