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
import io.opentelemetry.exporter.logging.LoggingMetricExporter;
import io.opentelemetry.exporter.logging.otlp.internal.metrics.OtlpStdoutMetricExporter;
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.component.MetricExporterComponentProvider;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ConsoleMetricExporterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.ExperimentalOtlpFileMetricExporterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.GrpcTlsModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.HttpTlsModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.NameStringValuePairModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OtlpGrpcMetricExporterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.OtlpHttpMetricExporterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.PushMetricExporterModel;
import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.PushMetricExporterPropertyModel;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
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
class MetricExporterFactoryTest {

  @RegisterExtension
  static final SelfSignedCertificateExtension serverTls = new SelfSignedCertificateExtension();

  @RegisterExtension
  static final SelfSignedCertificateExtension clientTls = new SelfSignedCertificateExtension();

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  private static final DeclarativeConfigContext context =
      new DeclarativeConfigContext(
          ComponentLoader.forClassLoader(MetricExporterFactoryTest.class.getClassLoader()));

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
  void create(PushMetricExporterModel model, MetricExporter expectedExporter) {
    cleanup.addCloseable(expectedExporter);
    MetricExporter exporter = MetricExporterFactory.getInstance().create(model, context);
    cleanup.addCloseable(exporter);
    assertThat(exporter.toString()).isEqualTo(expectedExporter.toString());
  }

  Stream<Arguments> createTestCases() {
    return Stream.of(
        Arguments.of(
            new PushMetricExporterModel().withOtlpHttp(new OtlpHttpMetricExporterModel()),
            OtlpHttpMetricExporter.getDefault().toBuilder().setComponentLoader(context).build()),
        Arguments.of(
            new PushMetricExporterModel()
                .withOtlpHttp(
                    new OtlpHttpMetricExporterModel()
                        .withEndpoint("http://example:4318/v1/metrics")
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
                                .withCertFile(clientCertificatePath))
                        .withTemporalityPreference(
                            OtlpHttpMetricExporterModel.ExporterTemporalityPreference.DELTA)
                        .withDefaultHistogramAggregation(
                            OtlpHttpMetricExporterModel.ExporterDefaultHistogramAggregation
                                .BASE_2_EXPONENTIAL_BUCKET_HISTOGRAM)),
            OtlpHttpMetricExporter.builder()
                .setEndpoint("http://example:4318/v1/metrics")
                .addHeader("key1", "value1")
                .addHeader("key2", "value2")
                .setTimeout(Duration.ofSeconds(15))
                .setCompression("gzip")
                .setAggregationTemporalitySelector(AggregationTemporalitySelector.deltaPreferred())
                .setDefaultAggregationSelector(
                    DefaultAggregationSelector.getDefault()
                        .with(
                            InstrumentType.HISTOGRAM,
                            Aggregation.base2ExponentialBucketHistogram()))
                .setComponentLoader(context)
                .build()),
        Arguments.of(
            new PushMetricExporterModel().withOtlpGrpc(new OtlpGrpcMetricExporterModel()),
            OtlpGrpcMetricExporter.getDefault().toBuilder().setComponentLoader(context).build()),
        Arguments.of(
            new PushMetricExporterModel()
                .withOtlpGrpc(
                    new OtlpGrpcMetricExporterModel()
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
                                .withCertFile(clientCertificatePath))
                        .withTemporalityPreference(
                            OtlpHttpMetricExporterModel.ExporterTemporalityPreference.DELTA)
                        .withDefaultHistogramAggregation(
                            OtlpHttpMetricExporterModel.ExporterDefaultHistogramAggregation
                                .BASE_2_EXPONENTIAL_BUCKET_HISTOGRAM)),
            OtlpGrpcMetricExporter.builder()
                .setEndpoint("http://example:4317")
                .addHeader("key1", "value1")
                .addHeader("key2", "value2")
                .setTimeout(Duration.ofSeconds(15))
                .setCompression("gzip")
                .setAggregationTemporalitySelector(AggregationTemporalitySelector.deltaPreferred())
                .setDefaultAggregationSelector(
                    DefaultAggregationSelector.getDefault()
                        .with(
                            InstrumentType.HISTOGRAM,
                            Aggregation.base2ExponentialBucketHistogram()))
                .setComponentLoader(context)
                .build()),
        Arguments.of(
            new PushMetricExporterModel().withConsole(new ConsoleMetricExporterModel()),
            LoggingMetricExporter.create()),
        Arguments.of(
            new PushMetricExporterModel()
                .withOtlpFileDevelopment(new ExperimentalOtlpFileMetricExporterModel()),
            OtlpStdoutMetricExporter.builder().build()));
  }

  @ParameterizedTest
  @MethodSource("createInvalidTestCases")
  void create_Invalid(PushMetricExporterModel model, String expectedMessage) {
    assertThatThrownBy(() -> MetricExporterFactory.getInstance().create(model, context))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessage(expectedMessage);
  }

  Stream<Arguments> createInvalidTestCases() {
    return Stream.of(
        Arguments.of(
            new PushMetricExporterModel()
                .withAdditionalProperty(
                    "unknown_key",
                    new PushMetricExporterPropertyModel().withAdditionalProperty("key1", "value1")),
            "No component provider detected for io.opentelemetry.sdk.metrics.export.MetricExporter with name \"unknown_key\"."));
  }

  @Test
  void create_SpiExporter_Valid() {
    MetricExporter metricExporter =
        MetricExporterFactory.getInstance()
            .create(
                new PushMetricExporterModel()
                    .withAdditionalProperty(
                        "test",
                        new PushMetricExporterPropertyModel()
                            .withAdditionalProperty("key1", "value1")),
                context);
    assertThat(metricExporter)
        .isInstanceOf(MetricExporterComponentProvider.TestMetricExporter.class);
    assertThat(
            ((MetricExporterComponentProvider.TestMetricExporter) metricExporter)
                .config.getString("key1"))
        .isEqualTo("value1");
  }

  @Test
  void create_Customizer() {
    context.setBuilder(new DeclarativeConfigurationBuilder());
    context
        .getBuilder()
        .addMetricExporterCustomizer(
            MetricExporter.class, (exporter, properties) -> LoggingMetricExporter.create());

    MetricExporter result =
        MetricExporterFactory.getInstance()
            .create(
                new PushMetricExporterModel().withConsole(new ConsoleMetricExporterModel()),
                context);
    cleanup.addCloseable(result);

    assertThat(result).isInstanceOf(LoggingMetricExporter.class);
  }

  @Test
  void create_Customizer_TypeSafe() {
    context.setBuilder(new DeclarativeConfigurationBuilder());
    context
        .getBuilder()
        .addMetricExporterCustomizer(
            OtlpGrpcMetricExporter.class,
            (exporter, properties) ->
                exporter.toBuilder().setTimeout(Duration.ofSeconds(42)).build());

    MetricExporter result =
        MetricExporterFactory.getInstance()
            .create(
                new PushMetricExporterModel().withOtlpGrpc(new OtlpGrpcMetricExporterModel()),
                context);
    cleanup.addCloseable(result);

    assertThat(result).isInstanceOf(OtlpGrpcMetricExporter.class);
    assertThat(result.toString()).contains("timeoutNanos=42000000000");
  }

  @Test
  void create_Customizer_TypeMismatch() {
    AtomicInteger callCount = new AtomicInteger(0);
    context.setBuilder(new DeclarativeConfigurationBuilder());
    context
        .getBuilder()
        .addMetricExporterCustomizer(
            OtlpGrpcMetricExporter.class,
            (exporter, properties) -> {
              callCount.incrementAndGet();
              return exporter;
            });

    MetricExporter result =
        MetricExporterFactory.getInstance()
            .create(
                new PushMetricExporterModel().withConsole(new ConsoleMetricExporterModel()),
                context);
    cleanup.addCloseable(result);

    assertThat(callCount.get()).isEqualTo(0);
  }

  @Test
  void create_Customizer_ReturnsNull() {
    context.setBuilder(new DeclarativeConfigurationBuilder());
    context
        .getBuilder()
        .addMetricExporterCustomizer(MetricExporter.class, (exporter, properties) -> null);

    assertThatThrownBy(
            () ->
                MetricExporterFactory.getInstance()
                    .create(
                        new PushMetricExporterModel().withConsole(new ConsoleMetricExporterModel()),
                        context))
        .isInstanceOf(DeclarativeConfigException.class)
        .hasMessageContaining("Customizer returned null for MetricExporter: console");
  }
}
